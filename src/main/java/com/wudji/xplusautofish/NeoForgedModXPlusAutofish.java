package com.wudji.xplusautofish;

import com.mojang.logging.LogUtils;
import com.wudji.xplusautofish.config.Config;
import com.wudji.xplusautofish.config.ConfigManager;
import com.wudji.xplusautofish.gui.AutoFishConfigScreen;
import com.wudji.xplusautofish.scheduler.AutofishScheduler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeoForgedModXPlusAutofish.MODID)
public class NeoForgedModXPlusAutofish
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "autofish";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    private static NeoForgedModXPlusAutofish instance;
    private XPlusAutofish autofish;
    private AutofishScheduler scheduler;
    private static KeyMapping autofishGuiKey;
    private ConfigManager configManager;
    private ModContainer modContainer;

    public static final KeyMapping.Category XPLUS_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("autofish", "category"));
    public static final Lazy<KeyMapping> CONFIG_SCREEN_MAPPING = Lazy.of(() ->
            new KeyMapping("key.autofish.open_gui", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, XPLUS_CATEGORY));


    public NeoForgedModXPlusAutofish(IEventBus modEventBus, ModContainer modContainer) {
        this.modContainer = modContainer;
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);

        modEventBus.addListener(this::onConfigLoaded);
        modEventBus.addListener(this::onConfigReloaded);

        modEventBus.addListener(this::clientSetup);

        NeoForge.EVENT_BUS.register(this);

    }

    private void onConfigLoaded(final ModConfigEvent.Loading event) {
        updateConfig(event.getConfig());
    }

    private void onConfigReloaded(final ModConfigEvent.Reloading event) {
        updateConfig(event.getConfig());
    }

    private void updateConfig(ModConfig config) {
        if (!config.getModId().equals(MODID)) return;
        if (instance == null) {
            instance = this;
            configManager = new ConfigManager(this);
        }
        if (autofish != null) {
            Minecraft.getInstance().execute(autofish::setDetection);
        }
    }

    private void clientSetup(final FMLClientSetupEvent clientSetupEvent) {
        // Register config screen factory (for mod list "Config" button and direct open)
        this.scheduler = new AutofishScheduler(this);
        this.autofish = new XPlusAutofish(this);
        clientSetupEvent.enqueueWork(() -> Minecraft.getInstance().getSoundManager()
                .addListener((sound, soundEvents, range) -> autofish.handleSoundPlayback(sound)));
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                AutoFishConfigScreen::create);
    }

    @SubscribeEvent
    public void tick(ClientTickEvent.Post event) {
        if (this.autofish != null){
            Minecraft client = Minecraft.getInstance();
            if (CONFIG_SCREEN_MAPPING.get().isDown()) {
                client.setScreenAndShow(AutoFishConfigScreen.create(modContainer, client.gui.screen()));
            }
            autofish.tick(client);
            scheduler.tick(client);
        }

    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.registerCategory(XPLUS_CATEGORY);
            event.register(CONFIG_SCREEN_MAPPING.get());
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

    public static NeoForgedModXPlusAutofish getInstance() {
        return instance;
    }

    public XPlusAutofish getAutofish() {
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
