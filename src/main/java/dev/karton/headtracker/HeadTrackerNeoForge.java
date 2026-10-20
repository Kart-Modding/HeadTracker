package dev.karton.headtracker;

//? if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = HeadTrackerMod.MOD_ID, dist = Dist.CLIENT)
public class HeadTrackerNeoForge {
    public HeadTrackerNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        HeadTrackerMod.init();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new HeadTrackerConfigScreen(parent));
    }
}
*///?}
