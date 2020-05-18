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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common;

import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

/**
 * Emit ticks at a constant rate specified at {@link #start(Duration, Duration)} and will continue until
 * {@link #stop()} is called or {@link #start(Duration, Duration)} is re-invoked, resetting the previous emitter.
 * The ticks are available from the {@link #ticks()} method.
 */
public class ResettableInterval {

    private final Scheduler scheduler;
    private final Disposable.Swap task;
    private final EmitterProcessor<Long> backing = EmitterProcessor.create(false);
    private final FluxSink<Long> backingSink = backing.sink(FluxSink.OverflowStrategy.LATEST);

    /**
     * Create a {@link ResettableInterval} that emits ticks on the given {@link Scheduler} upon calling
     * {@link #start(Duration, Duration)}.
     *
     * @param scheduler the Reactor {@link Scheduler} to use to emit ticks
     */
    public ResettableInterval(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.task = Disposables.swap();
    }

    /**
     * Begin producing ticks at the given rate.
     *
     * @param delay the {@link Duration} to wait before emitting the first tick
     * @param period the period {@link Duration} used to emit ticks.
     * @see Flux#interval(Duration, Duration, Scheduler)
     */
    public void start(Duration delay, Duration period) {
        this.task.update(Flux.interval(delay, period, scheduler).subscribe(backingSink::next));
    }

    /**
     * Dispose the current emitter task without completing or cancelling existing subscriptions to {@link #ticks()}.
     */
    public void stop() {
        if (task.get() != null) {
            task.get().dispose();
        }
    }

    /**
     * Return a {@link Flux} that emits ticks at the currently configured rate.
     *
     * @return a {@link Flux} of increasing values since the last {@link #start(Duration, Duration)} call
     */
    public Flux<Long> ticks() {
        return backing;
    }
}
