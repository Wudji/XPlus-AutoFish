package com.wudji.xplusautofish;


import com.wudji.xplusautofish.mointor.FishMonitorMP;
import com.wudji.xplusautofish.mointor.FishMonitorMPMotion;
import com.wudji.xplusautofish.mointor.FishMonitorMPSound;
import com.wudji.xplusautofish.scheduler.ActionType;
import net.minecraft.client.Minecraft;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPlusAutofish {
    private Minecraft client;
    private ForgeModXPlusAutofish modAutofish;
    private FishMonitorMP fishMonitorMP;

    private boolean hookExists = false;
    private long hookRemovedAt = 0L;
    private boolean alreadyAlertOP = false;
    private boolean alreadyPassOP = false;

    public long timeMillis = 0L;

    public XPlusAutofish(ForgeModXPlusAutofish modAutofish) {
        this.modAutofish = modAutofish;
        this.client = Minecraft.getInstance();
        setDetection();

        //Initiate the repeating action for persistent mode casting
        modAutofish.getScheduler().scheduleRepeatingAction(10000, () -> {
            if(!modAutofish.getConfig().isPersistentMode()) return;
            if(modAutofish.getConfig().isNoBreak() && Objects.requireNonNull(getHeldItem()).getDamageValue() >= 63) return;
            if(!isHoldingFishingRod()) return;
            if(hookExists) {
                 if (isBobberInWater()) return;
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
        client.execute(() ->{
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
            detectOpenWater();

            //queue actions
            queueRodSwitch();
            queueRecast();

            //reel in
            modAutofish.getScheduler().scheduleAction(ActionType.REEL_IN,
                    modAutofish.getConfig().getReelInDelay(),
                    this::useRod);
        }
    }

    public void queueRecast() {
        modAutofish.getScheduler().scheduleAction(ActionType.RECAST,
                getRandomDelay() + modAutofish.getConfig().getReelInDelay(), () -> {
            //State checks to ensure we can still fish once this runs
            if(hookExists) return;
            if(!isHoldingFishingRod()) return;
            if(modAutofish.getConfig().isNoBreak() && Objects.requireNonNull(getHeldItem()).getDamageValue() >= 63) return;

            useRod();
        });
    }

    private void queueRodSwitch(){
        modAutofish.getScheduler().scheduleAction(ActionType.ROD_SWITCH,
                (long) (getRandomDelay() * 0.83) + modAutofish.getConfig().getReelInDelay(), () -> {
            if(!modAutofish.getConfig().isMultiRod()) return;

            switchToFirstRod(client.player);
        });
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
            for (int i = 0; i < inventory.items.size(); i++) {
                ItemStack slot = inventory.items.get(i);
                if (slot.getItem() == Items.FISHING_ROD) {
                    if (i < 9) { //hotbar only
                        if (modAutofish.getConfig().isNoBreak()) {
                            if (slot.getDamageValue() < 63) {
                                inventory.selected = i;
                                return;
                            }
                        } else {
                            inventory.selected = i;
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if the hook is on the ground
     */
    public boolean isBobberInWater(){
        if(client.player != null && client.level != null && client.player.fishing != null) {
            return client.level.getBlockState(client.player.fishing.blockPosition()).getBlock() == Blocks.WATER;

        } else{
            return false;
        }
    }

    public void useRod() {
        if(client.player != null && client.level != null) {
            InteractionHand hand = getCorrectHand();
            InteractionResult actionResult = null;
            if (client.gameMode != null) {
                actionResult = client.gameMode.useItem(client.player, hand);
            }
            if (actionResult != null && actionResult.consumesAction()) {
                if (actionResult.shouldSwing()) {
                    client.player.swing(hand);
                }
                client.gameRenderer.itemInHandRenderer.itemUsed(hand);
            }
        }
    }

    public boolean isHoldingFishingRod() {
        return isItemFishingRod(Objects.requireNonNull(getHeldItem()).getItem());
    }

    private InteractionHand getCorrectHand() {
        if (!modAutofish.getConfig().isMultiRod()) {
            if (client.player != null && isItemFishingRod(client.player.getOffhandItem().getItem()))
                return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    private void detectOpenWater(){
        /*
         * To catch items in the treasure category, the bobber must be in open water,
         * defined as the 5×4×5 vicinity around the bobber resting on the water surface
         * (2 blocks away horizontally, 2 blocks above the water surface, and 2 blocks deep).
         * Each horizontal layer in this area must consist only of air and lily pads or water source blocks,
         * waterlogged blocks without collision (such as signs, kelp, or coral fans), and bubble columns.
         * (from Minecraft wiki)
         * */
        if (client.player == null || client.player.fishing == null) return;
        if (!modAutofish.getConfig().isOpenWaterDetectEnabled()) return;

        FishingHook bobber = client.player.fishing;

        int x = bobber.getBlockX();
        int y = bobber.getBlockY();
        int z = bobber.getBlockZ();
        boolean flag = true;
        for(int yi = -2; yi <= 2; yi++){
            if(!(BlockPos.betweenClosedStream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
                    // every block is water
                    bobber.getCommandSenderWorld().getBlockState(blockPos).getBlock() == Blocks.WATER
            )) || BlockPos.betweenClosedStream(x - 2, y + yi, z - 2, x + 2, y + yi, z + 2).allMatch((blockPos ->
                    // or every block is air or lily pad
                    bobber.getCommandSenderWorld().getBlockState(blockPos).getBlock() == Blocks.AIR
                            || bobber.getCommandSenderWorld().getBlockState(blockPos).getBlock() == Blocks.LILY_PAD
            )))){
                // didn't pass the check
                if(!alreadyAlertOP){
                    Objects.requireNonNull(bobber.getPlayerOwner()).displayClientMessage(Component.translatable("info.autofish.open_water_detection.fail"),true);
                    alreadyAlertOP = true;
                    alreadyPassOP = false;
                }
                flag = false;
            }
        }
        if(flag && !alreadyPassOP) {
            Objects.requireNonNull(bobber.getPlayerOwner()).displayClientMessage(Component.translatable("info.autofish.open_water_detection.success"),true);
            alreadyPassOP = true;
            alreadyAlertOP = false;
        }


    }

    private ItemStack getHeldItem() {
        if (!modAutofish.getConfig().isMultiRod()) {
            if (client.player != null && isItemFishingRod(client.player.getOffhandItem().getItem()))
                return client.player.getOffhandItem();
        }
        if (client.player != null) {
            return client.player.getMainHandItem();
        }
        return null;
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
        return Math.random() >=0.5 ?
                (long) (modAutofish.getConfig().getRecastDelay() * (1 - (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01))) :
                (long) (modAutofish.getConfig().getRecastDelay() * (1 + (Math.random() * modAutofish.getConfig().getRandomDelay() * 0.01)));

    }
    
}
