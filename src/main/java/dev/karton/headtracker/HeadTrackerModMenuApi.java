package dev.karton.headtracker;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class HeadTrackerModMenuApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    private static class ConfigScreen extends Screen {
        private final Screen parent;

        private ConfigScreen(Screen parent) {
            super(Component.translatable("headtracker.config.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;

            this.addRenderableWidget(
                CycleButton.onOffBuilder(HeadTrackerConfig.showCardinalDirections)
                    .create(
                        centerX - 100, 40, 200, 20,
                        Component.translatable("headtracker.config.show_cardinal_directions"),
                        (button, value) -> HeadTrackerConfig.showCardinalDirections = value
                    )
            );

            this.addRenderableWidget(
                CycleButton.onOffBuilder(HeadTrackerConfig.forceLocatorBar)
                    .create(
                        centerX - 100, 65, 200, 20,
                        Component.translatable("headtracker.config.force_locator_bar"),
                        (button, value) -> HeadTrackerConfig.forceLocatorBar = value
                    )
            );

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
    }
}
