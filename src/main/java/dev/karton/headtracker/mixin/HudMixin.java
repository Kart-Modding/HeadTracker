package dev.karton.headtracker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.karton.headtracker.BorderRenderer;
import dev.karton.headtracker.HeadTrackerConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(net.minecraft.client.gui.Hud.class)
//?} else {
/*@Mixin(net.minecraft.client.gui.Gui.class)
*///?}
public class HudMixin {

    // The locator bar is normally only picked as the active contextual bar when there
    // are waypoints to show; forcing it on lets the cardinal-direction letters stay
    // visible even with an empty bar. Border mode does the opposite - it draws the
    // waypoints itself, so the bar has to stay out of the experience bar's way.
    @ModifyExpressionValue(
        method = "nextContextualInfoState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z"
        )
    )
    private boolean headtracker$forceLocatorBar(boolean original) {
        if (HeadTrackerConfig.borderMode) {
            return false;
        }

        return original || HeadTrackerConfig.forceLocatorBar;
    }

    // Vanilla draws the whole hotbar block in one method; NeoForge and Forge split it into
    // separate HUD layers and skip that method, so there the hook is the contextual bar layer.
    //? if forge {
    /*@Inject(method = "extractContextualInfoState", at = @At("TAIL"))
    *///?} else if neoforge && >=26.1 {
    /*@Inject(method = "extractContextualInfoBar", at = @At("TAIL"))
    *///?} else if neoforge {
    /*@Inject(method = "renderContextualInfoBar", at = @At("TAIL"))
    *///?} else if >=26.1 {
    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    //?} else {
    /*@Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
    *///?}
    private void headtracker$renderLocatorBorder(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (HeadTrackerConfig.borderMode) {
            BorderRenderer.render(graphics, deltaTracker);
        }
    }
}
