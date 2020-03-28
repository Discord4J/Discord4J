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

import discord4j.gateway.limiter.BucketPool;
import discord4j.gateway.limiter.SupplierTransformer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Supplier;

public class BucketPoolTransformer<T> implements SupplierTransformer<T, Duration, T> {

    private final BucketPool pool;

    public BucketPoolTransformer(int capacity, Duration refillPeriod) {
        this.pool = new BucketPool(capacity, refillPeriod);
    }

    @Override
    public Publisher<T> apply(Flux<T> sequence, Supplier<Duration> supplier) {
        return sequence.flatMap(t2 -> this.pool.acquire(supplier.get()).thenReturn(t2));
    }
}
