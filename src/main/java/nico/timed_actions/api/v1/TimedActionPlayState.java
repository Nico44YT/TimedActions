package nico.timed_actions.api.v1;

import net.minecraft.util.StringIdentifiable;

public enum TimedActionPlayState implements StringIdentifiable {
    PLAYING("PLAYING"),
    PAUSED("PAUSED"),
    STOPPED("STOPPED"),
    RESTARTING("RESTARTING"),
    NO_ACTION("NOTHING");

    private final String name;
    TimedActionPlayState(String name) {
        this.name = name.toUpperCase();
    }

    @Override
    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this.name;
    }
}
