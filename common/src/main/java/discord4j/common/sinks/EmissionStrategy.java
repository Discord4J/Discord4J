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

package discord4j.common.sinks;

import discord4j.common.annotations.Experimental;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * A strategy to handle emission failures to a {@link reactor.core.publisher.Sinks.Many} instance.
 */
@Experimental
public interface EmissionStrategy {

    /**
     * Create an {@link EmissionStrategy} that will retry overflowing and non-serialized emissions until a given
     * {@code duration} and <strong>drop</strong> values upon timeout.
     *
     * @param duration the {@link Duration} to wait until elements are dropped
     * @return a strategy with a drop on timeout behavior
     */
    static TimeoutEmissionStrategy timeoutDrop(Duration duration) {
        return new TimeoutEmissionStrategy(Duration.ofMillis(10).toNanos(), duration.toNanos(), false);
    }

    /**
     * Create an {@link EmissionStrategy} that will retry overflowing and non-serialized emissions until a given
     * {@code duration} and <strong>error</strong> values upon timeout.
     *
     * @param duration the {@link Duration} to wait until elements are dropped
     * @return a strategy with an error on timeout behavior
     */
    static TimeoutEmissionStrategy timeoutError(Duration duration) {
        return new TimeoutEmissionStrategy(Duration.ofMillis(10).toNanos(), duration.toNanos(), true);
    }

    /**
     * Create an {@link EmissionStrategy} that will indefinitely park emissions on overflow or non-serialized
     * scenarios until it resolves, the emitter is cancelled or the sink is terminated.
     *
     * @param duration the {@link Duration} indicating how long to disable the emitting thread after each failed attempt
     * @return a strategy that awaits emissions on overflowing sinks
     */
    static EmissionStrategy park(Duration duration) {
        return new ParkEmissionStrategy(duration.toNanos());
    }

    /**
     * Try emitting a given {@code element} to the specified {@code sink}, respecting the semantics of
     * {@link reactor.core.publisher.Sinks.Many#tryEmitNext(Object)} and the failure handling of
     * {@link reactor.core.publisher.Sinks.Many#emitNext(Object, Sinks.EmitFailureHandler)}. Returns whether the emission was successful.
     * Implementations can throw unchecked exceptions like {@link reactor.core.publisher.Sinks.EmissionException} or perform side-effects
     * like waiting to determine a result.
     *
     * @param sink the target sink where this emission is attempted
     * @param element the element pushed to the sink
     * @param <T> the type associated with the sink and element
     * @return the result of the emission, {@code true} if the element was pushed to the sink, {@code false} otherwise
     */
    <T> boolean emitNext(Sinks.Many<T> sink, T element);

    /**
     * Try to terminate the given {@code sink} successfully, respecting the semantics of
     * {@link reactor.core.publisher.Sinks.Many#tryEmitComplete()} and the failure handling of
     * {@link reactor.core.publisher.Sinks.Many#emitComplete(Sinks.EmitFailureHandler)}. Returns whether the emission was successful.
     * Implementations can throw unchecked exceptions like {@link reactor.core.publisher.Sinks.EmissionException} or perform side-effects
     * like waiting to determine a result.
     *
     * @param sink the target sink where this emission is attempted
     * @param <T> the type associated with the sink and element
     * @return the result of the emission, {@code true} if the sink was terminated successfully, {@code false} otherwise
     */
    <T> boolean emitComplete(Sinks.Many<T> sink);

    /**
     * Try to fail the given {@code sink}, respecting the semantics of {@link reactor.core.publisher.Sinks.Many#tryEmitError(Throwable)} and
     * the failure handling of {@link reactor.core.publisher.Sinks.Many#emitError(Throwable, Sinks.EmitFailureHandler)}. Returns whether the
     * emission was successful. Implementations can throw unchecked exceptions like {@link reactor.core.publisher.Sinks.EmissionException}
     * or perform side-effects like waiting to determine a result.
     *
     * @param sink the target sink where this emission is attempted
     * @param error the exception to signal, non-null
     * @param <T> the type associated with the sink and element
     * @return the result of the emission, {@code true} if the failure was correctly emitted, {@code false} otherwise
     */
    <T> boolean emitError(Sinks.Many<T> sink, Throwable error);
}
