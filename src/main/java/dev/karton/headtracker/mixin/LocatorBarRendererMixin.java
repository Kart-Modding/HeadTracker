package dev.karton.headtracker.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

//? if >=26.2 {
@Mixin(net.minecraft.client.gui.contextualbar.LocatorBar.class)
//?} else {
/*@Mixin(net.minecraft.client.gui.contextualbar.LocatorBarRenderer.class)
*///?}
public class LocatorBarRendererMixin {

    @WrapOperation(
        method = "*",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"
        )
    )
    private void headtracker$renderPlayerHead(
        GuiGraphicsExtractor graphics,
        RenderPipeline renderPipeline,
        Identifier sprite,
        int x, int y, int width, int height, int color,
        Operation<Void> original,
        @Local TrackedWaypoint waypoint
    ) {
        // Only the 9x9 player-dot sprite gets replaced; other locator bar icons
        // (self arrow, death markers pointing off-screen, etc) pass through untouched.
        if (width != 9 || height != 9) {
            original.call(graphics, renderPipeline, sprite, x, y, width, height, color);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        UUID waypointId = mc.getConnection() != null ? waypoint.id().left().orElse(null) : null;
        PlayerInfo info = waypointId != null ? mc.getConnection().getPlayerInfo(waypointId) : null;
        if (info == null) {
            original.call(graphics, renderPipeline, sprite, x, y, width, height, color);
            return;
        }

        //? if >=1.21.9 {
        Identifier skinTexture = info.getSkin().body().texturePath();
        //?} else {
        /*Identifier skinTexture = info.getSkin().texture();
        *///?}

        // Colored ring behind the face, same color the vanilla dot would have used.
        // Face size 7 (near 1:1 with the 8x8 skin source) leaves only a 1px ring, but
        // that's sharper than the old 5x5 downscale, which wasn't an integer divisor of 8.
        int solidBorderColor = color | 0xFF000000;
        graphics.fill(x + 1, y, x + 8, y + 9, solidBorderColor);
        graphics.fill(x, y + 1, x + 9, y + 8, solidBorderColor);

        //? if >=26.1 {
        PlayerFaceExtractor.extractRenderState(graphics, skinTexture, x + 1, y + 1, 7, info.showHat(), false, -1);
        //?} else {
        /*PlayerFaceRenderer.draw(graphics, skinTexture, x + 1, y + 1, 7, true, false, -1);
        *///?}
    }
}
