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
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.time.Duration;

/**
 * Distributes events to subscribers. {@link Event} instances can be published over this class and dispatched to all
 * subscribers.
 * <p>
 * Individual events can be published to subscribers using {@link #publish(Event)} while they can be used to consumed
 * through {@link #on(Class)} giving the proper {@link Event} class as argument.
 * <p>
 * Uses an underlying {@link ReplayProcessor} that can be configured during instantiation. Thread affinity can also
 * be configured by supplying a {@link Scheduler}.
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
public class ReplayEventDispatcher implements EventDispatcher {

    private final ReplayProcessor<Event> processor;
    private final FluxSink<Event> sink;
    private final Scheduler scheduler;

    /**
     * Create an {@link ReplayEventDispatcher} with a backing {@link ReplayProcessor} that will replays up to {@code
     * historySize} elements to late subscribers.
     *
     * @param historySize the backlog size or maximum items retained for replay
     * @return an {@link ReplayEventDispatcher} with a customized backlog size
     * @see ReplayProcessor#create(int)
     */
    public static ReplayEventDispatcher withSize(int historySize) {
        return new ReplayEventDispatcher(
                ReplayProcessor.create(historySize),
                ForkJoinPoolScheduler.create("discord4j-events"));
    }

    /**
     * Create an {@link ReplayEventDispatcher} with a backing {@link ReplayProcessor} that is time-bounded and retains
     * all elements whose age is at most {@code maxAge}, replaying them to late subscribers.
     *
     * @param maxAge the maximum age of the contained items
     * @return an {@link ReplayEventDispatcher} with a time-bounded backlog
     * @see ReplayProcessor#createTimeout(Duration)
     */
    public static ReplayEventDispatcher withTimeout(Duration maxAge) {
        return new ReplayEventDispatcher(
                ReplayProcessor.createTimeout(maxAge),
                ForkJoinPoolScheduler.create("discord4j-events"));
    }

    /**
     * Creates a new event dispatcher using an externally managed processor, backpressure-handling strategy and
     * thread model.
     *
     * @param processor an {@link ReplayProcessor} of {@link Event}, used to bridge gateway events to the dispatcher
     * subscribers
     * @param scheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     */
    public ReplayEventDispatcher(ReplayProcessor<Event> processor, Scheduler scheduler) {
        this.processor = processor;
        this.sink = processor.sink();
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
    public void complete() {
        sink.complete();
    }
}
