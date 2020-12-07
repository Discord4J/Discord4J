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
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

/**
 * Distributes {@link Event} instances to subscribers, using Reactor {@link Sinks} as backend.
 * <p>
 * The underlying sink and thread affinity for event publishing can be configured at construction time.
 */
public class SinksEventDispatcher implements EventDispatcher, Sinks.EmitFailureHandler {

    private static final Logger log = Loggers.getLogger(SinksEventDispatcher.class);

    private final Sinks.Many<Event> eventSink;
    private final Scheduler eventScheduler;

    /**
     * Creates a new event dispatcher using the given event sink factory and threading model.
     *
     * @param eventSinkFactory the custom sink factory for events
     * @param eventScheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     */
    public SinksEventDispatcher(Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory,
                                Scheduler eventScheduler) {
        this.eventSink = eventSinkFactory.apply(Sinks.many());
        this.eventScheduler = eventScheduler;
    }

    @Override
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return eventSink.asFlux().publishOn(eventScheduler)
                .ofType(eventClass)
                .<E>handle((event, sink) -> {
                    if (log.isTraceEnabled()) {
                        log.trace(format(sink.currentContext().put(LogUtil.KEY_SHARD_ID,
                                event.getShardInfo().getIndex()), "{}"), event.toString());
                    }
                    sink.next(event);
                })
                .doOnSubscribe(sub -> {
                    subscription.set(sub);
                    if (log.isDebugEnabled()) {
                        log.debug("Subscription {} to {} created", Integer.toHexString(sub.hashCode()),
                                eventClass.getSimpleName());
                    }
                })
                .doFinally(signal -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Subscription {} to {} disposed due to {}",
                                Integer.toHexString(subscription.get().hashCode()), eventClass.getSimpleName(), signal);
                    }
                });
    }

    @Override
    public void publish(Event event) {
        Sinks.EmitResult result = eventSink.tryEmitNext(event);

        if (result.isSuccess()) {
            return;
        }

        switch (result) {
            case FAIL_ZERO_SUBSCRIBER:
                return;
            case FAIL_OVERFLOW:
            case FAIL_CANCELLED:
                Operators.onDiscard(event, Context.empty());
                return;
            case FAIL_TERMINATED:
                Operators.onNextDropped(event, Context.empty());
                return;
            case FAIL_NON_SERIALIZED:
                throw new Sinks.EmissionException(result,
                        "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST " +
                                "be signaled serially."
                );
        }
    }

    @Override
    public void shutdown() {
        eventSink.emitComplete(this);
    }

    @Override
    public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult result) {
        if (result == Sinks.EmitResult.FAIL_OVERFLOW) {
            LockSupport.parkNanos(10);
            return true;
        }
        return false;
    }

    /**
     * A builder to create {@link EventDispatcher} instances.
     */
    public static class Builder {

        protected Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory;
        protected Scheduler eventScheduler;

        protected Builder() {
        }

        /**
         * Set the underlying {@link Sinks.Many} the dispatcher will use to queue and distribute events. Defaults
         * to using a multicast buffering sink.
         *
         * @param eventSinkFactory the custom sink factory for events
         * @return this builder
         */
        public Builder eventSink(Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory) {
            this.eventSinkFactory = Objects.requireNonNull(eventSinkFactory);
            return this;
        }

        /**
         * Set the {@link Scheduler} this dispatcher should use to publish events to its subscribers. Using a bounded
         * elastic/blocking-capable one is recommended for general workloads that may have blocking sequences.
         *
         * @param eventScheduler a custom {@link Scheduler} to publish events
         * @return this builder
         */
        public Builder eventScheduler(Scheduler eventScheduler) {
            this.eventScheduler = Objects.requireNonNull(eventScheduler);
            return this;
        }

        public EventDispatcher build() {
            if (eventSinkFactory == null) {
                eventSinkFactory = spec -> spec.multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
            }
            if (eventScheduler == null) {
                eventScheduler = DEFAULT_EVENT_SCHEDULER.get();
            }
            return new SinksEventDispatcher(eventSinkFactory, eventScheduler);
        }

    }
}
