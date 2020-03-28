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

package discord4j.gateway.limiter;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * A transformation function to a sequence with a supplied value on apply.
 */
@FunctionalInterface
public interface SupplierTransformer<T, U, V> {

    /**
     * Transform a sequence, along with a supplied value.
     *
     * @param sequence a sequence of payloads
     * @param supplier a supplied value to enhance the applied transformation
     * @return the transformed sequence
     */
    Publisher<V> apply(Flux<T> sequence, Supplier<U> supplier);
}
