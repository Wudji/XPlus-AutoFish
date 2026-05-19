package troy.autofish;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import troy.autofish.config.ConfigManager;
import troy.autofish.scheduler.AutofishScheduler;

@Mod(AutofishMod.MOD_ID)
public class AutofishMod {
    public static final String MOD_ID = "autofish";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static AutofishMod instance;
    private final ConfigManager configManager;
    private final Autofish autofish;
    private final GuiChecker guiChecker;
    private final AutofishScheduler scheduler;

    public AutofishMod(IEventBus modEventBus) {
        instance = this;
        
        // Register events
        modEventBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onClientTickPre);
        
        // Initialize components (order matters!)
        this.configManager = new ConfigManager(modEventBus);
        this.scheduler = new AutofishScheduler(this);
        this.autofish = new Autofish(this);
        this.guiChecker = new GuiChecker(this);
        
        LOGGER.info("XPlus Autofish initialized");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Register key bindings here if needed
    }

    private void onClientTickPre(ClientTickEvent.Pre event) {
        if (autofish != null) {
            guiChecker.toggleAutoFish(net.minecraft.client.Minecraft.getInstance());
            autofish.tick(net.minecraft.client.Minecraft.getInstance());
            scheduler.tick(net.minecraft.client.Minecraft.getInstance());
        }
    }

    public static AutofishMod getInstance() {
        return instance;
    }

    public Autofish getAutofish() {
        return autofish;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public troy.autofish.config.Config getConfig() {
        return configManager.getConfig();
    }

    public AutofishScheduler getScheduler() {
        return scheduler;
    }
    
    /**
     * Mixin callback for catchingFish method of EntityFishHook (singleplayer detection)
     */
    public void tickFishingLogic(net.minecraft.world.entity.Entity owner, int ticksCatchable) {
        autofish.tickFishingLogic(owner, ticksCatchable);
    }
    
    /**
     * Mixin callback for Sound and EntityVelocity packets (multiplayer detection)
     */
    public void handlePacket(net.minecraft.network.protocol.Packet<?> packet) {
        autofish.handlePacket(packet);
    }
    
    /**
     * Mixin callback for chat packets
     */
    public void handleChat(net.minecraft.network.protocol.game.ClientboundSystemChatPacket packet) {
        autofish.handleChat(packet);
    }
}