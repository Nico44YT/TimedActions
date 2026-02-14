package nico.timed_actions.api.v1;

import net.minecraft.util.Identifier;
import nico.timed_actions.internal.v1.networking.SyncActionS2C;

import java.util.Optional;

public interface TimedActionHolder {
    default void startTimedAction(Identifier id) {

    }

    default void pauseTimedAction() {

    }

    default void resumeTimedAction() {

    }

    default void stopTimedAction() {

    }

    default <T extends TimedActionHolder> void restartTimedAction(TimedAction<T> newAnimations) {

    }

    default void removeTimedAction() {

    }

    default <U extends TimedActionHolder, T extends TimedAction<U>> Optional<T> getTimedAction() {
        return Optional.empty();
    }

    default TimedActionPlayState getTimedActionState() {
        return TimedActionPlayState.STOPPED;
    }

    default boolean isPlayingTimedAction() {
        return getTimedActionState() == TimedActionPlayState.PLAYING;
    }

    default void updateTimedAction(SyncActionS2C packet) {

    }

    enum AnimatableType {
        BLOCK_ENTITY, ENTITY
    }
}
