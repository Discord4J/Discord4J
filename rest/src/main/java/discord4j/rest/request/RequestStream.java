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
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClientResponse;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

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
    private final RateLimitStrategy rateLimitStrategy;
    private final RateLimitRetryOperator rateLimitRetryOperator;

    RequestStream(BucketKey id, RouterOptions routerOptions, DiscordWebClient httpClient,
                  RateLimitStrategy rateLimitStrategy) {
        this.id = id;
        this.requestQueue = routerOptions.getRequestQueueFactory().create();
        this.globalRateLimiter = routerOptions.getGlobalRateLimiter();
        this.timedTaskScheduler = routerOptions.getReactorResources().getTimerTaskScheduler();
        this.responseFunctions = routerOptions.getResponseTransformers();
        this.httpClient = httpClient;
        this.rateLimitStrategy = rateLimitStrategy;
        this.rateLimitRetryOperator = new RateLimitRetryOperator(timedTaskScheduler);
    }

    /**
     * This retry function is used for reading and completing HTTP requests in the event of a server error (codes
     * 500, 502, 503 and 504). The delay is calculated using exponential backoff with jitter.
     */
    private Retry<?> serverErrorRetryFactory() {
        return Retry.onlyIf(ClientException.isRetryContextStatusCode(500, 502, 503, 504))
                .withBackoffScheduler(timedTaskScheduler)
                .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofSeconds(30))
                .doOnRetry(ctx -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Retry {} in bucket {} due to {} for {}",
                                ctx.iteration(), id.toString(), ctx.exception().toString(), ctx.backoff());
                    }
                });
    }

    void push(RequestCorrelation<ClientResponse> request) {
        requestQueue.push(request);
    }

    void start() {
        requestQueue.requests().subscribe(new RequestSubscriber(rateLimitStrategy));
    }

    /**
     * Reads and completes one request from the stream at a time. If a request fails, it is retried according a retry
     * strategy. The reader may wait in between each request if preemptive rate limiting is necessary according to the
     * response headers.
     */
    private class RequestSubscriber extends BaseSubscriber<RequestCorrelation<ClientResponse>> {

        private volatile Duration sleepTime = Duration.ZERO;
        private final Function<ClientResponse, Mono<ClientResponse>> responseFunction;

        public RequestSubscriber(RateLimitStrategy strategy) {
            this.responseFunction = response -> {
                HttpClientResponse httpResponse = response.getHttpResponse();
                if (log.isDebugEnabled()) {
                    Instant requestTimestamp =
                            Instant.ofEpochMilli(httpResponse.currentContext().get(DiscordWebClient.KEY_REQUEST_TIMESTAMP));
                    Duration responseTime = Duration.between(requestTimestamp, Instant.now());
                    LogUtil.traceDebug(log, trace -> format(httpResponse.currentContext(),
                            "Read " + httpResponse.status() + " in " + responseTime + (!trace ? "" :
                                    " with headers: " + httpResponse.responseHeaders())));
                }
                Duration nextReset = strategy.apply(httpResponse);
                if (!nextReset.isZero()) {
                    if (log.isDebugEnabled()) {
                        log.debug(format(httpResponse.currentContext(), "Delaying next request by {}"), nextReset);
                    }
                    sleepTime = nextReset;
                }
                boolean global = Boolean.parseBoolean(httpResponse.responseHeaders().get("X-RateLimit-Global"));
                Mono<Void> action = Mono.empty();
                if (global) {
                    long retryAfter = Long.parseLong(httpResponse.responseHeaders().get("Retry-After"));
                    Duration fixedBackoff = Duration.ofMillis(retryAfter);
                    action = globalRateLimiter.rateLimitFor(fixedBackoff)
                            .doOnTerminate(() -> log.debug(format(httpResponse.currentContext(),
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
            MonoProcessor<ClientResponse> callback = correlation.getResponse();

            if (log.isDebugEnabled()) {
                log.debug("[B:{}, R:{}] {}", id.toString(), clientRequest.getId(), clientRequest.getDescription());
            }

            Mono.just(clientRequest)
                    .doOnEach(s -> log.trace(format(s.getContext(), ">> {}"), s))
                    .flatMap(req -> globalRateLimiter.withLimiter(httpClient.exchange(req)
                            .flatMap(responseFunction))
                            .next())
                    .doOnEach(s -> log.trace(format(s.getContext(), "<< {}"), s))
                    .subscriberContext(ctx -> ctx.putAll(correlation.getContext())
                            .put(LogUtil.KEY_REQUEST_ID, clientRequest.getId())
                            .put(LogUtil.KEY_BUCKET_ID, id.toString()))
                    .retryWhen(rateLimitRetryOperator::apply)
                    .transform(getResponseTransformers(request))
                    .retryWhen(serverErrorRetryFactory())
                    .doFinally(this::next)
                    .checkpoint("Request to " + clientRequest.getDescription() + " [RequestStream]")
                    .subscribeWith(callback)
                    .subscribe(null, t -> log.trace("Error while processing {}: {}", request, t));
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
            Mono<Long> timer = sleepTime.isZero() ? Mono.just(0L) : Mono.delay(sleepTime, timedTaskScheduler);
            timer.subscribe(l -> {
                if (log.isDebugEnabled()) {
                    log.debug("[B:{}] Ready to consume next request after {}", id.toString(), signal);
                }
                sleepTime = Duration.ZERO;
                request(1);
            }, t -> log.error("[B:{}] Error while scheduling next request", id.toString(), t));
        }
    }
}
