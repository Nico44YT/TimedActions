package nico.timed_actions.internal.test;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import nico.timed_actions.api.v1.BlockEntityTimedAction;
import nico.timed_actions.api.v1.TimedAction;

import java.util.function.Predicate;

public class TestBlockEntityAction extends BlockEntityTimedAction<BlockEntity> {
    public TestBlockEntityAction(Identifier actionIdentifier, Predicate<BlockEntity> predicate) {
        super(actionIdentifier, predicate);
    }

    @Override
    public void render(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        System.out.println("Render");
    }

    @Override
    public long getMaxDuration() {
        return 20 * 10;
    }

    @Override
    public void onStart(World world, BlockEntity blockEntity) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Started");
    }

    @Override
    public void onPause(World world, BlockEntity blockEntity) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Paused");
    }

    @Override
    public void onResume(World world, BlockEntity blockEntity) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Resumed");
    }

    @Override
    public void onStop(World world, BlockEntity blockEntity) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Stopped");
    }

    @Override
    public void onRestart(World world, BlockEntity blockEntity, TimedAction<BlockEntity> timedAction) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Restarted");
    }

    @Override
    public void onTick(World world, BlockEntity blockEntity) {
        if (world.isClient()) System.out.print("Client: ");
        else System.out.print("Server: ");
        System.out.println("Ticked");

        syncTimedAction(blockEntity);
    }
}
