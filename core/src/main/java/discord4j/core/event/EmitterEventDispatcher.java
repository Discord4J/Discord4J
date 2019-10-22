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
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.concurrent.Queues;

/**
 * Distributes events to subscribers. {@link Event} instances can be published over this class and dispatched to all
 * subscribers.
 * <p>
 * Individual events can be published to subscribers using {@link #publish(Event)} while they can be used to consumed
 * through {@link #on(Class)} giving the proper {@link Event} class as argument.
 * <p>
 * Uses an underlying {@link EmitterProcessor} that can be configured during instantiation. Thread affinity can also
 * be configured by supplying a {@link Scheduler}, while an {@link FluxSink.OverflowStrategy} is applied to handle
 * backpressure of inbound Events, especially during startup.
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
public class EmitterEventDispatcher implements EventDispatcher {

    private final EmitterProcessor<Event> processor;
    private final FluxSink<Event> sink;
    private final Scheduler scheduler;

    /**
     * Create an {@link EmitterEventDispatcher} that uses a dropping {@link FluxSink.OverflowStrategy} meaning that it
     * will discard events if the receiver is not ready. This typically occurs during startup before all shards have
     * connected. For a version of this dispatcher that can handle the initial burst of events, see {@link #buffering()}
     *
     * @return a dropping {@link EmitterEventDispatcher}
     */
    public static EmitterEventDispatcher create() {
        return withStrategy(FluxSink.OverflowStrategy.DROP);
    }

    /**
     * Create an {@link EmitterEventDispatcher} that will buffer incoming events to retain all startup events as each
     * shard connects at the cost of increased memory usage. Useful if you want to subscribe to events happening
     * early in the shard's lifecycle.
     *
     * @return a buffering {@link EmitterEventDispatcher}
     */
    public static EmitterEventDispatcher buffering() {
        return withStrategy(FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * Create an {@link EmitterEventDispatcher} with a custom {@link FluxSink.OverflowStrategy}.
     *
     * @param strategy the supplied strategy to use while publishing events to handle backpressure
     * @return an {@link EmitterEventDispatcher} with a customized backpressure strategy
     */
    public static EmitterEventDispatcher withStrategy(FluxSink.OverflowStrategy strategy) {
        return new EmitterEventDispatcher(
                EmitterProcessor.create(Queues.SMALL_BUFFER_SIZE, false),
                strategy,
                ForkJoinPoolScheduler.create("discord4j-events"));
    }

    /**
     * Creates a new event dispatcher using an externally managed processor, backpressure-handling strategy and
     * thread model.
     *
     * @param processor an {@link EmitterProcessor} of {@link Event}, used to bridge gateway events to the dispatcher
     * subscribers
     * @param strategy an overflow strategy, see {@link FluxSink.OverflowStrategy} for the available strategies
     * @param scheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     */
    public EmitterEventDispatcher(EmitterProcessor<Event> processor, FluxSink.OverflowStrategy strategy,
                                  Scheduler scheduler) {
        this.processor = processor;
        this.sink = processor.sink(strategy);
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
