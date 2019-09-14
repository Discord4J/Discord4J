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

package discord4j.core.event;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Distributes events to subscribers. {@link Event} instances can be published over this class and dispatched to all
 * subscribers.
 * <p>
 * Individual events can be published to subscribers using {@link #publish(Event)} while they can be used to consumed
 * through {@link #on(Class)} giving the proper {@link Event} class as argument.
 * <p>
 * Each event can be consumed using the following pattern:
 * <pre>
 *     dispatcher.on(MessageCreatedEvent.class)
 *           .subscribe(event -&gt; event.getMessage());
 * </pre>
 * While events can be published through:
 * <pre>
 *     fluxOfEvents.doOnNext(dispatcher::publish)
 *           .subscribe();
 * </pre>
 */
public interface EventDispatcher {

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type.
     *
     * @param eventClass the event class to obtain events from
     * @param <T> the type of the event class
     * @return a new {@link Flux} with the requested events
     */
    <T extends Event> Flux<T> on(Class<T> eventClass);

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type, registering a user-provided function
     * to process the event. Any error thrown within the function will be logged and suppressed.
     *
     * @param eventClass the event class to obtain events from
     * @param eventListener a function to process each event
     * @param <T> the type of the event class
     * @return a new {@link Flux} with the requested events
     */
    <T extends Event> Flux<T> on(Class<T> eventClass, Function<T, Mono<Void>> eventListener);

    /**
     * Publishes an {@link Event} to the dispatcher.
     *
     * @param event the {@link Event} to publish
     */
    void publish(Event event);

    /**
     * Signal that this event dispatcher must complete.
     */
    void complete();
}
