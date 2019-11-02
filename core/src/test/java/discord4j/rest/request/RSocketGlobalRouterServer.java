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

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RSocketGlobalRouterServer {

    private static final Logger log = Loggers.getLogger(RSocketGlobalRouterServer.class);
    private static final String REQUEST = "REQUEST";
    private static final String READY = "READY";
    private static final String LIMIT = "LIMIT";
    private static final String DONE = "DONE";
    private static final String ACQUIRE = "ACQUIRE";
    private static final String PERMIT = "PERMIT";
    private static final String RELEASE = "RELEASE";
    private static final String LIMIT_GLOBAL = "LIMIT:global";
    private static final String LIMIT_QUERY = "QUERY:global";

    private final TcpServerTransport serverTransport;
    private final GlobalRateLimiter delegate;
    private final Scheduler rateLimitScheduler;
    private final Map<String, RequestBridgeStream> streams = new ConcurrentHashMap<>();
    private final RequestBridgeStream globalStream;

    public RSocketGlobalRouterServer(InetSocketAddress socketAddress, GlobalRateLimiter delegate,
                                     Scheduler rateLimitScheduler) {
        this.serverTransport = TcpServerTransport.create(socketAddress);
        this.delegate = delegate;
        this.rateLimitScheduler = rateLimitScheduler;
        this.globalStream = new RequestBridgeStream("global", delegate, rateLimitScheduler);
        this.globalStream.start();
    }

    public Mono<CloseableChannel> start() {
        return RSocketFactory.receive()
                .errorConsumer(t -> log.error("Server error: {}", t.toString()))
                .acceptor((setup, sendingSocket) -> Mono.just(leaderAcceptor()))
                .transport(serverTransport)
                .start();
    }

    private RSocket leaderAcceptor() {
        return new AbstractRSocket() {

            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                AtomicReference<State> state = new AtomicReference<>(State.START);
                MonoProcessor<Void> release = MonoProcessor.create();
                return Flux.from(payloads)
                        .flatMap(payload -> {
                            String content = payload.getDataUtf8();
                            if (content.startsWith(REQUEST)) {
                                checkRouterMode(state);
                                // REQUEST:{bucket}:{reqId}
                                String[] tokens = content.split(":", 3);
                                String bucket = tokens[1];
                                String request = tokens[2];
                                log.debug("[B:{}, R:{}] Incoming request", bucket, request);
                                MonoProcessor<Void> acquire = MonoProcessor.create();
                                RequestBridge<Void> notifier = new RequestBridge<>(request, acquire, release);
                                getStream(bucket).push(notifier);
                                return acquire.thenReturn(DefaultPayload.create(READY))
                                        .doOnSuccess(__ ->
                                                log.debug("[B:{}, R:{}] Notifying worker to execute request", bucket,
                                                        request));
                            } else if (content.startsWith(DONE)) {
                                checkRouterMode(state);
                                // DONE:{bucket}:{reqId}
                                String[] tokens = content.split(":", 3);
                                String bucket = tokens[1];
                                String request = tokens[2];
                                log.debug("[B:{}, R:{}] Completing request", bucket, request);
                                release.onComplete();
                            } else if (content.startsWith(ACQUIRE)) {
                                checkLimiterMode(state);
                                // ACQUIRE:{id}
                                String[] tokens = content.split(":", 2);
                                String id = tokens[1];
                                log.debug("[{}] Acquire request", id);
                                MonoProcessor<Void> acquire = MonoProcessor.create();
                                RequestBridge<Void> notifier = new RequestBridge<>(id, acquire, release);
                                globalStream.push(notifier);
                                return acquire.thenReturn(DefaultPayload.create(PERMIT))
                                        .doOnSuccess(__ -> log.debug("[R:{}] Acquired permit for request", id));
                            } else if (content.startsWith(RELEASE)) {
                                checkLimiterMode(state);
                                // RELEASE:{id}
                                String[] tokens = content.split(":", 2);
                                String id = tokens[1];
                                log.debug("[{}] Release request", id);
                                release.onComplete();
                            }
                            return Mono.empty();
                        });
            }

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                String content = payload.getDataUtf8();
                if (content.startsWith(LIMIT_GLOBAL)) {
                    // LIMIT:global:{rateLimitNanos}:{nanoTimestamp}
                    // reply with "OK:{nanoTimestamp}"
                    String[] tokens = content.split(":", 4);
                    Duration rateLimit = Duration.ofNanos(Long.parseLong(tokens[2]));
                    Duration lag = Duration.ofNanos(System.nanoTime() - Long.parseLong(tokens[3]));
                    log.debug("[B:global] Notifying server to delay by {} (delta: {})", rateLimit, lag);
                    return delegate.rateLimitFor(orZero(rateLimit.minus(lag))).then(Mono.just(okPayload()));
                } else if (content.startsWith(LIMIT_QUERY)) {
                    // QUERY:global
                    // reply with "QUERY:global:{remaining}:{nanoTimestamp}"
                    return delegate.getRemaining().map(RSocketGlobalRouterServer::queryLimitReply);
                } else if (content.startsWith(LIMIT)) {
                    // TODO convert to nanos + ts format
                    // LIMIT:{bucket}:{rateLimitMillis}
                    String[] tokens = content.split(":", 3);
                    String bucket = tokens[1];
                    long millis = Long.parseLong(tokens[2]);
                    Duration delay = Duration.ofMillis(millis);
                    log.debug("[B:{}] Notifying server to delay by {}", bucket, delay);
                    getStream(bucket).setSleepTime(delay);
                }
                return Mono.empty();
            }

            private void checkRouterMode(AtomicReference<State> state) {
                switch (state.get()) {
                    case START:
                        state.set(State.ROUTER);
                        return;
                    case GLOBAL_LIMITER:
                        throw new IllegalStateException("Invalid usage: must only do REQUEST -> DONE");
                }
            }

            private void checkLimiterMode(AtomicReference<State> state) {
                switch (state.get()) {
                    case START:
                        state.set(State.GLOBAL_LIMITER);
                        return;
                    case ROUTER:
                        throw new IllegalStateException("Invalid usage: must only do ACQUIRE -> RELEASE");
                }

            }
        };
    }

    enum State {
        START, ROUTER, GLOBAL_LIMITER;
    }

    private RequestBridgeStream getStream(String bucket) {
        return streams.computeIfAbsent(bucket, k -> {
            RequestBridgeStream stream = new RequestBridgeStream(k, delegate, rateLimitScheduler);
            stream.start();
            return stream;
        });
    }

    private static Duration orZero(Duration duration) {
        return duration.isNegative() ? Duration.ZERO : duration;
    }

    private static Payload okPayload() {
        return DefaultPayload.create("OK:" + System.nanoTime());
    }

    private static Payload queryLimitReply(Duration remaining) {
        return DefaultPayload.create(LIMIT_QUERY + ":" + remaining.toNanos() + ":" + System.nanoTime());
    }

}
