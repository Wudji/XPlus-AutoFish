package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import troy.autofish.Autofish;


public class FishMonitorMPMotion implements FishMonitorMP {

    // The threshold of detecting a bobber moving downwards, to detect as a fish.
    public static final double PACKET_MOTION_Y_THRESHOLD = -0.1;

    // Start catching fish after a 1 second threshold of hitting the configured fluid.
    public static final int START_CATCHING_AFTER_THRESHOLD = 1000;

    // True if the bobber is in the configured fishing fluid.
    private boolean hasHitFishingFluid = false;
    
    // Time at which bobber begins to rise in the water.
    // 0 if the bobber has not rose in the water yet.
    private long bobberRiseTimestamp = 0;


    @Override
    public void hookTick(Autofish autofish, Minecraft minecraft, FishingHook hook) {
        if (worldContainsFishingFluid(autofish, hook.level(), hook.getBoundingBox())) {
            hasHitFishingFluid = true;

        }
    }

    @Override
    public void handleHookRemoved() {
        hasHitFishingFluid = false;
        bobberRiseTimestamp = 0;
    }

    @Override
    public void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft) {
        if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
            if (minecraft.player != null && minecraft.player.fishing != null && minecraft.player.fishing.getId() == velocityPacket.id()) {
                // Wait until the bobber has rose in the water.
                // Prevent remarking the bobber rise timestamp until it is reset by catching.
                if (hasHitFishingFluid && bobberRiseTimestamp == 0 && velocityPacket.movement().y() > 0) {
                    // Mark the time in which the bobber began to rise.
                    bobberRiseTimestamp = autofish.timeMillis;
                }

                // Calculate the time in which the bobber has been in the configured fluid.
                long timeInFluid = autofish.timeMillis - bobberRiseTimestamp;

                // If the bobber has been in the fluid long enough, start detecting the bobber movement.
                if (hasHitFishingFluid && bobberRiseTimestamp != 0 && timeInFluid > START_CATCHING_AFTER_THRESHOLD) {
                    // minecraft.player.sendMessage(Text.of("Y: "+ velocityPacket.getVelocityY()),true);
                    if (velocityPacket.movement().x() == 0.0 && velocityPacket.movement().z() == 0.0 && velocityPacket.movement().y() < PACKET_MOTION_Y_THRESHOLD) {
                        // Catch the fish
                        autofish.catchFish();

                        // Reset the class attributes to default.
                        this.handleHookRemoved();
                    }
                }
            }
        }
    }

    public static boolean worldContainsFishingFluid(Autofish autofish, Level world, AABB box) {
        int i = Mth.floor(box.minX);
        int j = Mth.ceil(box.maxX);
        int k = Mth.floor(box.minY);
        int l = Mth.ceil(box.maxY);
        int m = Mth.floor(box.minZ);
        int n = Mth.ceil(box.maxZ);
        return BlockPos.betweenClosedStream(i, k, m, j - 1, l - 1, n - 1)
                .anyMatch(blockPos -> autofish.isFishingFluid(world.getFluidState(blockPos)));
    }
}
