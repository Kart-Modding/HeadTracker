package dev.karton.headtracker;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic entry point, called from {@code HeadTrackerFabric}, {@code HeadTrackerNeoForge}
 * or {@code HeadTrackerForge} depending on which loader the jar was built for.
 */
public final class HeadTrackerMod {
    public static final String MOD_ID = "headtracker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.4.0";
    public static final String MINECRAFT = /*$ minecraft*/ "26.3";

    private HeadTrackerMod() {
    }

    public static void init() {
        //? if !release
        //LOGGER.info("Hello world!");

        HeadTrackerConfig.load();
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String namespace, String path) {
        //? if <1.21 {
        /*return new Identifier(namespace, path);
        *///?} else
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}