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

import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Supplier;

public class PoolingTransformer implements PayloadTransformer {

    private final BucketPool pool;

    public PoolingTransformer(int capacity, Duration refillPeriod) {
        this.pool = new BucketPool(capacity, refillPeriod);
    }

    @Override
    public Publisher<ByteBuf> apply(Flux<ByteBuf> sequence, Supplier<Duration> supplier) {
        return sequence.flatMap(t2 -> this.pool.acquire(supplier.get()).thenReturn(t2));
    }
}
