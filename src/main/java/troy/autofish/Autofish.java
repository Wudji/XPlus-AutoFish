package troy.autofish;

import java.util.Objects;
import java.util.regex.Pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import troy.autofish.config.Config;
import troy.autofish.monitor.FishMonitorMP;
import troy.autofish.monitor.FishMonitorMPMotion;
import troy.autofish.monitor.FishMonitorMPSound;
import troy.autofish.monitor.FishMonitorState;
import troy.autofish.scheduler.Action;
import troy.autofish.scheduler.ActionType;

public class Autofish {

    private static final long PERSISTENT_MODE_INTERVAL = 10000L;

    private Minecraft client;
    private FabricModAutofish modAutofish;
    private FishMonitorMP fishMonitor;
    private final Action persistentModeAction;

    private boolean hookExists = false;
    private OpenWaterState lastOpenWaterState = OpenWaterState.UNKNOWN;
    private long hookRemovedAt = 0L;

    private Pattern cachedPattern;
    private String cachedRegex;
    private String cachedFishingFluidId = Config.DEFAULT_FISHING_FLUID;
    private Fluid cachedFishingFluid = Fluids.WATER;

    public long timeMillis = 0L;

    public Autofish(FabricModAutofish modAutofish) {
        this.modAutofish = modAutofish;
        this.client = Minecraft.getInstance();
        setDetection();

        // Initiate the repeating action for persistent mode casting.
        persistentModeAction = modAutofish.getScheduler().scheduleRepeatingAction(
                PERSISTENT_MODE_INTERVAL, this::checkPersistentMode);
    }

    private void checkPersistentMode() {
        if(!modAutofish.getConfig().isAutofishEnabled()) return;
        if(!modAutofish.getConfig().isPersistentMode()) return;
        if(shouldPreventBreak()) return;
        if(!isHoldingFishingRod()) return;

        // A normal recast owns the rod until it has run. In particular, do not let
        // the ten-second check reel the hook just before that queued cast.
        if(modAutofish.getScheduler().isRecastQueued()) return;

        if(hookExists) {
            if(isBobberInFishingFluid()) return;

            // Reel once, then let the regular delayed recast perform the cast.
            // Falling through here used to use the rod twice in the same tick.
            queueRecast();
            useRod();
            return;
        }

        useRod();
    }

    public void tick(Minecraft client) {

        if (client.level != null && client.player != null && modAutofish.getConfig().isAutofishEnabled()) {

            timeMillis = Util.getMillis(); //update current working time for this tick

            if (isHoldingFishingRod()) {
                if (client.player.fishing != null) {
                    hookExists = true;
                    fishMonitor.hookTick(this, client, client.player.fishing);
                } else {
                    removeHook();
                }
            } else { //not holding fishing rod
                removeHook();
            }
        }
    }

    public void handlePacket(Packet<?> packet) {
        if (modAutofish.getConfig().isAutofishEnabled()) {
            fishMonitor.handlePacket(this, packet, client);
        }
    }

    /**
     * Callback from mixin when chat packets are received
     * For multiplayer detection only
     */
    public void handleChat(ClientboundSystemChatPacket packet) {
        if (!modAutofish.getConfig().isAutofishEnabled()) {
            return;
        }
        if (client.isLocalServer()) {
            return;
        }
        if (!isHoldingFishingRod()) {
            return;
        }
        if (!hookExists && (timeMillis - hookRemovedAt >= 2000)) {
            return;
        }

        String regex = modAutofish.getConfig().getClearLagRegex();
        if (org.apache.commons.lang3.StringUtils.deleteWhitespace(regex).isEmpty()) {
            return;
        }
        if (cachedPattern == null || !regex.equals(cachedRegex)) {
            cachedPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            cachedRegex = regex;
        }
        if (cachedPattern.matcher(StringUtil.stripColor(packet.content().getString())).find()) {
            queueRecast();
        }
    }

    public void catchFish() {
        if(!modAutofish.getScheduler().isRecastQueued()) { //prevents double reels
            modAutofish.getScheduler().onFishCaught();
            if (client.player != null) {
                detectOpenWater(client.player.fishing);
            }
            //queue actions
            queueRodSwitch();
            queueRecast();
            modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN, modAutofish.getConfig().getReelInDelay(), () -> {
                for (int i = 0; i < modAutofish.getConfig().getReelInCount(); i++) {
                    useRod();
                }
            });
        }
    }

    public void queueRecast() {
        modAutofish.getScheduler().scheduleAction(ActionType.RECAST, getRandomDelay()
                + modAutofish.getConfig().getReelInDelay(), () -> {
            //State checks to ensure we can still fish once this runs
            if(hookExists) return;
            if(!isHoldingFishingRod()) return;
            if(shouldPreventBreak()) return;

            useRod();
        });
    }

    private void queueRodSwitch(){
        modAutofish.getScheduler().scheduleAction(ActionType.ROD_SWITCH, (long) (getRandomDelay() * 0.83)
                + modAutofish.getConfig().getReelInDelay(), () -> {
            if(!modAutofish.getConfig().isMultiRod()) return;

            switchToFirstRod(client.player);
        });
    }

    private void detectOpenWater(FishingHook bobber){
        /*
         * To catch items in the treasure category, the bobber must be in open water,
         * defined as the 5×4×5 vicinity around the bobber resting on the water surface
         * (2 blocks away horizontally, 2 blocks above the water surface, and 2 blocks deep).
         * Each horizontal layer in this area must consist only of air and lily pads or water source blocks,
         * waterlogged blocks without collision (such as signs, kelp, or coral fans), and bubble columns.
         * (from Minecraft wiki)
         * */
        if(!modAutofish.getConfig().isOpenWaterDetectEnabled()) return;

        BlockPos bobberPos = bobber.blockPosition();
        for(int yOffset = -2; yOffset <= 2; yOffset++){
            if (!isOpenWaterLayer(bobber, bobberPos, yOffset)) {
                notifyOpenWaterFailure(bobber);
                return;
            }
        }

        notifyOpenWaterSuccess(bobber);
    }

    private boolean isOpenWaterLayer(FishingHook bobber, BlockPos bobberPos, int yOffset) {
        int x = bobberPos.getX();
        int y = bobberPos.getY() + yOffset;
        int z = bobberPos.getZ();
        return BlockPos.betweenClosedStream(x - 2, y, z - 2, x + 2, y, z + 2).allMatch(blockPos ->
                isFishingFluidBlock(bobber, blockPos))
                || BlockPos.betweenClosedStream(x - 2, y, z - 2, x + 2, y, z + 2).allMatch(blockPos ->
                isAirOrLilyPad(bobber, blockPos));
    }

    private boolean isFishingFluidBlock(FishingHook bobber, BlockPos blockPos) {
        return isFishingFluid(bobber.level().getFluidState(blockPos));
    }

    private boolean isAirOrLilyPad(FishingHook bobber, BlockPos blockPos) {
        Block block = bobber.level().getBlockState(blockPos).getBlock();
        return block == Blocks.AIR || block == Blocks.LILY_PAD;
    }

    private void notifyOpenWaterFailure(FishingHook bobber) {
        if(lastOpenWaterState == OpenWaterState.FAIL) return;

        Objects.requireNonNull(bobber.getPlayerOwner()).sendOverlayMessage(Component.translatable("info.autofish.open_water_detection.fail"));
        lastOpenWaterState = OpenWaterState.FAIL;
    }

    private void notifyOpenWaterSuccess(FishingHook bobber) {
        if(lastOpenWaterState == OpenWaterState.SUCCESS) return;

        Objects.requireNonNull(bobber.getPlayerOwner()).sendOverlayMessage(Component.translatable("info.autofish.open_water_detection.success"));
        lastOpenWaterState = OpenWaterState.SUCCESS;
    }

    /**
     * Call this when the hook disappears
     */
    private void removeHook() {
        if (hookExists) {
            hookExists = false;
            hookRemovedAt = timeMillis;
            fishMonitor.handleHookRemoved();
        }
    }

    public void switchToFirstRod(LocalPlayer player) {
        if(player != null) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getNonEquipmentItems().size(); i++) {
                ItemStack slot = inventory.getNonEquipmentItems().get(i);
                if (slot.getItem() == Items.FISHING_ROD) {
                    if (i < 9) { //hotbar only
                        if (modAutofish.getConfig().isNoBreak()) {
                            if (slot.getDamageValue() < slot.getMaxDamage() - 1) {
                                inventory.setSelectedSlot(i);
                                return;
                            }
                        } else {
                            inventory.setSelectedSlot(i);
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean isBobberInFishingFluid(){
        if(client.player != null && client.level != null && client.player.fishing != null) {
            return isFishingFluid(client.level.getFluidState(client.player.fishing.blockPosition()));
        } else{
            return false;
        }
    }

    public boolean isFishingFluid(FluidState state) {
        String fishingFluidId = modAutofish.getConfig().getFishingFluid();
        if (!fishingFluidId.equals(cachedFishingFluidId)) {
            Identifier id = Identifier.tryParse(fishingFluidId);
            cachedFishingFluid = id == null ? Fluids.WATER : BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.WATER);
            cachedFishingFluidId = fishingFluidId;
        }

        Fluid fluid = state.getType();
        Fluid sourceFluid = fluid instanceof FlowingFluid flowingFluid ? flowingFluid.getSource() : fluid;
        return sourceFluid == cachedFishingFluid;
    }

    public boolean isReelInSound(Identifier soundId) {
        return soundId.toString().equals(modAutofish.getConfig().getReelInSound());
    }

    public int getSoundDetectionRange() {
        return modAutofish.getConfig().getSoundDetectionRange();
    }

    public void handleSoundPlayback(SoundInstance sound) {
        Config config = modAutofish.getConfig();
        Identifier soundId = sound.getIdentifier();
        if (!config.isAutofishEnabled() || !config.isUseSoundDetection()
                || config.getSoundDetectionSource() != Config.SoundDetectionSource.CLIENT_PLAYBACK
                || !isReelInSound(soundId)) {
            return;
        }

        double x = sound.getX();
        double y = sound.getY();
        double z = sound.getZ();
        // Defer playback callbacks until the scheduler is no longer processing a rod action.
        client.schedule(() -> handleSound(soundId, x, y, z, Config.SoundDetectionSource.CLIENT_PLAYBACK));
    }

    public void handleSound(Identifier soundId, double x, double y, double z, Config.SoundDetectionSource source) {
        Config config = modAutofish.getConfig();
        if (!config.isAutofishEnabled() || !config.isUseSoundDetection()
                || config.getSoundDetectionSource() != source || !isReelInSound(soundId)
                || client.player == null || client.player.fishing == null) {
            return;
        }

        Entity origin = config.getSoundDistanceOrigin() == Config.SoundDistanceOrigin.PLAYER
                ? client.player : client.player.fishing;
        double soundDetectionRange = getSoundDetectionRange();
        if (origin.distanceToSqr(x, y, z) < soundDetectionRange * soundDetectionRange) {
            catchFish();
        }
    }

    public void useRod() {
        if(client.player != null && client.level != null) {
            InteractionHand hand = getCorrectHand();
            if (modAutofish.getConfig().isEnableArmSwing()) {
                client.player.swing(hand, client.player.getItemInHand(hand).getInteractAnimation(), false);
            }
            InteractionResult actionResult = null;
            if (client.gameMode != null) {
                actionResult = client.gameMode.useItem(client.player, hand);
            }
            if (actionResult != null && actionResult.consumesAction()) {
                client.player.itemUsed(hand);
                // Start the persistent-mode timeout from the actual rod action.
                // This also prevents a due repeating check from casting again in
                // the same tick as a scheduled recast.
                persistentModeAction.resetTimer();
            }
        }
    }

    public boolean isHoldingFishingRod() {
        return isItemFishingRod(getHeldItem().getItem());
    }

    private InteractionHand getCorrectHand() {
        if (!modAutofish.getConfig().isMultiRod()) {
            if (client.player != null && isItemFishingRod(client.player.getOffhandItem().getItem()))
                return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    private ItemStack getHeldItem() {
        if (client.player == null) return ItemStack.EMPTY;

        if (!modAutofish.getConfig().isMultiRod()) {
            if (isItemFishingRod(client.player.getOffhandItem().getItem()))
                return client.player.getOffhandItem();
        }
        return client.player.getMainHandItem();
    }

    private boolean isItemFishingRod(Item item) {
        return item == Items.FISHING_ROD || item instanceof FishingRodItem;
    }

    public void setDetection() {
        fishMonitor = switch (modAutofish.getConfig().getDetectionMode()) {
            case ENTITY -> new FishMonitorState();
            case SOUND -> new FishMonitorMPSound();
            case MOTION -> new FishMonitorMPMotion();
        };
    }

    private long getRandomDelay(){
        long recastDelay = modAutofish.getConfig().getRecastDelay();
        double randomDelayRatio = modAutofish.getConfig().getRandomDelay() * 0.01;
        double randomOffset = (Math.random() * 2 - 1) * randomDelayRatio;
        return (long) (recastDelay * (1 + randomOffset));
    }

    private boolean shouldPreventBreak(){
        if(!modAutofish.getConfig().isNoBreak()) return false;
        ItemStack item = getHeldItem();
        return item != null && item.getDamageValue() == item.getMaxDamage() - 1;
    }

    private enum OpenWaterState {
        UNKNOWN,
        SUCCESS,
        FAIL
    }
}
