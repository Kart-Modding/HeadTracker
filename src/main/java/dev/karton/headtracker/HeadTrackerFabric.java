package dev.karton.headtracker;

//? if fabric {
import net.fabricmc.api.ModInitializer;

public class HeadTrackerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        HeadTrackerMod.init();
    }
}
//?}
