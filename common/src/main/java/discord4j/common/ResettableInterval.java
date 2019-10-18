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
import reactor.core.Disposables;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

public class ResettableInterval {

    private final Scheduler scheduler;
    private final Disposable.Swap task;
    private final EmitterProcessor<Long> backing = EmitterProcessor.create(false);

    public ResettableInterval(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.task = Disposables.swap();
    }

    public void start(Duration period) {
        this.task.update(Flux.interval(Duration.ZERO, period, scheduler).subscribe(backing::onNext));
    }

    public void stop() {
        task.get().dispose();
    }

    public Flux<Long> ticks() {
        return backing;
    }
}
