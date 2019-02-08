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

package discord4j.common;

import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

public class SimpleBucketTest {

    @Test
    public void testReactiveBucket() {
        RateLimiter limiter = new SimpleBucket(60, Duration.ofSeconds(30));

        EmitterProcessor<Integer> outbound = EmitterProcessor.create();
        FluxSink<Integer> sender = outbound.sink();

        int requests = 200;

        outbound.concatMap(t -> Mono.defer(() -> Mono.delay(Duration.ofMillis(limiter.delayMillisToConsume(1)))
                .map(tick -> limiter.tryConsume(1))
                .flatMap(consumed -> {
                    if (!consumed) {
                        return Mono.error(new RuntimeException());
                    }
                    return Mono.just(t);
                }))
                .retry())
                .subscribe();

        StepVerifier.withVirtualTime(() ->
                Flux.range(0, requests)
                        .parallel()
                        .doOnNext(sender::next)
                        .sequential())
                .expectNextCount(requests)
                .expectComplete()
                .verify();
    }

}
