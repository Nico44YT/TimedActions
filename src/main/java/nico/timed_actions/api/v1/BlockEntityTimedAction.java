package nico.timed_actions.api.v1;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import nico.timed_actions.internal.v1.networking.SyncActionS2C;

import java.util.function.Predicate;

public abstract class BlockEntityTimedAction<T extends BlockEntity> extends TimedAction<T> {
    public BlockEntityTimedAction(Identifier actionIdentifier, Predicate<T> predicate) {
        super(actionIdentifier, predicate);
    }

    @Environment(EnvType.CLIENT)
    public BlockEntityTimedAction.RenderType shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return BlockEntityTimedAction.RenderType.DEFAULT_RENDER;
    }

    @Environment(EnvType.CLIENT)
    abstract public void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);


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
    public interface BlockEntityFactory<T extends BlockEntityTimedAction<U>, U extends BlockEntity & TimedActionHolder> extends TimedAction.Factory<T, U> {
        T apply(Identifier actionIdentifier, Predicate<U> predicate);
    }
}
