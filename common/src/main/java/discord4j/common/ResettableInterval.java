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

import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class ResettableInterval {

	private final EmitterProcessor<Long> backing = EmitterProcessor.create(false);
	private Disposable task;

	public void start(Duration duration) {
		task = Flux.interval(duration).subscribe(backing::onNext);
	}

	public void stop() {
		if (task == null) {
			throw new IllegalStateException("Emitter has not started!");
		}
		task.dispose();
	}

	public Flux<Long> ticks() {
		return backing;
	}
}
