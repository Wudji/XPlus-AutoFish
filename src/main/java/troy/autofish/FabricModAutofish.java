package troy.autofish;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import troy.autofish.config.Config;
import troy.autofish.config.ConfigManager;
import troy.autofish.gui.AutofishScreenBuilder;
import troy.autofish.scheduler.AutofishScheduler;

public class FabricModAutofish implements ClientModInitializer {

    public static final String MOD_ID = "autofish";
    private static FabricModAutofish instance;
    private Autofish autofish;
    private GuiChecker guiChecker;
    private AutofishScheduler scheduler;
    private KeyMapping autofishGuiKey;
    private ConfigManager configManager;

    @Override
    public void onInitializeClient() {

        if (instance == null) instance = this;

        //Create ConfigManager
        this.configManager = new ConfigManager(this);

        KeyMapping.Category CATEGORY = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(MOD_ID, "autofish")
        );

        autofishGuiKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.autofish.open_gui",
                        InputConstants.Type.KEYBOARD,
                        InputConstants.KEY_V,
                        CATEGORY
                )
        );

        //Register Tick Callback
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        //Create Scheduler instance
        this.scheduler = new AutofishScheduler(this);
        //Create Autofisher instance
        this.autofish = new Autofish(this);
        this.guiChecker = new GuiChecker(this);

    }

    public void tick(Minecraft client) {
        if (this.autofish != null){
            if (autofishGuiKey.consumeClick()) {
                client.setScreenAndShow(AutofishScreenBuilder.buildScreen(this, client.gui.screen()));
            }
            guiChecker.toggleAutoFish(client);
            autofish.tick(client);
            scheduler.tick(client);
        }
    }

    /**
     * Mixin callback for Sound and EntityVelocity packets (multiplayer detection)
     */
    public void handlePacket(Packet<?> packet) {
        autofish.handlePacket(packet);
    }

    /**
     * Mixin callback for chat packets
     */
    public void handleChat(ClientboundSystemChatPacket packet) {
        autofish.handleChat(packet);
    }

    /**
     * Mixin callback for catchingFish method of EntityFishHook (singleplayer detection)
     */
    public void tickFishingLogic(Entity owner, int ticksCatchable) {
        autofish.tickFishingLogic(owner, ticksCatchable);
    }

    public static FabricModAutofish getInstance() {
        return instance;
    }

    public Autofish getAutofish() {
        return autofish;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Config getConfig() {
        return configManager.getConfig();
    }

    public AutofishScheduler getScheduler() {
        return scheduler;
    }
}
