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
package discord4j.gateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.time.Duration;

public class ResettableInterval {

	private volatile boolean started = false;
	private final UnicastProcessor<Object> processor = UnicastProcessor.create();
	private final Duration interval;

	public ResettableInterval(Duration interval) {
		this.interval = interval;
	}

	public void start() {
		if (started) throw new IllegalStateException();
		started = true;

		Flux.interval(interval).subscribe(processor::onNext);
	}

	public void reset() {
		started = false;
	}

	public Flux<Object> out() {
		return processor;
	}
}
