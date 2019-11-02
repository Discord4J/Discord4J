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

import discord4j.common.LogUtil;
import discord4j.common.ReactorResources;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.http.client.ClientRequest;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.response.ResponseFunction;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClientResponse;
import reactor.retry.Retry;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

/**
 * A distributed {@link Router} implementation using RSocket TCP transport. This {@link Router} is capable of
 * connecting to a {@link RSocketRouterServer} and apply reactive streams semantics over a network connection, to
 * schedule {@link DiscordWebRequest} instances in a distributed fashion.
 * <p>
 * Each request is mapped to a server that is tasked with coordinating each request. It is recommended that a 1-to-1
 * relationship exists between Discord rate limiting buckets and {@link RSocketRouterServer} to maintain ordering
 * across multiple request sources.
 */
public class RSocketRouter implements Router {

    private static final Logger log = Loggers.getLogger(RSocketRouter.class);
    private static final ResponseHeaderStrategy HEADER_STRATEGY = new ResponseHeaderStrategy();
    private static final String READY = "READY";

    private final ReactorResources reactorResources;
    private final List<ResponseFunction> responseFunctions;
    private final DiscordWebClient httpClient;
    private final GlobalRateLimiter globalRateLimiter;
    private final Function<DiscordWebRequest, InetSocketAddress> requestTransportMapper;
    private final Map<InetSocketAddress, Mono<RSocket>> sockets = new ConcurrentHashMap<>();
    private final Map<BucketKey, BucketRequestExecutor> buckets = new ConcurrentHashMap<>();

    public RSocketRouter(RSocketRouterOptions routerOptions) {
        this.reactorResources = Objects.requireNonNull(routerOptions.getReactorResources(), "reactorResources");
        this.responseFunctions = Objects.requireNonNull(routerOptions.getResponseTransformers(), "responseFunctions");
        this.httpClient = new DiscordWebClient(reactorResources.getHttpClient(),
                routerOptions.getExchangeStrategies(), routerOptions.getToken(), responseFunctions);
        this.globalRateLimiter = Objects.requireNonNull(routerOptions.getGlobalRateLimiter(), "globalRateLimiter");
        this.requestTransportMapper = Objects.requireNonNull(routerOptions.getRequestTransportMapper(),
                "requestTransportMapper");
    }

    @Override
    public DiscordWebResponse exchange(DiscordWebRequest request) {
        return new DiscordWebResponse(Mono.defer(Mono::subscriberContext)
                .flatMap(ctx -> {
                    ClientRequest clientRequest = new ClientRequest(request);
                    String reqId = clientRequest.getId();
                    String bucket = BucketKey.of(request).toString();
                    BucketRequestExecutor executor = getExecutor(request);
                    InetSocketAddress socketAddress = requestTransportMapper.apply(request);
                    return Mono.defer(() -> getSocket(socketAddress))
                            .flatMap(rSocket -> {
                                UnicastProcessor<Payload> toLeader = UnicastProcessor.create();
                                toLeader.onNext(requestPayload(bucket, reqId));
                                return rSocket.requestChannel(toLeader)
                                        .onErrorMap(RouterException::new)
                                        .flatMap(payload -> {
                                            String content = payload.getDataUtf8();
                                            if (content.startsWith(READY)) {
                                                return executor.exchange(clientRequest, rSocket, ctx)
                                                        .doOnTerminate(() -> {
                                                            log.debug("[B:{}, R:{}] Request completed", bucket, reqId);
                                                            toLeader.onNext(donePayload(bucket, reqId));
                                                            toLeader.onComplete();
                                                        });
                                            } else {
                                                log.warn("Unknown payload: {}", content);
                                            }
                                            return Mono.empty();
                                        })
                                        .next();
                            })
                            .retryWhen(Retry.anyOf(RouterException.class)
                                    .exponentialBackoffWithJitter(Duration.ofSeconds(2), Duration.ofMinutes(1))
                                    .doOnRetry(rc -> {
                                        sockets.remove(socketAddress);
                                        log.info("Reconnecting to server: {}", rc.exception().toString());
                                    }));
                })
                .publishOn(reactorResources.getBlockingTaskScheduler()));
    }

    private Mono<RSocket> getSocket(InetSocketAddress socketAddress) {
        return sockets.computeIfAbsent(socketAddress, address -> RSocketFactory.connect()
                .errorConsumer(t -> log.error("Client error: {}", t.toString()))
                .transport(TcpClientTransport.create(address))
                .start()
                .doOnSubscribe(s -> log.debug("Connecting to RSocket server: {}", address))
                .cache(rSocket -> Duration.ofHours(1), t -> Duration.ZERO, () -> Duration.ZERO))
                .flatMap(rSocket -> {
                    if (rSocket.isDisposed()) {
                        sockets.remove(socketAddress);
                        return Mono.error(new RouterException("Lost connection to leader"));
                    } else {
                        return Mono.just(rSocket);
                    }
                });
    }

    private BucketRequestExecutor getExecutor(DiscordWebRequest request) {
        return buckets.computeIfAbsent(BucketKey.of(request), k -> {
            if (log.isTraceEnabled()) {
                log.trace("Creating RequestStream with key {} for request: {} -> {}",
                        k, request.getRoute().getUriTemplate(), request.getCompleteUri());
            }
            return new BucketRequestExecutor(k, globalRateLimiter,
                    HEADER_STRATEGY, reactorResources.getTimerTaskScheduler());
        });
    }

    class BucketRequestExecutor {

        private final BucketKey id;
        private final RateLimitRetryOperator rateLimitRetryOperator;
        private final Function<ClientResponse, Mono<ClientResponse>> rateLimitHandler;

        private volatile Duration sleepTime = Duration.ZERO;

        BucketRequestExecutor(BucketKey id, GlobalRateLimiter globalRateLimiter, RateLimitStrategy strategy,
                              Scheduler rateLimitScheduler) {
            this.id = id;
            this.rateLimitRetryOperator = new RateLimitRetryOperator(rateLimitScheduler);
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
                // handle GRL updates
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

        public Mono<ClientResponse> exchange(ClientRequest request, RSocket leaderSocket, Context context) {
            return Mono.just(request)
                    .doOnEach(s -> log.trace(format(s.getContext(), ">> {}"), s))
                    .flatMap(httpClient::exchange)
                    .flatMap(rateLimitHandler)
                    .doOnEach(s -> log.trace(format(s.getContext(), "<< {}"), s))
                    .flatMap(response -> leaderSocket.requestResponse(limitPayload(id.toString(), sleepTime.toMillis()))
                            .thenReturn(response))
                    .subscriberContext(ctx -> ctx.putAll(context)
                            .put(LogUtil.KEY_REQUEST_ID, request.getId())
                            .put(LogUtil.KEY_BUCKET_ID, id.toString()))
                    .retryWhen(rateLimitRetryOperator::apply)
                    .transform(getResponseTransformers(request.getDiscordRequest()))
                    .retryWhen(serverErrorRetryFactory())
                    .checkpoint("Request to " + request.getDescription() + " [RSocketRouter]");
        }

        private Function<Mono<ClientResponse>, Mono<ClientResponse>> getResponseTransformers(DiscordWebRequest discordRequest) {
            return responseFunctions.stream()
                    .map(rt -> rt.transform(discordRequest)
                            .andThen(mono -> mono.checkpoint("Apply " + rt + " to " +
                                    discordRequest.getDescription() + " [RSocketRouter]")))
                    .reduce(Function::andThen)
                    .orElse(mono -> mono);
        }

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

    }

    private static Payload requestPayload(String bucket, String request) {
        return DefaultPayload.create("REQUEST:" + bucket + ":" + request);
    }

    private static Payload donePayload(String bucket, String request) {
        return DefaultPayload.create("DONE:" + bucket + ":" + request);
    }

    private static Payload limitPayload(String bucket, long millis) {
        return DefaultPayload.create("LIMIT:" + bucket + ":" + millis);
    }
}
