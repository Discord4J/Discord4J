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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.function.Function;

class SinksRequestQueueFactory implements RequestQueueFactory {

    private static final Logger log = Loggers.getLogger(SinksRequestQueueFactory.class);

    private final Function<Sinks.ManySpec, Sinks.Many<Object>> requestSinkFactory;
    private final EmissionStrategy emissionStrategy;

    SinksRequestQueueFactory(Function<Sinks.ManySpec, Sinks.Many<Object>> requestSinkFactory,
                             EmissionStrategy emissionStrategy) {
        this.requestSinkFactory = requestSinkFactory;
        this.emissionStrategy = emissionStrategy;
    }

    @Override
    public <T> RequestQueue<T> create() {
        return new RequestQueue<T>() {

            private final Sinks.Many<Object> sink = requestSinkFactory.apply(Sinks.many());

            @Override
            public boolean push(T request) {
                return emissionStrategy.emitNext(sink, request);
            }

            @SuppressWarnings("unchecked")
            @Override
            public Flux<T> requests() {
                return (Flux<T>) sink.asFlux();
            }
        };
    }
}
