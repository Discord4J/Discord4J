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
import java.util.function.Supplier;

class TimeoutEmissionStrategy implements EmissionStrategy {

    private static final Logger log = Loggers.getLogger(TimeoutEmissionStrategy.class);

    private final long parkNanos;
    private final long timeoutNanos;
    private final boolean errorOnTimeout;

    TimeoutEmissionStrategy(long parkNanos, long timeoutNanos, boolean errorOnTimeout) {
        this.parkNanos = parkNanos;
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
            remaining -= parkNanos;
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
                    LockSupport.parkNanos(parkNanos);
                    continue;
                case FAIL_OVERFLOW:
                    log.trace("Emission overflowing at {}: {}", sink.name(), element.toString());
                    LockSupport.parkNanos(parkNanos);
                    continue;
                default:
                    throw new Sinks.EmissionException(emission, "Unknown emitResult value");
            }
        }
    }

    @Override
    public <T> boolean emitComplete(Sinks.Many<T> sink) {
        return emitTerminal(sink::tryEmitComplete);
    }

    @Override
    public <T> boolean emitError(Sinks.Many<T> sink, Throwable error) {
        return emitTerminal(() -> sink.tryEmitError(error));
    }

    private <T> boolean emitTerminal(Supplier<Sinks.EmitResult> resultSupplier) {
        long remaining = 0;
        if (timeoutNanos > 0) {
            remaining = timeoutNanos;
        }
        for (;;) {
            Sinks.EmitResult emission = resultSupplier.get();
            if (emission.isSuccess()) {
                return true;
            }
            remaining -= parkNanos;
            if (timeoutNanos >= 0 && remaining <= 0) {
                if (errorOnTimeout) {
                    throw new Sinks.EmissionException(emission, "Emission timed out");
                }
                return false;
            }
            switch (emission) {
                case FAIL_ZERO_SUBSCRIBER:
                case FAIL_CANCELLED:
                case FAIL_TERMINATED:
                case FAIL_OVERFLOW:
                    return false;
                case FAIL_NON_SERIALIZED:
                    LockSupport.parkNanos(parkNanos);
                    continue;
                default:
                    throw new Sinks.EmissionException(emission, "Unknown emitResult value");
            }
        }
    }
}
