package dev.karton.headtracker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.karton.headtracker.HeadTrackerConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.2 {
@Mixin(net.minecraft.client.gui.Hud.class)
//?} else {
/*@Mixin(net.minecraft.client.gui.Gui.class)
*///?}
public class HudMixin {

    // The locator bar is normally only picked as the active contextual bar when there
    // are waypoints to show; forcing it on lets the cardinal-direction letters stay
    // visible even with an empty bar.
    @ModifyExpressionValue(
        method = "nextContextualInfoState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z"
        )
    )
    private boolean headtracker$forceLocatorBar(boolean original) {
        return original || HeadTrackerConfig.forceLocatorBar;
    }
}
