package troy.autofish;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import troy.autofish.monitor.FishMonitorMP;
import troy.autofish.monitor.FishMonitorMPMotion;
import troy.autofish.monitor.FishMonitorMPSound;
import troy.autofish.scheduler.ActionType;

public class Autofish {

    private Minecraft client;
    private FabricModAutofish modAutofish;
    private FishMonitorMP fishMonitorMP;

    private boolean hookExists = false;
    private OpenWaterState lastOpenWaterState = OpenWaterState.UNKNOWN;
    private long hookRemovedAt = 0L;

    public long timeMillis = 0L;

    public Autofish(FabricModAutofish modAutofish) {
        this.modAutofish = modAutofish;
        this.client = Minecraft.getInstance();
        setDetection();

        //Initiate the repeating action for persistent mode casting
        modAutofish.getScheduler().scheduleRepeatingAction(10000, () -> {
            if(!modAutofish.getConfig().isPersistentMode()) return;
            if(shouldPreventBreak()) return;
            if(!isHoldingFishingRod()) return;
            if(hookExists){
                if(isBobberInWater()) return;

                else useRod();
            }
            if(modAutofish.getScheduler().isRecastQueued()) return;

            useRod();
        });
    }

    public void tick(Minecraft client) {

        if (client.level != null && client.player != null && modAutofish.getConfig().isAutofishEnabled()) {

            timeMillis = Util.getMillis(); //update current working time for this tick

            if (isHoldingFishingRod()) {
                if (client.player.fishing != null) {
                    hookExists = true;
                    //MP catch listener
                    if (shouldUseMPDetection()) {//multiplayer only, send tick event to monitor
                        fishMonitorMP.hookTick(this, client, client.player.fishing);
                    }
                } else {
                    removeHook();
                }
            } else { //not holding fishing rod
                removeHook();
            }
        }
    }

    /**
     * Callback from mixin for the catchingFish method of the EntityFishHook
     * for singleplayer detection only
     */
    public void tickFishingLogic(Entity owner, int ticksCatchable) {
        //This callback will come from the Server thread. Use client.execute() to run this action in the Render thread
        client.execute(() -> {
            if (modAutofish.getConfig().isAutofishEnabled() && !shouldUseMPDetection()) {
                //null checks for sanity
                if (client.player != null && client.player.fishing != null) {
                    //hook is catchable and player is correct
                    if (ticksCatchable > 0 && owner.getUUID().compareTo(client.player.getUUID()) == 0) {
                        catchFish();
                    }
                }
            }
        });
    }

    /**
     * Callback from mixin when sound and motion packets are received
     * For multiplayer detection only
     */
    public void handlePacket(Packet<?> packet) {
        if (modAutofish.getConfig().isAutofishEnabled()) {
            if (shouldUseMPDetection()) {
                fishMonitorMP.handlePacket(this, packet, client);
            }
        }
    }

    /**
     * Callback from mixin when chat packets are received
     * For multiplayer detection only
     */
    public void handleChat(ClientboundSystemChatPacket packet) {
        if (modAutofish.getConfig().isAutofishEnabled()) {
            if (!client.isLocalServer()) {
                if (isHoldingFishingRod()) {
                    //check that either the hook exists, or it was just removed
                    //this prevents false casts if we are holding a rod but not fishing
                    if (hookExists || (timeMillis - hookRemovedAt < 2000)) {
                        //make sure there is actually something there in the regex field
                        if (org.apache.commons.lang3.StringUtils.deleteWhitespace(modAutofish.getConfig().getClearLagRegex()).isEmpty())
                            return;
                        //check if it matches
                        Matcher matcher = Pattern.compile(modAutofish.getConfig().getClearLagRegex(), Pattern.CASE_INSENSITIVE).matcher(StringUtil.stripColor(packet.content().getString()));
                        if (matcher.find()) {
                            queueRecast();
                        }
                    }
                }
            }
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

            long humanDelay = 50 + (long)(Math.random() * 101); 

            modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN, humanDelay, this::useRod);
            
        }
    }

    public void queueRecast() {
        modAutofish.getScheduler().scheduleAction(ActionType.RECAST, getRandomDelay()
                + modAutofish.getConfig().getRecastDelay(), () -> {
            //State checks to ensure we can still fish once this runs
            if(hookExists) return;
            if(!isHoldingFishingRod()) return;
            if(shouldPreventBreak()) return;

            useRod();
        });
    }

    private void queueRodSwitch(){
        modAutofish.getScheduler().scheduleAction(ActionType.ROD_SWITCH, (long) (getRandomDelay() * 0.83)
                + modAutofish.getConfig().getRecastDelay(), () -> {
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
                isWaterBlock(bobber, blockPos))
                || BlockPos.betweenClosedStream(x - 2, y, z - 2, x + 2, y, z + 2).allMatch(blockPos ->
                isAirOrLilyPad(bobber, blockPos));
    }

    private boolean isWaterBlock(FishingHook bobber, BlockPos blockPos) {
        return bobber.level().getBlockState(blockPos).getBlock() == Blocks.WATER;
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
            fishMonitorMP.handleHookRemoved();
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

    public boolean isBobberInWater(){
        if(client.player != null && client.level != null && client.player.fishing != null) {
            Block block = client.level.getBlockState(client.player.fishing.blockPosition()).getBlock();
            return block == Blocks.WATER || block == Blocks.BUBBLE_COLUMN;
        } else{
            return false;
        }
    }

    public void useRod() {
        if(client.player != null && client.level != null) {
            InteractionHand hand = getCorrectHand();
            if (modAutofish.getConfig().isEnableArmSwing()) {
                client.player.swing(hand);
            }
            InteractionResult actionResult = null;
            if (client.gameMode != null) {
                actionResult = client.gameMode.useItem(client.player, hand);
            }
            if (actionResult != null && actionResult.consumesAction()) {
                client.gameRenderer.itemInHandRenderer.itemUsed(hand);
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
        if (modAutofish.getConfig().isUseSoundDetection()) {
            fishMonitorMP = new FishMonitorMPSound();
        } else {
            fishMonitorMP = new FishMonitorMPMotion();
        }
    }

    private boolean shouldUseMPDetection(){
        if(modAutofish.getConfig().isForceMPDetection()) return true;
        return !client.isLocalServer();
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
