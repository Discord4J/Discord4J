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

import discord4j.common.JacksonResourceProvider;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.request.DefaultRouter;
import discord4j.rest.request.DiscordRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Route;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebClientTest {

    private DisposableServer server;
    private int port;

    @BeforeAll
    public void setup() {
        server = HttpServer.create()
                .host("0.0.0.0")
                .port(0)
                .route(r -> r
                        .get("/html/bad-request", (req, res) -> {
                            return res.header("content-type", "text/html")
                                    .status(400)
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
                .doOnNext(server -> port = server.port())
                .block();
    }

    @Test
    public void shouldErrorWithClientExceptionOnHtmlBadRequest() {
        ExchangeStrategies strategies = ExchangeStrategies.jackson(new JacksonResourceProvider().getObjectMapper());
        DiscordWebClient webClient = new DiscordWebClient(HttpClient.create(), strategies, "");
        Route<?> badRequestRoute = Route.get("http://0.0.0.0:" + port + "/html/bad-request", Object.class);
        Router router = new DefaultRouter(webClient);
        StepVerifier.create(router.exchange(new DiscordRequest<>(badRequestRoute)))
                .expectSubscription()
                .verifyError(ClientException.class);
    }

    @AfterAll
    public void dispose() {
        server.disposeNow();
    }
}
