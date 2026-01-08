package nico.timed_actions.mixin.v1.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import nico.timed_actions.api.v1.EntityTimedAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = EntityRenderer.class, priority = 500)
public abstract class EntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public <T extends Entity> void timedActions$render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        entity.getTimedAction().ifPresent(action -> {
            ((EntityTimedAction)action).render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        });
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public <T extends Entity> void timedActions$shouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        entity.getTimedAction().ifPresent(action -> {
            switch(((EntityTimedAction)action).shouldRender(entity, frustum, x, y, z)) {
                case ALWAYS_RENDER -> cir.setReturnValue(true);
                case DEFAULT_RENDER -> {}
                case NEVER_RENDER -> cir.setReturnValue(false);
            }
        });
    }
}
