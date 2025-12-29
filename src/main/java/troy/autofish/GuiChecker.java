package troy.autofish;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

public class GuiChecker {
    private FabricModAutofish modAutofish;

    public GuiChecker(FabricModAutofish modAutofish) {
        this.modAutofish = modAutofish;
    }

    public void toggleAutoFish(MinecraftClient client) {
        System.out.println(client.currentScreen);
        if (this.modAutofish.getConfig().isDisableInInventory()) {
            this.modAutofish.getConfig().setAutofishEnabled(!(client.currentScreen instanceof GenericContainerScreen));
        }
    }
}
