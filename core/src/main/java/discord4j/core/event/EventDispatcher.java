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
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;

import java.util.logging.Level;

/**
 * Distributes events to each subscriber.
 * <p>
 * Can be used in a reactive manner by calling {@link #on(Class)} giving the proper
 * {@link discord4j.core.event.domain.Event} class as argument. Uses an underlying
 * {@link reactor.core.publisher.FluxProcessor} that must be supplied on construction, as well as a
 * {@link reactor.core.scheduler.Scheduler} to define subscriber thread affinity.
 * <p>
 * Each event can be consumed using the following pattern:
 * <pre>
 *     dispatcher.on(MessageCreatedEvent.class)
 *           .subscribe(event -&gt; event.getMessage());
 * </pre>
 */
public class EventDispatcher {

    private final FluxProcessor<Event, Event> processor;
    private final Scheduler scheduler;
    private final int shardIndex;

    /**
     * Creates a new event dispatcher using the given processor and thread model.
     *
     * @param processor a FluxProcessor of Event types, used to bridge gateway events to the dispatcher subscribers
     * @param scheduler a Scheduler to ensure a certain thread model on each published signal
     * @param shardIndex the shard ID for logger name purposes
     */
    public EventDispatcher(FluxProcessor<Event, Event> processor, Scheduler scheduler, int shardIndex) {
        this.processor = processor;
        this.scheduler = scheduler;
        this.shardIndex = shardIndex;
    }

    /**
     * Retrieves a {@link reactor.core.publisher.Flux} with elements of the given
     * {@link discord4j.core.event.domain.Event} type.
     *
     * @param eventClass the event class to obtain events from
     * @param <T> the type of the event class
     * @return a new {@link reactor.core.publisher.Flux} with the requested events
     */
    public <T extends Event> Flux<T> on(Class<T> eventClass) {
        return processor.publishOn(scheduler)
                .ofType(eventClass)
                .log("discord4j.dispatch." + eventClass.getSimpleName() + "." + shardIndex, Level.FINE,
                        SignalType.ON_NEXT, SignalType.ON_SUBSCRIBE, SignalType.ON_ERROR, SignalType.CANCEL);
    }
}
