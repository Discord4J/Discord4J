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

import discord4j.common.operator.RateLimitOperator;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class RateLimitTransformer implements PayloadTransformer {

    private final RateLimitOperator<ByteBuf> operator;

    public RateLimitTransformer(int capacity, Duration refillPeriod) {
        this(capacity, refillPeriod, Schedulers.parallel());
    }

    public RateLimitTransformer(int capacity, Duration refillPeriod, Scheduler delayScheduler) {
        this.operator = new RateLimitOperator<>(capacity, refillPeriod, delayScheduler);
    }

    @Override
    public Publisher<ByteBuf> apply(Publisher<ByteBuf> sequence) {
        return operator.apply(sequence);
    }
}
