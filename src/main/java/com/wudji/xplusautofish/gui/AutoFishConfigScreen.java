package com.wudji.xplusautofish.gui;

import com.wudji.xplusautofish.NeoForgedModXPlusAutofish;
import com.wudji.xplusautofish.config.Config;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import net.minecraft.locale.Language;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;


public class AutoFishConfigScreen {
    private static final Function<Boolean, Component> yesNoTextSupplier = bool -> {
        if (bool) return (Component.translatable("options.autofish.toggle.on"));
        else return (Component.translatable("options.autofish.toggle.off"));
    };

    public static Screen buildScreen(NeoForgedModXPlusAutofish modAutofish, Screen parentScreen) {

        Config defaults = new Config();
        Config config = modAutofish.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("options.autofish.title"))
                .transparentBackground()
                .setDoesConfirmSave(true)
                .setSavingRunnable(() -> {
                    modAutofish.getConfig().enforceConstraints();
                    modAutofish.getAutofish().setDetection();
                    modAutofish.getConfigManager().writeConfig(true);
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory configCat = builder.getOrCreateCategory(Component.translatable("options.autofish.config"));


        //Enable Autofish
        AbstractConfigListEntry toggleAutofish = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.enable.title"), config.isAutofishEnabled())
                .setDefaultValue(defaults.isAutofishEnabled())
                .setTooltip(Component.translatable("options.autofish.enable.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setAutofishEnabled(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable MultiRod
        AbstractConfigListEntry toggleMultiRod = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.multirod.title"), config.isMultiRod())
                .setDefaultValue(defaults.isMultiRod())
                .setTooltip(
                        Component.translatable("options.autofish.multirod.tooltip_0"),
                        Component.translatable("options.autofish.multirod.tooltip_1"),
                        Component.translatable("options.autofish.multirod.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setMultiRod(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Open Water Detection
        AbstractConfigListEntry toggleOpenWaterDetection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.open_water_detection.title"), config.isOpenWaterDetectEnabled())
                .setDefaultValue(defaults.isOpenWaterDetectEnabled())
                .setTooltip(
                        Component.translatable("options.autofish.open_water_detection.tooltip_0"),
                        Component.translatable("options.autofish.open_water_detection.tooltip_1"),
                        Component.translatable("options.autofish.open_water_detection.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setOpenWaterDetectEnabled(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();
        //Enable Break Protection
        AbstractConfigListEntry toggleBreakProtection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.break_protection.title"), config.isNoBreak())
                .setDefaultValue(defaults.isNoBreak())
                .setTooltip(
                        Component.translatable("options.autofish.break_protection.tooltip_0"),
                        Component.translatable("options.autofish.break_protection.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setNoBreak(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Persistent Mode
        AbstractConfigListEntry togglePersistentMode = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.persistent.title"), config.isPersistentMode())
                .setDefaultValue(defaults.isPersistentMode())
                .setTooltip(
                        Component.translatable("options.autofish.persistent.tooltip_0"),
                        Component.translatable("options.autofish.persistent.tooltip_1"),
                        Component.translatable("options.autofish.persistent.tooltip_2"),
                        Component.translatable("options.autofish.persistent.tooltip_3"),
                        Component.translatable("options.autofish.persistent.tooltip_4"),
                        Component.translatable("options.autofish.persistent.tooltip_5")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setPersistentMode(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();


        //Enable Sound Detection
        AbstractConfigListEntry toggleSoundDetection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.sound.title"), config.isUseSoundDetection())
                .setDefaultValue(defaults.isUseSoundDetection())
                .setTooltip(Component.translatable("options.autofish.sound.title.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setUseSoundDetection(newValue);
                    modAutofish.getAutofish().setDetection();
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Force MP Detection
        AbstractConfigListEntry toggleForceMPDetection = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.multiplayer_compat.title"), config.isForceMPDetection())
                .setDefaultValue(defaults.isForceMPDetection())
                .setTooltip(Component.translatable("options.autofish.multiplayer_compat.title.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setForceMPDetection(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Recast Delay
        AbstractConfigListEntry recastDelaySlider = entryBuilder.startLongSlider(Component.translatable("options.autofish.recast_delay.title"), config.getRecastDelay(), 500, 5000)
                .setDefaultValue(defaults.getRecastDelay())
                .setTooltip(
                        Component.translatable("options.autofish.recast_delay.tooltip_0"),
                        Component.translatable("options.autofish.recast_delay.tooltip_1")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.recast_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setRecastDelay(newValue);
                })
                .build();
        AbstractConfigListEntry randomDelaySlider = entryBuilder.startLongSlider(Component.translatable("options.autofish.random_delay.title"), config.getRandomDelay(), 0, 75)
                .setDefaultValue(defaults.getRandomDelay())
                .setTooltip(
                        Component.translatable("options.autofish.random_delay.tooltip_0"),
                        Component.translatable("options.autofish.random_delay.tooltip_1"),
                        Component.translatable("options.autofish.random_delay.tooltip_2"),
                        Component.translatable("options.autofish.random_delay.tooltip_3")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.random_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setRandomDelay(newValue);
                })
                .build();

        AbstractConfigListEntry reelInDelay = entryBuilder.startLongSlider(Component.translatable("options.autofish.reel_in_delay.title"), config.getReelInDelay(), 1, 2000)
                .setDefaultValue(defaults.getReelInDelay())
                .setTooltip(
                        Component.translatable("options.autofish.reel_in_delay.tooltip_0"),
                        Component.translatable("options.autofish.reel_in_delay.tooltip_1")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.reel_in_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setReelInDelay(newValue);
                })
                .build();

        //ClearLag Regex
        AbstractConfigListEntry clearLagRegexField = entryBuilder.startTextField(Component.translatable("options.autofish.clear_regex.title"), config.getClearLagRegex())
                .setDefaultValue(defaults.getClearLagRegex())
                .setTooltip(
                        Component.translatable("options.autofish.clear_regex.tooltip_0"),
                        Component.translatable("options.autofish.clear_regex.tooltip_1"),
                        Component.translatable("options.autofish.clear_regex.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setClearLagRegex(newValue);
                })
                .build();

        AbstractConfigListEntry toggleAutoTurnView = entryBuilder.startBooleanToggle(Component.translatable("options.autofish.auto_turn_view.title"), config.isAutoTurnView())
                .setDefaultValue(defaults.isAutoTurnView())
                .setTooltip(
                        Component.translatable("options.autofish.auto_turn_view.tooltip_0"),
                        Component.translatable("options.autofish.auto_turn_view.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setAutoTurnView(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Turn Angle Slider
        AbstractConfigListEntry turnAngleSlider = entryBuilder.startFloatField(Component.translatable("options.autofish.turn_angle.title"), config.getTurnAngle())
                .setDefaultValue(defaults.getTurnAngle())
                .setTooltip(
                        Component.translatable("options.autofish.turn_angle.tooltip_0"),
                        Component.translatable("options.autofish.turn_angle.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setTurnAngle(newValue);
                })
                .build();

        //Turn Duration Slider
        AbstractConfigListEntry turnDurationSlider = entryBuilder.startIntSlider(Component.translatable("options.autofish.turn_duration.title"), config.getTurnDuration(), 100, 5000)
                .setDefaultValue(defaults.getTurnDuration())
                .setTooltip(
                        Component.translatable("options.autofish.turn_duration.tooltip_0"),
                        Component.translatable("options.autofish.turn_duration.tooltip_1")
                )
                .setTextGetter(value -> Component.translatable("options.autofish.turn_duration.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setTurnDuration(newValue);
                })
                .build();

        SubCategoryBuilder subCatBuilderBasic = entryBuilder.startSubCategory(Component.translatable("options.autofish.basic.title"));
        subCatBuilderBasic.add(toggleAutofish);
        subCatBuilderBasic.add(toggleMultiRod);
        subCatBuilderBasic.add(toggleOpenWaterDetection);
        subCatBuilderBasic.add(toggleBreakProtection);
        subCatBuilderBasic.add((togglePersistentMode));
        subCatBuilderBasic.add(toggleAutoTurnView);
        subCatBuilderBasic.add(turnAngleSlider);
        subCatBuilderBasic.add(turnDurationSlider);

        subCatBuilderBasic.setExpanded(true);

        SubCategoryBuilder subCatBuilderAdvanced = entryBuilder.startSubCategory(Component.translatable("options.autofish.advanced.title"));
        subCatBuilderAdvanced.add(recastDelaySlider);
        subCatBuilderAdvanced.add(randomDelaySlider);
        subCatBuilderAdvanced.add(reelInDelay);
        subCatBuilderAdvanced.add(clearLagRegexField);

        subCatBuilderAdvanced.setExpanded(true);

        configCat.addEntry(subCatBuilderBasic.build());
        configCat.addEntry(subCatBuilderAdvanced.build());

        List<String> soundIds = BuiltInRegistries.SOUND_EVENT.keySet().stream().map(Identifier::toString).sorted().toList();
        List<String> fluidIds = BuiltInRegistries.FLUID.keySet().stream().filter(value -> {
            Fluid fluid = BuiltInRegistries.FLUID.getValue(value);
            return fluid != Fluids.EMPTY && (!(fluid instanceof FlowingFluid flowing) || fluid == flowing.getSource());
        }).map(Identifier::toString).sorted().toList();

        AbstractConfigListEntry<String> reelInSound = registryEntry(entryBuilder, "reel_in_sound",
                config.getReelInSound(), defaults.getReelInSound(), soundIds, AutoFishConfigScreen::soundName, config::setReelInSound);
        AbstractConfigListEntry<String> fishingFluid = registryEntry(entryBuilder, "fishing_fluid",
                config.getFishingFluid(), defaults.getFishingFluid(), fluidIds, AutoFishConfigScreen::fluidName, config::setFishingFluid);
        AbstractConfigListEntry<Config.SoundDetectionSource> soundSource = entryBuilder
                .startEnumSelector(Component.translatable("options.autofish.sound_detection_source.title"),
                        Config.SoundDetectionSource.class, config.getSoundDetectionSource())
                .setDefaultValue(defaults.getSoundDetectionSource())
                .setEnumNameProvider(value -> Component.translatable("options.autofish.sound_detection_source." + value.name().toLowerCase(Locale.ROOT)))
                .setTooltip(Component.translatable("options.autofish.sound_detection_source.title.tooltip"))
                .setSaveConsumer(config::setSoundDetectionSource).build();
        AbstractConfigListEntry<Config.SoundDistanceOrigin> soundOrigin = entryBuilder
                .startEnumSelector(Component.translatable("options.autofish.sound_distance_origin.title"),
                        Config.SoundDistanceOrigin.class, config.getSoundDistanceOrigin())
                .setDefaultValue(defaults.getSoundDistanceOrigin())
                .setEnumNameProvider(value -> Component.translatable("options.autofish.sound_distance_origin." + value.name().toLowerCase(Locale.ROOT)))
                .setTooltip(Component.translatable("options.autofish.sound_distance_origin.title.tooltip"))
                .setSaveConsumer(config::setSoundDistanceOrigin).build();
        AbstractConfigListEntry<Integer> soundRange = entryBuilder
                .startIntSlider(Component.translatable("options.autofish.sound_detection_range.title"), config.getSoundDetectionRange(), 1, 32)
                .setDefaultValue(defaults.getSoundDetectionRange())
                .setTooltip(Component.translatable("options.autofish.sound_detection_range.title.tooltip"))
                .setSaveConsumer(config::setSoundDetectionRange).build();
        AbstractConfigListEntry<Integer> reelInCount = entryBuilder
                .startIntSlider(Component.translatable("options.autofish.reel_in_count.title"), config.getReelInCount(), 1, 20)
                .setDefaultValue(defaults.getReelInCount())
                .setTooltip(Component.translatable("options.autofish.reel_in_count.title.tooltip"))
                .setSaveConsumer(config::setReelInCount).build();

        ConfigCategory compatibility = builder.getOrCreateCategory(Component.translatable("options.autofish.compatibility.title"));
        compatibility.addEntry(toggleSoundDetection);
        compatibility.addEntry(toggleForceMPDetection);
        compatibility.addEntry(reelInSound);
        compatibility.addEntry(soundSource);
        compatibility.addEntry(soundOrigin);
        compatibility.addEntry(soundRange);
        compatibility.addEntry(fishingFluid);
        compatibility.addEntry(reelInCount);

        return builder.build();

    }

    private static AbstractConfigListEntry<String> registryEntry(ConfigEntryBuilder builder, String key,
            String current, String defaultValue, List<String> values, Function<String, Component> name,
            java.util.function.Consumer<String> save) {
        java.util.Map<String, Component> names = new java.util.HashMap<>();
        java.util.Map<String, String> ids = new java.util.HashMap<>();
        for (String value : values) {
            Component label = name.apply(value);
            names.put(value, label);
            ids.put(value, value);
            ids.put(label.getString(), value);
        }
        Function<String, Component> displayName = value -> names.getOrDefault(value, Component.literal(value));
        return builder.startDropdownMenu(Component.translatable("options.autofish." + key + ".title"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(current, input -> ids.getOrDefault(input, input),
                                displayName), DropdownMenuBuilder.CellCreatorBuilder.ofWidth(360, displayName))
                .setSelections(values).setSuggestionMode(true)
                .setDefaultValue(defaultValue)
                .setTooltip(Component.translatable("options.autofish." + key + ".title.tooltip"),
                        Component.translatable("options.autofish.registry_search"))
                .setErrorSupplier(value -> values.contains(value) ? Optional.empty()
                        : Optional.of(Component.translatable("options.autofish.invalid_registry_entry")))
                .setSaveConsumer(save).build();
    }

    private static Component soundName(String value) {
        Identifier id = Identifier.tryParse(value);
        WeighedSoundEvents sound = id == null ? null : Minecraft.getInstance().getSoundManager().getSoundEvent(id);
        if (sound == null || sound.getSubtitle() == null) return Component.literal(value);
        return Component.translatable("options.autofish.registry_entry", sound.getSubtitle(), value);
    }

    private static Component fluidName(String value) {
        Identifier id = Identifier.tryParse(value);
        Fluid fluid = id == null ? null : BuiltInRegistries.FLUID.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) return Component.literal(value);
        var bucket = fluid.getBucket().getDefaultInstance();
        if (!bucket.isEmpty()) {
            return Component.translatable("options.autofish.registry_entry", bucket.getHoverName(), value);
        }
        String translationKey = "fluid." + id.getNamespace() + "." + id.getPath().replace('/', '.');
        return Language.getInstance().has(translationKey)
                ? Component.translatable("options.autofish.registry_entry", Component.translatable(translationKey), value)
                : Component.literal(value);
    }
}
