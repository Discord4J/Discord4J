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

package discord4j.common.sinks;

import reactor.core.publisher.Sinks;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.locks.LockSupport;

class TimeoutEmissionStrategy implements EmissionStrategy {

    private static final Logger log = Loggers.getLogger(TimeoutEmissionStrategy.class);

    private final long parkTimeoutNanos;
    private final long timeoutNanos;
    private final boolean errorOnTimeout;

    TimeoutEmissionStrategy(long parkTimeoutNanos, long timeoutNanos, boolean errorOnTimeout) {
        this.parkTimeoutNanos = parkTimeoutNanos;
        this.timeoutNanos = timeoutNanos;
        this.errorOnTimeout = errorOnTimeout;
    }

    @Override
    public <T> boolean emitNext(Sinks.Many<T> sink, T element) {
        long remaining = 0;
        if (timeoutNanos > 0) {
            remaining = timeoutNanos;
        }
        for (;;) {
            Sinks.EmitResult emission = sink.tryEmitNext(element);
            if (emission.isSuccess()) {
                return true;
            }
            remaining -= parkTimeoutNanos;
            if (timeoutNanos >= 0 && remaining <= 0) {
                log.debug("Emission timed out at {}: {}", sink.name(), element.toString());
                if (errorOnTimeout) {
                    throw new Sinks.EmissionException(emission, "Emission timed out");
                }
                return false;
            }
            switch (emission) {
                case FAIL_ZERO_SUBSCRIBER:
                case FAIL_CANCELLED:
                case FAIL_TERMINATED:
                    return false;
                case FAIL_NON_SERIALIZED:
                case FAIL_OVERFLOW:
                    log.debug("Emission overflowing at {}: {}", sink.name(), element.toString());
                    LockSupport.parkNanos(parkTimeoutNanos);
                    continue;
                default:
                    throw new Sinks.EmissionException(emission, "Unknown emitResult value");
            }
        }
    }

}
