package com.wudji.xplusautofish;

import com.wudji.xplusautofish.config.Config;
import com.wudji.xplusautofish.config.ConfigManager;
import com.wudji.xplusautofish.gui.AutoFishConfigScreen;
import com.wudji.xplusautofish.scheduler.AutofishScheduler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("autofish")
public class ForgeModXPlusAutofish {

    private static ForgeModXPlusAutofish instance;
    private XPlusAutofish autofish;
    private AutofishScheduler scheduler;
    public static final Lazy<KeyMapping> autofishGuiKey = Lazy.of(() ->
            new KeyMapping("key.autofish.open_gui", GLFW.GLFW_KEY_V, "XPlus Autofish"));
    private ConfigManager configManager;

    public ForgeModXPlusAutofish() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::fmlClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void fmlClientSetup(final FMLClientSetupEvent event){
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> (new IExtensionPoint.DisplayTest(IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> {return true;})));

        if (instance == null) instance = this;

        //Create ConfigManager
        this.configManager = new ConfigManager(this);
        //Create Scheduler instance
        this.scheduler = new AutofishScheduler(this);
        //Create Autofisher instance
        this.autofish = new XPlusAutofish(this);
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if(this.autofish != null){
            Minecraft client = Minecraft.getInstance();
            if (autofishGuiKey.get().isDown()) {
                client.setScreen(AutoFishConfigScreen.buildScreen(this, client.screen));
            }
            autofish.tick(client);
            scheduler.tick(client);
        }

    }

    @Mod.EventBusSubscriber(modid = "autofish", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class MyStaticClientOnlyEventHandler {
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(autofishGuiKey.get());
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

    public static ForgeModXPlusAutofish getInstance() {
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

