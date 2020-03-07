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
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Distributes events to subscribers. {@link Event} instances can be published over this class and dispatched to all
 * subscribers.
 * <p>
 * Individual events can be published to subscribers using {@link #publish(Event)} while they can be used to consumed
 * through {@link #on(Class)} giving the proper {@link Event} class as argument.
 * <p>
 * Uses an underlying {@link FluxProcessor} that must be supplied on construction, as well as a {@link Scheduler} to
 * define subscriber thread affinity.
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
public class EventDispatcher {

    private static final Logger log = Loggers.getLogger(EventDispatcher.class);

    private final FluxProcessor<Event, Event> processor;
    private final Scheduler scheduler;

    /**
     * Creates a new event dispatcher using the given processor and thread model.
     *
     * @param processor a FluxProcessor of Event types, used to bridge gateway events to the dispatcher subscribers
     * @param scheduler a Scheduler to ensure a certain thread model on each published signal
     */
    public EventDispatcher(FluxProcessor<Event, Event> processor, Scheduler scheduler) {
        this.processor = processor;
        this.scheduler = scheduler;
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type.
     * <p>
     * <strong>Note: </strong> Errors occurring while processing events will terminate your sequence. If you wish to use
     * a version capable of handling errors for you, use {@link #on(Class, Function)}. See
     * <a href="https://github.com/reactive-streams/reactive-streams-jvm#1.7">Reactive Streams Spec</a>
     * explaining this behavior.
     * </p>
     * <p>
     * A recommended pattern to use this method is wrapping your code that may throw exceptions within a {@code
     * flatMap} block and use {@link Mono#onErrorResume(Function)}, {@link Flux#onErrorResume(Function)} or
     * equivalent methods to maintain the sequence active:
     * </p>
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
     * </p>
     *
     * @param eventClass the event class to obtain events from
     * @param <E> the type of the event class
     * @return a new {@link reactor.core.publisher.Flux} with the requested events
     */
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return processor.publishOn(scheduler)
                .ofType(eventClass)
                .doOnNext(event -> {
                    int shard = event.getClient().getConfig().getShardIndex();
                    Logger log = logger(eventClass, shard);
                    if (log.isDebugEnabled()) {
                        log.debug("{}", event);
                    }
                })
                .doOnSubscribe(sub -> {
                    subscription.set(sub);
                    log.debug(format(sub, "{} subscription created"), eventClass.getSimpleName());
                })
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        log.debug(format(subscription.get(), "{} subscription cancelled"), eventClass.getSimpleName());
                    }
                });
    }

    /**
     * Retrieves a {@link Flux} with elements of the given {@link Event} type, processing them through a given
     * {@link Function}. Errors occurring within the mapper will be logged and discarded, preventing the termination of
     * the "infinite" event sequence.
     * <p>
     * There are multiple ways of using this event handling method, for example:
     * </p>
     * <pre>
     * client.getEventDispatcher()
     *     .on(MessageCreateEvent.class, event -> {
     *         // myCodeThatMightThrow should return a Reactor type (Mono or Flux)
     *         return myCodeThatMightThrow(event);
     *     })
     *     .subscribe();
     *
     * client.getEventDispatcher()
     *     .on(MessageCreateEvent.class, event -> {
     *         // myCodeThatMightThrow *can* be blocking
     *         myCodeThatMightThrow(event);
     *         return Mono.empty(); // but we have to return a Reactor type
     *     })
     *     .subscribe();
     * </pre>
     * <p>
     * Continuing the chain after {@code on(class, event -> ...)} will require your own error handling strategy.
     * Check the docs for {@link #on(Class)} for more details.
     * </p>
     *
     * @param eventClass the event class to obtain events from
     * @param mapper an event mapping function called on each event. If you do not wish to perform further operations
     * you can return {@code Mono.empty()}.
     * @param <E> the type of the event class
     * @param <T> the type of the event mapper function
     * @return a new {@link reactor.core.publisher.Flux} with the type resulting from the given event mapper
     */
    public <E extends Event, T> Flux<T> on(Class<E> eventClass, Function<E, Publisher<T>> mapper) {
        return on(eventClass)
                .flatMap(event -> Flux.defer(() -> mapper.apply(event))
                        .onErrorResume(t -> {
                            int shard = event.getClient().getConfig().getShardIndex();
                            logger(eventClass, shard).warn("Error while handling event", t);
                            return Mono.empty();
                        }));
    }

    private Logger logger(Class<?> eventClass, int shard) {
        return Loggers.getLogger("discord4j.events." + eventClass.getSimpleName() + "." + shard);
    }

    private String format(Subscription s, String msg) {
        return '[' + Integer.toHexString(s.hashCode()) + "] " + msg;
    }

    /**
     * Publishes an {@link Event} to the dispatcher.
     *
     * @param event the {@link Event} to publish
     */
    public void publish(Event event) {
        processor.onNext(event);
    }
}
