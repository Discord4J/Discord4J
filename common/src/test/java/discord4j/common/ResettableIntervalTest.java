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
package discord4j.common;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class ResettableIntervalTest {

    @Test
    public void test() {
        ResettableInterval interval = new ResettableInterval(Schedulers.boundedElastic());
        StepVerifier.withVirtualTime(() -> interval.ticks()
                .doOnSubscribe(s -> interval.start(Duration.ZERO, Duration.ofSeconds(1))))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(0L)
                .then(interval::stop)
                .then(() -> interval.start(Duration.ZERO, Duration.ofSeconds(2)))
                .expectNoEvent(Duration.ofSeconds(2))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

}
