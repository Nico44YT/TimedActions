package nico.timed_actions.api.v1.registry;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import nico.liby.api.util.LibyIdentifier;
import nico.timed_actions.api.v1.EntityTimedAction;
import nico.timed_actions.api.v1.TimedAction;
import nico.timed_actions.api.v1.TimedActionHolder;
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;

import java.util.Objects;
import java.util.function.Predicate;

public class TimedActionRegistry {
    private final String namespace;

    protected TimedActionRegistry(String namespace) {
        this.namespace = namespace;
    }

    public static TimedActionRegistry of(String namespace) {
        return new TimedActionRegistry(namespace);
    }

    public <T extends EntityTimedAction<U>, U extends Entity> Identifier registerEntityAction(String name, EntityTimedAction.EntityFactory<T, U> actionFactory, Predicate<U> predicate) {
        LibyIdentifier identifier = LibyIdentifier.of(this.namespace, name);
        if (InternalTimedActionRegistry.getTimedActionEntries().containsKey(identifier)) {
            throw new RuntimeException(
                    String.format("[TimedActions] Action with id \"%s\" has already been registered", identifier));
        }

        InternalTimedActionRegistry.registerEntityAction(identifier, actionFactory, predicate);
        return identifier;
    }


    public <T extends TimedAction<U>, U extends TimedActionHolder> Identifier registerAction(String name, TimedAction.Factory<T, U> actionFactory, Predicate<U> predicate) {
        LibyIdentifier identifier = LibyIdentifier.of(this.namespace, name);
        if (InternalTimedActionRegistry.getTimedActionEntries().containsKey(identifier)) {
            throw new RuntimeException(
                    String.format("[TimedActions] Action with id \"%s\" has already been registered", identifier));
        }

        InternalTimedActionRegistry.registerAction(identifier, actionFactory, predicate);
        return identifier;
    }

    public <T extends TimedAction<U>, U extends TimedActionHolder> Identifier registerAction(String name, TimedAction.Factory<T, U> actionFactory, Class<U> predicateClass) {
        return registerAction(name, actionFactory, predicateClass::isInstance);
    }

    public <T extends TimedAction<U>, U extends TimedActionHolder> Identifier registerAction(String name, TimedAction.Factory<T, U> actionFactory) {
        return registerAction(name, actionFactory, Objects::nonNull);
    }

    public <T extends TimedAction<U>, U extends TimedActionHolder> Identifier overwriteAction(String name, TimedAction.Factory<T, U> actionFactory, Predicate<U> predicate) {
        LibyIdentifier identifier = LibyIdentifier.of(this.namespace, name);
        InternalTimedActionRegistry.setAction(identifier, actionFactory, predicate);
        return identifier;
    }

    public <T extends TimedAction<U>, U extends TimedActionHolder> Identifier overwriteAction(String name, TimedAction.Factory<T, U> actionFactory, Class<U> predicateClass) {
        return overwriteAction(name, actionFactory, predicateClass::isInstance);
    }

    public <T extends TimedAction<U>, U extends TimedActionHolder> Identifier overwriteAction(String name, TimedAction.Factory<T, U> actionFactory) {
        return overwriteAction(name, actionFactory, Objects::nonNull);
    }
}
