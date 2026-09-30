package com.wudji.xplusautofish.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import java.util.Locale;

public class Config {

    public static final String DEFAULT_REEL_IN_SOUND = "minecraft:entity.fishing_bobber.splash";
    public static final String DEFAULT_FISHING_FLUID = "minecraft:water";
    public static final int DEFAULT_SOUND_DETECTION_RANGE = 5;

    // --- General ---
    public static ModConfigSpec.BooleanValue autofishEnabled;
    public static ModConfigSpec.BooleanValue multiRod;
    public static ModConfigSpec.BooleanValue openWaterDetectEnabled;
    public static ModConfigSpec.BooleanValue noBreak;
    public static ModConfigSpec.BooleanValue persistentMode;
    public static ModConfigSpec.BooleanValue autoTurnView;
    public static ModConfigSpec.DoubleValue turnAngle;
    public static ModConfigSpec.IntValue turnDuration;

    // --- Advanced ---
    public static ModConfigSpec.BooleanValue useSoundDetection;
    public static ModConfigSpec.BooleanValue forceMPDetection;
    public static ModConfigSpec.LongValue recastDelay;
    public static ModConfigSpec.LongValue randomPercent;
    public static ModConfigSpec.LongValue reelInDelay;
    public static ModConfigSpec.ConfigValue<String> clearLagRegex;

    // Compatibility options. Existing detection toggles keep their saved paths.
    public static ModConfigSpec.ConfigValue<String> reelInSound;
    public static ModConfigSpec.EnumValue<SoundDetectionSource> soundDetectionSource;
    public static ModConfigSpec.EnumValue<SoundDistanceOrigin> soundDistanceOrigin;
    public static ModConfigSpec.IntValue soundDetectionRange;
    public static ModConfigSpec.ConfigValue<String> fishingFluid;
    public static ModConfigSpec.IntValue reelInCount;

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation("autofish.configuration.general").push("general");
        autofishEnabled = builder
                .comment("Toggles the entire mod on or off.")
                .define("autofishEnabled", true);
        multiRod = builder
                .comment("Cycles through all available rods in the hotbar, moving to the next as they break.")
                .define("multiRod", false);
        openWaterDetectEnabled = builder
                .comment("Detect whether you are fishing in open water (required for treasure loot in 1.16+).")
                .define("openWaterDetectEnabled", true);
        noBreak = builder
                .comment("Stop using rods with low durability before they break.")
                .define("noBreak", false);
        persistentMode = builder
                .comment("Always keep the fish hook cast whenever a rod is in hand. Checks every 10 seconds and recasts if needed.")
                .define("persistentMode", false);
        autoTurnView = builder
                .comment("Automatically turn the camera view when a fish is caught.")
                .define("autoTurnView", false);
        turnAngle = builder
                .comment("The angle in degrees to turn the camera when a fish is caught.")
                .defineInRange("turnAngle", 30.0, -180.0, 180.0);
        turnDuration = builder
                .comment("How long to hold the turned view before restoring to original position (ms).")
                .defineInRange("turnDuration", 500, 100, 5000);
        builder.pop();

        builder.translation("autofish.configuration.advanced").push("advanced");
        useSoundDetection = builder
                .comment("Use sound-based detection instead of motion-based detection. More accurate but requires proximity to the hook.")
                .translation("autofish.configuration.useSoundDetection")
                .define("useSoundDetection", false);
        forceMPDetection = builder
                .comment("Force multiplayer detection even in singleplayer. Provides compatibility with third-party mods.")
                .translation("autofish.configuration.forceMPDetection")
                .define("forceMPDetection", false);
        recastDelay = builder
                .comment("Delay between catching a fish and recasting the rod (ms).")
                .defineInRange("recastDelay", 1500L, 500L, 5000L);
        randomPercent = builder
                .comment("Add +/-% randomness to the Recast Delay. Set to 0 to disable.")
                .defineInRange("randomPercent", 50L, 0L, 75L);
        reelInDelay = builder
                .comment("Delay before reeling in when a fish bites (ms). Set to 1 to disable this function.")
                .defineInRange("reelInDelay", 1L, 1L, 2000L);
        clearLagRegex = builder
                .comment("Regular expression pattern. Recast the rod when this pattern is matched in chat.")
                .define("clearLagRegex", "\\[ClearLag\\] Removed [0-9]+ Entities!");
        builder.pop();

        builder.translation("options.autofish.compatibility.title").push("compatibility");

        reelInSound = builder.comment("Sound event used to detect a bite when sound detection is enabled.")
                .translation("options.autofish.reel_in_sound.title")
                .define("reelInSound", Config.DEFAULT_REEL_IN_SOUND, Config::isRegisteredSound);
        soundDetectionSource = builder.comment("Detect sounds from server packets or client playback.")
                .translation("options.autofish.sound_detection_source.title")
                .defineEnum("soundDetectionSource", Config.SoundDetectionSource.SERVER_PACKET);
        soundDistanceOrigin = builder.comment("Measure sound detection distance from the bobber or the player.")
                .translation("options.autofish.sound_distance_origin.title")
                .defineEnum("soundDistanceOrigin", Config.SoundDistanceOrigin.BOBBER);
        soundDetectionRange = builder.comment("Maximum distance from the selected origin for matching sound events, in blocks.")
                .translation("options.autofish.sound_detection_range.title")
                .defineInRange("soundDetectionRange", Config.DEFAULT_SOUND_DETECTION_RANGE, 1, 32);
        fishingFluid = builder.comment("Fluid in which the bobber is considered ready for fishing.")
                .translation("options.autofish.fishing_fluid.title")
                .define("fishingFluid", Config.DEFAULT_FISHING_FLUID, Config::isRegisteredFluid);
        reelInCount = builder.comment("Number of consecutive reel-in attempts after a bite is detected.")
                .translation("options.autofish.reel_in_count.title")
                .defineInRange("reelInCount", 1, 1, 20);

        builder.pop();

        SPEC = builder.build();
    }

    public boolean isAutofishEnabled() { return autofishEnabled.get(); }
    public boolean isMultiRod() { return multiRod.get(); }
    public boolean isOpenWaterDetectEnabled() { return openWaterDetectEnabled.get(); }
    public boolean isNoBreak() { return noBreak.get(); }
    public boolean isPersistentMode() { return persistentMode.get(); }
    public boolean isUseSoundDetection() { return useSoundDetection.get(); }
    public boolean isForceMPDetection() { return forceMPDetection.get(); }
    public boolean isAutoTurnView() { return autoTurnView.get(); }
    public double getTurnAngle() { return turnAngle.get(); }
    public int getTurnDuration() { return turnDuration.get(); }
    public long getRecastDelay() { return recastDelay.get(); }
    public long getRandomDelay() { return randomPercent.get(); }
    public long getRandomPercent() { return randomPercent.get(); }
    public long getReelInDelay() { return reelInDelay.get(); }
    public String getClearLagRegex() { return clearLagRegex.get(); }

    public void setAutofishEnabled(boolean v) { autofishEnabled.set(v); }
    public void setMultiRod(boolean v) { multiRod.set(v); }
    public void setNoBreak(boolean v) { noBreak.set(v); }
    public void setPersistentMode(boolean v) { persistentMode.set(v); }
    public void setUseSoundDetection(boolean v) { useSoundDetection.set(v); }
    public void setForceMPDetection(boolean v) { forceMPDetection.set(v); }
    public void setAutoTurnView(boolean v) { autoTurnView.set(v); }
    public void setTurnAngle(double v) { turnAngle.set(v); }
    public void setTurnDuration(int v) { turnDuration.set(v); }
    public void setRecastDelay(long v) { recastDelay.set(v); }
    public void setRandomDelay(long v) { randomPercent.set(v); }
    public void setReelInDelay(long v) { reelInDelay.set(v); }
    public void setClearLagRegex(String v) { clearLagRegex.set(v); }
    public void setOpenWaterDetectEnabled(boolean v) { openWaterDetectEnabled.set(v); }

    /**
     * Constraints are now enforced via defineInRange in the builder.
     * Kept as a no-op for backward compatibility.
     * @return always false (no changes to flush)
     */
    public boolean enforceConstraints() {
        return false;
    }
    public String getReelInSound() { return reelInSound.get(); }
    public void setReelInSound(String value) { reelInSound.set(value); }

    public SoundDetectionSource getSoundDetectionSource() { return soundDetectionSource.get(); }
    public void setSoundDetectionSource(SoundDetectionSource value) { soundDetectionSource.set(value); }

    public SoundDistanceOrigin getSoundDistanceOrigin() { return soundDistanceOrigin.get(); }
    public void setSoundDistanceOrigin(SoundDistanceOrigin value) { soundDistanceOrigin.set(value); }

    public int getSoundDetectionRange() { return soundDetectionRange.get(); }
    public void setSoundDetectionRange(int value) { soundDetectionRange.set(value); }

    public String getFishingFluid() { return fishingFluid.get(); }
    public void setFishingFluid(String value) { fishingFluid.set(value); }

    public int getReelInCount() { return reelInCount.get(); }
    public void setReelInCount(int value) { reelInCount.set(value); }

    public static boolean isRegisteredSound(Object value) {
        if (!(value instanceof String string)) return false;
        Identifier id = Identifier.tryParse(string);
        return id != null && BuiltInRegistries.SOUND_EVENT.containsKey(id);
    }

    public static boolean isRegisteredFluid(Object value) {
        if (!(value instanceof String string)) return false;
        Identifier id = Identifier.tryParse(string);
        if (id == null) return false;
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
        return fluid != Fluids.EMPTY && (!(fluid instanceof FlowingFluid flowing) || fluid == flowing.getSource());
    }

    public enum SoundDetectionSource implements TranslatableEnum {
        SERVER_PACKET,
        CLIENT_PLAYBACK;

        @Override
        public Component getTranslatedName() {
            return Component.translatable("options.autofish.sound_detection_source." + name().toLowerCase(Locale.ROOT));
        }
    }

    public enum SoundDistanceOrigin implements TranslatableEnum {
        BOBBER,
        PLAYER;

        @Override
        public Component getTranslatedName() {
            return Component.translatable("options.autofish.sound_distance_origin." + name().toLowerCase(Locale.ROOT));
        }
    }
}
