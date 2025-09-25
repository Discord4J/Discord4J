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
import org.reactivestreams.Subscription;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static discord4j.common.LogUtil.format;

/**
 * Distributes events to active subscribers, while using a fallback storage if no subscribers are present that can be
 * replayed to future late subscribers.
 * <p>
 * Allows configuring the main {@link FluxProcessor} backend, its publishing {@link Scheduler} for thread affinity, the
 * {@link ReplayProcessor} for storage and a {@link Predicate} to determine which events should be stored for replay.
 * <p>
 * While under no subscribers, this event dispatcher will store all incoming events that match the given filter to
 * its replay processor. Once the first subscriber has connected, the given stop trigger is subscribed and once it
 * terminates or is cancelled, no further events will be stored for replay, until all existing subscribers have
 * disconnected.
 * <p>
 * During the replay window, late subscribers benefit from all events that are pushed to this dispatcher, therefore
 * enabling scenarios where very early events are not missed despite connecting late.
 */
public class ReplayingEventDispatcher implements EventDispatcher {

    private static final Logger log = Loggers.getLogger(ReplayingEventDispatcher.class);

    private final FluxProcessor<Event, Event> eventProcessor;
    private final FluxSink<Event> sink;
    private final Scheduler eventScheduler;
    private final ReplayProcessor<Event> replayEventProcessor;
    private final FluxSink<Event> replaySink;
    private final Predicate<Event> replayEventFilter;
    private final Scheduler timedTaskScheduler;
    private final Publisher<?> stopReplayingTrigger;

    private final AtomicReference<State> state = new AtomicReference<>(State.REPLAY);

    /**
     * Creates a new event dispatcher that is able to retain events to replay to multiple late subscribers, using the
     * given parameters.
     *
     * @param eventProcessor a processor used to bridge gateway events to the dispatcher connected subscribers
     * @param overflowStrategy an overflow strategy, see {@link reactor.core.publisher.FluxSink.OverflowStrategy} for the available strategies
     * @param eventScheduler a {@link Scheduler} to ensure a certain thread model on each published signal
     * @param replayEventProcessor a processor used to store events while no subscribers are connected and will be
     * forwarded to all late subscribers
     * @param replayEventOverflowStrategy an overflow strategy while pushing events to the replay processor
     * @param replayEventFilter a filter used to decide whether an {@link Event} should be published to the
     * replay processor
     * @param timedTaskScheduler a time-capable {@link Scheduler} used to detect whether all events have replayed to
     * a subscriber
     * @param stopReplayingTrigger a trigger {@link Publisher} that is subscribed upon the first subscriber connects
     * to this dispatcher. Upon completion, error or cancellation, the replaying window will be closed until all
     * subscribers have disconnected
     */
    public ReplayingEventDispatcher(FluxProcessor<Event, Event> eventProcessor,
                                    FluxSink.OverflowStrategy overflowStrategy,
                                    Scheduler eventScheduler,
                                    ReplayProcessor<Event> replayEventProcessor,
                                    FluxSink.OverflowStrategy replayEventOverflowStrategy,
                                    Predicate<Event> replayEventFilter,
                                    Scheduler timedTaskScheduler,
                                    Publisher<?> stopReplayingTrigger) {
        this.eventProcessor = eventProcessor;
        this.sink = eventProcessor.sink(overflowStrategy);
        this.eventScheduler = eventScheduler;
        this.replayEventProcessor = replayEventProcessor;
        this.replaySink = replayEventProcessor.sink(replayEventOverflowStrategy);
        this.replayEventFilter = replayEventFilter;
        this.timedTaskScheduler = timedTaskScheduler;
        this.stopReplayingTrigger = stopReplayingTrigger;
    }

    @Override
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return eventProcessor.publishOn(eventScheduler)
                .startWith(replayEventProcessor.timeout(Duration.ofMillis(1), Mono.empty(), timedTaskScheduler))
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
        if (state.get() != State.EMIT && replayEventFilter.test(event)) {
            replaySink.next(event);
        }
        if (eventProcessor.hasDownstreams()) {
            if (state.compareAndSet(State.REPLAY, State.REPLAY_EMIT)) {
                Flux.from(stopReplayingTrigger).doFinally(__ -> state.set(State.EMIT)).subscribe();
            }
            sink.next(event);
        } else if (state.compareAndSet(State.EMIT, State.REPLAY) && replayEventFilter.test(event)) {
            log.warn("All subscribers have disconnected from this dispatcher");
            replaySink.next(event);
        }
    }

    @Override
    public void shutdown() {
        replaySink.complete();
        sink.complete();
    }

    private enum State {
        REPLAY, REPLAY_EMIT, EMIT
    }

    /**
     * Return a new default {@link ReplayingEventDispatcher}.
     *
     * @return a new replay event dispatcher
     */
    public static EventDispatcher create() {
        return builder().build();
    }

    /**
     * Return a new builder for {@link ReplayingEventDispatcher}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder to create {@link EventDispatcher} instances.
     */
    public static class Builder extends DefaultEventDispatcher.Builder {

        protected Predicate<Event> replayEventFilter =
                event -> event instanceof GatewayLifecycleEvent || event instanceof GuildCreateEvent;
        protected Scheduler timedTaskScheduler = Schedulers.parallel();
        protected ReplayProcessor<Event> replayEventProcessor;
        protected FluxSink.OverflowStrategy replayEventOverflowStrategy = FluxSink.OverflowStrategy.DROP;
        protected Publisher<?> stopReplayingTrigger;

        protected Builder() {
        }

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

        /**
         * Set a processor used to store events while no subscribers are connected and will be forwarded to all late
         * subscribers.
         *
         * @param replayEventProcessor the replay processor to use as backend
         * @return this builder
         */
        public Builder replayEventProcessor(ReplayProcessor<Event> replayEventProcessor) {
            this.replayEventProcessor = Objects.requireNonNull(replayEventProcessor);
            return this;
        }

        /**
         * Set an overflow strategy while pushing events to the replay processor.
         *
         * @param replayEventOverflowStrategy the strategy to use when pushing values to the replay processor
         * @return this builder
         */
        public Builder replayEventOverflowStrategy(FluxSink.OverflowStrategy replayEventOverflowStrategy) {
            this.replayEventOverflowStrategy = Objects.requireNonNull(replayEventOverflowStrategy);
            return this;
        }

        /**
         * Set a filter used to determine whether an incoming {@link Event} should be stored for replay.
         *
         * @param replayEventFilter the filter for events that can be replayed
         * @return this builder
         */
        public Builder replayEventFilter(Predicate<Event> replayEventFilter) {
            this.replayEventFilter = Objects.requireNonNull(replayEventFilter);
            return this;
        }

        /**
         * Set a time-capable {@link Scheduler} used to detect whether all events have replayed to a subscriber.
         *
         * @param timedTaskScheduler the scheduler to detect no more events can be replayed
         * @return this builder
         */
        public Builder timedTaskScheduler(Scheduler timedTaskScheduler) {
            this.timedTaskScheduler = Objects.requireNonNull(timedTaskScheduler);
            return this;
        }

        /**
         * Set a trigger {@link Publisher} that is subscribed upon the first subscriber connects to this dispatcher.
         * Upon completion, error or cancellation, the replaying window will be closed until all subscribers have
         * disconnected.
         *
         * @param stopReplayingTrigger the sequence to signal that no more events should be replayed. Can be
         * subscribed to multiple times as subscribers disconnect.
         * @return this builder
         */
        public Builder stopReplayingTrigger(Publisher<?> stopReplayingTrigger) {
            this.stopReplayingTrigger = Objects.requireNonNull(stopReplayingTrigger);
            return this;
        }

        @Override
        public EventDispatcher build() {
            if (eventProcessor == null) {
                eventProcessor = EmitterProcessor.create(Queues.SMALL_BUFFER_SIZE, false);
            }
            if (eventScheduler == null) {
                eventScheduler = DEFAULT_EVENT_SCHEDULER.get();
            }
            if (timedTaskScheduler == null) {
                timedTaskScheduler = Schedulers.parallel();
            }
            if (replayEventProcessor == null) {
                replayEventProcessor = ReplayProcessor.createTimeout(Duration.ofMinutes(2), timedTaskScheduler);
            }
            if (stopReplayingTrigger == null) {
                stopReplayingTrigger = Mono.delay(Duration.ofSeconds(5), timedTaskScheduler);
            }
            return new ReplayingEventDispatcher(
                    eventProcessor,
                    overflowStrategy,
                    eventScheduler,
                    replayEventProcessor,
                    replayEventOverflowStrategy,
                    replayEventFilter,
                    timedTaskScheduler,
                    stopReplayingTrigger);
        }

    }
}

