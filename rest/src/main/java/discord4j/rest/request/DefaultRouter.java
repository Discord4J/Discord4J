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
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * Facilitates the routing of {@link DiscordWebRequest} instances to the proper {@link RequestStream} according to
 * the bucket in which the request falls.
 */
public class DefaultRouter implements Router {

    private static final Logger log = Loggers.getLogger(DefaultRouter.class);
    private static final ResponseHeaderStrategy HEADER_STRATEGY = new ResponseHeaderStrategy();
    private static final Duration HOUSE_KEEPING_PERIOD = Duration.ofSeconds(30);

    private final ReactorResources reactorResources;
    private final DiscordWebClient httpClient;
    private final Map<BucketKey, RequestStream> streamMap = new ConcurrentHashMap<>();
    private final RouterOptions routerOptions;

    private final AtomicBoolean isHousekeeping = new AtomicBoolean(false);
    private volatile Instant lastHousekeepingTime = Instant.EPOCH;

    /**
     * Create a Discord API bucket-aware {@link Router} configured with the given options.
     *
     * @param routerOptions the options that configure this {@link Router}
     */
    public DefaultRouter(RouterOptions routerOptions) {
        this.routerOptions = routerOptions;
        this.reactorResources = routerOptions.getReactorResources();
        this.httpClient = new DiscordWebClient(reactorResources.getHttpClient(),
                routerOptions.getExchangeStrategies(), routerOptions.getAuthorizationScheme(), routerOptions.getToken(),
                routerOptions.getResponseTransformers(), routerOptions.getDiscordBaseUrl());
    }

    @Override
    public DiscordWebResponse exchange(DiscordWebRequest request) {
        return new DiscordWebResponse(Mono.deferContextual(
                ctx -> {
                    Sinks.One<ClientResponse> callback = Sinks.one();
                    housekeepIfNecessary();
                    BucketKey bucketKey = BucketKey.of(request);
                    RequestStream stream = streamMap.computeIfAbsent(bucketKey, key -> createStream(key, request));
                    if (!stream.push(new RequestCorrelation<>(request, callback, ctx))) {
                        callback.emitError(new DiscardedRequestException(request), FAIL_FAST);
                    }
                    return callback.asMono();
                })
                .checkpoint("Request to " + request.getDescription() + " [DefaultRouter]"), reactorResources);
    }

    private RequestStream createStream(BucketKey bucketKey, DiscordWebRequest request) {
        if (log.isTraceEnabled()) {
            log.trace("Creating RequestStream with key {} for request: {} -> {}",
                bucketKey, request.getRoute().getUriTemplate(), request.getCompleteUri());
        }
        RequestStream stream = new RequestStream(bucketKey, routerOptions, httpClient, HEADER_STRATEGY);
        stream.start();
        return stream;
    }

    private void housekeepIfNecessary() {
        Instant now = Instant.now();
        if (lastHousekeepingTime.plus(HOUSE_KEEPING_PERIOD).isAfter(now)) {
            return;
        }

        tryHousekeep(now);
    }

    private void tryHousekeep(Instant now) {
        if (isHousekeeping.compareAndSet(false, true)) {
            try {
                doHousekeep(now);
            } finally {
                lastHousekeepingTime = Instant.now();
                isHousekeeping.set(false);
            }
        }
    }

    private void doHousekeep(Instant now) {
        streamMap.keySet().forEach(key ->
            streamMap.compute(key, (bucketKey , stream) -> {
                if (stream == null) {
                    return null;
                }
                if (stream.getResetAt().isBefore(now) && stream.countRequestsInFlight() < 1) {
                    if (log.isTraceEnabled()) {
                        log.trace("Evicting RequestStream with bucket ID {}", bucketKey);
                    }
                    stream.stop();
                    return null;
                }
                return stream;
            })
        );
    }
}
