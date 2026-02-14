package nico.timed_actions.api.v1;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import nico.timed_actions.internal.v1.networking.SyncActionS2C;

import java.util.function.Predicate;

public abstract class EntityTimedAction<T extends Entity> extends TimedAction<T> {

    public EntityTimedAction(Identifier actionIdentifier, Predicate<T> predicate) {
        super(actionIdentifier, predicate);
    }

    @Environment(EnvType.CLIENT)
    public RenderType shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return RenderType.DEFAULT_RENDER;
    }

    @Environment(EnvType.CLIENT)
    abstract public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    public <U extends LivingEntity> boolean canEntityTakeDamage(U entity, DamageSource source, float amount) {
        return true;
    }

    public <U extends LivingEntity> boolean canEntityMove(U entity, Vec3d movementInput, float slipperiness) {
        return true;
    }

    @Override
    public void syncTimedAction(T holder) {
        if (holder.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getPlayers().forEach(player -> {
                ServerPlayNetworking.send(player, SyncActionS2C.create(holder));
            });
        }
    }

    public enum RenderType {
        ALWAYS_RENDER,
        DEFAULT_RENDER,
        NEVER_RENDER;
    }

    @FunctionalInterface
    public interface EntityFactory<T extends EntityTimedAction<U>, U extends Entity & TimedActionHolder> extends TimedAction.Factory<T, U> {
        T apply(Identifier actionIdentifier, Predicate<U> predicate);
    }
}
