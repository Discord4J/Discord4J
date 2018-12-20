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

import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facilitates the routing of {@link discord4j.rest.request.DiscordRequest DiscordRequests} to the proper
 * {@link discord4j.rest.request.RequestStream RequestStream} according to the bucket in which the request falls.
 */
public class Router {

    private static final Logger log = Loggers.getLogger(Router.class);

    private final DiscordWebClient httpClient;
    private final Scheduler scheduler;
    private final GlobalRateLimiter globalRateLimiter = new GlobalRateLimiter();
    private final Map<BucketKey, RequestStream<?>> streamMap = new ConcurrentHashMap<>();

    public Router(DiscordWebClient httpClient, Scheduler scheduler) {
        this.httpClient = httpClient;
        this.scheduler = scheduler;
    }

    /**
     * Queues a request for execution in the appropriate {@link discord4j.rest.request.RequestStream request stream}
     * according to the request's {@link discord4j.rest.request.BucketKey bucket}.
     *
     * @param request The request to queue.
     * @param <T> The request's response type.
     * @return A mono that receives signals based on the request's response.
     */
    public <T> Mono<T> exchange(DiscordRequest<T> request) {
        return Mono.defer(() -> {
            RequestStream<T> stream = getStream(request);
            MonoProcessor<T> callback = MonoProcessor.create();

            stream.push(Tuples.of(callback, request));
            return callback;
        }).publishOn(scheduler);
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
                            RequestStream<T> stream = new RequestStream<>(k, httpClient, globalRateLimiter);
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
}
