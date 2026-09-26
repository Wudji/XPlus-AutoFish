package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.entity.projectile.FishingHook;
import troy.autofish.Autofish;

/**
 * Multiplayer bite detection based on entity data packets.
 * <p>
 * Watches {@link ClientboundSetEntityDataPacket} updates for the player's own
 * fishing bobber. A bite is detected when the bobber's "biting" metadata flag
 * (index 9) becomes true, or when a hooked entity id (index 8) is set to a
 * non-zero value.
 * <p>
 * See https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Fishing_Bobber
 */
public class FishMonitorMPData implements FishMonitorMP {

    // Entity metadata index of the hooked entity id on a fishing bobber. 0 means nothing is hooked.
    public static final int HOOKED_ENTITY_DATA_INDEX = 8;

    // Entity metadata index of the biting flag on a fishing bobber.
    public static final int BITING_DATA_INDEX = 9;

    @Override
    public void hookTick(Autofish autofish, Minecraft minecraft, FishingHook hook) {
        // No per-tick state needed: detection is purely packet driven.
    }

    @Override
    public void handleHookRemoved() {
        // No state to reset.
    }

    @Override
    public void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft) {
        if (!(packet instanceof ClientboundSetEntityDataPacket dataPacket)) return;
        if (minecraft.player == null || minecraft.player.fishing == null) return;
        if (dataPacket.id() != minecraft.player.fishing.getId()) return;

        for (DataValue<?> dataValue : dataPacket.packedItems()) {
            Object value = dataValue.value();
            boolean biting = dataValue.id() == BITING_DATA_INDEX && Boolean.TRUE.equals(value);
            boolean hookedEntity = dataValue.id() == HOOKED_ENTITY_DATA_INDEX
                    && value instanceof Integer hookedId && hookedId != 0;

            if (biting || hookedEntity) {
                // Catch the fish
                autofish.catchFish();

                // Reset the class attributes to default.
                this.handleHookRemoved();
                return;
            }
        }
    }
}
