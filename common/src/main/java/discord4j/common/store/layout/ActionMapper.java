package discord4j.common.store.layout;

import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActionMapper {

    private final Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Publisher<?>>> mappings;

    private ActionMapper(Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Publisher<?>>> mappings) {
        this.mappings = mappings;
    }

    public static ActionMapper create() {
        return new ActionMapper(new HashMap<>());
    }

    public static ActionMapper aggregate(ActionMapper... mappers) {
        Objects.requireNonNull(mappers);
        return new ActionMapper(Arrays.stream(mappers)
                .flatMap(mapper -> mapper.mappings.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))); // throws ISE if duplicates
    }

    /**
     * Maps a specific action type to a handler function to execute.
     *
     * @param actionType the type of the action
     * @param handler    the handler to execute when an action of the specified type is received
     * @param <R>        the return type of the action
     * @param <S>        the type of the action itself
     * @return this {@link ActionMapper} enriched with the added mapping
     */
    @SuppressWarnings("unchecked")
    public <R, S extends StoreAction<R>> ActionMapper map(Class<S> actionType,
                                                          Function<? super S, ? extends Publisher<R>> handler) {
        Objects.requireNonNull(actionType);
        Objects.requireNonNull(handler);
        mappings.put(actionType, action -> handler.apply((S) action));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <R> Optional<Function<StoreAction<R>, ? extends Publisher<R>>> findHandlerForAction(StoreAction<R> action) {
        Objects.requireNonNull(action);
        return Optional.ofNullable(mappings.get(action.getClass()))
                .map(handler -> a -> (Publisher<R>) handler.apply(a));
    }
}
