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

package discord4j.rest.http;

import discord4j.common.JacksonResources;
import discord4j.common.LogUtil;
import discord4j.common.ReactorResources;
import discord4j.rest.request.*;
import discord4j.rest.route.Route;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Collections;

public class WebClientTest {

    private static final Logger log = Loggers.getLogger(WebClientTest.class);

    private static DisposableServer SERVER;
    private static int PORT;

    @BeforeClass
    public static void setup() {
        Hooks.onOperatorDebug();
        SERVER = HttpServer.create()
                .host("0.0.0.0")
                .port(0)
                .route(r -> r
                        .get("/html", (req, res) -> {
                            log.info("Hit /html endpoint");
                            return res.header("content-type", "text/html")
                                    .header("retry-after", "9999")
                                    .status(429)
                                    .sendString(Mono.just("<!doctype html>\n" +
                                            "<html>\n" +
                                            "  <head>\n" +
                                            "    <title>Testing</title>\n" +
                                            "  </head>\n" +
                                            "  <body>\n" +
                                            "    <p>Example paragraph for an HTML response</p>\n" +
                                            "  </body>\n" +
                                            "</html>"));
                        }))
                .bind()
                .doOnNext(server -> {
                    log.info("Server started on: {}", server.address());
                    PORT = server.port();
                })
                .block();
    }

    @Test
    @Ignore
    public void htmlResponse() {
        ExchangeStrategies ex2 = ExchangeStrategies.jackson(new JacksonResources().getObjectMapper());
        Route fakeRoute = Route.get("http://0.0.0.0:" + PORT + "/html");
        Router router = new DefaultRouter(new RouterOptions("", ReactorResources.create(), ex2, Collections.emptyList(),
                BucketGlobalRateLimiter.create(), RequestQueueFactory.buffering()));
        String response = router.exchange(new DiscordWebRequest(fakeRoute))
                .bodyToMono(String.class)
                .log()
                .subscriberContext(ctx -> ctx.put(LogUtil.KEY_SHARD_ID, 123))
                .block();
        log.info("{}", response);
    }

    @AfterClass
    public static void dispose() {
        SERVER.disposeNow();
    }
}
