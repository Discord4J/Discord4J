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
import reactor.core.publisher.*;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.concurrent.Queues;

import java.time.Duration;
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
     * <p>
     * <strong>Note: </strong> Errors occurring while processing events will terminate your sequence. See
     * <a href="https://github.com/reactive-streams/reactive-streams-jvm#1.7">Reactive Streams Spec</a>
     * explaining this behavior.
     * <p>
     * A recommended pattern to use this method is wrapping your code that may throw exceptions within a {@code
     * flatMap} block and use {@link Mono#onErrorResume(Function)}, {@link Flux#onErrorResume(Function)} or
     * equivalent methods to maintain the sequence active:
     * <pre>
     * client.getEventDispatcher().on(MessageCreateEvent.class)
     *     .flatMap(event -&gt; myCodeThatMightThrow(event)
     *             .onErrorResume(error -&gt; {
     *                 // log and then discard the error to keep the sequence alive
     *                 log.error("Failed to handle event!", error);
     *                 return Mono.empty();
     *             }))
     *     .subscribe();
     * </pre>
     * <p>
     * For more alternatives to handling errors, please see
     * <a href="https://github.com/Discord4J/Discord4J/wiki/Error-Handling">Error Handling</a> wiki page.
     *
     * @param eventClass the event class to obtain events from
     * @param <E> the type of the event class
     * @return a new {@link reactor.core.publisher.Flux} with the requested events
     */
    <E extends Event> Flux<E> on(Class<E> eventClass);

    /**
     * Publishes an {@link Event} to the dispatcher.
     *
     * @param event the {@link Event} to publish
     */
    void publish(Event event);

    /**
     * Signal that this event dispatcher must terminate and release its resources.
     */
    void shutdown();

    // Factories

    /**
     * Create an {@link EventDispatcher} that will buffer incoming events to retain all startup events as each
     * shard connects at the cost of increased memory usage and potential {@link OutOfMemoryError} if events are not
     * consumed. Since this factory uses {@link EmitterProcessor}, it will only buffer events until the first
     * subscriber.
     *
     * @return a buffering {@link EventDispatcher} backed by an {@link EmitterProcessor}
     */
    static EventDispatcher buffering() {
        return new DefaultEventDispatcher(
                EmitterProcessor.create(Queues.SMALL_BUFFER_SIZE, false),
                FluxSink.OverflowStrategy.BUFFER,
                ForkJoinPoolScheduler.create("discord4j-events"));
    }

    /**
     * Create an {@link EventDispatcher} that is time-bounded and retains all elements whose age is at most {@code
     * maxAge}, replaying them to late subscribers.
     *
     * @param maxAge the maximum age of the contained items
     * @return an {@link EventDispatcher} backed by a {@link ReplayProcessor} with a time-bounded backlog
     * @see ReplayProcessor#createTimeout(Duration)
     */
    static EventDispatcher replayingWithTimeout(Duration maxAge) {
        return new DefaultEventDispatcher(
                ReplayProcessor.createTimeout(maxAge),
                FluxSink.OverflowStrategy.IGNORE,
                ForkJoinPoolScheduler.create("discord4j-events"));
    }

    /**
     * Create an {@link EventDispatcher} that will replays up to {@code historySize} elements to late subscribers.
     *
     * @param historySize the backlog size or maximum items retained for replay
     * @return an {@link EventDispatcher} backed by a {@link ReplayProcessor} with a customized backlog size
     * @see ReplayProcessor#create(int)
     */
    static EventDispatcher replayingWithSize(int historySize) {
        return new DefaultEventDispatcher(
                ReplayProcessor.create(historySize),
                FluxSink.OverflowStrategy.IGNORE,
                ForkJoinPoolScheduler.create("discord4j-events"));
    }
}
