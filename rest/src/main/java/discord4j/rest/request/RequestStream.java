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
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
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

    private final EmitterProcessor<RequestCorrelation<ClientResponse>> backing = EmitterProcessor.create(false);
    private final BucketKey id;
    private final DiscordWebClient httpClient;
    private final GlobalRateLimiter globalRateLimiter;
    private final RateLimitStrategy rateLimitStrategy;
    private final Scheduler rateLimitScheduler;
    private final List<ResponseFunction> responseFunctions;
    private final RateLimitRetryOperator rateLimitRetryOperator;

    RequestStream(BucketKey id, DiscordWebClient httpClient, GlobalRateLimiter globalRateLimiter,
                  RateLimitStrategy rateLimitStrategy, Scheduler rateLimitScheduler,
                  List<ResponseFunction> responseFunctions) {
        this.id = id;
        this.httpClient = httpClient;
        this.globalRateLimiter = globalRateLimiter;
        this.rateLimitStrategy = rateLimitStrategy;
        this.rateLimitScheduler = rateLimitScheduler;
        this.responseFunctions = responseFunctions;
        this.rateLimitRetryOperator = new RateLimitRetryOperator(globalRateLimiter, Schedulers.parallel());
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

    void push(RequestCorrelation<ClientResponse> request) {
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
    private class RequestSubscriber extends BaseSubscriber<RequestCorrelation<ClientResponse>> {

        private volatile Duration sleepTime = Duration.ZERO;
        private final Function<ClientResponse, Mono<ClientResponse>> rateLimitHandler;

        public RequestSubscriber(RateLimitStrategy strategy) {
            this.rateLimitHandler = response -> {
                HttpClientResponse httpResponse = response.getHttpResponse();
                if (log.isDebugEnabled()) {
                    Instant requestTimestamp =
                            Instant.ofEpochMilli(httpResponse.currentContext().get(DiscordWebClient.KEY_REQUEST_TIMESTAMP));
                    Duration responseTime = Duration.between(requestTimestamp, Instant.now());
                    log.debug(format(httpResponse.currentContext(), "Read {} in {} with headers: {}"),
                            httpResponse.status(), responseTime, httpResponse.responseHeaders());
                }
                Duration nextReset = strategy.apply(httpResponse);
                if (!nextReset.isZero()) {
                    if (log.isDebugEnabled()) {
                        log.debug(format(httpResponse.currentContext(), "Delaying next request by {}"), nextReset);
                    }
                    sleepTime = nextReset;
                }
                // if this response is a 429, intercept it
                if (httpResponse.status().code() == 429) {
                    return response.createException().flatMap(Mono::error);
                } else {
                    return Mono.just(response);
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

            if (tracesLog.isDebugEnabled()) {
                tracesLog.debug("Accepting request in bucket {}: {}", id.toString(), request);
            }

            globalRateLimiter.withLimiter(
                    Mono.just(clientRequest)
                            .doOnEach(s -> requestLog.debug(format(s.getContext(), "{}"), s))
                            .flatMap(httpClient::exchange)
                            .flatMap(rateLimitHandler)
                            .doOnEach(s -> responseLog.debug(format(s.getContext(), "{}"), s))
                            .subscriberContext(ctx -> ctx
                                    .putAll(correlation.getContext())
                                    .put(LogUtil.KEY_REQUEST_ID, clientRequest.getId())
                                    .put(LogUtil.KEY_BUCKET_ID, id.toString()))
                            .retryWhen(rateLimitRetryOperator::apply)
                            .transform(getResponseTransformers(request))
                            .retryWhen(serverErrorRetryFactory())
                            .doFinally(this::next))
                    .subscribeWith(callback)
                    .subscribe(null, t -> log.error("Error while processing {}", request, t));
        }

        private Function<Mono<ClientResponse>, Mono<ClientResponse>> getResponseTransformers(DiscordWebRequest discordRequest) {
            return responseFunctions.stream()
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
