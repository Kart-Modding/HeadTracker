package dev.karton.headtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HeadTrackerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Platform.configDir().resolve(HeadTrackerMod.MOD_ID + ".json");

    public static final int BORDER_INSET_MIN = 0;
    public static final int BORDER_INSET_MAX = 64;
    public static final int BORDER_HEAD_SIZE_MIN = 6;
    public static final int BORDER_HEAD_SIZE_MAX = 24;

    public static boolean showCardinalDirections = true;
    public static boolean forceLocatorBar = false;
    public static boolean borderMode = false;
    public static boolean borderShowNames = false;
    public static boolean borderShowDistance = false;
    public static boolean borderXaeroWaypoints = true;
    public static int borderInset = 16;
    public static int borderHeadSize = 12;

    public static void load() {
        if (!Files.isRegularFile(PATH)) {
            return;
        }

        try (var reader = Files.newBufferedReader(PATH)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                showCardinalDirections = data.showCardinalDirections;
                forceLocatorBar = data.forceLocatorBar;
                borderMode = data.borderMode;
                borderShowNames = data.borderShowNames;
                borderShowDistance = data.borderShowDistance;
                borderXaeroWaypoints = data.borderXaeroWaypoints;
                borderInset = clamp(data.borderInset, BORDER_INSET_MIN, BORDER_INSET_MAX);
                borderHeadSize = clamp(data.borderHeadSize, BORDER_HEAD_SIZE_MIN, BORDER_HEAD_SIZE_MAX);
            }
        } catch (IOException e) {
            HeadTrackerMod.LOGGER.warn("Failed to load config, using defaults", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(Data.current()));
        } catch (IOException e) {
            HeadTrackerMod.LOGGER.warn("Failed to save config", e);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Field initializers double as the defaults for keys a config file written by an older
     * version of the mod doesn't have yet - Gson only overwrites what it actually finds.
     */
    private static final class Data {
        boolean showCardinalDirections = true;
        boolean forceLocatorBar = false;
        boolean borderMode = false;
        boolean borderShowNames = false;
        boolean borderShowDistance = false;
        boolean borderXaeroWaypoints = true;
        int borderInset = 16;
        int borderHeadSize = 12;

        static Data current() {
            Data data = new Data();
            data.showCardinalDirections = HeadTrackerConfig.showCardinalDirections;
            data.forceLocatorBar = HeadTrackerConfig.forceLocatorBar;
            data.borderMode = HeadTrackerConfig.borderMode;
            data.borderShowNames = HeadTrackerConfig.borderShowNames;
            data.borderShowDistance = HeadTrackerConfig.borderShowDistance;
            data.borderXaeroWaypoints = HeadTrackerConfig.borderXaeroWaypoints;
            data.borderInset = HeadTrackerConfig.borderInset;
            data.borderHeadSize = HeadTrackerConfig.borderHeadSize;
            return data;
        }
    }
}
