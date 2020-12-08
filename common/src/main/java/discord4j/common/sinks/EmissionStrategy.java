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

import discord4j.common.annotations.Experimental;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Experimental
public interface EmissionStrategy {

    static TimeoutEmissionStrategy timeoutDrop(Duration duration) {
        return new TimeoutEmissionStrategy(Duration.ofMillis(10).toNanos(), duration.toNanos(), false);
    }

    static TimeoutEmissionStrategy timeoutError(Duration duration) {
        return new TimeoutEmissionStrategy(Duration.ofMillis(10).toNanos(), duration.toNanos(), true);
    }

    static EmissionStrategy park(Duration duration) {
        return new TimeoutEmissionStrategy(duration.toNanos(), 0L, false);
    }

    <T> boolean emitNext(Sinks.Many<T> sink, T element);
}
