package nico.timed_actions.mixin.v1.blockentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.WrappedBlockEntityTickInvoker.class)
public abstract class WrappedBlockEntityTickInvokerMixin {

    @Shadow
    private BlockEntityTickInvoker wrapped;

    @Inject(method = "tick", at = @At("TAIL"))
    public void timedActions$tick(CallbackInfo ci) {
        if (wrapped instanceof WorldChunk.DirectBlockEntityTickInvoker<? extends BlockEntity> directTicker) {
            directTicker.blockEntity.getTimedAction().ifPresent(action -> {
                action.tick(directTicker.blockEntity.getWorld(), directTicker.blockEntity);
            });
        }
    }
}
