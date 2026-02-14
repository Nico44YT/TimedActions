package nico.timed_actions.mixin.v1.blockentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import nico.kittylib.api.nbt.KittyLibNbtCompound;
import nico.timed_actions.api.v1.BlockEntityTimedAction;
import nico.timed_actions.api.v1.TimedAction;
import nico.timed_actions.api.v1.TimedActionHolder;
import nico.timed_actions.api.v1.TimedActionPlayState;
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;
import nico.timed_actions.internal.v1.networking.SyncActionS2C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BlockEntity.class)
@SuppressWarnings("all")
public abstract class BlockEntityInjectMixin implements TimedActionHolder {
    @Unique
    private BlockEntityTimedAction playingAction;

    @Override
    public void startTimedAction(Identifier identifier) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        BlockEntityTimedAction newAction = (BlockEntityTimedAction) InternalTimedActionRegistry.createAction(identifier, blockEntity);

        if (newAction != null) {
            if (this.playingAction != null && this.playingAction.matches(newAction) && this.playingAction.getPlayState() != TimedActionPlayState.RESTARTING) {
                this.restartTimedAction(newAction);
                return;
            }

            if (this.playingAction != null) this.playingAction.onStop(blockEntity.getWorld(), blockEntity);

            this.playingAction = newAction;
            this.playingAction.start(blockEntity.getWorld(), blockEntity);
        }
    }

    @Override
    public void pauseTimedAction() {
        BlockEntity blockEntity = (BlockEntity) (Object) this;

        getTimedAction().ifPresent(action -> action.pause(blockEntity.getWorld(), blockEntity));
    }

    @Override
    public void resumeTimedAction() {
        BlockEntity blockEntity = (BlockEntity) (Object) this;

        getTimedAction().ifPresent(action -> action.resume(blockEntity.getWorld(), blockEntity));
    }

    @Override
    public void stopTimedAction() {
        BlockEntity blockEntity = (BlockEntity) (Object) this;

        getTimedAction().ifPresent(action -> action.stop(blockEntity.getWorld(), blockEntity));
        this.playingAction = null;
    }

    @Override
    public void restartTimedAction(TimedAction newAction) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;

        getTimedAction().ifPresent(action -> action.restart(blockEntity.getWorld(), blockEntity, newAction));

        this.playingAction.setPlayState(TimedActionPlayState.RESTARTING);

        this.startTimedAction(newAction.getActionIdentifier());
    }

    @Override
    public void removeTimedAction() {
        this.playingAction = null;
    }

    @Override
    public Optional<BlockEntityTimedAction> getTimedAction() {
        return (Optional<BlockEntityTimedAction>) Optional.ofNullable(this.playingAction);
    }

    @Override
    public TimedActionPlayState getTimedActionState() {
        return this.getTimedAction()
                .map(TimedAction::getPlayState)
                .orElse(TimedActionPlayState.NO_ACTION);
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    public void timedActions$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.playingAction != null) {
            KittyLibNbtCompound nbtCompound = new KittyLibNbtCompound();
            this.playingAction.writeNbt(nbtCompound);
            nbt.put("timed_action", nbtCompound);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void timedActions$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("timed_action")) {
            BlockEntity entity = (BlockEntity) (Object) this;
            KittyLibNbtCompound nbtCompound = new KittyLibNbtCompound(nbt.getCompound("timed_action"));

            Identifier identifier = nbtCompound.getIdentifier("identifier");

            this.playingAction = (BlockEntityTimedAction) InternalTimedActionRegistry.createAction(identifier, entity);
            if (this.playingAction != null) this.playingAction.readNbt(nbtCompound);
        }
    }

    @Override
    public void updateTimedAction(SyncActionS2C packet) {
        if(this.playingAction != null) this.playingAction.readNbt(packet.getData());
        else {
            NbtCompound idNbt = packet.getData().getCompound("identifier");
            Identifier id = Identifier.of(idNbt.getString("namespace"), idNbt.getString("path"));
            this.playingAction = (BlockEntityTimedAction) InternalTimedActionRegistry.createAction(id, this);
        }
    }
}

