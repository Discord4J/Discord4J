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

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

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
     */
    static RequestQueueFactory backedByProcessor(Supplier<FluxProcessor<Object, Object>> processorSupplier,
                                                 FluxSink.OverflowStrategy overflowStrategy) {
        return new ProcessorRequestQueueFactory(processorSupplier, overflowStrategy);
    }

    /**
     * Returns a factory of {@link RequestQueue} with default parameters capable of buffering requests in an
     * unbounded way.
     *
     * @return a {@link RequestQueueFactory} backed by an {@link EmitterProcessor} with a buffering configuration.
     */
    static RequestQueueFactory buffering() {
        return RequestQueueFactory.backedByProcessor(() -> EmitterProcessor.create(false),
                FluxSink.OverflowStrategy.BUFFER);
    }
}
