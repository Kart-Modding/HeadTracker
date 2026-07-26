package dev.karton.headtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HeadTrackerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(HeadTrackerMod.MOD_ID + ".json");

    public static boolean showCardinalDirections = true;
    public static boolean forceLocatorBar = false;

    public static void load() {
        if (!Files.isRegularFile(PATH)) {
            return;
        }

        try (var reader = Files.newBufferedReader(PATH)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                showCardinalDirections = data.showCardinalDirections;
                forceLocatorBar = data.forceLocatorBar;
            }
        } catch (IOException e) {
            HeadTrackerMod.LOGGER.warn("Failed to load config, using defaults", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(new Data(showCardinalDirections, forceLocatorBar)));
        } catch (IOException e) {
            HeadTrackerMod.LOGGER.warn("Failed to save config", e);
        }
    }

    private record Data(boolean showCardinalDirections, boolean forceLocatorBar) {
    }
}
