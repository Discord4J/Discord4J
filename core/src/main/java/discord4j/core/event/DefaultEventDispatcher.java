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
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

/**
 * Distributes events to subscribers. {@link Event} instances can be published over this class and dispatched to all
 * subscribers.
 * <p>
 * Individual events can be published to subscribers using {@link #publish(Event)} while they can be used to consumed
 * through {@link #on(Class)} giving the proper {@link Event} class as argument.
 * <p>
 * Uses an underlying {@link FluxProcessor} that can be configured at construction time. Thread affinity can also
 * be configured by supplying a {@link Scheduler}, while an {@link FluxSink.OverflowStrategy} is applied to handle
 * back-pressure of inbound Events, especially during startup.
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
public class DefaultEventDispatcher implements EventDispatcher {

    private final FluxProcessor<Event, Event> processor;
    private final FluxSink<Event> sink;
    private final Scheduler scheduler;

    /**
     * Creates a new event dispatcher using the given {@link FluxProcessor}, backpressure-handling strategy and
     * threading model.
     *
     * @param processor a {@link FluxProcessor} of {@link Event}, used to bridge gateway events to the dispatcher
     * subscribers
     * @param overflowStrategy an overflow strategy, see {@link FluxSink.OverflowStrategy} for the available strategies
     * @param scheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     */
    public DefaultEventDispatcher(FluxProcessor<Event, Event> processor, FluxSink.OverflowStrategy overflowStrategy,
                                  Scheduler scheduler) {
        this.processor = processor;
        this.sink = processor.sink(overflowStrategy);
        this.scheduler = scheduler;
    }

    @Override
    public <T extends Event> Flux<T> on(Class<T> eventClass) {
        return processor.publishOn(scheduler).ofType(eventClass);
    }

    @Override
    public void publish(Event event) {
        sink.next(event);
    }

    @Override
    public void shutdown() {
        sink.complete();
    }
}
