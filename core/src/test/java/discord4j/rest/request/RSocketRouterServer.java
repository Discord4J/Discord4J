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

public class RSocketRouterServer {

    private static final Logger log = Loggers.getLogger(RSocketRouterServer.class);
    private static final String REQUEST = "REQUEST";
    private static final String READY = "READY";
    private static final String LIMIT = "LIMIT";
    private static final String DONE = "DONE";

    private final TcpServerTransport serverTransport;
    private final GlobalRateLimiter globalRateLimiter;
    private final Scheduler rateLimitScheduler;
    private final Map<String, RequestBridgeStream> streams = new ConcurrentHashMap<>();

    public RSocketRouterServer(InetSocketAddress socketAddress, GlobalRateLimiter globalRateLimiter,
                               Scheduler rateLimitScheduler) {
        this.serverTransport = TcpServerTransport.create(socketAddress);
        this.globalRateLimiter = globalRateLimiter;
        this.rateLimitScheduler = rateLimitScheduler;
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
                MonoProcessor<Void> release = MonoProcessor.create();
                return Flux.from(payloads)
                        .flatMap(payload -> {
                            String content = payload.getDataUtf8();
                            if (content.startsWith(REQUEST)) {
                                String[] tokens = content.split(":", 3);
                                String bucket = tokens[1];
                                String request = tokens[2];
                                log.debug("[B:{}, R:{}] Incoming request", bucket, request);
                                MonoProcessor<Void> acquire = MonoProcessor.create();
                                RequestBridge<Void> notifier = new RequestBridge<>(request, acquire, release);
                                getStream(bucket).push(notifier);
                                return acquire.thenReturn(DefaultPayload.create(READY))
                                        .doOnSuccess(__ -> log.debug("[B:{}, R:{}] Notifying worker to execute request",
                                                bucket, request));
                            } else if (content.startsWith(DONE)) {
                                String[] tokens = content.split(":", 3);
                                String bucket = tokens[1];
                                String request = tokens[2];
                                log.debug("[B:{}, R:{}] Completing request", bucket, request);
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
                if (content.startsWith(LIMIT)) {
                    String[] tokens = content.split(":", 3);
                    String bucket = tokens[1];
                    long millis = Long.parseLong(tokens[2]);
                    Duration delay = Duration.ofMillis(millis);
                    log.debug("[B:{}] Notifying server to delay by {}", bucket, delay);
                    getStream(bucket).setSleepTime(delay);
                } else {
                    log.warn("[requestResponse] Unsupported payload: {}", content);
                }
                return Mono.empty();
            }
        };
    }

    private RequestBridgeStream getStream(String bucket) {
        return streams.computeIfAbsent(bucket, k -> {
            RequestBridgeStream stream = new RequestBridgeStream(k, globalRateLimiter, rateLimitScheduler);
            stream.start();
            return stream;
        });
    }

}
