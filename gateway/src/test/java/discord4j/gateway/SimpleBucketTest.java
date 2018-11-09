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

package discord4j.gateway;

import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleBucketTest {

    private static final Logger log = Loggers.getLogger(SimpleBucketTest.class);

    @Test
    @Ignore
    public void testReactiveBucket() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        GatewayLimiter limiter = new SimpleBucket(60, Duration.ofSeconds(30));

        EmitterProcessor<Integer> outbound = EmitterProcessor.create();
        FluxSink<Integer> sender = outbound.sink();
        AtomicInteger received = new AtomicInteger();

        int requests = 200;

        outbound.concatMap(t -> Mono.defer(() -> Mono.delay(Duration.ofMillis(limiter.delayMillisToConsume(1)))
                        .map(tick -> limiter.tryConsume(1))
                        .flatMap(consumed -> {
                            if (!consumed) {
                                log.info("Retrying...");
                                return Mono.error(new RuntimeException());
                            }
                            return Mono.just(t);
                        }))
                        .retry())
                .subscribe(t -> {
                    log.info("Got {}", t);
                    if (received.incrementAndGet() == requests) {
                        latch.countDown();
                    }
                });
        Flux.range(0, requests)
                .parallel()
                .doOnNext(sender::next)
                .sequential()
                .subscribeOn(Schedulers.elastic())
                .subscribe();
        latch.await();
    }

}
