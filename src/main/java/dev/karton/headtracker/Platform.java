package dev.karton.headtracker;

import java.nio.file.Path;

/**
 * The few loader APIs the mod needs, so the rest of the code stays loader-agnostic.
 * Quilt runs the Fabric build, and Quilt Loader still provides {@code FabricLoader}.
 */
public final class Platform {
    private Platform() {
    }

    public static Path configDir() {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //?} else if neoforge {
        /*return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?} else {
        /*return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?}
    }

    public static boolean isModLoaded(String modId) {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
        //?} else if neoforge {
        /*return net.neoforged.fml.ModList.get().isLoaded(modId);
        *///?} else if >=26.1 {
        /*return net.minecraftforge.fml.ModList.isLoaded(modId);
        *///?} else {
        /*return net.minecraftforge.fml.ModList.get().isLoaded(modId);
        *///?}
    }
}
