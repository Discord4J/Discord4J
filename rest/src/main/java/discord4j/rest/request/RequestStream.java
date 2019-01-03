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

import discord4j.rest.http.client.ClientException;
import discord4j.rest.http.client.ClientRequest;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.util.RouteUtils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.http.client.HttpClientResponse;
import reactor.retry.BackoffDelay;
import reactor.retry.IterationContext;
import reactor.retry.Retry;
import reactor.retry.RetryContext;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * A stream of {@link discord4j.rest.request.DiscordRequest DiscordRequests}. Any number of items may be
 * {@link #push(reactor.util.function.Tuple2)} written to the stream. However, the
 * {@link discord4j.rest.request.RequestStream.Reader reader} ensures that only one is read at a time. This
 * linearization ensures proper ratelimit handling.
 * <p>
 * The flow of a request through the stream is as follows:
 *
 * <img src="{@docRoot}img/RequestStream_Flow.png">
 *
 * @param <T> The type of items in the stream.
 */
class RequestStream<T> {

    private final EmitterProcessor<Tuple2<MonoProcessor<T>, DiscordRequest<T>>> backing =
            EmitterProcessor.create(false);
    private final BucketKey id;
    private final Logger log;
    private final DiscordWebClient httpClient;
    private final GlobalRateLimiter globalRateLimiter;
    private final RateLimitStrategy rateLimitStrategy;

    RequestStream(BucketKey id, DiscordWebClient httpClient, GlobalRateLimiter globalRateLimiter,
            RateLimitStrategy rateLimitStrategy) {
        this.id = id;
        this.log = Loggers.getLogger("discord4j.rest.request." + id);
        this.httpClient = httpClient;
        this.globalRateLimiter = globalRateLimiter;
        this.rateLimitStrategy = rateLimitStrategy;
    }

    /**
     * The retry function used for reading and completing HTTP requests. The backoff is determined by the ratelimit
     * headers returned by Discord in the event of a 429. If the bot is being globally ratelimited, the backoff is
     * applied to the global rate limiter. Otherwise, it is applied only to this stream.
     */
    private Retry<?> rateLimitRetryFactory() {
        return Retry.onlyIf(this::isRateLimitError).backoff(context -> {
            if (isRateLimitError(context)) {
                RetryContext<?> ctx = (RetryContext) context;
                ClientException clientException = (ClientException) ctx.exception();
                boolean global = Boolean.valueOf(clientException.getHeaders().get("X-RateLimit-Global"));
                long retryAfter = Long.valueOf(clientException.getHeaders().get("Retry-After"));
                Duration fixedBackoff = Duration.ofMillis(retryAfter);
                if (global) {
                    globalRateLimiter.rateLimitFor(fixedBackoff);
                }
                return new BackoffDelay(fixedBackoff);
            }
            return new BackoffDelay(Duration.ZERO);
        }).doOnRetry(ctx -> {
            if (log.isTraceEnabled()) {
                log.trace("Retry {} due to {} for {}", ctx.iteration(), ctx.exception().toString(), ctx.backoff());
            }
        });
    }

    /**
     * This retry function is used for reading and completing HTTP requests in the event of a server error (codes
     * 502, 503 and 504). The delay is calculated using exponential backoff with jitter.
     */
    private boolean isRateLimitError(IterationContext<?> context) {
        RetryContext<?> ctx = (RetryContext) context;
        Throwable exception = ctx.exception();
        if (exception instanceof ClientException) {
            ClientException clientException = (ClientException) exception;
            return clientException.getStatus().code() == 429;
        }
        return false;
    }

    private Retry<?> serverErrorRetryFactory() {
        return Retry.onlyIf(this::isServerError)
                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(30))
                .doOnRetry(ctx -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Retry {} due to {} for {}", ctx.iteration(), ctx.exception().toString(),
                                ctx.backoff());
                    }
                });
    }

    private boolean isServerError(IterationContext<?> context) {
        RetryContext<?> ctx = (RetryContext) context;
        Throwable exception = ctx.exception();
        if (exception instanceof ClientException) {
            ClientException clientException = (ClientException) exception;
            int code = clientException.getStatus().code();
            return code == 502 || code == 503 || code == 504;
        }
        return false;
    }

    void push(Tuple2<MonoProcessor<T>, DiscordRequest<T>> request) {
        backing.onNext(request);
    }

    void start() {
        read().subscribe(new Reader(rateLimitStrategy), t -> log.error("Error while consuming first request", t));
    }

    private Mono<Tuple2<MonoProcessor<T>, DiscordRequest<T>>> read() {
        return backing.next();
    }

    @FunctionalInterface
    interface RateLimitStrategy extends Function<HttpClientResponse, Duration> {
    }

    /**
     * Reads and completes one request from the stream at a time. If a request fails, it is retried according a retry
     * strategy. The reader may wait in between each request if preemptive ratelimiting is necessary according to the
     * response headers.
     *
     * @see #sleepTime
     * @see #rateLimitHandler
     */
    private class Reader implements Consumer<Tuple2<MonoProcessor<T>, DiscordRequest<T>>> {

        private volatile Duration sleepTime = Duration.ZERO;
        private final Consumer<HttpClientResponse> rateLimitHandler;

        private Reader(RateLimitStrategy strategy) {
            this.rateLimitHandler = response -> {
                if (log.isTraceEnabled()) {
                    log.trace("Read {} with headers: {}", response.status(), response.responseHeaders());
                }
                Duration nextReset = strategy.apply(response);
                if (!nextReset.isZero()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Delaying next request by {}", nextReset);
                    }
                    sleepTime = nextReset;
                }
            };
        }

        private Mono<ClientRequest> adaptRequest(DiscordRequest<?> req) {
            return Mono.fromCallable(() -> new ClientRequest(req.getRoute().getMethod(),
                    RouteUtils.expandQuery(req.getCompleteUri(), req.getQueryParams()),
                    Optional.ofNullable(req.getHeaders())
                            .map(map -> map.entrySet().stream()
                                    .reduce((HttpHeaders) new DefaultHttpHeaders(), (headers, entry) -> {
                                        String key = entry.getKey();
                                        entry.getValue().forEach(value -> headers.add(key, value));
                                        return headers;
                                    }, HttpHeaders::add))
                            .orElse(new DefaultHttpHeaders())));
        }

        @Override
        public void accept(Tuple2<MonoProcessor<T>, DiscordRequest<T>> tuple) {
            MonoProcessor<T> callback = tuple.getT1();
            DiscordRequest<T> req = tuple.getT2();
            if (log.isTraceEnabled()) {
                log.trace("Accepting request: {}", req);
            }
            Mono<ClientRequest> request = adaptRequest(req);
            Class<T> responseType = req.getRoute().getResponseType();

            globalRateLimiter.onComplete()
                    .then(request)
                    .log("discord4j.rest.request." + id, Level.FINEST)
                    .flatMap(r -> httpClient.exchange(r, req.getBody(), responseType, rateLimitHandler))
                    .retryWhen(rateLimitRetryFactory())
                    .retryWhen(serverErrorRetryFactory())
                    .log("discord4j.rest.response." + id, Level.FINEST)
                    .doFinally(signal -> Mono.delay(sleepTime).subscribe(l -> {
                        if (log.isTraceEnabled()) {
                            log.trace("Ready to consume next request");
                        }
                        sleepTime = Duration.ZERO;
                        read().subscribe(this, t -> log.error("Error while consuming request", t));
                    }, t -> log.error("Error while scheduling next request", t)))
                    .subscribeWith(callback);
        }
    }
}
