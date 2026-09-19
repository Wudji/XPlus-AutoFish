package troy.autofish.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import troy.autofish.FabricModAutofish;

public final class AutofishScreenBuilder {

    private AutofishScreenBuilder() {
    }

    public static Screen buildScreen(FabricModAutofish modAutofish, Screen parentScreen) {
        return new ConfigurationScreen(FabricModAutofish.MOD_ID, parentScreen == null ? new ReturnToGameScreen() : parentScreen);
    }

    /**
     * Config API Port automatically returns from its category screen when a mod has only one config file.
     * Minecraft 26.3 cannot change to a {@code null} screen from that nested {@code added()} callback.
     */
    private static final class ReturnToGameScreen extends Screen {

        private ReturnToGameScreen() {
            super(Component.empty());
        }

        @Override
        public void added() {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.schedule(() -> {
                if (minecraft.gui.screen() == this) {
                    minecraft.gui.setScreen(null);
                }
            });
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
