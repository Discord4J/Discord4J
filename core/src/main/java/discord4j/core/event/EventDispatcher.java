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

package discord4j.core.event;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.scheduler.Scheduler;

public class EventDispatcher {

	private final FluxProcessor<Event, Event> processor;
	private final Scheduler scheduler;

	public EventDispatcher(FluxProcessor<Event, Event> processor, Scheduler scheduler) {
		this.processor = processor;
		this.scheduler = scheduler;
	}

	public <T extends Event> Flux<T> on(Class<T> eventClass) {
		return processor.publishOn(scheduler).ofType(eventClass);
	}
}
