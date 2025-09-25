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
import discord4j.common.annotations.Experimental;
import discord4j.common.sinks.EmissionStrategy;
import discord4j.core.event.domain.Event;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

/**
 * Distributes {@link Event} instances to subscribers, using Reactor {@link Sinks} as backend.
 * <p>
 * The underlying sink and thread affinity for event publishing can be configured at construction time.
 */
@Experimental
public class SinksEventDispatcher implements EventDispatcher {

    private static final Logger log = Loggers.getLogger(SinksEventDispatcher.class);

    private final Sinks.Many<Event> events;
    private final EmissionStrategy emissionStrategy;
    private final Scheduler eventScheduler;

    /**
     * Creates a new event dispatcher using the given event sink factory and threading model.
     *
     * @param eventSinkFactory the custom sink factory for events
     * @param emissionStrategy a strategy to handle emission failures
     * @param eventScheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     */
    public SinksEventDispatcher(Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory,
                                EmissionStrategy emissionStrategy,
                                Scheduler eventScheduler) {
        this.events = eventSinkFactory.apply(Sinks.many());
        this.emissionStrategy = emissionStrategy;
        this.eventScheduler = eventScheduler;
    }

    @Override
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return events.asFlux()
                .publishOn(eventScheduler)
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
        emissionStrategy.emitNext(events, event);
    }

    @Override
    public void shutdown() {
        emissionStrategy.emitComplete(events);
    }

    /**
     * A builder to create {@link EventDispatcher} instances.
     */
    public static class Builder {

        protected Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory;
        protected EmissionStrategy emissionStrategy;
        protected Scheduler eventScheduler;

        protected Builder() {
        }

        /**
         * Set the underlying {@link reactor.core.publisher.Sinks.Many} the dispatcher will use to queue and distribute events. Defaults
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
         * Set the {@link EmissionStrategy} to apply when event publishing fails, which can be useful to handle
         * overflowing, non-serialized or terminal scenarios through the means of retrying, parking threads or throwing
         * an exception back to the emitter. Defaults to a timeout-then-drop strategy after 10 seconds.
         *
         * @param emissionStrategy the emission failure handling strategy
         * @return this builder
         */
        public Builder emissionStrategy(EmissionStrategy emissionStrategy) {
            this.emissionStrategy = Objects.requireNonNull(emissionStrategy);
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
            if (emissionStrategy == null) {
                emissionStrategy = EmissionStrategy.timeoutDrop(Duration.ofSeconds(10));
            }
            if (eventScheduler == null) {
                eventScheduler = DEFAULT_EVENT_SCHEDULER.get();
            }
            return new SinksEventDispatcher(eventSinkFactory, emissionStrategy, eventScheduler);
        }

    }
}
