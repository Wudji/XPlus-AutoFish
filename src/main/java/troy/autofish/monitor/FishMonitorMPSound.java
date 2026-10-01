package troy.autofish.monitor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import troy.autofish.Autofish;
import troy.autofish.config.Config;

public class FishMonitorMPSound implements FishMonitorMP {

    @Override
    public void hookTick(Autofish autofish, Minecraft minecraft, FishingHook hook) {
    }

    @Override
    public void handleHookRemoved() {
    }

    @Override
    public void handlePacket(Autofish autofish, Packet<?> packet, Minecraft minecraft) {

        SoundEvent soundEvent;
        double x, y, z;

        if (packet instanceof ClientboundSoundPacket soundPacket) {
            soundEvent = soundPacket.getSound().value();
            x = soundPacket.getX();
            y = soundPacket.getY();
            z = soundPacket.getZ();
        } else if (packet instanceof ClientboundSoundEntityPacket soundPacket) {
            if (minecraft.level == null) {
                return;
            }
            Entity entity = minecraft.level.getEntity(soundPacket.getId());
            if (entity == null) {
                return;
            }
            soundEvent = soundPacket.getSound().value();
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        } else {
            return;
        }

        autofish.handleSound(soundEvent.location(), x, y, z, Config.SoundDetectionSource.SERVER_PACKET);

    }
}
