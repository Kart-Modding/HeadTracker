package dev.karton.headtracker;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.client.resources.WaypointStyleManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;

//? if >=1.21.9
import net.minecraft.world.waypoints.PartialTickSupplier;

import java.util.List;
import java.util.UUID;

/**
 * Border mode: projects the tracked waypoints onto the edges of the screen instead of
 * drawing them on the locator bar, the way the Locator Border mod does. Player waypoints
 * keep the head + colored ring look the locator bar mixin gives them, everything else
 * falls back to its vanilla waypoint style sprite.
 */
public final class BorderRenderer {

    private static final String[] CARDINAL_LETTERS = {"N", "E", "S", "W"};
    private static final float[] CARDINAL_YAWS = {180.0F, -90.0F, 0.0F, 90.0F};

    // The cardinal letters get their own, much shallower bottom edge so the southern letter
    // sits flush against the bottom of the screen rather than stacking above the hotbar.
    private static final int CARDINAL_BOTTOM_INSET = 5;

    private static final int ARROW_WIDTH = 7;
    private static final int ARROW_HEIGHT = 5;
    private static final Identifier ARROW_UP = Identifier.withDefaultNamespace("hud/locator_bar_arrow_up");
    private static final Identifier ARROW_DOWN = Identifier.withDefaultNamespace("hud/locator_bar_arrow_down");

    private BorderRenderer() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        // Called from the tail of the hotbar/decorations pass, which vanilla already skips
        // while the HUD is hidden, so there's no hide-gui check to repeat here.
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return;
        }

        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        // Everything drawn from here on goes into a fresh stratum, so the heads and the
        // cardinal letters sit on top of the hotbar and the rest of the HUD instead of
        // being batched in among them.
        graphics.nextStratum();

        // Same inset on all four edges - the icons sit as low as the border inset allows and
        // are drawn over the hotbar rather than stacked above it.
        Level level = cameraEntity.level();
        Rect rect = Rect.of(graphics, HeadTrackerConfig.borderInset);

        mc.getConnection().getWaypointManager().forEachWaypoint(cameraEntity, waypoint -> {
            // The camera entity's own waypoint would just sit wherever we happen to be looking.
            if (waypoint.id().left().filter(cameraEntity.getUUID()::equals).isPresent()) {
                return;
            }

            float angle = (float) yawAngleToCamera(waypoint, level, cameraEntity, deltaTracker);
            renderWaypoint(graphics, rect, waypoint, angle, level, cameraEntity, deltaTracker);
        });

        if (HeadTrackerConfig.borderXaeroWaypoints) {
            renderXaeroWaypoints(graphics, rect, cameraEntity, deltaTracker);
        }

        if (HeadTrackerConfig.showCardinalDirections) {
            renderCardinalDirections(graphics, Rect.of(graphics, CARDINAL_BOTTOM_INSET), cameraEntity, deltaTracker);
        }
    }

    private static void renderWaypoint(
        GuiGraphicsExtractor graphics,
        Rect rect,
        TrackedWaypoint waypoint,
        float angle,
        Level level,
        Entity cameraEntity,
        DeltaTracker deltaTracker
    ) {
        Minecraft mc = Minecraft.getInstance();
        Projected at = project(rect, angle);

        int size = HeadTrackerConfig.borderHeadSize;
        int x = Mth.floor(at.x());
        int y = Mth.floor(at.y());
        int iconX = x - size / 2;
        int iconY = y - size / 2;

        UUID uuid = waypoint.id().left().orElse(null);
        PlayerInfo info = uuid != null ? mc.getConnection().getPlayerInfo(uuid) : null;
        // Server-supplied waypoint colors don't always carry an alpha channel, and the bar's
        // own background is gone in border mode, so force the icons fully opaque.
        int color = waypointColor(waypoint) | 0xFF000000;
        float distance = Mth.sqrt((float) waypoint.distanceSquared(cameraEntity));

        if (info != null) {
            // Colored ring behind the face, same color the vanilla locator bar dot would have used.
            graphics.fill(iconX, iconY - 1, iconX + size, iconY + size + 1, color);
            graphics.fill(iconX - 1, iconY, iconX + size + 1, iconY + size, color);
            face(graphics, skinTexture(info), iconX, iconY, size, info.showHat());
        } else {
            WaypointStyle style = waypointStyles(mc).get(waypoint.icon().style);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, style.sprite(distance), iconX, iconY, size, size, color);
        }

        // The single arrow slot carries the pitch direction when there is one. Otherwise it
        // flags a waypoint sitting behind the camera - the icon's position on the border says
        // as much already, but a marker is a lot quicker to read.
        TrackedWaypoint.PitchDirection pitchDirection = pitchDirectionToCamera(waypoint, level, cameraEntity, deltaTracker);
        boolean levelWithCamera = pitchDirection == TrackedWaypoint.PitchDirection.NONE;
        boolean behind = Math.abs(Mth.wrapDegrees(angle)) > 90.0F;
        boolean showArrow = !levelWithCamera || behind;
        boolean arrowBelow = levelWithCamera ? behind : pitchDirection == TrackedWaypoint.PitchDirection.DOWN;

        int arrowSpaceAbove = 0;
        int arrowSpaceBelow = 0;
        if (showArrow) {
            Identifier arrow = arrowBelow ? ARROW_DOWN : ARROW_UP;
            int arrowY = arrowBelow ? iconY + size + 2 : iconY - ARROW_HEIGHT - 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrow, x - ARROW_WIDTH / 2, arrowY, ARROW_WIDTH, ARROW_HEIGHT, 0xFFFFFFFF);

            if (arrowBelow) {
                arrowSpaceBelow = ARROW_HEIGHT + 2;
            } else {
                arrowSpaceAbove = ARROW_HEIGHT + 2;
            }
        }

        String name = HeadTrackerConfig.borderShowNames && info != null ? profileName(info) : null;
        String distanceText = HeadTrackerConfig.borderShowDistance ? Mth.floor(distance) + "m" : null;
        if (name != null || distanceText != null) {
            renderLabels(graphics, at, x, y, size, arrowSpaceAbove, arrowSpaceBelow, name, distanceText);
        }
    }

    // Labels go on the inward side of the icon so they never run off the screen edge, and
    // step out of the way of whichever side the arrow took.
    private static void renderLabels(
        GuiGraphicsExtractor graphics,
        Projected at,
        int x,
        int y,
        int size,
        int arrowSpaceAbove,
        int arrowSpaceBelow,
        String name,
        String distanceText
    ) {
        Font font = Minecraft.getInstance().font;
        int lineSpacing = 1;
        int lines = (name != null ? 1 : 0) + (distanceText != null ? 1 : 0);
        int blockHeight = lines * font.lineHeight + (lines - 1) * lineSpacing;
        int blockWidth = Math.max(
            name != null ? font.width(name) : 0,
            distanceText != null ? font.width(distanceText) : 0
        );

        int textX;
        int textY;
        if (Math.abs(at.dirY()) > Math.abs(at.dirX())) {
            // Top or bottom edge - centered under (or above) the icon.
            textX = x - blockWidth / 2;
            textY = at.dirY() > 0.0F
                ? y - size / 2 - blockHeight - 2 - arrowSpaceAbove
                : y + size / 2 + 2 + arrowSpaceBelow;
        } else {
            // Left or right edge - beside the icon, vertically centered on it.
            textX = at.dirX() > 0.0F ? x - size / 2 - blockWidth - 3 : x + size / 2 + 3;
            textY = y - blockHeight / 2;
        }

        if (name != null) {
            text(graphics, font, name, textX + (blockWidth - font.width(name)) / 2, textY, 0xFFFFFFFF);
            textY += font.lineHeight + lineSpacing;
        }
        if (distanceText != null) {
            text(graphics, font, distanceText, textX + (blockWidth - font.width(distanceText)) / 2, textY, 0xFFAAAAAA);
        }
    }

    /**
     * Xaero's Minimap draws its own in-world marker for a waypoint whenever that waypoint is
     * inside the camera's field of view, so only the ones that fall outside it get put on the
     * border here. The check is a plain yaw/pitch comparison against the FOV rather than a real
     * frustum test - close enough for deciding when to hand over, and it needs no per-version
     * projection matrix handling.
     */
    private static void renderXaeroWaypoints(
        GuiGraphicsExtractor graphics,
        Rect rect,
        Entity cameraEntity,
        DeltaTracker deltaTracker
    ) {
        List<XaeroWaypoints.Waypoint> waypoints = XaeroWaypoints.collect();
        if (waypoints.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        Vec3 eye = cameraEntity.getEyePosition(partialTick);
        float cameraYaw = cameraEntity.getViewYRot(partialTick);
        float cameraPitch = cameraEntity.getViewXRot(partialTick);

        float halfVerticalFov = mc.options.fov().get() / 2.0F;
        float aspect = (float) graphics.guiWidth() / Math.max(1, graphics.guiHeight());
        float halfHorizontalFov =
            (float) Math.toDegrees(Math.atan(Math.tan(Math.toRadians(halfVerticalFov)) * aspect));

        int size = HeadTrackerConfig.borderHeadSize;

        for (XaeroWaypoints.Waypoint waypoint : waypoints) {
            double dx = waypoint.x() + 0.5 - eye.x;
            double dy = waypoint.y() + 0.5 - eye.y;
            double dz = waypoint.z() + 0.5 - eye.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);

            float yawDiff = Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-dx, dz)) - cameraYaw);
            float pitchDiff = Mth.wrapDegrees((float) -Math.toDegrees(Math.atan2(dy, horizontal)) - cameraPitch);

            boolean above = pitchDiff < -halfVerticalFov;
            boolean below = pitchDiff > halfVerticalFov;
            if (!above && !below && Math.abs(yawDiff) < halfHorizontalFov) {
                // On screen - leave it to Xaero.
                continue;
            }

            Projected at = project(rect, yawDiff);
            int x = Mth.floor(at.x());
            int y = Mth.floor(at.y());
            int iconX = x - size / 2;
            int iconY = y - size / 2;

            // Xaero paints its own waypoint marker as a solid plate in the waypoint's color
            // with the initials in white on top of it, so use that here rather than inventing
            // a second look for the same waypoint.
            graphics.fill(iconX, iconY, iconX + size, iconY + size, waypoint.color());

            String initials = waypoint.initials();
            if (!initials.isEmpty()) {
                text(
                    graphics, font, initials,
                    x - font.width(initials) / 2,
                    y - font.lineHeight / 2,
                    0xFFFFFFFF
                );
            }

            boolean behind = Math.abs(yawDiff) > 90.0F;
            boolean arrowBelow = below || (!above && behind);
            int arrowSpaceAbove = 0;
            int arrowSpaceBelow = 0;
            if (above || below || behind) {
                Identifier arrow = arrowBelow ? ARROW_DOWN : ARROW_UP;
                int arrowY = arrowBelow ? iconY + size + 2 : iconY - ARROW_HEIGHT - 2;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, arrow, x - ARROW_WIDTH / 2, arrowY, ARROW_WIDTH, ARROW_HEIGHT, 0xFFFFFFFF);

                if (arrowBelow) {
                    arrowSpaceBelow = ARROW_HEIGHT + 2;
                } else {
                    arrowSpaceAbove = ARROW_HEIGHT + 2;
                }
            }

            String name = HeadTrackerConfig.borderShowNames && !waypoint.name().isEmpty() ? waypoint.name() : null;
            String distanceText = HeadTrackerConfig.borderShowDistance
                ? Mth.floor(Math.sqrt(dx * dx + dy * dy + dz * dz)) + "m"
                : null;
            if (name != null || distanceText != null) {
                renderLabels(graphics, at, x, y, size, arrowSpaceAbove, arrowSpaceBelow, name, distanceText);
            }
        }
    }

    private static void renderCardinalDirections(
        GuiGraphicsExtractor graphics,
        Rect rect,
        Entity cameraEntity,
        DeltaTracker deltaTracker
    ) {
        Font font = Minecraft.getInstance().font;
        float cameraYaw = cameraEntity.getViewYRot(deltaTracker.getGameTimeDeltaPartialTick(true));

        for (int i = 0; i < CARDINAL_LETTERS.length; i++) {
            String letter = CARDINAL_LETTERS[i];
            Projected at = project(rect, Mth.wrapDegrees(CARDINAL_YAWS[i] - cameraYaw));
            text(
                graphics, font, letter,
                Mth.floor(at.x()) - font.width(letter) / 2,
                Mth.floor(at.y()) - font.lineHeight / 2,
                0xFFFFFFFF
            );
        }
    }

    /**
     * Scales the unit direction vector for {@code angleDegrees} until it lands exactly on the
     * border rectangle. Angle 0 is straight ahead, which puts the icon at the top edge.
     */
    private static Projected project(Rect rect, float angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        float dirX = (float) Math.sin(radians);
        float dirY = (float) -Math.cos(radians);

        float hit = Math.max(Math.abs(dirX) / rect.halfWidth(), Math.abs(dirY) / rect.halfHeight());
        if (hit <= 0.0F) {
            return new Projected(rect.centerX(), rect.centerY() - rect.halfHeight(), 0.0F, -1.0F);
        }

        return new Projected(rect.centerX() + dirX / hit, rect.centerY() + dirY / hit, dirX, dirY);
    }

    // Mirrors the color the vanilla locator bar picks for a waypoint dot.
    private static int waypointColor(TrackedWaypoint waypoint) {
        return waypoint.icon().color.orElseGet(() -> waypoint.id().map(
            uuid -> ARGB.setBrightness(ARGB.color(255, uuid.hashCode()), 0.9F),
            name -> ARGB.setBrightness(ARGB.color(255, name.hashCode()), 0.9F)
        ));
    }

    private static double yawAngleToCamera(TrackedWaypoint waypoint, Level level, Entity cameraEntity, DeltaTracker deltaTracker) {
        //? if >=26.2 {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        //?} else {
        /*Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        *///?}

        //? if >=1.21.9 {
        return waypoint.yawAngleToCamera(level, camera, partialTicks(level, cameraEntity, deltaTracker));
        //?} else {
        /*return waypoint.yawAngleToCamera(level, camera);
        *///?}
    }

    private static TrackedWaypoint.PitchDirection pitchDirectionToCamera(
        TrackedWaypoint waypoint,
        Level level,
        Entity cameraEntity,
        DeltaTracker deltaTracker
    ) {
        //? if >=1.21.9 {
        return waypoint.pitchDirectionToCamera(level, Minecraft.getInstance().gameRenderer, partialTicks(level, cameraEntity, deltaTracker));
        //?} else {
        /*return waypoint.pitchDirectionToCamera(level, Minecraft.getInstance().gameRenderer);
        *///?}
    }

    //? if >=1.21.9 {
    private static PartialTickSupplier partialTicks(Level level, Entity cameraEntity, DeltaTracker deltaTracker) {
        boolean frozen = level.tickRateManager().isEntityFrozen(cameraEntity);
        return entity -> deltaTracker.getGameTimeDeltaPartialTick(!frozen);
    }
    //?}

    private static WaypointStyleManager waypointStyles(Minecraft mc) {
        //? if >=26.2 {
        return mc.gui.hud.getWaypointStyles();
        //?} else {
        /*return mc.getWaypointStyles();
        *///?}
    }

    private static Identifier skinTexture(PlayerInfo info) {
        //? if >=1.21.9 {
        return info.getSkin().body().texturePath();
        //?} else {
        /*return info.getSkin().texture();
        *///?}
    }

    private static String profileName(PlayerInfo info) {
        //? if >=1.21.9 {
        return info.getProfile().name();
        //?} else {
        /*return info.getProfile().getName();
        *///?}
    }

    private static void face(GuiGraphicsExtractor graphics, Identifier skin, int x, int y, int size, boolean showHat) {
        //? if >=26.1 {
        PlayerFaceExtractor.extractRenderState(graphics, skin, x, y, size, showHat, false, -1);
        //?} else {
        /*PlayerFaceExtractor.draw(graphics, skin, x, y, size, showHat, false, -1);
        *///?}
    }

    private static void text(GuiGraphicsExtractor graphics, Font font, String value, int x, int y, int color) {
        //? if >=26.1 {
        graphics.text(font, value, x, y, color);
        //?} else {
        /*graphics.drawString(font, value, x, y, color);
        *///?}
    }

    /** The rectangle waypoints get projected onto, in GUI-scaled pixels. */
    private record Rect(float centerX, float centerY, float halfWidth, float halfHeight) {
        static Rect of(GuiGraphicsExtractor graphics, int bottomInset) {
            int inset = HeadTrackerConfig.borderInset;
            float left = inset;
            float right = graphics.guiWidth() - inset;
            float top = inset;
            float bottom = graphics.guiHeight() - bottomInset;

            return new Rect(
                (left + right) / 2.0F,
                (top + bottom) / 2.0F,
                Math.max(1.0F, (right - left) / 2.0F),
                Math.max(1.0F, (bottom - top) / 2.0F)
            );
        }
    }

    /** A point on the border plus the unit direction that produced it, used to place labels. */
    private record Projected(float x, float y, float dirX, float dirY) {
    }
}
