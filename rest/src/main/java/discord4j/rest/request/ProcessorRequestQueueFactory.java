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

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

import java.util.function.Supplier;

class ProcessorRequestQueueFactory implements RequestQueueFactory {

    private final Supplier<FluxProcessor<Object, Object>> processorSupplier;
    private final FluxSink.OverflowStrategy overflowStrategy;

    ProcessorRequestQueueFactory(Supplier<FluxProcessor<Object, Object>> processorSupplier,
                                 FluxSink.OverflowStrategy overflowStrategy) {
        this.processorSupplier = processorSupplier;
        this.overflowStrategy = overflowStrategy;
    }

    @Override
    public <T> RequestQueue<T> create() {
        return new RequestQueue<T>() {

            private final FluxProcessor<Object, Object> processor = processorSupplier.get();
            private final FluxSink<Object> sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);

            @Override
            public boolean push(T request) {
                sink.next(request);
                return true; // rejection happens on the requests() side via onDiscard handler
            }

            @SuppressWarnings("unchecked")
            @Override
            public Flux<T> requests() {
                return (Flux<T>) Flux.create(sink -> processor.subscribe(sink::next), overflowStrategy);
            }
        };
    }
}
