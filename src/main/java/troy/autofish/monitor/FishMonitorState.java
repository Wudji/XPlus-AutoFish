package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.projectile.FishingHook;
import troy.autofish.Autofish;
import troy.autofish.mixin.FishingHookAccessor;

/** Reads the server-synchronized bite state of the player's own bobber on the client. */
public class FishMonitorState implements FishMonitorMP {

    @Override
    public void hookTick(Autofish autofish, Minecraft minecraft, FishingHook hook) {
        if (hook != null && minecraft.player != null && minecraft.player.fishing == hook
                && ((FishingHookAccessor) hook).autofish$isBiting()) {
            autofish.catchFish();
        }
    }

    @Override
    public void handleHookRemoved() {
    }

    @Override
    public void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft) {
        // Vanilla applies entity data packets to the bobber before the client tick reads its state.
    }
}
