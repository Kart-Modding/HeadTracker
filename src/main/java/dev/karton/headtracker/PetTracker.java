package dev.karton.headtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Remembers where the player's own tamed pets (wolves, cats, parrots - anything extending
 * {@link TamableAnimal}) were last seen, per singleplayer world and per multiplayer server.
 *
 * The server only sends entities that are inside the player's tracking range, so this is
 * purely what the client has seen. That's accurate more often than it sounds: a pet that's
 * out of range is almost always either sitting or in an unloaded chunk, and neither moves.
 *
 * Stored in {@code config/headtracker/pets/<singleplayer|multiplayer>/<world or address>.json}.
 */
public final class PetTracker {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR = Platform.configDir().resolve(HeadTrackerMod.MOD_ID).resolve("pets");

    private static final int SCAN_INTERVAL = 10;
    private static final int SAVE_INTERVAL = 600;
    // Entities arrive a little after the chunks they're in, so give a freshly joined world
    // a moment before treating "not here" as "gone".
    private static final int JOIN_GRACE_TICKS = 100;
    // A remembered pet this close to the player is well inside entity tracking range, so if
    // the server still hasn't sent it after a few scans it isn't there any more.
    private static final double GONE_DISTANCE = 48.0;
    private static final int GONE_SCANS = 6;

    private static final Map<String, Pet> PETS = new LinkedHashMap<>();
    private static String worldKey;
    private static int ticksInWorld;
    private static boolean dirty;

    private PetTracker() {
    }

    /** Pets remembered for the current world, in any dimension. */
    public static Collection<Pet> pets() {
        return PETS.values();
    }

    public static void tick(Minecraft mc) {
        String key = HeadTrackerConfig.trackPets && mc.level != null && mc.player != null ? worldKey(mc) : null;
        if (key == null ? worldKey != null : !key.equals(worldKey)) {
            switchWorld(key);
        }
        if (key == null) {
            return;
        }

        ticksInWorld++;
        if (ticksInWorld % SCAN_INTERVAL == 0) {
            scan(mc.level, mc.player);
        }
        if (dirty && ticksInWorld % SAVE_INTERVAL == 0) {
            save();
        }
    }

    private static void scan(ClientLevel level, LocalPlayer player) {
        String dimension = level.dimension().toString();
        long now = System.currentTimeMillis();
        Set<String> seen = new HashSet<>();

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof TamableAnimal animal)) {
                continue;
            }

            String uuid = animal.getUUID().toString();
            if (!animal.isTame() || !animal.isOwnedBy(player) || animal.isDeadOrDying()) {
                // Died, was untamed, or now belongs to someone else.
                if (PETS.remove(uuid) != null) {
                    dirty = true;
                }
                continue;
            }

            seen.add(uuid);
            Pet pet = PETS.computeIfAbsent(uuid, id -> new Pet());
            pet.uuid = uuid;
            pet.name = animal.getName().getString();
            pet.type = EntityType.getKey(animal.getType()).toString();
            pet.dimension = dimension;
            pet.x = animal.getX();
            pet.y = animal.getY();
            pet.z = animal.getZ();
            pet.sitting = animal.isInSittingPose();
            pet.lastSeen = now;
            pet.loaded = true;
            pet.missingScans = 0;
            dirty = true;
        }

        PETS.values().removeIf(pet -> {
            if (seen.contains(pet.uuid)) {
                return false;
            }

            pet.loaded = false;
            boolean shouldBeVisible = ticksInWorld > JOIN_GRACE_TICKS
                && dimension.equals(pet.dimension)
                && player.distanceToSqr(pet.x, pet.y, pet.z) < GONE_DISTANCE * GONE_DISTANCE;
            pet.missingScans = shouldBeVisible ? pet.missingScans + 1 : 0;
            if (pet.missingScans >= GONE_SCANS) {
                dirty = true;
                return true;
            }
            return false;
        });
    }

    private static void switchWorld(String key) {
        if (worldKey != null && dirty) {
            save();
        }

        PETS.clear();
        worldKey = key;
        ticksInWorld = 0;
        dirty = false;

        if (key == null) {
            return;
        }

        Path path = file(key);
        if (!Files.isRegularFile(path)) {
            return;
        }

        try (var reader = Files.newBufferedReader(path)) {
            List<Pet> stored = GSON.fromJson(reader, new TypeToken<List<Pet>>() {}.getType());
            if (stored != null) {
                for (Pet pet : stored) {
                    if (pet != null && pet.uuid != null && pet.dimension != null) {
                        PETS.put(pet.uuid, pet);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            HeadTrackerMod.LOGGER.warn("Failed to load remembered pets from {}", path, e);
        }
    }

    private static void save() {
        Path path = file(worldKey);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(new ArrayList<>(PETS.values())));
            dirty = false;
        } catch (IOException e) {
            HeadTrackerMod.LOGGER.warn("Failed to save remembered pets to {}", path, e);
        }
    }

    /**
     * Singleplayer worlds are keyed by their save folder, servers by the address they were
     * joined with. Realms has no server entry to key on, so nothing is tracked there.
     */
    private static String worldKey(Minecraft mc) {
        IntegratedServer singleplayer = mc.getSingleplayerServer();
        if (singleplayer != null) {
            Path folder = singleplayer.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().getFileName();
            return folder != null ? "singleplayer/" + sanitize(folder.toString()) : null;
        }

        ServerData server = mc.getCurrentServer();
        return server != null ? "multiplayer/" + sanitize(server.ip) : null;
    }

    private static String sanitize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    private static Path file(String key) {
        return DIR.resolve(key + ".json");
    }

    /** One remembered pet. Field names double as the JSON keys. */
    public static final class Pet {
        public String uuid;
        public String name;
        public String type;
        public String dimension;
        public double x;
        public double y;
        public double z;
        public boolean sitting;
        public long lastSeen;

        // Runtime only - whether the server is currently sending this pet to us.
        public transient boolean loaded;
        private transient int missingScans;
    }
}
