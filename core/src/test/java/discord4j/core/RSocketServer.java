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

package discord4j.core;

import discord4j.rest.request.*;
import io.rsocket.transport.netty.server.CloseableChannel;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.time.Duration;

public class RSocketServer {

    private static final Logger log = Loggers.getLogger(RSocketServer.class);

    public static void main(String[] args) {
        GlobalRateLimiter limiterBackend = new ParallelGlobalRateLimiter(12);

        //InetSocketAddress limiterServerAddress = new InetSocketAddress(12124);
        InetSocketAddress routerServerAddress = new InetSocketAddress(12123);

        /*
        RSocketGlobalRateLimiterServer limiterServer = new RSocketGlobalRateLimiterServer(limiterServerAddress,
                limiterBackend, Schedulers.parallel());
        GlobalRateLimiter rSocketLimiter = new RSocketGlobalRateLimiter(limiterServerAddress);
        RSocketRouterServer routerServer = new RSocketRouterServer(routerServerAddress, rSocketLimiter,
                Schedulers.parallel());
         */

        RSocketGlobalRouterServer routerServer = new RSocketGlobalRouterServer(routerServerAddress, limiterBackend,
                Schedulers.parallel());

        /*
        Mono<Void> onLimiterClose = limiterServer.start()
                .doOnNext(cc -> log.info("Started limiter server at {}", cc.address()))
                .retryBackoff(Long.MAX_VALUE, Duration.ofSeconds(1), Duration.ofMinutes(1))
                .flatMap(CloseableChannel::onClose);
         */

        Mono<Void> onRouterClose = routerServer.start()
                .doOnNext(cc -> log.info("Started global router server at {}", cc.address()))
                .retryBackoff(Long.MAX_VALUE, Duration.ofSeconds(1), Duration.ofMinutes(1))
                .flatMap(CloseableChannel::onClose);

        Mono.when(/*onLimiterClose, */onRouterClose).block();
    }
}
