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

package discord4j.core.event.dispatch;

import discord4j.common.annotations.Experimental;
import discord4j.core.event.domain.Event;
import discord4j.common.store.Store;
import reactor.core.publisher.Mono;

/**
 * A transformation between Gateway inbound Dispatch class of payloads to Discord4J core {@link Event} instances.
 */
@FunctionalInterface
@Experimental
public interface DispatchEventMapper {

    /**
     * Process a {@link DispatchContext} to potentially obtain an {@link Event}.
     *
     * @param context the DispatchContext used with this Dispatch object
     * @param <D> the Dispatch type
     * @param <E> the resulting Event type
     * @return a {@link Mono} of {@link Event} mapped from the given {@link DispatchContext} object, or empty if no
     * Event is produced. If an error occurs during processing, it is emitted through the {@code Mono}.
     */
    <D, S, E extends Event> Mono<E> handle(DispatchContext<D, S> context);

    /**
     * Create a {@link DispatchEventMapper} that processes updates and records them into the right {@link Store},
     * then derives the proper {@link Event}.
     *
     * @return a {@link DispatchEventMapper} that caches updates and produces {@link Event} instances
     */
    static DispatchEventMapper emitEvents() {
        return new DispatchHandlers();
    }

    /**
     * Create a {@link DispatchEventMapper} that processes updates and records them into the right {@link Store}, while
     * not producing any {@link Event} downstream.
     *
     * @return a {@link DispatchEventMapper} that only caches updates
     */
    static DispatchEventMapper discardEvents() {
        DispatchHandlers handlers = new DispatchHandlers();
        return new DispatchEventMapper() {
            @Override
            public <D, S, E extends Event> Mono<E> handle(DispatchContext<D, S> context) {
                // TODO improve DispatchHandlers to avoid creating Event objects to then discard here
                return handlers.handle(context).then(Mono.empty());
            }
        };
    }

    /**
     * Create a {@link DispatchEventMapper} that doesn't process any dispatches
     *
     * @return a {@link DispatchEventMapper} that does nothing
     */
    static DispatchEventMapper noOp() {
        return new DispatchEventMapper() {
            @Override
            public <D, S, E extends Event> Mono<E> handle(DispatchContext<D, S> context) {
                return Mono.empty();
            }
        };
    }
}
