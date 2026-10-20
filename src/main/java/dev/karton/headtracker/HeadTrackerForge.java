package dev.karton.headtracker;

//? if forge {
/*import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HeadTrackerMod.MOD_ID)
public class HeadTrackerForge {
    public HeadTrackerForge(FMLJavaModLoadingContext context) {
        HeadTrackerMod.init();
        context.registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new HeadTrackerConfigScreen(parent))
        );
    }
}
*///?}
