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

import discord4j.common.LogUtil;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.http.client.ClientRequest;
import discord4j.rest.http.client.DiscordWebClient;
import org.reactivestreams.Subscription;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClientResponse;
import reactor.retry.BackoffDelay;
import reactor.retry.IterationContext;
import reactor.retry.Retry;
import reactor.retry.RetryContext;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

/**
 * A stream of {@link DiscordRequest DiscordRequests}. Any number of items may be {@link #push(RequestCorrelation)}
 * written to the stream. However, the {@link RequestSubscriber} ensures that only one is read at a time. This
 * serialization ensures proper rate limit handling.
 * <p>
 * The flow of a request through the stream is as follows:
 *
 * <img src="{@docRoot}img/RequestStream_Flow.png">
 *
 * @param <T> The type of items in the stream.
 */
class RequestStream<T> {

    private final EmitterProcessor<RequestCorrelation<T>> backing = EmitterProcessor.create(false);
    private final BucketKey id;
    private final DiscordWebClient httpClient;
    private final GlobalRateLimiter globalRateLimiter;
    private final RateLimitStrategy rateLimitStrategy;
    private final Scheduler rateLimitScheduler;
    private final RouterOptions routerOptions;

    RequestStream(BucketKey id, DiscordWebClient httpClient, GlobalRateLimiter globalRateLimiter,
                  RateLimitStrategy rateLimitStrategy, Scheduler rateLimitScheduler, RouterOptions routerOptions) {
        this.id = id;
        this.httpClient = httpClient;
        this.globalRateLimiter = globalRateLimiter;
        this.rateLimitStrategy = rateLimitStrategy;
        this.rateLimitScheduler = rateLimitScheduler;
        this.routerOptions = routerOptions;
    }

    /**
     * The retry function used for reading and completing HTTP requests. The backoff is determined by the rate limit
     * headers returned by Discord in the event of a 429. If the bot is being globally rate limited, the backoff is
     * applied to the global rate limiter. Otherwise, it is applied only to this stream.
     */
    private Retry<?> rateLimitRetryFactory() {
        return Retry.onlyIf(this::isRateLimitError).backoff(context -> {
            if (isRateLimitError(context)) {
                RetryContext<?> ctx = (RetryContext) context;
                ClientException clientException = (ClientException) ctx.exception();
                boolean global = Boolean.parseBoolean(clientException.getHeaders().get("X-RateLimit-Global"));
                long retryAfter = Long.parseLong(clientException.getHeaders().get("Retry-After"));
                Duration fixedBackoff = Duration.ofMillis(retryAfter);
                if (global) {
                    Duration remaining = globalRateLimiter.getRemaining();
                    if (!remaining.isNegative() && !remaining.isZero()) {
                        return new BackoffDelay(remaining);
                    }
                    log.debug("Globally rate limited for {}", fixedBackoff);
                    globalRateLimiter.rateLimitFor(fixedBackoff);
                }
                return new BackoffDelay(fixedBackoff);
            }
            return new BackoffDelay(Duration.ZERO);
        }).doOnRetry(ctx -> {
            if (log.isDebugEnabled()) {
                log.debug("Retry {} in {} due to {} for {}",
                        ctx.iteration(), id.toString(), ctx.exception().toString(), ctx.backoff());
            }
        });
    }

    private boolean isRateLimitError(IterationContext<?> context) {
        RetryContext<?> ctx = (RetryContext) context;
        Throwable exception = ctx.exception();
        if (exception instanceof ClientException) {
            ClientException clientException = (ClientException) exception;
            return clientException.getStatus().code() == 429;
        }
        return false;
    }

    /**
     * This retry function is used for reading and completing HTTP requests in the event of a server error (codes
     * 500, 502, 503 and 504). The delay is calculated using exponential backoff with jitter.
     */
    private Retry<?> serverErrorRetryFactory() {
        return Retry.onlyIf(ClientException.isRetryContextStatusCode(500, 502, 503, 504))
                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(30))
                .doOnRetry(ctx -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Retry {} in bucket {} due to {} for {}",
                                ctx.iteration(), id.toString(), ctx.exception().toString(), ctx.backoff());
                    }
                });
    }

    void push(RequestCorrelation<T> request) {
        backing.onNext(request);
    }

    void start() {
        backing.subscribe(new RequestSubscriber(rateLimitStrategy));
    }

    /**
     * Reads and completes one request from the stream at a time. If a request fails, it is retried according a retry
     * strategy. The reader may wait in between each request if preemptive rate limiting is necessary according to the
     * response headers.
     *
     * @see #sleepTime
     * @see #rateLimitHandler
     */
    private class RequestSubscriber extends BaseSubscriber<RequestCorrelation<T>> {

        private volatile Duration sleepTime = Duration.ZERO;
        private final Consumer<HttpClientResponse> rateLimitHandler;

        public RequestSubscriber(RateLimitStrategy strategy) {
            this.rateLimitHandler = response -> {
                if (log.isDebugEnabled()) {
                    Instant requestTimestamp =
                            Instant.ofEpochMilli(response.currentContext().get(DiscordWebClient.KEY_REQUEST_TIMESTAMP));
                    Duration responseTime = Duration.between(requestTimestamp, Instant.now());
                    log.debug(format(response.currentContext(), "Read {} in {} with headers: {}"),
                            response.status(), responseTime, response.responseHeaders());
                }
                Duration nextReset = strategy.apply(response);
                if (!nextReset.isZero()) {
                    if (log.isDebugEnabled()) {
                        log.debug(format(response.currentContext(), "Delaying next request by {}"), nextReset);
                    }
                    sleepTime = nextReset;
                }
            };
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(1);
        }

        @Override
        protected void hookOnNext(RequestCorrelation<T> correlation) {
            DiscordRequest<T> request = correlation.getRequest();
            MonoProcessor<T> callback = correlation.getResponse();

            if (tracesLog.isDebugEnabled()) {
                tracesLog.debug("Accepting request in bucket {}: {}", id.toString(), request);
            }

            Class<T> responseType = request.getRoute().getResponseType();

            globalRateLimiter.withLimiter(
                    Mono.fromCallable(() -> new ClientRequest(request))
                            .doOnEach(s -> requestLog.debug(format(s.getContext(), "{}"), s))
                            .flatMap(r -> httpClient.exchange(r, request.getBody(), responseType, rateLimitHandler))
                            .doOnEach(s -> responseLog.debug(format(s.getContext(), "{}"), s))
                            .subscriberContext(ctx -> ctx
                                    .putAll(correlation.getContext())
                                    .put(LogUtil.KEY_BUCKET_ID, id.toString()))
                            .retryWhen(rateLimitRetryFactory())
                            .transform(getResponseTransformers(request))
                            .retryWhen(serverErrorRetryFactory())
                            .doFinally(this::next))
                    .materialize()
                    .subscribe(signal -> {
                        if (signal.isOnSubscribe()) {
                            callback.onSubscribe(signal.getSubscription());
                        } else if (signal.isOnNext()) {
                            callback.onNext(signal.get());
                        } else if (signal.isOnError()) {
                            callback.onError(signal.getThrowable());
                        } else if (signal.isOnComplete()) {
                            callback.onComplete();
                        }
                    });
        }

        private Function<Mono<T>, Mono<T>> getResponseTransformers(DiscordRequest<T> discordRequest) {
            return routerOptions.getResponseTransformers()
                    .stream()
                    .map(rt -> rt.transform(discordRequest))
                    .reduce(Function::andThen)
                    .orElse(mono -> mono);
        }

        private void next(SignalType signal) {
            Mono.delay(sleepTime, rateLimitScheduler).subscribe(l -> {
                if (tracesLog.isDebugEnabled()) {
                    tracesLog.debug("Ready to consume next request in bucket {} after {}", id.toString(), signal);
                }
                sleepTime = Duration.ZERO;
                request(1);
            }, t -> tracesLog.error("Error while scheduling next request in bucket {}", id.toString(), t));
        }
    }

    @FunctionalInterface
    interface RateLimitStrategy extends Function<HttpClientResponse, Duration> {
    }

    private static final Logger log = Loggers.getLogger("discord4j.rest");
    private static final Logger tracesLog = Loggers.getLogger("discord4j.rest.traces");
    private static final Logger requestLog = Loggers.getLogger("discord4j.rest.request");
    private static final Logger responseLog = Loggers.getLogger("discord4j.rest.response");
}
