package troy.autofish.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import troy.autofish.AutofishMod;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientGamePacketListener {
    protected MixinClientPacketListener(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }


    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    public void onPlaySound(ClientboundSoundPacket playSoundS2CPacket_1, CallbackInfo ci) {
        if (minecraft.isSameThread()) AutofishMod.getInstance().handlePacket(playSoundS2CPacket_1);
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
    public void onVelocityUpdate(ClientboundSetEntityMotionPacket entityVelocityUpdateS2CPacket_1, CallbackInfo ci) {
        if (minecraft.isSameThread()) AutofishMod.getInstance().handlePacket(entityVelocityUpdateS2CPacket_1);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    public void onChatMessage(ClientboundSystemChatPacket chatMessageS2CPacket_1, CallbackInfo ci) {
        if (minecraft.isSameThread()) AutofishMod.getInstance().handleChat(chatMessageS2CPacket_1);
    }

}
