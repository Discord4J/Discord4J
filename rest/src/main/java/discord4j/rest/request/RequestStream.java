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
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.response.ResponseFunction;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;
import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * A stream of {@link DiscordWebRequest DiscordRequests}. Any number of items may be {@link #push(RequestCorrelation)}
 * written to the stream. However, the {@link RequestSubscriber} ensures that only one is read at a time. This
 * serialization ensures proper rate limit handling.
 * <p>
 * The flow of a request through the stream is as follows:
 *
 * <img src="{@docRoot}img/RequestStream_Flow.png">
 */
class RequestStream {

    private static final Logger log = Loggers.getLogger(RequestStream.class);

    private final BucketKey id;
    private final RequestQueue<RequestCorrelation<ClientResponse>> requestQueue;
    private final GlobalRateLimiter globalRateLimiter;
    private final Scheduler timedTaskScheduler;
    private final List<ResponseFunction> responseFunctions;
    private final DiscordWebClient httpClient;
    private final RequestSubscriber requestSubscriber;
    private final RateLimitRetryOperator rateLimitRetryOperator;
    private final AtomicLong requestsInFlight = new AtomicLong(0);
    private final Sinks.Empty<?> stopCallback = Sinks.empty();

    RequestStream(BucketKey id, RouterOptions routerOptions, DiscordWebClient httpClient,
                  RateLimitStrategy rateLimitStrategy) {
        this.id = id;
        this.requestQueue = routerOptions.getRequestQueueFactory().create();
        this.globalRateLimiter = routerOptions.getGlobalRateLimiter();
        this.timedTaskScheduler = routerOptions.getReactorResources().getTimerTaskScheduler();
        this.responseFunctions = routerOptions.getResponseTransformers();
        this.httpClient = httpClient;
        this.requestSubscriber = new RequestSubscriber(rateLimitStrategy, requestsInFlight::decrementAndGet);
        this.rateLimitRetryOperator = new RateLimitRetryOperator(timedTaskScheduler);
    }

    /**
     * This retry function is used for reading and completing HTTP requests in the event of a server error (codes
     * 500, 502, 503 and 504). The delay is calculated using exponential backoff with jitter.
     */
    private reactor.util.retry.Retry serverErrorRetryFactory() {
        return RetryBackoffSpec.backoff(10, Duration.ofSeconds(2))
                .filter(ex -> {
                    if (ex instanceof ClientException) {
                        int code = ((ClientException) ex).getStatus().code();
                        return code == 500 || code == 502 || code == 503 || code == 504;
                    }
                    return false;
                })
                .jitter(0.5)
                .maxBackoff(Duration.ofSeconds(30))
                .scheduler(timedTaskScheduler)
                .doBeforeRetry(retrySignal -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Retry {} in bucket {} due to {}",
                                retrySignal.totalRetries(),
                                id.toString(),
                                retrySignal.failure().toString());
                    }
                });
    }

    boolean push(RequestCorrelation<ClientResponse> request) {
        requestsInFlight.incrementAndGet();
        boolean accepted = requestQueue.push(request);
        if (!accepted) {
            requestsInFlight.decrementAndGet();
        }
        return accepted;
    }

    void start() {
        requestQueue.requests()
                .doOnDiscard(RequestCorrelation.class, this::onDiscard)
                .takeUntilOther(stopCallback.asMono())
                .subscribe(requestSubscriber);
    }

    void stop() {
        stopCallback.emitEmpty(FAIL_FAST);
    }

    /**
     * If we exhausted ratelimits, this holds the point-in-time when the ratelimits will reset again.
     */
    Instant getResetAt() {
        return requestSubscriber.getResetAt();
    }

    /**
     * @return the sum of requests still in the queue, as well as any potential request being processed or waiting for ratelimits to reset
     */
    long countRequestsInFlight() {
        return requestsInFlight.get();
    }

    private void onDiscard(RequestCorrelation<?> requestCorrelation) {
        requestsInFlight.decrementAndGet();
        requestCorrelation.getResponse()
                .emitError(new DiscardedRequestException(requestCorrelation.getRequest()), FAIL_FAST);
    }

    /**
     * Reads and completes one request from the stream at a time. If a request fails, it is retried according a retry
     * strategy. The reader may wait in between each request if preemptive rate limiting is necessary according to the
     * response headers.
     */
    private class RequestSubscriber extends BaseSubscriber<RequestCorrelation<ClientResponse>> {

        private volatile Instant resetAt = Instant.EPOCH;
        private final Function<ClientResponse, Mono<ClientResponse>> responseFunction;
        private final Runnable processedCallback;

        public Instant getResetAt() {
            return resetAt;
        }

        public RequestSubscriber(RateLimitStrategy strategy, Runnable processedCallback) {
            this.processedCallback = processedCallback;
            this.responseFunction = response -> {
                HttpClientResponse httpResponse = response.getHttpResponse();
                if (log.isDebugEnabled()) {
                    Instant requestTimestamp =
                            Instant.ofEpochMilli(httpResponse.currentContextView().get(DiscordWebClient.KEY_REQUEST_TIMESTAMP));
                    Duration responseTime = Duration.between(requestTimestamp, Instant.now());
                    LogUtil.traceDebug(log, trace -> format(httpResponse.currentContextView(),
                            "Read " + httpResponse.status() + " in " + responseTime + (!trace ? "" :
                                    " with headers: " + httpResponse.responseHeaders())));
                }
                Duration resetAfter = strategy.apply(httpResponse);
                if (!resetAfter.isZero()) {
                    if (log.isDebugEnabled()) {
                        log.debug(format(httpResponse.currentContextView(), "Delaying next request by {}"), resetAfter);
                    }
                    resetAt = Instant.now().plus(resetAfter);
                }
                boolean global = Boolean.parseBoolean(httpResponse.responseHeaders().get("X-RateLimit-Global"));
                Mono<Void> action = Mono.empty();
                if (global) {
                    long retryAfter = Long.parseLong(httpResponse.responseHeaders().get("Retry-After"));
                    Duration fixedBackoff = Duration.ofSeconds(retryAfter);
                    action = globalRateLimiter.rateLimitFor(fixedBackoff)
                            .doOnTerminate(() -> log.debug(format(httpResponse.currentContextView(),
                                    "Globally rate limited for {}"), fixedBackoff));
                }
                if (httpResponse.status().code() >= 400) {
                    return action.then(response.createException().flatMap(Mono::error));
                } else {
                    return action.thenReturn(response);
                }
            };
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(1);
        }

        @Override
        protected void hookOnNext(RequestCorrelation<ClientResponse> correlation) {
            DiscordWebRequest request = correlation.getRequest();
            ClientRequest clientRequest = new ClientRequest(request);
            Sinks.One<ClientResponse> callback = correlation.getResponse();

            Mono.just(clientRequest)
                    .flatMap(req -> Mono.deferContextual(ctx -> {
                        LogUtil.traceDebug(log, trace -> format(ctx, trace ? req.toString() : req.getDescription()));
                        return globalRateLimiter.withLimiter(httpClient.exchange(req).flatMap(responseFunction)).next();
                    }))
                    .contextWrite(ctx -> ctx.putAll(correlation.getContext())
                            .put(LogUtil.KEY_REQUEST_ID, clientRequest.getId())
                            .put(LogUtil.KEY_BUCKET_ID, id.toString()))
                    .retryWhen(Retry.withThrowable(rateLimitRetryOperator::apply))
                    .transform(getResponseTransformers(request))
                    .retryWhen(serverErrorRetryFactory())
                    .takeUntilOther(correlation.onCancel())
                    .doFinally(this::next)
                    .checkpoint("Request to " + clientRequest.getDescription() + " [RequestStream]")
                    .subscribe(
                            response -> callback.emitValue(response, FAIL_FAST),
                            t -> {
                                log.trace("Error while processing {}: {}", request, t);
                                callback.emitError(t, FAIL_FAST);
                            },
                            () -> callback.emitEmpty(FAIL_FAST));
        }

        private Function<Mono<ClientResponse>, Mono<ClientResponse>> getResponseTransformers(DiscordWebRequest discordRequest) {
            return responseFunctions.stream()
                    .map(rt -> rt.transform(discordRequest)
                            .andThen(mono -> mono.checkpoint("Apply " + rt + " to " +
                                    discordRequest.getDescription() + " [RequestStream]")))
                    .reduce(Function::andThen)
                    .orElse(mono -> mono);
        }

        private void next(SignalType signal) {
            Duration wait = Duration.between(Instant.now(), resetAt);
            Mono<Long> timer = wait.isNegative() || wait.isZero() ? Mono.just(0L) : Mono.delay(wait, timedTaskScheduler);
            timer
                .doFinally(__ -> processedCallback.run())
                .subscribe(l -> {
                    if (log.isDebugEnabled()) {
                        log.debug("[B:{}] Ready to consume next request after {}", id.toString(), signal);
                    }
                    request(1);
                }, t -> log.error("[B:{}] Error while scheduling next request", id.toString(), t));
        }

        @Override
        protected void hookOnComplete() {
            log.debug("[B:{}] RequestStream completed", id.toString());
        }
    }
}
