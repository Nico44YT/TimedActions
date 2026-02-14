package nico.timed_actions.internal.test;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import nico.timed_actions.api.v1.EntityTimedAction;
import nico.timed_actions.api.v1.TimedAction;

import java.util.function.Predicate;

public class TestEntityAction extends EntityTimedAction<PlayerEntity> {
    public TestEntityAction(Identifier actionIdentifier, Predicate<PlayerEntity> predicate) {
        super(actionIdentifier, predicate);
    }

    @Override
    public void render(PlayerEntity player, float v, float v1, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {

    }

    @Override
    public RenderType shouldRender(PlayerEntity entity, Frustum frustum, double x, double y, double z) {
        return RenderType.DEFAULT_RENDER;
    }

    @Override
    public long getMaxDuration() {
        return 20 * 10;
    }

    @Override
    public void onStart(World world, PlayerEntity player) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Started");
    }

    @Override
    public void onPause(World world, PlayerEntity player) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Paused");
    }

    @Override
    public void onResume(World world, PlayerEntity player) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Resumed");
    }

    @Override
    public void onStop(World world, PlayerEntity player) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Stopped");
    }

    @Override
    public void onRestart(World world, PlayerEntity player, TimedAction<PlayerEntity> timedAction) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Restarted");
    }

    @Override
    public void onTick(World world, PlayerEntity player) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Ticked");

        syncTimedAction(player);
    }
}
