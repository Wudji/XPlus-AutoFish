package troy.autofish.gui;

import net.minecraft.client.gui.screens.Screen;
import troy.autofish.AutofishMod;

public final class AutofishScreenBuilder {

    private AutofishScreenBuilder() {
    }

    public static Screen buildScreen(AutofishMod modAutofish, Screen parentScreen) {
        // NeoForge uses its own config screen system
        // For now, return null - config can be edited via the config file
        return null;
    }
}