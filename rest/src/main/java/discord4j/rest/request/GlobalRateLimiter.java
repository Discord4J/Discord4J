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
package discord4j.rest.request;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Used to prevent requests from being sent while the bot is
 * <a href="https://discordapp.com/developers/docs/topics/rate-limits#exceeding-a-rate-limit">globally ratelimited</a>.
 * <p>
 * When subscribing to the rate limiter, the only guarantee is that the subscription will be completed at some point in
 * the future. If a global ratelimit is in effect, it will be completed when the cooldown ends. Otherwise, it is
 * completed immediately.
 */
public class GlobalRateLimiter implements Publisher<Void> {

	private static final Object PERMIT = new Object();

	private final EmitterProcessor<Object> resetNotifier = EmitterProcessor.create(false);
	private volatile boolean isRateLimited;
	private final Flux<Void> flux = Flux.create(sink -> sink.onRequest(l -> {
		if (isRateLimited) {
			resetNotifier.next().subscribe(o -> sink.complete());
		} else {
			sink.complete();
		}
	}));

	/**
	 * Prevents the rate limiter from completing subscriptions for the given duration.
	 *
	 * @param duration The duration to prevent completions for.
	 */
	void rateLimitFor(Duration duration) {
		isRateLimited = true;
		Mono.delay(duration).subscribe(l -> {
			isRateLimited = false;
			resetNotifier.onNext(PERMIT);
		});
	}

	@Override
	public void subscribe(Subscriber<? super Void> s) {
		flux.subscribe(s);
	}
}
