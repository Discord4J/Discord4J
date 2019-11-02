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

public class RSocketGlobalRateLimiterServer {

    private static final Logger log = Loggers.getLogger(RSocketGlobalRateLimiterServer.class);
    private static final String ACQUIRE = "ACQUIRE";
    private static final String PERMIT = "PERMIT";
    private static final String RELEASE = "RELEASE";
    private static final String LIMIT_GLOBAL = "LIMIT:global";
    private static final String LIMIT_QUERY = "QUERY:global";

    private final TcpServerTransport serverTransport;
    private final GlobalRateLimiter delegate;
    private final RequestBridgeStream globalStream;

    public RSocketGlobalRateLimiterServer(InetSocketAddress socketAddress, GlobalRateLimiter delegate,
                                          Scheduler rateLimitScheduler) {
        this.serverTransport = TcpServerTransport.create(socketAddress);
        this.delegate = delegate;
        this.globalStream = new RequestBridgeStream("global", delegate, rateLimitScheduler);
        this.globalStream.start();
    }

    public Mono<CloseableChannel> start() {
        return RSocketFactory.receive()
                .errorConsumer(t -> log.error("Server error: {}", t.toString()))
                .acceptor((setup, sendingSocket) -> Mono.just(new AbstractRSocket() {

                    @Override
                    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                        MonoProcessor<Void> release = MonoProcessor.create();
                        return Flux.from(payloads)
                                .flatMap(payload -> {
                                    String content = payload.getDataUtf8();
                                    if (content.startsWith(ACQUIRE)) {
                                        String[] tokens = content.split(":", 2);
                                        String id = tokens[1];
                                        log.debug("[{}] Acquire request", id);
                                        MonoProcessor<Void> acquire = MonoProcessor.create();
                                        RequestBridge<Void> notifier = new RequestBridge<>(id, acquire, release);
                                        globalStream.push(notifier);
                                        return acquire.thenReturn(DefaultPayload.create(PERMIT))
                                                .doOnSuccess(__ -> log.debug("[{}] Acquired permit for request", id));
                                    } else if (content.startsWith(RELEASE)) {
                                        String[] tokens = content.split(":", 2);
                                        String id = tokens[1];
                                        log.debug("[{}] Release request", id);
                                        release.onComplete();
                                    } else {
                                        log.warn("[requestChannel] Unsupported payload: {}", content);
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
                            log.debug("[{}] Rate limiting globally by {} (delta: {})", rateLimit, lag);
                            return delegate.rateLimitFor(orZero(rateLimit.minus(lag))).then(Mono.just(okPayload()));
                        } else if (content.startsWith(LIMIT_QUERY)) {
                            // QUERY:global
                            // reply with "QUERY:global:{remaining}:{nanoTimestamp}"
                            return delegate.getRemaining().map(RSocketGlobalRateLimiterServer::queryLimitReply);
                        }
                        return Mono.empty();
                    }
                }))
                .transport(serverTransport)
                .start();
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
