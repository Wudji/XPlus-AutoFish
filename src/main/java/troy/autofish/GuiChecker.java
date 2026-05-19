package troy.autofish;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

public class GuiChecker {
    private AutofishMod modAutofish;

    public GuiChecker(AutofishMod modAutofish) {
        this.modAutofish = modAutofish;
    }

    public void toggleAutoFish(Minecraft client) {
        if(modAutofish.getConfig().isDisableInGUI())
            this.modAutofish.getConfig().setAutofishEnabled(!(client.screen instanceof ContainerScreen));
    }
}
