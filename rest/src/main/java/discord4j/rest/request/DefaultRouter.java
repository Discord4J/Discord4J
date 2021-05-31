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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * Facilitates the routing of {@link DiscordWebRequest} instances to the proper {@link RequestStream} according to
 * the bucket in which the request falls.
 */
public class DefaultRouter implements Router {

    private static final Logger log = Loggers.getLogger(DefaultRouter.class);
    private static final ResponseHeaderStrategy HEADER_STRATEGY = new ResponseHeaderStrategy();

    private final ReactorResources reactorResources;
    private final DiscordWebClient httpClient;
    private final Map<BucketKey, RequestStream> streamMap = new ConcurrentHashMap<>();
    private final RouterOptions routerOptions;

    /**
     * Create a Discord API bucket-aware {@link Router} configured with the given options.
     *
     * @param routerOptions the options that configure this {@link Router}
     */
    public DefaultRouter(RouterOptions routerOptions) {
        this.routerOptions = routerOptions;
        this.reactorResources = routerOptions.getReactorResources();
        this.httpClient = new DiscordWebClient(reactorResources.getHttpClient(),
                routerOptions.getExchangeStrategies(), "Bot", routerOptions.getToken(),
                routerOptions.getResponseTransformers(), routerOptions.getDiscordBaseUrl());
    }

    @Override
    public DiscordWebResponse exchange(DiscordWebRequest request) {
        return new DiscordWebResponse(Mono.deferContextual(
                ctx -> {
                    RequestStream stream = getStream(request);
                    Sinks.One<ClientResponse> callback = Sinks.one();
                    if (!stream.push(new RequestCorrelation<>(request, callback, ctx))) {
                        callback.emitError(new DiscardedRequestException(request), FAIL_FAST);
                    }
                    return callback.asMono();
                })
                .checkpoint("Request to " + request.getDescription() + " [DefaultRouter]"), reactorResources);
    }

    private RequestStream getStream(DiscordWebRequest request) {
        return streamMap.computeIfAbsent(BucketKey.of(request),
                k -> {
                    if (log.isTraceEnabled()) {
                        log.trace("Creating RequestStream with key {} for request: {} -> {}",
                                k, request.getRoute().getUriTemplate(), request.getCompleteUri());
                    }
                    RequestStream stream = new RequestStream(k, routerOptions, httpClient, HEADER_STRATEGY);
                    stream.start();
                    return stream;
                });
    }
}
