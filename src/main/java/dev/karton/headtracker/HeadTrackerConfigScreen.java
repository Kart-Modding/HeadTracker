package dev.karton.headtracker;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/**
 * Settings screen, opened from Mod Menu on Fabric/Quilt and from the mods list on NeoForge/Forge.
 */
public class HeadTrackerConfigScreen extends Screen {
    private static final int ROW_HEIGHT = 22;

    private final Screen parent;

    public HeadTrackerConfigScreen(Screen parent) {
        super(Component.translatable("headtracker.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int row = 26;

        this.addRenderableWidget(
            CycleButton.onOffBuilder(HeadTrackerConfig.showCardinalDirections)
                .create(
                    centerX - 100, row, 200, 20,
                    Component.translatable("headtracker.config.show_cardinal_directions"),
                    (button, value) -> HeadTrackerConfig.showCardinalDirections = value
                )
        );
        row += ROW_HEIGHT;

        this.addRenderableWidget(
            CycleButton.onOffBuilder(HeadTrackerConfig.forceLocatorBar)
                .create(
                    centerX - 100, row, 200, 20,
                    Component.translatable("headtracker.config.force_locator_bar"),
                    (button, value) -> HeadTrackerConfig.forceLocatorBar = value
                )
        );
        row += ROW_HEIGHT;

        this.addRenderableWidget(
            CycleButton.onOffBuilder(HeadTrackerConfig.borderMode)
                .create(
                    centerX - 100, row, 200, 20,
                    Component.translatable("headtracker.config.border_mode"),
                    (button, value) -> HeadTrackerConfig.borderMode = value
                )
        );
        row += ROW_HEIGHT;

        this.addRenderableWidget(
            CycleButton.onOffBuilder(HeadTrackerConfig.borderShowNames)
                .create(
                    centerX - 100, row, 200, 20,
                    Component.translatable("headtracker.config.border_show_names"),
                    (button, value) -> HeadTrackerConfig.borderShowNames = value
                )
        );
        row += ROW_HEIGHT;

        this.addRenderableWidget(
            CycleButton.onOffBuilder(HeadTrackerConfig.borderShowDistance)
                .create(
                    centerX - 100, row, 200, 20,
                    Component.translatable("headtracker.config.border_show_distance"),
                    (button, value) -> HeadTrackerConfig.borderShowDistance = value
                )
        );
        row += ROW_HEIGHT;

        this.addRenderableWidget(
            CycleButton.onOffBuilder(HeadTrackerConfig.borderXaeroWaypoints)
                .create(
                    centerX - 100, row, 200, 20,
                    Component.translatable("headtracker.config.border_xaero_waypoints"),
                    (button, value) -> HeadTrackerConfig.borderXaeroWaypoints = value
                )
        );
        row += ROW_HEIGHT;

        this.addRenderableWidget(new IntSlider(
            centerX - 100, row, 200, 20,
            "headtracker.config.border_inset",
            HeadTrackerConfig.BORDER_INSET_MIN, HeadTrackerConfig.BORDER_INSET_MAX,
            HeadTrackerConfig.borderInset,
            value -> HeadTrackerConfig.borderInset = value
        ));
        row += ROW_HEIGHT;

        this.addRenderableWidget(new IntSlider(
            centerX - 100, row, 200, 20,
            "headtracker.config.border_head_size",
            HeadTrackerConfig.BORDER_HEAD_SIZE_MIN, HeadTrackerConfig.BORDER_HEAD_SIZE_MAX,
            HeadTrackerConfig.borderHeadSize,
            value -> HeadTrackerConfig.borderHeadSize = value
        ));

        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(centerX - 100, this.height - 30, 200, 20)
                .build()
        );
    }

    @Override
    public void onClose() {
        HeadTrackerConfig.save();
        //? if >=26.2 {
        this.minecraft.setScreenAndShow(this.parent);
        //?} else {
        /*this.minecraft.setScreen(this.parent);
        *///?}
    }

    private static class IntSlider extends AbstractSliderButton {
        private final String translationKey;
        private final int min;
        private final int max;
        private final IntConsumer setter;

        private IntSlider(int x, int y, int width, int height, String translationKey, int min, int max, int current, IntConsumer setter) {
            super(x, y, width, height, Component.empty(), (double) (current - min) / (max - min));
            this.translationKey = translationKey;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.updateMessage();
        }

        private int intValue() {
            return this.min + (int) Math.round(this.value * (this.max - this.min));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(this.translationKey).append(": " + this.intValue()));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.intValue());
        }
    }
}
