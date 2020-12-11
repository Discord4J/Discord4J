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
package discord4j.rest.request;

import discord4j.common.sinks.EmissionStrategy;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Factory to create {@link RequestQueue} instances.
 */
public interface RequestQueueFactory {

    /**
     * Creates a new {@link RequestQueue} instance.
     *
     * @param <T> the desired generic type of {@link RequestQueue}
     * @return the freshly instantiated {@link RequestQueue}
     */
    <T> RequestQueue<T> create();

    /**
     * Returns a factory of {@link RequestQueue} backed by a {@link FluxProcessor}.
     *
     * @param processorSupplier a Supplier that provides a processor. The Supplier <b>must</b> provide a new instance
     * every time it is called, and the processor must not be pre-filled with any elements, otherwise it may lead to
     * non-deterministic behavior.
     * @param overflowStrategy the overflow strategy to apply on the processor
     * @return a {@link RequestQueueFactory} backed by a {@link FluxProcessor}
     * @deprecated use the new Sinks API based approach at {@link #createFromSink(Function, EmissionStrategy)}
     */
    @Deprecated
    static RequestQueueFactory backedByProcessor(Supplier<FluxProcessor<Object, Object>> processorSupplier,
                                                 FluxSink.OverflowStrategy overflowStrategy) {
        return new ProcessorRequestQueueFactory(processorSupplier, overflowStrategy);
    }

    /**
     * Returns a factory of {@link RequestQueue} backed by a {@link Sinks.Many} instance.
     *
     * @param requestSinkFactory a Function that provides a sink. The factory <b>must</b> provide a new instance
     * every time it is called, and the processor must not be pre-filled with any elements, otherwise it may lead to
     * non-deterministic behavior.
     * @param emissionStrategy the strategy to handle request submission (emission) failures
     * @return a {@link RequestQueueFactory} backed by a {@link Sinks.Many}
     */
    static RequestQueueFactory createFromSink(Function<Sinks.ManySpec, Sinks.Many<Object>> requestSinkFactory,
                                              EmissionStrategy emissionStrategy) {
        return new SinksRequestQueueFactory(requestSinkFactory, emissionStrategy);
    }

    /**
     * Returns a factory of {@link RequestQueue} with default parameters capable of buffering requests up to a
     * reasonable capacity, then applying a delay on overflowing requests.
     *
     * @return a {@link RequestQueueFactory} backed by a multicasting {@link Sinks.Many} with capacity given by
     * {@link Queues#SMALL_BUFFER_SIZE} and a parking {@link EmissionStrategy}.
     */
    static RequestQueueFactory buffering() {
        return RequestQueueFactory.createFromSink(
                spec -> spec.multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false),
                EmissionStrategy.park(Duration.ofMillis(10)));
    }
}
