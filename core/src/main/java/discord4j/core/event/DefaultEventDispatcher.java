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
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

/**
 * Distributes events to subscribers using a Reactor {@link FluxProcessor} as backend.
 * <p>
 * The underlying processor can be configured at construction time. Thread affinity can also be configured by
 * supplying a {@link Scheduler}, while an {@link reactor.core.publisher.FluxSink.OverflowStrategy} is applied to handle
 * back-pressure of inbound Events, especially during startup.
 * @see SinksEventDispatcher
 */
public class DefaultEventDispatcher implements EventDispatcher {

    private static final Logger log = Loggers.getLogger(DefaultEventDispatcher.class);

    private final FluxProcessor<Event, Event> eventProcessor;
    private final FluxSink<Event> sink;
    private final Scheduler eventScheduler;

    /**
     * Creates a new event dispatcher using the given {@link FluxProcessor}, backpressure-handling strategy and
     * threading model.
     *
     * @param eventProcessor a {@link FluxProcessor} of {@link Event}, used to bridge gateway events to the dispatcher
     * subscribers
     * @param overflowStrategy an overflow strategy, see {@link reactor.core.publisher.FluxSink.OverflowStrategy} for the available strategies
     * @param eventScheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     */
    public DefaultEventDispatcher(FluxProcessor<Event, Event> eventProcessor,
                                  FluxSink.OverflowStrategy overflowStrategy,
                                  Scheduler eventScheduler) {
        this.eventProcessor = eventProcessor;
        this.sink = eventProcessor.sink(overflowStrategy);
        this.eventScheduler = eventScheduler;
    }

    @Override
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return eventProcessor.publishOn(eventScheduler)
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
        sink.next(event);
    }

    @Override
    public void shutdown() {
        sink.complete();
    }

    /**
     * A builder to create {@link EventDispatcher} instances.
     * @see DefaultEventDispatcher.Builder#eventSink(Function)
     */
    public static class Builder implements EventDispatcher.Builder {

        protected FluxProcessor<Event, Event> eventProcessor;
        protected FluxSink.OverflowStrategy overflowStrategy = FluxSink.OverflowStrategy.BUFFER;
        protected Scheduler eventScheduler;

        protected Builder() {
        }

        @Override
        public SinksEventDispatcher.Builder eventSink(Function<Sinks.ManySpec, Sinks.Many<Event>> eventSinkFactory) {
            SinksEventDispatcher.Builder builder = new SinksEventDispatcher.Builder()
                    .eventSink(Objects.requireNonNull(eventSinkFactory));
            if (this.eventScheduler != null) {
                builder.eventScheduler(this.eventScheduler);
            }
            return builder;
        }

        @Deprecated
        @Override
        public Builder eventProcessor(FluxProcessor<Event, Event> eventProcessor) {
            this.eventProcessor = Objects.requireNonNull(eventProcessor);
            return this;
        }

        @Override
        public Builder overflowStrategy(FluxSink.OverflowStrategy overflowStrategy) {
            this.overflowStrategy = Objects.requireNonNull(overflowStrategy);
            return this;
        }

        @Override
        public Builder eventScheduler(Scheduler eventScheduler) {
            this.eventScheduler = Objects.requireNonNull(eventScheduler);
            return this;
        }

        @Deprecated
        @Override
        public EventDispatcher build() {
            if (eventProcessor == null) {
                eventProcessor = EmitterProcessor.create(Queues.SMALL_BUFFER_SIZE, false);
            }
            if (eventScheduler == null) {
                eventScheduler = DEFAULT_EVENT_SCHEDULER.get();
            }
            return new DefaultEventDispatcher(eventProcessor, overflowStrategy, eventScheduler);
        }

    }
}
