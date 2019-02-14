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

import discord4j.common.RateLimiter;
import discord4j.common.SimpleBucket;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.route.Routes;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facilitates the routing of {@link discord4j.rest.request.DiscordRequest DiscordRequests} to the proper
 * {@link discord4j.rest.request.RequestStream RequestStream} according to the bucket in which the request falls.
 * <p>
 * Must be cached using {@link discord4j.rest.request.SingleRouterFactory} if intended for sharding, to properly
 * coordinate queueing and rate-limiting across buckets.
 */
public class DefaultRouter implements Router {

    private static final Logger log = Loggers.getLogger(DefaultRouter.class);
    private static final ResponseHeaderStrategy HEADER_STRATEGY = new ResponseHeaderStrategy();

    private final DiscordWebClient httpClient;
    private final RouterOptions routerOptions;
    private final GlobalRateLimiter globalRateLimiter = new GlobalRateLimiter();
    private final Map<BucketKey, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

    /**
     * Create a bucket-aware router using the {@link reactor.core.scheduler.Schedulers#elastic()} scheduler, to allow
     * the use of blocking API. Use the alternate constructor to customize it.
     *
     * @param httpClient the web client executing each request instructed by this router
     */
    public DefaultRouter(DiscordWebClient httpClient) {
        this(httpClient, RouterOptions.builder().build());
    }

    /**
     * Create a bucket-aware router that uses the given {@link reactor.core.scheduler.Scheduler}.
     *
     * @param httpClient the web client executing each request instructed by this router
     * @param responseScheduler the scheduler used to execute each request
     * @param rateLimitScheduler the scheduler used to perform delays caused by rate limiting
     */
    @Deprecated
    public DefaultRouter(DiscordWebClient httpClient, Scheduler responseScheduler, Scheduler rateLimitScheduler) {
        this.httpClient = httpClient;
        this.routerOptions = RouterOptions.builder()
                .responseScheduler(responseScheduler)
                .rateLimitScheduler(rateLimitScheduler)
                .build();
    }

    public DefaultRouter(DiscordWebClient httpClient, RouterOptions routerOptions) {
        this.httpClient = httpClient;
        this.routerOptions = routerOptions;
    }

    @Override
    public <T> Mono<T> exchange(DiscordRequest<T> request) {
        return Mono.defer(Mono::subscriberContext)
                .flatMap(ctx -> {
                    RequestStream<T> stream = getStream(request);
                    MonoProcessor<T> callback = MonoProcessor.create();
                    String shardId = ctx.getOrEmpty("shard")
                            .map(Object::toString)
                            .orElse(null);
                    stream.push(new RequestCorrelation<>(request, callback, shardId));
                    return callback;
                })
                .publishOn(routerOptions.getResponseScheduler());
    }

    @SuppressWarnings("unchecked")
    private <T> RequestStream<T> getStream(DiscordRequest<T> request) {
        return (RequestStream<T>)
                streamMap.computeIfAbsent(computeBucket(request),
                        k -> {
                            if (log.isTraceEnabled()) {
                                log.trace("Creating RequestStream with key {} for request: {} -> {}",
                                        k, request.getRoute().getUriTemplate(), request.getCompleteUri());
                            }
                            RequestStream<T> stream = new RequestStream<>(k, httpClient, globalRateLimiter,
                                    getRateLimitStrategy(request), routerOptions.getRateLimitScheduler(), routerOptions);
                            stream.start();
                            return stream;
                        });
    }

    private <T> BucketKey computeBucket(DiscordRequest<T> request) {
        if (Routes.MESSAGE_DELETE.equals(request.getRoute())) {
            return BucketKey.of("DELETE " + request.getRoute().getUriTemplate(), request.getCompleteUri());
        }
        return BucketKey.of(request.getRoute().getUriTemplate(), request.getCompleteUri());
    }

    private RequestStream.RateLimitStrategy getRateLimitStrategy(DiscordRequest<?> request) {
        if (Routes.REACTION_CREATE.equals(request.getRoute())) {
            return new RateLimiterStrategy(new SimpleBucket(1, Duration.ofMillis(250)));
        }
        return HEADER_STRATEGY;
    }

    static class RateLimiterStrategy implements RequestStream.RateLimitStrategy {

        private final RateLimiter rateLimiter;

        RateLimiterStrategy(RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        @Override
        public Duration apply(HttpClientResponse response) {
            rateLimiter.tryConsume(1);
            return Duration.ofMillis(rateLimiter.delayMillisToConsume(1));
        }
    }

    static class ResponseHeaderStrategy implements RequestStream.RateLimitStrategy {

        @Override
        public Duration apply(HttpClientResponse response) {
            HttpHeaders headers = response.responseHeaders();
            int remaining = headers.getInt("X-RateLimit-Remaining", -1);
            if (remaining == 0) {
                long resetAt = Long.parseLong(headers.get("X-RateLimit-Reset"));
                long discordTime = headers.getTimeMillis("Date") / 1000;
                return Duration.ofSeconds(resetAt - discordTime);
            }
            return Duration.ZERO;
        }
    }
}
