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

package discord4j.rest.request;

import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpHeaders;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import java.time.Duration;

/**
 * The retry function used for reading and completing HTTP requests. The backoff is determined by the rate limit
 * headers returned by Discord in the event of a 429.
 */
public class RateLimitRetryOperator {

    private static final Logger log = Loggers.getLogger(RateLimitRetryOperator.class);

    private final Scheduler backoffScheduler;

    public RateLimitRetryOperator(Scheduler backoffScheduler) {
        this.backoffScheduler = backoffScheduler;
    }

    public Publisher<Context> apply(Flux<Throwable> errors) {
        return errors.index().concatMap(tuple -> retry(tuple.getT2(), tuple.getT1() + 1L));
    }

    private Publisher<Context> retry(Throwable error, long iteration) {
        if (!isRateLimitError(error)) {
            return Mono.error(error);
        } else {
            ClientException clientException = (ClientException) error;
            HttpHeaders headers = clientException.getHeaders();
            try {
                boolean global = Boolean.parseBoolean(headers.get("X-RateLimit-Global"));
                Context context = Context.of("iteration", iteration);
                String retryAfter = headers.get("Retry-After");
                String resetAfter = headers.get("X-RateLimit-Reset-After");
                if (global) {
                    Duration fixedBackoff = Duration.ofSeconds(Long.parseLong(retryAfter));
                    return retryMono(fixedBackoff).thenReturn(context);
                } else if (resetAfter != null) {
                    long resetAt = (long) (Double.parseDouble(resetAfter) * 1000);
                    Duration fixedBackoff = Duration.ofMillis(resetAt);
                    return retryMono(fixedBackoff).thenReturn(context);
                } else {
                    Duration fixedBackoff = Duration.ofSeconds(Long.parseLong(retryAfter));
                    return retryMono(fixedBackoff).thenReturn(context);
                }
            } catch (Exception e) {
                log.error("Unable to parse rate limit headers: {}", headers);
                return Mono.error(e);
            }
        }
    }

    private boolean isRateLimitError(Throwable error) {
        if (error instanceof ClientException) {
            ClientException clientException = (ClientException) error;
            return clientException.getStatus().code() == 429;
        }
        return false;
    }

    private Mono<Long> retryMono(Duration delay) {
        if (delay == Duration.ZERO) {
            return Mono.just(0L);
        } else {
            return Mono.delay(delay, backoffScheduler);
        }
    }
}
