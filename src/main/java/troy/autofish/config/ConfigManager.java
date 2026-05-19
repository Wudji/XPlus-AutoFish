package troy.autofish.config;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import troy.autofish.AutofishMod;

public class ConfigManager {

    private static final String CONFIG_FILE_NAME = AutofishMod.MOD_ID + "-client.toml";
    private static final Pair<ConfigSpec, ModConfigSpec> SPEC_PAIR = new ModConfigSpec.Builder().configure(ConfigSpec::new);
    private static final ConfigSpec SPEC_VALUES = SPEC_PAIR.getLeft();
    private static final ModConfigSpec SPEC = SPEC_PAIR.getRight();

    private final Config config = new Config();

    public ConfigManager(net.neoforged.bus.api.IEventBus modEventBus) {
        // Register config
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, SPEC, CONFIG_FILE_NAME);
        
        // Register event listeners on the MOD event bus (not game bus)
        modEventBus.register(this);
        
        // Do NOT call syncFromSpec() here - config is not loaded yet!
        // It will be called when ModConfigEvent.Loading fires
    }

    @SubscribeEvent
    public void onConfigChanged(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            syncFromSpec();
        }
    }

    @SubscribeEvent
    public void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
            syncFromSpec();
        }
    }

    private void copyToSpec(Config source) {
        SPEC_VALUES.autofishEnabled.set(source.isAutofishEnabled());
        SPEC_VALUES.multiRod.set(source.isMultiRod());
        SPEC_VALUES.openWaterDetectEnabled.set(source.isOpenWaterDetectEnabled());
        SPEC_VALUES.noBreak.set(source.isNoBreak());
        SPEC_VALUES.persistentMode.set(source.isPersistentMode());
        SPEC_VALUES.disableInGui.set(source.isDisableInGUI());
        SPEC_VALUES.useSoundDetection.set(source.isUseSoundDetection());
        SPEC_VALUES.forceMpDetection.set(source.isForceMPDetection());
        SPEC_VALUES.autoTurnView.set(source.isAutoTurnView());
        SPEC_VALUES.enableArmSwing.set(source.isEnableArmSwing());
        SPEC_VALUES.turnAngle.set((double) source.getTurnAngle());
        SPEC_VALUES.turnDuration.set(source.getTurnDuration());
        SPEC_VALUES.recastDelay.set(source.getRecastDelay());
        SPEC_VALUES.randomPercent.set(source.getRandomPercent());
        SPEC_VALUES.reelInDelay.set(source.getReelInDelay());
        SPEC_VALUES.clearLagRegex.set(source.getClearLagRegex());
    }

    private void syncFromSpec() {
        config.setAutofishEnabled(SPEC_VALUES.autofishEnabled.get());
        config.setMultiRod(SPEC_VALUES.multiRod.get());
        config.setOpenWaterDetectEnabled(SPEC_VALUES.openWaterDetectEnabled.get());
        config.setNoBreak(SPEC_VALUES.noBreak.get());
        config.setPersistentMode(SPEC_VALUES.persistentMode.get());
        config.setDisableInGUI(SPEC_VALUES.disableInGui.get());
        config.setUseSoundDetection(SPEC_VALUES.useSoundDetection.get());
        config.setForceMPDetection(SPEC_VALUES.forceMpDetection.get());
        config.setAutoTurnView(SPEC_VALUES.autoTurnView.get());
        config.setEnableArmSwing(SPEC_VALUES.enableArmSwing.get());
        config.setTurnAngle(SPEC_VALUES.turnAngle.get().floatValue());
        config.setTurnDuration(SPEC_VALUES.turnDuration.get());
        config.setRecastDelay(SPEC_VALUES.recastDelay.get());
        config.setRandomPercent(SPEC_VALUES.randomPercent.get());
        config.setReelInDelay(SPEC_VALUES.reelInDelay.get());
        config.setClearLagRegex(SPEC_VALUES.clearLagRegex.get());
        config.enforceConstraints();
    }

    public Config getConfig() {
        return config;
    }

    private static final class ConfigSpec {
        final ModConfigSpec.BooleanValue autofishEnabled;
        final ModConfigSpec.BooleanValue multiRod;
        final ModConfigSpec.BooleanValue openWaterDetectEnabled;
        final ModConfigSpec.BooleanValue noBreak;
        final ModConfigSpec.BooleanValue persistentMode;
        final ModConfigSpec.BooleanValue disableInGui;
        final ModConfigSpec.BooleanValue useSoundDetection;
        final ModConfigSpec.BooleanValue forceMpDetection;
        final ModConfigSpec.BooleanValue autoTurnView;
        final ModConfigSpec.BooleanValue enableArmSwing;
        final ModConfigSpec.DoubleValue turnAngle;
        final ModConfigSpec.IntValue turnDuration;
        final ModConfigSpec.LongValue recastDelay;
        final ModConfigSpec.LongValue randomPercent;
        final ModConfigSpec.LongValue reelInDelay;
        final ModConfigSpec.ConfigValue<String> clearLagRegex;

        ConfigSpec(ModConfigSpec.Builder builder) {
            builder.translation("options.autofish.basic.title").push("basic");

            this.autofishEnabled = builder.comment("Enable or disable autofishing.")
                    .translation("options.autofish.enable.title")
                    .define("enabled", true);
            this.multiRod = builder.comment("Cycle to another fishing rod when the current one is no longer usable.")
                    .translation("options.autofish.multirod.title")
                    .define("multiRod", false);
            this.openWaterDetectEnabled = builder.comment("Warn when treasure loot conditions for open water are not met.")
                    .translation("options.autofish.open_water_detection.title")
                    .define("openWaterDetection", true);
            this.noBreak = builder.comment("Stop using rods before they break.")
                    .translation("options.autofish.break_protection.title")
                    .define("breakProtection", false);
            this.persistentMode = builder.comment("Keep the rod cast by periodically retrying when needed.")
                    .translation("options.autofish.persistent.title")
                    .define("persistentMode", false);
            this.disableInGui = builder.comment("Pause autofishing while container screens are open.")
                    .translation("options.autofish.guichecker.title")
                    .define("disableInGui", false);
            this.autoTurnView = builder.comment("Turn the camera automatically after catching a fish.")
                    .translation("options.autofish.auto_turn_view.title")
                    .define("autoTurnView", false);
            this.enableArmSwing = builder.comment("Play the arm swing animation when the rod is used.")
                    .translation("options.autofish.arm_swing.title")
                    .define("enableArmSwing", true);
            this.turnAngle = builder.comment("Camera turn angle in degrees.")
                    .translation("options.autofish.turn_angle.title")
                    .defineInRange("turnAngle", 30.0D, 0.0D, 180.0D);
            this.turnDuration = builder.comment("How long to keep the turned view before restoring it.")
                    .translation("options.autofish.turn_duration.title")
                    .defineInRange("turnDuration", 500, 100, 5000);

            builder.pop();
            builder.translation("options.autofish.advanced.title").push("advanced");

            this.useSoundDetection = builder.comment("Detect bites from bobber sounds instead of bobber motion.")
                    .translation("options.autofish.sound.title")
                    .define("useSoundDetection", false);
            this.forceMpDetection = builder.comment("Force multiplayer-style detection even in local worlds.")
                    .translation("options.autofish.multiplayer_compat.title")
                    .define("forceMultiplayerDetection", false);
            this.recastDelay = builder.comment("Delay before recasting after a catch, in milliseconds.")
                    .translation("options.autofish.recast_delay.title")
                    .defineInRange("recastDelay", 1500L, 500L, 5000L);
            this.randomPercent = builder.comment("Randomize recast delay by up to this percentage.")
                    .translation("options.autofish.random_delay.title")
                    .defineInRange("randomDelayPercent", 50L, 0L, 75L);
            this.reelInDelay = builder.comment("Delay before reeling in after a bite, in milliseconds.")
                    .translation("options.autofish.reel_in_delay.title")
                    .defineInRange("reelInDelay", 1L, 1L, 2000L);
            this.clearLagRegex = builder.comment("Regular expression used to trigger a recast from chat messages.")
                    .translation("options.autofish.clear_regex.title")
                    .define("clearLagRegex", "\\[ClearLag\\] Removed [0-9]+ Entities!");

            builder.pop();
        }
    }
}