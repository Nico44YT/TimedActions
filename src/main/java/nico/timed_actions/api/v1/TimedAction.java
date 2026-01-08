package nico.timed_actions.api.v1;

import nazario.liby.api.nbt.LibyNbtCompound;
import nazario.liby.api.nbt.NbtStorable;
import nazario.liby.api.util.LibyIdentifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

public abstract class TimedAction<T extends TimedActionHolder> implements NbtStorable {
    protected Identifier actionIdentifier;
    protected Predicate<T> predicate;

    protected long ticksLeft;
    protected TimedActionPlayState playState;

    private final long MAX_DURATION;

    public TimedAction(Identifier actionIdentifier, Predicate<T> predicate) {
        this.actionIdentifier = actionIdentifier;
        this.predicate = predicate;
        this.playState = TimedActionPlayState.STOPPED;

        MAX_DURATION = getMaxDuration();
        this.ticksLeft = MAX_DURATION;
    }

    abstract public long getMaxDuration();
    abstract public void onStart(World world, T holder);
    abstract public void onPause(World world, T holder);
    abstract public void onResume(World world, T holder);
    abstract public void onStop(World world, T holder);
    abstract public void onRestart(World world, T holder, TimedAction<T> newTimedAction);
    abstract public void onTick(World world, T holder);

    public boolean shouldStop(World world, T holder) {
        return this.ticksLeft < 0;
    }

    abstract public void syncTimedAction(T holder);

    @ApiStatus.Internal
    public void tick(World world, T animatable) {
        syncTimedAction(animatable);

        if(this.playState == TimedActionPlayState.PLAYING) {
            this.ticksLeft--;

            this.onTick(world, animatable);

            if(shouldStop(world, animatable)) this.stop(world, animatable);
        }
    }

    @ApiStatus.Internal
    public void start(World world, T animatable) {
        this.playState = TimedActionPlayState.PLAYING;

        syncTimedAction(animatable);

        this.onStart(world, animatable);
    }

    @ApiStatus.Internal
    public void pause(World world, T animatable) {
        this.playState = TimedActionPlayState.PAUSED;

        syncTimedAction(animatable);

        this.onPause(world, animatable);
    }

    @ApiStatus.Internal
    public void resume(World world, T animatable) {
        this.playState = TimedActionPlayState.PLAYING;

        syncTimedAction(animatable);

        this.onResume(world, animatable);
    }

    @ApiStatus.Internal
    public void stop(World world, T animatable) {
        this.playState = TimedActionPlayState.STOPPED;

        syncTimedAction(animatable);

        this.onStop(world, animatable);
    }

    @ApiStatus.Internal
    public void restart(World world, T animatable, TimedAction<T> newTimedAction) {
        this.playState = TimedActionPlayState.RESTARTING;

        syncTimedAction(animatable);

        this.onRestart(world, animatable, newTimedAction);
    }

    public void readCustomData(NbtCompound nbtCompound) {

    }

    public void writeCustomData(NbtCompound nbtCompound) {

    }

    @ApiStatus.Internal
    public void readNbt(NbtCompound tag) {
        LibyNbtCompound libyNbtCompound = new LibyNbtCompound(tag);
        readNbt(libyNbtCompound);
        tag.copyFrom(libyNbtCompound);
    }

    private void readNbt(LibyNbtCompound tag) {
        this.actionIdentifier = new LibyIdentifier(tag.getIdentifier("identifier"));
        this.ticksLeft = tag.getLong("ticks_left");
        this.playState = tag.getEnum("play_state", TimedActionPlayState.class);

        this.readCustomData(tag.getCompound("custom_data"));
    }

    @ApiStatus.Internal
    public void writeNbt(NbtCompound tag) {
        LibyNbtCompound libyNbtCompound = new LibyNbtCompound(tag);
        writeNbt(libyNbtCompound);
        tag.copyFrom(libyNbtCompound);
    }

    private void writeNbt(LibyNbtCompound tag) {
        tag.putIdentifier("identifier", this.actionIdentifier);
        tag.putLong("ticks_left", this.ticksLeft);
        tag.putEnum("play_state", this.playState);

        LibyNbtCompound customData = new LibyNbtCompound();
        this.writeCustomData(customData);
        tag.put("custom_data", customData);
    }

    public Identifier getActionIdentifier() {
        return this.actionIdentifier;
    }

    public boolean checkPredicate(T animatable) {
        return this.predicate.test(animatable);
    }

    public TimedActionPlayState getPlayState() {
        return this.playState;
    }

    public long getTicksLeft() {
        return this.ticksLeft;
    }

    public long getTicksPassed() {
        return MAX_DURATION - this.ticksLeft;
    }

    public <U extends TimedActionHolder> boolean matches(TimedAction<U> otherAnimation) {
        return this.getActionIdentifier().equals(otherAnimation.getActionIdentifier());
    }

    @ApiStatus.Internal
    public void setPlayState(TimedActionPlayState playState) {
        this.playState = playState;
    }

    @FunctionalInterface
    public interface Factory<T extends TimedAction<U>, U extends TimedActionHolder> {
        T apply(Identifier actionIdentifier, Predicate<U> predicate);
    }
}
