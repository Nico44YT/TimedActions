package nico.timed_actions.internal.v1;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import nico.liby.api.util.LibyIdentifier;
import nico.timed_actions.api.v1.BlockEntityTimedAction;
import nico.timed_actions.api.v1.EntityTimedAction;
import nico.timed_actions.api.v1.TimedAction;
import nico.timed_actions.api.v1.TimedActionHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class InternalTimedActionRegistry {
    static Map<Identifier, TimedActionEntry<? extends TimedAction<? extends TimedActionHolder>, ? extends TimedActionHolder>> timedActionEntries = new ConcurrentHashMap<>();

    public static <T extends EntityTimedAction<U>, U extends Entity> Identifier registerEntityAction(LibyIdentifier identifier, EntityTimedAction.EntityFactory<T, U> factory, Predicate<U> predicate) {
        if (timedActionEntries.containsKey(identifier)) {
            throw new RuntimeException(String.format("[TimedActions] Action with id %s is already registered, if you want to overwrite an action use .setAction", identifier));
        }
        timedActionEntries.put(identifier, new TimedActionEntry<>(factory, predicate));
        return identifier;
    }

    public static <T extends BlockEntityTimedAction<U>, U extends BlockEntity> Identifier registerBlockEntityAction(LibyIdentifier identifier, BlockEntityTimedAction.BlockEntityFactory<T, U> factory, Predicate<U> predicate) {
        if (timedActionEntries.containsKey(identifier)) {
            throw new RuntimeException(String.format("[TimedActions] Action with id %s is already registered, if you want to overwrite an action use .setAction", identifier));
        }
        timedActionEntries.put(identifier, new TimedActionEntry<>(factory, predicate));
        return identifier;
    }

    public static <T extends TimedAction<U>, U extends TimedActionHolder> Identifier registerAction(LibyIdentifier identifier, TimedAction.Factory<T, U> factory, Predicate<U> predicate) {
        if (timedActionEntries.containsKey(identifier)) {
            throw new RuntimeException(String.format("[TimedActions] Action with id %s is already registered, if you want to overwrite an action use .setAction", identifier));
        }
        timedActionEntries.put(identifier, new TimedActionEntry<>(factory, predicate));
        return identifier;
    }

    public static <T extends TimedAction<U>, U extends TimedActionHolder> Identifier setAction(LibyIdentifier identifier, TimedAction.Factory<T, U> factory, Predicate<U> predicate) {
        timedActionEntries.put(identifier, new TimedActionEntry<>(factory, predicate));

        return identifier;
    }


    public static <T extends TimedAction<U>, U extends TimedActionHolder> T createAction(Identifier identifier, U holder) {
        TimedActionEntry<T, U> entry = (TimedActionEntry<T, U>) timedActionEntries.get(identifier);
        if (entry == null) return null;

        T animation = entry.factory().apply(new LibyIdentifier(identifier), entry.predicate());
        if (animation.checkPredicate(holder)) {
            return animation;
        }
        return null;
    }

    public static Map<Identifier, TimedActionEntry<?, ?>> getTimedActionEntries() {
        return timedActionEntries;
    }

    public static <T extends TimedAction<U>, U extends TimedActionHolder> Optional<TimedActionEntry<T, U>> getAction(Identifier identifier) {
        return Optional.of((TimedActionEntry<T, U>) timedActionEntries.getOrDefault(identifier, null));
    }

    public static <T extends TimedAction<U>, U extends TimedActionHolder> Optional<Supplier<T>> getActionSupplier(Identifier identifier) {
        return Optional.of((Supplier<T>) timedActionEntries.getOrDefault(identifier, null).supplier(new LibyIdentifier(identifier)));
    }

    public static Map<Identifier, TimedActionEntry<?, ?>> getAllFromNamespace(String namespace) {
        Map<Identifier, TimedActionEntry<?, ?>> filteredActions = new HashMap<>();

        timedActionEntries.forEach((id, entry) -> {
            if(id.getNamespace().equals(namespace)) filteredActions.put(id, entry);
        });

        return filteredActions;
    }


    public record TimedActionEntry<T extends TimedAction<U>, U extends TimedActionHolder>(
            TimedAction.Factory<T, U> factory,
            Predicate<U> predicate
    ) {
        Supplier<T> supplier(Identifier id) {
            return () -> factory.apply(id, predicate);
        }
    }
}
