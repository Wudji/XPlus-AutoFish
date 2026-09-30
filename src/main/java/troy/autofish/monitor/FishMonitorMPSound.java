package troy.autofish.monitor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import troy.autofish.Autofish;
import troy.autofish.config.Config;

public class FishMonitorMPSound implements FishMonitorMP {

    @Override
    public void hookTick(Autofish autofish, MinecraftClient minecraft, FishingBobberEntity hook) {
    }

    @Override
    public void handleHookRemoved() {
    }

    @Override
    public void handlePacket(Autofish autofish, Packet<?> packet, MinecraftClient minecraft) {

        SoundEvent soundEvent;
        double x, y, z;

        if (packet instanceof PlaySoundS2CPacket soundPacket) {
            soundEvent = soundPacket.getSound().value();
            x = soundPacket.getX();
            y = soundPacket.getY();
            z = soundPacket.getZ();
        } else if (packet instanceof PlaySoundFromEntityS2CPacket soundPacket) {
            if (minecraft.world == null) {
                return;
            }
            Entity entity = minecraft.world.getEntityById(soundPacket.getEntityId());
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

        autofish.handleSound(soundEvent.getId(), x, y, z, Config.SoundDetectionSource.SERVER_PACKET);

    }
}
