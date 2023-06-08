/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.common.store.api;

import org.reactivestreams.Publisher;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds mappings between concrete {@link StoreAction} types and the code to execute when they are invoked.
 */
public class ActionMapper {

    private static final ActionMapper EMPTY = new ActionMapper(Collections.emptyMap());

    private final Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Publisher<?>>> mappings;

    private ActionMapper(Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Publisher<?>>> mappings) {
        this.mappings = mappings;
    }

    /**
     * Initializes a new builder for an {@link ActionMapper}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns an {@link ActionMapper} containing no mappings.
     *
     * @return an empty {@link ActionMapper}
     */
    public static ActionMapper empty() {
        return EMPTY;
    }

    /**
     * Aggregates the mappings contained in the given {@link ActionMapper mappers} into a single {@link ActionMapper}
     * instance. All mappers must be defining distinct sets of actions, any conflicts will cause an
     * {@link IllegalStateException} to be thrown.
     *
     * @param mappers the mappers to aggregate
     * @return an aggregated {@link ActionMapper}
     * @throws IllegalStateException if two or more mappers define a mapping for the same action type
     */
    public static ActionMapper aggregate(ActionMapper... mappers) {
        Objects.requireNonNull(mappers);
        if (mappers.length == 0) return EMPTY;
        if (mappers.length == 1) return mappers[0];
        return new ActionMapper(Arrays.stream(mappers)
                .flatMap(mapper -> mapper.mappings.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))); // throws ISE if duplicates
    }

    /**
     * Aggregates a collection of {@link ActionMapper ActionMappers} into a single instance. Mappers with matching
     * actions will be merged by preserving the first declared one.
     *
     * @param mappers the mappers to aggregate
     * @return an aggregated {@link ActionMapper}, merging any overlap by preserving the first mapper by collection
     * order
     */
    public static ActionMapper mergeFirst(Collection<ActionMapper> mappers) {
        Objects.requireNonNull(mappers);
        return new ActionMapper(mappers.stream()
                .flatMap(mapper -> mapper.mappings.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a)));
    }

    /**
     * Retrieves the handler associated to the given action based on its concrete type.
     *
     * @param action the action to retrieve the handler for
     * @param <R> the return type of the action, to ensure type safety
     * @return the handler for the given action, or {@link Optional#empty()} if not found.
     */
    @SuppressWarnings("unchecked")
    public <R> Optional<Function<StoreAction<R>, ? extends Publisher<R>>> findHandlerForAction(StoreAction<R> action) {
        Objects.requireNonNull(action);
        return Optional.ofNullable(mappings.get(action.getClass()))
                .map(handler -> a -> (Publisher<R>) handler.apply(a));
    }

    public static class Builder {

        private final Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Publisher<?>>> mappings;

        private Builder() {
            this.mappings = new HashMap<>();
        }

        /**
         * Maps a specific action type to a handler function to execute.
         *
         * @param actionType the type of the action
         * @param handler    the handler to execute when an action of the specified type is received
         * @param <R>        the return type of the action
         * @param <S>        the type of the action itself
         * @return this {@link Builder} enriched with the added mapping
         */
        @SuppressWarnings("unchecked")
        public <R, S extends StoreAction<R>> Builder map(Class<S> actionType,
                                                         Function<? super S, ? extends Publisher<R>> handler) {
            Objects.requireNonNull(actionType);
            Objects.requireNonNull(handler);
            mappings.put(actionType, action -> handler.apply((S) action));
            return this;
        }

        /**
         * Builds an {@link ActionMapper} with all declared mappings.
         *
         * @return a new {@link ActionMapper}
         */
        public ActionMapper build() {
            return new ActionMapper(mappings);
        }
    }
}
