package troy.autofish.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import troy.autofish.AutofishMod;

@Mixin(FishingHook.class)
public class MixinFishHookEntity {

    //field_7173;
    @Shadow private int nibble;

    //method_6949
    @Inject(method = "catchingFish(Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void tickFishingLogic(BlockPos pos, CallbackInfo ci) {
        AutofishMod.getInstance().tickFishingLogic(((FishingHook) (Object) this).getOwner(), nibble);
    }
}
