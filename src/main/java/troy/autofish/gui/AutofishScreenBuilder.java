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

        //Disable in Inventory
        AbstractConfigListEntry toggleDisableInInventory = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.disable_in_inventory.title"), config.isDisableInInventory())
                .setDefaultValue(defaults.isDisableInInventory())
                .setTooltip(
                        Text.translatable("options.autofish.disable_in_inventory.tooltip_0"),
                        Text.translatable("options.autofish.disable_in_inventory.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setDisableInInventory(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();


        //Enable Sound Detection
        AbstractConfigListEntry toggleSoundDetection = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.sound.title"), config.isUseSoundDetection())
                .setDefaultValue(defaults.isUseSoundDetection())
                .setTooltip(
                        Text.translatable("options.autofish.sound.tooltip_0"),
                        Text.translatable("options.autofish.sound.tooltip_1"),
                        Text.translatable("options.autofish.sound.tooltip_2"),
                        Text.translatable("options.autofish.sound.tooltip_3"),
                        Text.translatable("options.autofish.sound.tooltip_4"),
                        Text.translatable("options.autofish.sound.tooltip_5"),
                        Text.translatable("options.autofish.sound.tooltip_6"),
                        Text.translatable("options.autofish.sound.tooltip_7"),
                        Text.translatable("options.autofish.sound.tooltip_8"),
                        Text.translatable("options.autofish.sound.tooltip_9")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setUseSoundDetection(newValue);
                    modAutofish.getAutofish().setDetection();
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Enable Force MP Detection
        AbstractConfigListEntry toggleForceMPDetection = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.multiplayer_compat.title"), config.isForceMPDetection())
                .setDefaultValue(defaults.isPersistentMode())
                .setTooltip(
                        Text.translatable("options.autofish.multiplayer_compat.tooltip_0"),
                        Text.translatable("options.autofish.multiplayer_compat.tooltip_1"),
                        Text.translatable("options.autofish.multiplayer_compat.tooltip_2")
                )
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

        //Enable Auto Turn View
        AbstractConfigListEntry toggleAutoTurnView = entryBuilder.startBooleanToggle(Text.translatable("options.autofish.auto_turn_view.title"), config.isAutoTurnView())
                .setDefaultValue(defaults.isAutoTurnView())
                .setTooltip(
                        Text.translatable("options.autofish.auto_turn_view.tooltip_0"),
                        Text.translatable("options.autofish.auto_turn_view.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setAutoTurnView(newValue);
                })
                .setYesNoTextSupplier(yesNoTextSupplier)
                .build();

        //Turn Angle Slider
        AbstractConfigListEntry turnAngleSlider = entryBuilder.startFloatField(Text.translatable("options.autofish.turn_angle.title"), config.getTurnAngle())
                .setDefaultValue(defaults.getTurnAngle())
                .setTooltip(
                        Text.translatable("options.autofish.turn_angle.tooltip_0"),
                        Text.translatable("options.autofish.turn_angle.tooltip_1")
                )
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setTurnAngle(newValue);
                })
                .build();

        //Turn Duration Slider
        AbstractConfigListEntry turnDurationSlider = entryBuilder.startIntSlider(Text.translatable("options.autofish.turn_duration.title"), config.getTurnDuration(), 100, 5000)
                .setDefaultValue(defaults.getTurnDuration())
                .setTooltip(
                        Text.translatable("options.autofish.turn_duration.tooltip_0"),
                        Text.translatable("options.autofish.turn_duration.tooltip_1")
                )
                .setTextGetter(value -> Text.translatable("options.autofish.turn_duration.value", value))
                .setSaveConsumer(newValue -> {
                    modAutofish.getConfig().setTurnDuration(newValue);
                })
                .build();


        SubCategoryBuilder subCatBuilderBasic = entryBuilder.startSubCategory(Text.translatable("options.autofish.basic.title"));
        subCatBuilderBasic.add(toggleAutofish);
        subCatBuilderBasic.add(toggleMultiRod);
        subCatBuilderBasic.add(toggleOpenWaterDetection);
        subCatBuilderBasic.add(toggleBreakProtection);
        subCatBuilderBasic.add((togglePersistentMode));
        subCatBuilderBasic.add(toggleDisableInInventory);
        subCatBuilderBasic.setExpanded(true);
        subCatBuilderBasic.add(toggleAutoTurnView);
        subCatBuilderBasic.add(turnAngleSlider);
        subCatBuilderBasic.add(turnDurationSlider);

        SubCategoryBuilder subCatBuilderAdvanced = entryBuilder.startSubCategory(Text.translatable("options.autofish.advanced.title"));
        subCatBuilderAdvanced.add(toggleSoundDetection);
        subCatBuilderAdvanced.add(toggleForceMPDetection);
        subCatBuilderAdvanced.add(recastDelaySlider);
        subCatBuilderAdvanced.add(randomDelaySlider);
        subCatBuilderAdvanced.add(reelInDelay);
        subCatBuilderAdvanced.add(clearLagRegexField);
        subCatBuilderAdvanced.setExpanded(true);

        configCat.addEntry(subCatBuilderBasic.build());
        configCat.addEntry(subCatBuilderAdvanced.build());

        return builder.build();

    }
}
