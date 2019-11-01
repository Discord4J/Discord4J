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

import discord4j.common.ReactorResources;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facilitates the routing of {@link DiscordWebRequest DiscordRequests} to the proper
 * {@link discord4j.rest.request.RequestStream RequestStream} according to the bucket in which the request falls.
 * <p>
 * Must be cached using {@link discord4j.rest.request.SingleRouterFactory} if intended for sharding, to properly
 * coordinate queueing and rate-limiting across buckets.
 */
public class DefaultRouter implements Router {

    private static final Logger log = Loggers.getLogger(DefaultRouter.class);
    private static final ResponseHeaderStrategy HEADER_STRATEGY = new ResponseHeaderStrategy();

    private final ReactorResources reactorResources;
    private final DiscordWebClient httpClient;
    private final List<ResponseFunction> responseFunctions;
    private final GlobalRateLimiter globalRateLimiter;
    private final Map<BucketKey, RequestStream> streamMap = new ConcurrentHashMap<>();

    /**
     * Create a Discord API bucket-aware {@link Router} configured with the given options.
     *
     * @param routerOptions the options that configure this {@link Router}
     */
    public DefaultRouter(RouterOptions routerOptions) {
        this.reactorResources = routerOptions.getReactorResources();
        this.httpClient = new DiscordWebClient(reactorResources.getHttpClient(),
                routerOptions.getExchangeStrategies(), routerOptions.getToken());
        this.responseFunctions = routerOptions.getResponseTransformers();
        this.globalRateLimiter = routerOptions.getGlobalRateLimiter();
    }

    @Override
    public DiscordWebResponse exchange(DiscordWebRequest request) {
        return new DiscordWebResponse(Mono.defer(Mono::subscriberContext)
                .flatMap(ctx -> {
                    RequestStream stream = getStream(request);
                    MonoProcessor<ClientResponse> callback = MonoProcessor.create();
                    stream.push(new RequestCorrelation<>(request, callback, ctx));
                    return callback;
                })
                .publishOn(reactorResources.getBlockingTaskScheduler()));
    }

    private RequestStream getStream(DiscordWebRequest request) {
        return streamMap.computeIfAbsent(computeBucket(request),
                k -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Creating RequestStream with key {} for request: {} -> {}",
                                k, request.getRoute().getUriTemplate(), request.getCompleteUri());
                    }
                    RequestStream stream = new RequestStream(k, httpClient, globalRateLimiter,
                            HEADER_STRATEGY, reactorResources.getTimerTaskScheduler(),
                            responseFunctions);
                    stream.start();
                    return stream;
                });
    }

    private BucketKey computeBucket(DiscordWebRequest request) {
        if (Routes.MESSAGE_DELETE.equals(request.getRoute())) {
            return BucketKey.of("DELETE " + request.getRoute().getUriTemplate(), request.getCompleteUri());
        }
        return BucketKey.of(request.getRoute().getUriTemplate(), request.getCompleteUri());
    }

    static class ResponseHeaderStrategy implements RequestStream.RateLimitStrategy {

        @Override
        public Duration apply(HttpClientResponse response) {
            HttpHeaders headers = response.responseHeaders();
            int remaining = headers.getInt("X-RateLimit-Remaining", -1);
            if (remaining == 0) {
                long resetAt = (long) (Double.parseDouble(headers.get("X-RateLimit-Reset")) * 1000);
                long discordTime = headers.getTimeMillis("Date");
                return Duration.ofMillis(resetAt - discordTime);
            }
            return Duration.ZERO;
        }
    }
}
