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

import discord4j.common.LogUtil;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.GatewayLifecycleEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

import static discord4j.common.LogUtil.format;

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

    Logger log = Loggers.getLogger(EventDispatcher.class);
    Supplier<Scheduler> DEFAULT_EVENT_SCHEDULER = () -> ForkJoinPoolScheduler.create("d4j-events");

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type. This {@link Flux} has to be subscribed to
     * in order to start processing. See {@link Event} class for the list of possible event classes.
     * <p>
     * <strong>Note: </strong> Errors occurring while processing events will terminate your sequence. If you wish to use
     * a version capable of handling errors for you, use {@link #on(Class, Function)}. See
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
     * @return a new {@link Flux} with the requested events
     */
    <E extends Event> Flux<E> on(Class<E> eventClass);

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type, to be processed through a given
     * {@link Function} upon subscription. Errors occurring within the mapper will be logged and discarded, preventing
     * the termination of the "infinite" event sequence. See {@link Event} class for the list of possible event classes.
     * <p>
     * There are multiple ways of using this event handling method, for example:
     * <pre>
     * client.on(MessageCreateEvent.class, event -&gt; {
     *         // myCodeThatMightThrow should return a Reactor type (Mono or Flux)
     *         return myCodeThatMightThrow(event);
     *     })
     *     .subscribe();
     *
     * client.on(MessageCreateEvent.class, event -&gt; {
     *         // myCodeThatMightThrow *can* be blocking, so wrap it in a Reactor type
     *         return Mono.fromRunnable(() -&gt; myCodeThatMightThrow(event));
     *     })
     *     .subscribe();
     * </pre>
     * <p>
     * Continuing the chain after {@code on(class, event -> ...)} will require your own error handling strategy.
     * Check the docs for {@link #on(Class)} for more details.
     *
     * @param eventClass the event class to obtain events from
     * @param mapper an event mapping function called on each event. If you do not wish to perform further operations
     * you can return {@code Mono.empty()}.
     * @param <E> the type of the event class
     * @param <T> the type of the event mapper function
     * @return a new {@link Flux} with the type resulting from the given event mapper
     */
    default <E extends Event, T> Flux<T> on(Class<E> eventClass, Function<E, Publisher<T>> mapper) {
        return on(eventClass)
                .flatMap(event -> Flux.defer(() -> mapper.apply(event))
                        .contextWrite(ctx -> ctx.put(LogUtil.KEY_SHARD_ID, event.getShardInfo().getIndex()))
                        .onErrorResume(t -> {
                            log.warn(format(Context.of(LogUtil.KEY_SHARD_ID, event.getShardInfo().getIndex()),
                                    "Error while handling {}"), eventClass.getSimpleName(), t);
                            return Mono.empty();
                        }));
    }

    /**
     * Applies a given {@code adapter} to all events from this dispatcher. Errors occurring within the mapper will be
     * logged and discarded, preventing the termination of the "infinite" event sequence. This variant allows you to
     * have a single subscriber to this dispatcher, which is useful to collect all startup events.
     * <p>
     * A standard approach to this method is to subclass {@link ReactiveEventAdapter}, overriding the methods you want
     * to listen for:
     * <pre>
     * client.on(new ReactiveEventAdapter() {
     *
     *     public Publisher&lt;?&gt; onReady(ReadyEvent event) {
     *         return Mono.fromRunnable(() -&gt;
     *                 System.out.println("Connected as " + event.getSelf().getTag()));
     *     }
     *
     *     public Publisher&lt;?&gt; onMessageCreate(MessageCreateEvent event) {
     *         if (event.getMessage().getContent().equals("!ping")) {
     *             return event.getMessage().getChannel()
     *                     .flatMap(channel -&gt; channel.createMessage("Pong!"));
     *         }
     *         return Mono.empty();
     *     }
     *
     * }).subscribe(); // nothing happens until you subscribe
     * </pre>
     * <p>
     * Each method requires a {@link Publisher} return like {@link Mono} or {@link Flux} and all errors
     * will be logged and discarded. To use a synchronous implementation you can wrap your code with
     * {@link Mono#fromRunnable(Runnable)}.
     * <p>
     * Continuing the chain will require your own error handling strategy.
     * Check the docs for {@link #on(Class)} for more details.
     *
     * @param adapter an adapter meant to be subclassed with its appropriate methods overridden
     * @return a new {@link Flux} with the type resulting from the given event mapper
     */
    default Flux<Event> on(ReactiveEventAdapter adapter) {
        return on(Event.class)
                .flatMap(event -> Flux.defer(() -> adapter.hookOnEvent(event))
                        .contextWrite(ctx -> ctx.put(LogUtil.KEY_SHARD_ID, event.getShardInfo().getIndex()))
                        .onErrorResume(t -> {
                            log.warn(format(Context.of(LogUtil.KEY_SHARD_ID, event.getShardInfo().getIndex()),
                                    "Error while handling {}"), event.getClass().getSimpleName(), t);
                            return Mono.empty();
                        })
                        .then(Mono.just(event)));
    }

    /**
     * Publishes an {@link Event} to the dispatcher. Might throw an unchecked exception if the dispatcher can't
     * handle this event.
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
     * Create an {@link EventDispatcher} builder. It can be configured with a custom {@link FluxProcessor} for
     * events, a custom {@link FluxSink.OverflowStrategy} to handle backpressure and a custom {@link Scheduler} to
     * dispatch events.
     *
     * @return a {@link Builder}
     */
    static Builder builder() {
        return new DefaultEventDispatcher.Builder();
    }

    /**
     * Create an {@link EventDispatcher} that will buffer incoming events to retain all startup events as each
     * shard connects at the cost of increased memory usage and potential {@link OutOfMemoryError} if events are not
     * consumed. Startup events collected before the first subscription are only forwarded to that subscriber.
     *
     * @return a buffering {@link EventDispatcher} backed by an {@link EmitterProcessor}
     */
    static EventDispatcher buffering() {
        return builder().build();
    }

    /**
     * Create an {@link EventDispatcher} that will buffer incoming events up to the given {@code bufferSize} elements,
     * where subsequent events will be dropped in favor of retaining the earliest ones. Startup events collected
     * before the first subscription are only forwarded to that subscriber.
     *
     * @param bufferSize the number of events to keep in the backlog
     * @return an {@link EventDispatcher} keeping the earliest events up to {@code bufferSize}
     */
    static EventDispatcher withEarliestEvents(int bufferSize) {
        return builder()
                .eventSink(spec -> spec.multicast().onBackpressureBuffer(bufferSize, false))
                .build();
    }

    /**
     * Create an {@link EventDispatcher} that will buffer incoming events up to the given {@code bufferSize} elements,
     * where earliest events will be dropped in favor of retaining the latest ones. Startup events collected before
     * the first subscription are only forwarded to that subscriber.
     *
     * @param bufferSize the number of events to keep in the backlog
     * @return an {@link EventDispatcher} keeping the latest events backed by an {@link EmitterProcessor}
     * @deprecated due to Processor API being deprecated, we recommend moving to {@link #replayingWithSize(int)} for a
     * dispatcher that is able to retain a given number of latest events
     */
    static EventDispatcher withLatestEvents(int bufferSize) {
        return builder()
                .eventProcessor(EmitterProcessor.create(bufferSize, false))
                .overflowStrategy(FluxSink.OverflowStrategy.LATEST)
                .build();
    }

    /**
     * Create an {@link EventDispatcher} that is capable of replaying up to 2 minutes worth of important events like
     * {@link GuildCreateEvent} and {@link GatewayLifecycleEvent} that arrive while no subscribers are connected to
     * all late subscribers, as long as they subscribe within the replay window of 5 seconds. After the replay window
     * has closed, it behaves like an emitter event dispatcher.
     * <p>
     * This allows controlling the memory overhead of dispatchers like {@link #buffering()} while still keeping a record
     * of important events to all late subscribers, even after login has completed.
     * <p>
     * This dispatcher can be customized through the use of {@link ReplayingEventDispatcher#builder()}.
     *
     * @return an {@link EventDispatcher} that is capable of replaying events to late subscribers
     */
    static EventDispatcher replaying() {
        return ReplayingEventDispatcher.create();
    }

    /**
     * Create an {@link EventDispatcher} that is time-bounded and retains all elements whose age is at most {@code
     * maxAge}, replaying them to late subscribers. Be aware that using this type of dispatcher with operators such
     * as {@link Flux#retry()} or {@link Flux#repeat()} that re-subscribe to the dispatcher will observe the same
     * elements as the backlog contains.
     *
     * @param maxAge the maximum age of the contained items
     * @return an {@link EventDispatcher} that will replay elements up to {@code maxAge} duration to late subscribers
     */
    static EventDispatcher replayingWithTimeout(Duration maxAge) {
        return builder()
                .eventSink(spec -> spec.replay().limit(maxAge))
                .build();
    }

    /**
     * Create an {@link EventDispatcher} that will replay up to {@code historySize} elements to late subscribers.
     * Be aware that using this type of dispatcher with operators such as {@link Flux#retry()} or
     * {@link Flux#repeat()} that re-subscribe to the dispatcher will observe the same elements as the backlog contains.
     *
     * @param historySize the backlog size or maximum items retained for replay
     * @return an {@link EventDispatcher} that will replay up to {@code historySize} elements to late subscribers
     */
    static EventDispatcher replayingWithSize(int historySize) {
        return builder()
                .eventSink(spec -> spec.replay().limit(historySize))
                .build();
    }

    interface Builder {

        /**
         * Set the underlying {@link Sinks.Many} the dispatcher will use to queue and distribute events. Defaults
         * to using a multicast buffering sink.
         *
         * @param eventSinkFactory the custom sink factory for events
         * @return this builder
         */
        SinksEventDispatcher.Builder eventSink(Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory);

        /**
         * Set the underlying {@link FluxProcessor} the dispatcher will use to queue and distribute events. Defaults
         * to using an {@link EmitterProcessor}.
         * <p>
         * Using {@link EmitterProcessor} only emits events since a subscriber has subscribed to the processor
         * (except for the first one which receives all queued signals until that point), and it allows you to
         * configure the backing queue size while allowing you to use operators like {@link Flux#repeat()} and
         * {@link Flux#retry()} to drop the triggering signal.
         *
         * @param eventProcessor the custom processor for events
         * @return this builder
         * @deprecated due to the Processor API being deprecated, we recommend using {@link #eventSink(Function)}
         * moving forward
         */
        @Deprecated
        DefaultEventDispatcher.Builder eventProcessor(FluxProcessor<Event, Event> eventProcessor);

        /**
         * Set the {@link FluxSink.OverflowStrategy} for dealing with overflow scenarios where too many events are
         * being published. Defaults to using {@link FluxSink.OverflowStrategy#BUFFER} to ensure all events are
         * delivered at the cost of higher memory footprint and potential {@link OutOfMemoryError} scenarios.
         * <p>
         * To only keep the earliest events you can use {@link FluxSink.OverflowStrategy#DROP}, and to only keep the
         * most recent events, use {@link FluxSink.OverflowStrategy#LATEST}. The number of events that can be queued
         * until this strategy is applied depends on the underlying processor implementation.
         *
         * @param overflowStrategy the custom backpressure strategy
         * @return this builder
         * @deprecated due to the Processor API being deprecated, we recommend using {@link #eventSink(Function)}
         * moving forward
         */
        @Deprecated
        DefaultEventDispatcher.Builder overflowStrategy(FluxSink.OverflowStrategy overflowStrategy);

        /**
         * Set the {@link Scheduler} this dispatcher should use to publish events to its subscribers. Using a bounded
         * elastic/blocking-capable one is recommended for general workloads that may have blocking sequences.
         *
         * @param eventScheduler a custom {@link Scheduler} to publish events
         * @return this builder
         */
        DefaultEventDispatcher.Builder eventScheduler(Scheduler eventScheduler);

        /**
         * Create the {@link EventDispatcher}
         *
         * @return an {@link EventDispatcher} with the configured parameters.
         */
        EventDispatcher build();
    }
}
