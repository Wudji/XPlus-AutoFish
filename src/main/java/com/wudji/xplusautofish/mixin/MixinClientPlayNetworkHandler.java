package com.wudji.xplusautofish.mixin;

import com.wudji.xplusautofish.NeoForgedModXPlusAutofish;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientGamePacketListener {
    protected MixinClientPlayNetworkHandler(Minecraft minecraft, Connection connection, CommonListenerCookie connectionState) {
        super(minecraft, connection, connectionState);
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    public void onPlaySound(ClientboundSoundPacket playSoundS2CPacket_1, CallbackInfo ci) {
        if (minecraft.isSameThread()) NeoForgedModXPlusAutofish.getInstance().handlePacket(playSoundS2CPacket_1);
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("HEAD"))
    public void onEntitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        if (minecraft.isSameThread()) NeoForgedModXPlusAutofish.getInstance().handlePacket(packet);
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
    public void onVelocityUpdate(ClientboundSetEntityMotionPacket entityVelocityUpdateS2CPacket_1, CallbackInfo ci) {
        if (minecraft.isSameThread()) NeoForgedModXPlusAutofish.getInstance().handlePacket(entityVelocityUpdateS2CPacket_1);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    public void onSysChatMessage(ClientboundSystemChatPacket p_233708_, CallbackInfo ci) {
        if (minecraft.isSameThread()) NeoForgedModXPlusAutofish.getInstance().handleChat(p_233708_);
    }
}
