package troy.autofish.gui;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import troy.autofish.FabricModAutofish;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class AutofishScreenBuilder {

    private AutofishScreenBuilder() {
    }

    public static Screen buildScreen(FabricModAutofish modAutofish, Screen parentScreen) {
        return new ConfigurationScreen(
                FabricModAutofish.MOD_ID,
                parentScreen == null ? new ReturnToGameScreen() : parentScreen,
                AutofishConfigurationSectionScreen::new
        );
    }

    private static final class AutofishConfigurationSectionScreen extends ConfigurationScreen.ConfigurationSectionScreen {

        private static final String COMPATIBILITY_SECTION = "compatibility";
        private static final String REEL_IN_SOUND_KEY = "reelInSound";
        private static final String FISHING_FLUID_KEY = "fishingFluid";
        private static final String SECTION = "neoforge.configuration.uitext.section";
        private static final String SECTION_TEXT = "neoforge.configuration.uitext.sectiontext";

        private static List<String> soundIds;
        private static List<String> fluidIds;

        private AutofishConfigurationSectionScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
            super(parent, type, modConfig, title);
        }

        private AutofishConfigurationSectionScreen(Context parentContext, Screen parent, Map<String, Object> valueSpecs,
                                                   String key, Set<? extends Entry> entries, Component title) {
            super(parentContext, parent, valueSpecs, key, entries, title);
        }

        @Override
        protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
            if (!COMPATIBILITY_SECTION.equals(key)) {
                return super.createSection(key, subconfig, subsection);
            }
            if (subconfig.isEmpty()) {
                return null;
            }

            Map<String, Object> sectionSpecs = new LinkedHashMap<>(subconfig.valueMap());
            Set<Entry> sectionEntries = new LinkedHashSet<>(subsection.entrySet());

            Component tooltip = getTooltipComponent(key, null);
            return new Element(
                    Component.translatable(SECTION, getTranslationComponent(key)),
                    tooltip,
                    Button.builder(Component.translatable(SECTION, Component.translatable(SECTION_TEXT)), button ->
                                    minecraft.gui.setScreen(sectionCache.computeIfAbsent(key, ignored ->
                                            new AutofishConfigurationSectionScreen(
                                                    context,
                                                    this,
                                                    sectionSpecs,
                                                    key,
                                                    sectionEntries,
                                                    Component.translatable(getTranslationKey(key))
                                            ).rebuild()
                                    )))
                            .tooltip(Tooltip.create(tooltip))
                            .width(Button.DEFAULT_WIDTH)
                            .build(),
                    false
            );
        }

        @Override
        protected Element createStringValue(String key, Predicate<String> tester, Supplier<String> source, Consumer<String> target) {
            if (REEL_IN_SOUND_KEY.equals(key)) {
                return createRegistryValue(key, source, target, AutofishConfigurationSectionScreen::getSoundIds,
                        AutofishConfigurationSectionScreen::getSoundName);
            }
            if (FISHING_FLUID_KEY.equals(key)) {
                return createRegistryValue(key, source, target, AutofishConfigurationSectionScreen::getFluidIds,
                        AutofishConfigurationSectionScreen::getFluidName);
            }
            return super.createStringValue(key, tester, source, target);
        }

        private Element createRegistryValue(String key, Supplier<String> source, Consumer<String> target,
                                            Supplier<List<String>> values, RegistryName registryName) {
            Component tooltip = getTooltipComponent(key, null);
            Button button = Button.builder(registryName.get(source.get()), ignored ->
                            minecraft.gui.setScreen(new RegistrySelectionScreen(
                                    this,
                                    getTranslationComponent(key),
                                    values.get(),
                                    source.get(),
                                    registryName,
                                    newValue -> {
                                        if (!newValue.equals(source.get())) {
                                            Consumer<String> applyValue = value -> {
                                                target.accept(value);
                                                if (REEL_IN_SOUND_KEY.equals(key)) {
                                                    FabricModAutofish.getInstance().getConfig().setReelInSound(value);
                                                } else if (FISHING_FLUID_KEY.equals(key)) {
                                                    FabricModAutofish.getInstance().getConfig().setFishingFluid(value);
                                                }
                                                onChanged(key);
                                            };
                                            undoManager.add(applyValue, newValue, applyValue, source.get());
                                            rebuild();
                                        }
                                    }
                            )))
                    .tooltip(Tooltip.create(tooltip))
                    .width(Button.DEFAULT_WIDTH)
                    .build();
            return new Element(getTranslationComponent(key), tooltip, button);
        }

        private static List<String> getSoundIds() {
            if (soundIds == null) {
                soundIds = BuiltInRegistries.SOUND_EVENT.keySet().stream()
                        .map(Identifier::toString)
                        .sorted()
                        .toList();
            }
            return soundIds;
        }

        private static List<String> getFluidIds() {
            if (fluidIds == null) {
                fluidIds = BuiltInRegistries.FLUID.keySet().stream()
                        .filter(id -> {
                            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
                            return fluid != Fluids.EMPTY
                                    && (!(fluid instanceof FlowingFluid flowingFluid) || fluid == flowingFluid.getSource());
                        })
                        .map(Identifier::toString)
                        .sorted()
                        .toList();
            }
            return fluidIds;
        }

        private static Component getSoundName(String value) {
            Identifier id = Identifier.tryParse(value);
            if (id == null) {
                return Component.literal(value);
            }

            WeighedSoundEvents sound = Minecraft.getInstance().getSoundManager().getSoundEvent(id);
            if (sound == null || sound.getSubtitle() == null) {
                return Component.literal(value);
            }
            return Component.translatable("options.autofish.registry_entry", sound.getSubtitle(), value);
        }

        private static Component getFluidName(String value) {
            Identifier id = Identifier.tryParse(value);
            if (id == null) {
                return Component.literal(value);
            }

            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
            ItemStack bucket = fluid == null ? ItemStack.EMPTY : fluid.getBucket().getDefaultInstance();
            if (bucket.isEmpty()) {
                String translationKey = id.toLanguageKey("fluid");
                if (!Language.getInstance().has(translationKey)) {
                    return Component.literal(value);
                }
                return Component.translatable("options.autofish.registry_entry", Component.translatable(translationKey), value);
            }
            Component name = bucket.getHoverName();
            return Component.translatable("options.autofish.registry_entry", name, value);
        }

        private static final class RegistrySelectionScreen extends OptionsSubScreen {

            private final List<RegistryOption> options;
            private final String currentValue;
            private final Consumer<String> onSelect;
            private RegistrySelectionList selectionList;
            private EditBox search;

            private RegistrySelectionScreen(Screen parent, Component title, List<String> values, String currentValue,
                                            RegistryName registryName, Consumer<String> onSelect) {
                super(parent, Minecraft.getInstance().options, title);
                this.options = values.stream()
                        .map(value -> {
                            Component name = registryName.get(value);
                            String searchText = (name.getString() + " " + value).toLowerCase(Locale.ROOT);
                            return new RegistryOption(value, name, searchText);
                        })
                        .toList();
                this.currentValue = currentValue;
                this.onSelect = onSelect;
            }

            @Override
            protected void addTitle() {
                LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4));
                header.defaultCellSetting().alignHorizontallyCenter();
                header.addChild(new StringWidget(title, font));
                search = header.addChild(new EditBox(font, 0, 0, 200, 15, Component.empty()));
                search.setHint(Component.translatable("options.autofish.registry_search")
                        .withStyle(EditBox.SEARCH_HINT_STYLE));
                search.setResponder(value -> {
                    if (selectionList != null) {
                        selectionList.filterEntries(value);
                    }
                });
                layout.setHeaderHeight(12 + font.lineHeight + 15);
            }

            @Override
            protected void setInitialFocus() {
                if (search == null) {
                    super.setInitialFocus();
                    return;
                }
                setInitialFocus(search);
            }

            @Override
            protected void addContents() {
                selectionList = layout.addToContents(new RegistrySelectionList(minecraft));
            }

            @Override
            protected void addOptions() {
            }

            @Override
            protected void addFooter() {
                layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, ignored -> onDone())
                        .width(200)
                        .build());
            }

            @Override
            protected void repositionElements() {
                super.repositionElements();
                if (selectionList != null) {
                    selectionList.updateSize(width, layout);
                }
            }

            private void onDone() {
                if (selectionList != null) {
                    onSelect.accept(selectionList.getSelectedValue());
                }
                minecraft.gui.setScreen(lastScreen);
            }

            private final class RegistrySelectionList extends ObjectSelectionList<RegistryEntry> {

                private String selectedValue = currentValue;

                private RegistrySelectionList(Minecraft minecraft) {
                    super(minecraft, RegistrySelectionScreen.this.width,
                            RegistrySelectionScreen.this.height - layout.getHeaderHeight() - layout.getFooterHeight(),
                            layout.getHeaderHeight(), 18);
                    filterEntries("");
                    RegistryEntry selected = getSelected();
                    if (selected != null) {
                        centerScrollOn(selected);
                    }
                }

                private void filterEntries(String query) {
                    String normalizedQuery = query.toLowerCase(Locale.ROOT);
                    List<RegistryEntry> entries = options.stream()
                            .filter(option -> option.searchText().contains(normalizedQuery))
                            .map(RegistryEntry::new)
                            .toList();
                    replaceEntries(entries);
                    setSelected(entries.stream()
                            .filter(entry -> entry.option.value().equals(selectedValue))
                            .findFirst()
                            .orElse(null));
                    refreshScrollAmount();
                }

                private void select(RegistryEntry entry) {
                    selectedValue = entry.option.value();
                    setSelected(entry);
                }

                private String getSelectedValue() {
                    RegistryEntry selected = getSelected();
                    return selected == null ? selectedValue : selected.option.value();
                }

                @Override
                public int getRowWidth() {
                    return super.getRowWidth() + 50;
                }
            }

            private final class RegistryEntry extends ObjectSelectionList.Entry<RegistryEntry> {

                private final RegistryOption option;

                private RegistryEntry(RegistryOption option) {
                    this.option = option;
                }

                @Override
                public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                           boolean hovered, float partialTick) {
                    graphics.centeredText(font, option.name(), getContentXMiddle(),
                            getContentYMiddle() - font.lineHeight / 2, -1);
                }

                @Override
                public boolean keyPressed(KeyEvent event) {
                    if (event.isSelection()) {
                        selectionList.select(this);
                        onDone();
                        return true;
                    }
                    return super.keyPressed(event);
                }

                @Override
                public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                    selectionList.select(this);
                    if (doubleClick) {
                        onDone();
                    }
                    return super.mouseClicked(event, doubleClick);
                }

                @Override
                public Component getNarration() {
                    return Component.translatable("narrator.select", option.name());
                }
            }

            private record RegistryOption(String value, Component name, String searchText) {
            }
        }

        @FunctionalInterface
        private interface RegistryName {
            Component get(String value);
        }
    }

    /**
     * Config API Port automatically returns from its category screen when a mod has only one config file.
     * Minecraft 26.3 cannot change to a {@code null} screen from that nested {@code added()} callback.
     */
    private static final class ReturnToGameScreen extends Screen {

        private ReturnToGameScreen() {
            super(Component.empty());
        }

        @Override
        public void added() {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.schedule(() -> {
                if (minecraft.gui.screen() == this) {
                    minecraft.gui.setScreen(null);
                }
            });
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
