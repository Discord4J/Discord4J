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

import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class TokenBucketTest {

	private static final Logger log = Loggers.getLogger(TokenBucketTest.class);

	@Test
	public void testReactiveBucket() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		TokenBucket limiter = new TokenBucket(120, Duration.ofSeconds(60));

		EmitterProcessor<Long> outbound = EmitterProcessor.create();
		FluxSink<Long> sender = outbound.sink();

		int requests = 150;

		outbound.log().concatMap(t -> {
			boolean success = limiter.tryConsume(1);
			if (success) {
				return Mono.just(t);
			} else {
				return Mono.delay(Duration.ofMillis(limiter.delayMillisToConsume(1)))
						.map(x -> limiter.tryConsume(1))
						.publishOn(Schedulers.elastic())
						.map(consumed -> t);
			}
		}).subscribe(t -> {
			log.info("Got {}", t);
			if (t == requests) {
				latch.countDown();
			}
		});
		Mono.fromCallable(() -> {
			long i = 1;
			while (i <= requests) {
				sender.next(i++);
			}
			sender.complete();
			return "OK";
		}).subscribeOn(Schedulers.elastic()).subscribe();
		latch.await();
	}

}
