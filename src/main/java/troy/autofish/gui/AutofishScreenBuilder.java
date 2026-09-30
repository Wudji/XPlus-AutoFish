package troy.autofish.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import troy.autofish.FabricModAutofish;
import troy.autofish.config.Config;

import java.util.function.Function;
import net.minecraft.util.Language;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluids;


public class AutofishScreenBuilder {

    private static final Function<Boolean, Text> yesNoTextSupplier = bool -> {
        if (bool) return Text.translatable("options.autofish.toggle.on");
        else return Text.translatable("options.autofish.toggle.off");
    };

    public static Screen buildScreen(FabricModAutofish modAutofish, Screen parentScreen) {

        Config defaults = new Config();
        Config config = modAutofish.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Text.translatable("options.autofish.title"))
                .transparentBackground()
                .setDoesConfirmSave(true)
                .setSavingRunnable(() -> {
                    modAutofish.getConfig().enforceConstraints();
                    modAutofish.getAutofish().setDetection();
                    modAutofish.getConfigManager().writeConfig(true);
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory configCat = builder.getOrCreateCategory(Text.translatable("options.autofish.config"));


        //Enable Autofish
        AbstractConfigListEntry toggleAutofish = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.enable.title"), config.isAutofishEnabled())
                .setDefaultValue(defaults.isAutofishEnabled())
                .setTooltip(Text.translatable("options.autofish.enable.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setAutofishEnabled(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable MultiRod
        AbstractConfigListEntry toggleMultiRod = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.multirod.title"), config.isMultiRod())
                .setDefaultValue(defaults.isMultiRod())
                .setTooltip(
                        Text.translatable("options.autofish.multirod.tooltip_0"),
                        Text.translatable("options.autofish.multirod.tooltip_1"),
                        Text.translatable("options.autofish.multirod.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setMultiRod(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Open Water Detection
        AbstractConfigListEntry toggleOpenWaterDetection = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.open_water_detection.title"), config.isOpenWaterDetectEnabled())
                .setDefaultValue(defaults.isOpenWaterDetectEnabled())
                .setTooltip(
                        Text.translatable("options.autofish.open_water_detection.tooltip_0"),
                        Text.translatable("options.autofish.open_water_detection.tooltip_1"),
                        Text.translatable("options.autofish.open_water_detection.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setOpenWaterDetectEnabled(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();
        //Enable Break Protection
        AbstractConfigListEntry toggleBreakProtection = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.break_protection.title"), config.isNoBreak())
                .setDefaultValue(defaults.isNoBreak())
                .setTooltip(
                        Text.translatable("options.autofish.break_protection.tooltip_0"),
                        Text.translatable("options.autofish.break_protection.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setNoBreak(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Persistent Mode
        AbstractConfigListEntry togglePersistentMode = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.persistent.title"), config.isPersistentMode())
                .setDefaultValue(defaults.isPersistentMode())
                .setTooltip(
                        Text.translatable("options.autofish.persistent.tooltip_0"),
                        Text.translatable("options.autofish.persistent.tooltip_1"),
                        Text.translatable("options.autofish.persistent.tooltip_2"),
                        Text.translatable("options.autofish.persistent.tooltip_3"),
                        Text.translatable("options.autofish.persistent.tooltip_4"),
                        Text.translatable("options.autofish.persistent.tooltip_5")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setPersistentMode(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();


        //Enable Sound Detection
        AbstractConfigListEntry toggleSoundDetection = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.sound.title"), config.isUseSoundDetection())
                .setDefaultValue(defaults.isUseSoundDetection())
                .setTooltip(Text.translatable("options.autofish.sound.title.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setUseSoundDetection(newValue);
                    modAutofish.getAutofish().setDetection();
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Force MP Detection
        AbstractConfigListEntry toggleForceMPDetection = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.multiplayer_compat.title"), config.isForceMPDetection())
                .setDefaultValue(defaults.isForceMPDetection())
                .setTooltip(Text.translatable("options.autofish.multiplayer_compat.title.tooltip"))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setForceMPDetection(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Recast Delay
        AbstractConfigListEntry recastDelaySlider = entryBuilder.startLongSlider(Text.translatable("options.autofish.recast_delay.title"), config.getRecastDelay(), 500, 5000)
                .setDefaultValue(defaults.getRecastDelay())
                .setTooltip(
                        Text.translatable("options.autofish.recast_delay.tooltip_0"),
                        Text.translatable("options.autofish.recast_delay.tooltip_1")
                )
                .setTextGetter(value -> Text.translatable("options.autofish.recast_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setRecastDelay(newValue);
                })
                .build();
        AbstractConfigListEntry randomDelaySlider = entryBuilder.startLongSlider(Text.translatable("options.autofish.random_delay.title"), config.getRandomDelay(), 0, 75)
                .setDefaultValue(defaults.getRandomPercent())
                .setTooltip(
                        Text.translatable("options.autofish.random_delay.tooltip_0"),
                        Text.translatable("options.autofish.random_delay.tooltip_1"),
                        Text.translatable("options.autofish.random_delay.tooltip_2"),
                        Text.translatable("options.autofish.random_delay.tooltip_3")
                )
                .setTextGetter(value -> Text.translatable("options.autofish.random_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setRandomDelay(newValue);
                })
                .build();
        AbstractConfigListEntry reelInDelay = entryBuilder.startLongSlider(Text.translatable("options.autofish.reel_in_delay.title"), config.getReelInDelay(), 1, 2000)
                .setDefaultValue(defaults.getReelInDelay())
                .setTooltip(
                        Text.translatable("options.autofish.reel_in_delay.tooltip_0"),
                        Text.translatable("options.autofish.reel_in_delay.tooltip_1")
                )
                .setTextGetter(value -> Text.translatable("options.autofish.reel_in_delay.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setReelInDelay(newValue);
                })
                .build();

        //ClearLag Regex
        AbstractConfigListEntry clearLagRegexField = entryBuilder.startTextField(Text.translatable("options.autofish.clear_regex.title"), config.getClearLagRegex())
                .setDefaultValue(defaults.getClearLagRegex())
                .setTooltip(
                        Text.translatable("options.autofish.clear_regex.tooltip_0"),
                        Text.translatable("options.autofish.clear_regex.tooltip_1"),
                        Text.translatable("options.autofish.clear_regex.tooltip_2")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setClearLagRegex(newValue);
                })
                .build();


        SubCategoryBuilder subCatBuilderBasic = entryBuilder.startSubCategory(Text.translatable("options.autofish.basic.title"));
        subCatBuilderBasic.add(toggleAutofish);
        subCatBuilderBasic.add(toggleMultiRod);
        subCatBuilderBasic.add(toggleOpenWaterDetection);
        subCatBuilderBasic.add(toggleBreakProtection);
        subCatBuilderBasic.add((togglePersistentMode));
        subCatBuilderBasic.setExpanded(true);

        SubCategoryBuilder subCatBuilderAdvanced = entryBuilder.startSubCategory(Text.translatable("options.autofish.advanced.title"));
        subCatBuilderAdvanced.add(recastDelaySlider);
        subCatBuilderAdvanced.add(randomDelaySlider);
        subCatBuilderAdvanced.add(reelInDelay);
        subCatBuilderAdvanced.add(clearLagRegexField);
        subCatBuilderAdvanced.setExpanded(true);

        configCat.addEntry(subCatBuilderBasic.build());
        configCat.addEntry(subCatBuilderAdvanced.build());

        List<String> soundIds = Registries.SOUND_EVENT.getIds().stream().map(Identifier::toString).sorted().toList();
        List<String> fluidIds = Registries.FLUID.getIds().stream().filter(value -> {
            Fluid fluid = Registries.FLUID.get(value);
            return fluid != Fluids.EMPTY && (!(fluid instanceof FlowableFluid flowing) || fluid == flowing.getStill());
        }).map(Identifier::toString).sorted().toList();

        AbstractConfigListEntry<String> reelInSound = registryEntry(entryBuilder, "reel_in_sound",
                config.getReelInSound(), defaults.getReelInSound(), soundIds, AutofishScreenBuilder::soundName, config::setReelInSound);
        AbstractConfigListEntry<String> fishingFluid = registryEntry(entryBuilder, "fishing_fluid",
                config.getFishingFluid(), defaults.getFishingFluid(), fluidIds, AutofishScreenBuilder::fluidName, config::setFishingFluid);
        AbstractConfigListEntry<Config.SoundDetectionSource> soundSource = entryBuilder
                .startEnumSelector(Text.translatable("options.autofish.sound_detection_source.title"),
                        Config.SoundDetectionSource.class, config.getSoundDetectionSource())
                .setDefaultValue(defaults.getSoundDetectionSource())
                .setEnumNameProvider(value -> Text.translatable("options.autofish.sound_detection_source." + value.name().toLowerCase(Locale.ROOT)))
                .setTooltip(Text.translatable("options.autofish.sound_detection_source.title.tooltip"))
                .setSaveConsumer(config::setSoundDetectionSource).build();
        AbstractConfigListEntry<Config.SoundDistanceOrigin> soundOrigin = entryBuilder
                .startEnumSelector(Text.translatable("options.autofish.sound_distance_origin.title"),
                        Config.SoundDistanceOrigin.class, config.getSoundDistanceOrigin())
                .setDefaultValue(defaults.getSoundDistanceOrigin())
                .setEnumNameProvider(value -> Text.translatable("options.autofish.sound_distance_origin." + value.name().toLowerCase(Locale.ROOT)))
                .setTooltip(Text.translatable("options.autofish.sound_distance_origin.title.tooltip"))
                .setSaveConsumer(config::setSoundDistanceOrigin).build();
        AbstractConfigListEntry<Integer> soundRange = entryBuilder
                .startIntSlider(Text.translatable("options.autofish.sound_detection_range.title"), config.getSoundDetectionRange(), 1, 32)
                .setDefaultValue(defaults.getSoundDetectionRange())
                .setTooltip(Text.translatable("options.autofish.sound_detection_range.title.tooltip"))
                .setSaveConsumer(config::setSoundDetectionRange).build();
        AbstractConfigListEntry<Integer> reelInCount = entryBuilder
                .startIntSlider(Text.translatable("options.autofish.reel_in_count.title"), config.getReelInCount(), 1, 20)
                .setDefaultValue(defaults.getReelInCount())
                .setTooltip(Text.translatable("options.autofish.reel_in_count.title.tooltip"))
                .setSaveConsumer(config::setReelInCount).build();

        ConfigCategory compatibility = builder.getOrCreateCategory(Text.translatable("options.autofish.compatibility.title"));
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
            String current, String defaultValue, List<String> values, Function<String, Text> name,
            java.util.function.Consumer<String> save) {
        java.util.Map<String, Text> names = new java.util.HashMap<>();
        java.util.Map<String, String> ids = new java.util.HashMap<>();
        for (String value : values) {
            Text label = name.apply(value);
            names.put(value, label);
            ids.put(value, value);
            ids.put(label.getString(), value);
        }
        Function<String, Text> displayName = value -> names.getOrDefault(value, Text.literal(value));
        return builder.startDropdownMenu(Text.translatable("options.autofish." + key + ".title"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(current, input -> ids.getOrDefault(input, input),
                                displayName), DropdownMenuBuilder.CellCreatorBuilder.ofWidth(360, displayName))
                .setSelections(values).setSuggestionMode(true)
                .setDefaultValue(defaultValue)
                .setTooltip(Text.translatable("options.autofish." + key + ".title.tooltip"),
                        Text.translatable("options.autofish.registry_search"))
                .setErrorSupplier(value -> values.contains(value) ? Optional.empty()
                        : Optional.of(Text.translatable("options.autofish.invalid_registry_entry")))
                .setSaveConsumer(save).build();
    }

    private static Text soundName(String value) {
        Identifier id = Identifier.tryParse(value);
        WeightedSoundSet sound = id == null ? null : MinecraftClient.getInstance().getSoundManager().get(id);
        if (sound == null || sound.getSubtitle() == null) return Text.literal(value);
        return Text.translatable("options.autofish.registry_entry", sound.getSubtitle(), value);
    }

    private static Text fluidName(String value) {
        Identifier id = Identifier.tryParse(value);
        Fluid fluid = id == null ? null : Registries.FLUID.get(id);
        if (fluid == null || fluid == Fluids.EMPTY) return Text.literal(value);
        var bucket = fluid.getBucketItem().getDefaultStack();
        if (!bucket.isEmpty()) {
            return Text.translatable("options.autofish.registry_entry", bucket.getName(), value);
        }
        String translationKey = "fluid." + id.getNamespace() + "." + id.getPath().replace('/', '.');
        return Language.getInstance().hasTranslation(translationKey)
                ? Text.translatable("options.autofish.registry_entry", Text.translatable(translationKey), value)
                : Text.literal(value);
    }
}
