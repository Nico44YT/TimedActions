package nico.timed_actions.mixin.v1.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import nico.liby.api.nbt.LibyNbtCompound;
import nico.timed_actions.api.v1.*;
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;
import nico.timed_actions.internal.v1.networking.SyncActionS2C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Entity.class)
@SuppressWarnings("all")
public abstract class EntityInjectMixin implements TimedActionHolder {

    @Unique
    private EntityTimedAction playingAction;

    @Override
    public void startTimedAction(Identifier identifier) {
        Entity entity = (Entity)(Object)this;
        EntityTimedAction newAction = (EntityTimedAction) InternalTimedActionRegistry.createAction(identifier, entity);

        if(newAction != null) {
            if(this.playingAction != null && this.playingAction.matches(newAction) && this.playingAction.getPlayState() != TimedActionPlayState.RESTARTING) {
                this.restartTimedAction(newAction);
                return;
            }

            if(this.playingAction != null) this.playingAction.onStop(entity.getWorld(), entity);

            this.playingAction = newAction;
            this.playingAction.start(entity.getWorld(), entity);
        }
    }

    @Override
    public void pauseTimedAction() {
        Entity entity = (Entity)(Object)this;

        getTimedAction().ifPresent(action -> action.pause(entity.getWorld(), entity));
    }

    @Override
    public void resumeTimedAction() {
        Entity entity = (Entity)(Object)this;

        getTimedAction().ifPresent(action -> action.resume(entity.getWorld(), entity));
    }

    @Override
    public void stopTimedAction() {
        Entity entity = (Entity)(Object)this;

        getTimedAction().ifPresent(action -> action.stop(entity.getWorld(), entity));
        this.playingAction = null;
    }

    @Override
    public void restartTimedAction(TimedAction newAction) {
        Entity entity = (Entity)(Object)this;

        getTimedAction().ifPresent(action -> action.restart(entity.getWorld(), entity, newAction));

        this.playingAction.setPlayState(TimedActionPlayState.RESTARTING);

        this.startTimedAction(newAction.getActionIdentifier());
    }

    @Override
    public void removeTimedAction() {
        this.playingAction = null;
    }

    @Override
    public Optional<EntityTimedAction> getTimedAction() {
        return (Optional<EntityTimedAction>) Optional.ofNullable(this.playingAction);
    }

    @Override
    public TimedActionPlayState getTimedActionState() {
        return this.getTimedAction()
                .map(TimedAction::getPlayState)
                .orElse(TimedActionPlayState.NO_ACTION);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void timedActions$tick(CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        if(this.playingAction != null) this.playingAction.tick(entity.getWorld(), entity);
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    public void timedActions$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if(this.playingAction != null) {
            LibyNbtCompound nbtCompound = new LibyNbtCompound();
            this.playingAction.writeNbt(nbtCompound);
            nbt.put("timed_action", nbtCompound);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void timedActions$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if(nbt.contains("timed_action")) {
            Entity entity = (Entity)(Object)this;
            LibyNbtCompound nbtCompound = new LibyNbtCompound(nbt.getCompound("timed_action"));

            Identifier identifier = nbtCompound.getIdentifier("identifier");

            this.playingAction = (EntityTimedAction) InternalTimedActionRegistry.createAction(identifier, entity);
            if(this.playingAction != null) this.playingAction.readNbt(nbtCompound);
        }
    }

    @Override
    public void updateTimedAction(SyncActionS2C packet) {
        if(this.playingAction != null) this.playingAction.readNbt(packet.getData());
        else {
            NbtCompound idNbt = packet.getData().getCompound("identifier");
            Identifier id = Identifier.of(idNbt.getString("namespace"), idNbt.getString("path"));
            this.playingAction = (EntityTimedAction) InternalTimedActionRegistry.createAction(id, this);
        }
    }
}
