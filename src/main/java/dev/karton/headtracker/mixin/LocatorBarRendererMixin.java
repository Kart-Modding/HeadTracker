package dev.karton.headtracker.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.karton.headtracker.HeadTrackerConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    // Letters drawn at the same height as the waypoint dots, positioned with the
    // same angle-to-screen-x math the vanilla bar uses for the dots themselves.
    private static final String[] headtracker$CARDINAL_LETTERS = {"N", "E", "S", "W"};
    private static final float[] headtracker$CARDINAL_YAWS = {180.0F, -90.0F, 0.0F, 90.0F};

    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void headtracker$renderCardinalDirections(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!HeadTrackerConfig.showCardinalDirections) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        float cameraYaw = cameraEntity.getViewYRot(deltaTracker.getGameTimeDeltaPartialTick(true));
        int top = mc.getWindow().getGuiScaledHeight() - 24 - 5;
        int screenMiddle = Mth.ceil((graphics.guiWidth() - 9) / 2.0F);

        for (int i = 0; i < headtracker$CARDINAL_LETTERS.length; i++) {
            float angle = Mth.wrapDegrees(headtracker$CARDINAL_YAWS[i] - cameraYaw);
            if (angle <= -60.0F || angle > 60.0F) {
                continue;
            }

            int dotPosition = Mth.floor(angle * 173.0F / 2.0F / 60.0F);
            String letter = headtracker$CARDINAL_LETTERS[i];
            int textX = screenMiddle + dotPosition + (9 - mc.font.width(letter)) / 2;
            graphics.text(mc.font, letter, textX, top - 2, 0xFFFFFFFF);
        }
    }
    //?} else {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void headtracker$renderCardinalDirections(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!HeadTrackerConfig.showCardinalDirections) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        float cameraYaw = cameraEntity.getViewYRot(deltaTracker.getGameTimeDeltaPartialTick(true));
        int top = mc.getWindow().getGuiScaledHeight() - 24 - 5;
        int screenMiddle = Mth.ceil((graphics.guiWidth() - 9) / 2.0F);

        for (int i = 0; i < headtracker$CARDINAL_LETTERS.length; i++) {
            float angle = Mth.wrapDegrees(headtracker$CARDINAL_YAWS[i] - cameraYaw);
            if (angle <= -60.0F || angle > 60.0F) {
                continue;
            }

            int dotPosition = Mth.floor(angle * 173.0F / 2.0F / 60.0F);
            String letter = headtracker$CARDINAL_LETTERS[i];
            int textX = screenMiddle + dotPosition + (9 - mc.font.width(letter)) / 2;
            graphics.drawString(mc.font, letter, textX, top - 2, 0xFFFFFFFF);
        }
    }
    *///?}
}
