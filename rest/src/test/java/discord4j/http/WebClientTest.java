/**
 * Copyright 2002-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package discord4j.http;

import discord4j.http.entity.DatadogPayload;
import discord4j.http.function.BodyInserters;
import discord4j.http.function.client.ExchangeFilterFunction;
import discord4j.http.function.client.WebClient;
import discord4j.pojo.GatewayPojo;
import discord4j.route.Router;
import discord4j.route.Routes;
import discord4j.route.WebFluxRouter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class WebClientTest {

    private static final Logger log = LoggerFactory.getLogger(WebClientTest.class);

    @Test
    public void getGatewayAsStringTest() throws InterruptedException {

        WebClient client = WebClient.create("https://discordapp.com/api");

        String result = client.get()
                .uri("/v6/gateway")
                .exchange()
                .flatMap(response -> {
                    log.info("{}", response.statusCode());
                    return response.bodyToMono(String.class);
                })
                .toFuture()
                .join();

        log.info("/gateway: {}", result);
    }

    @Test
    public void getGatewayAsPojoTest() throws InterruptedException {

        WebClient client = WebClient.create("https://discordapp.com/api");

        GatewayPojo result = client.get()
                .uri("/v6/gateway")
                .exchange()
                .flatMap(response -> {
                    log.info("{}", response.statusCode());
                    return response.bodyToMono(GatewayPojo.class);
                })
                .toFuture()
                .join();

        log.info("/gateway: {}", result);
    }

    private static Map<String, ?> of(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @Test
    public void getGatewayAsRouteTest() {
        String token = System.getProperty("token");

        WebClient client = WebClient.builder()
                .baseUrl(Routes.BASE_URL)
                .defaultHeader("user-agent", "DiscordBot (http://discord4j.com, Discord4J/3.0.0)")
                .defaultHeader("authorization", "Bot " + token)
                .defaultHeader("content-type", "application/json")
                .defaultUriVariables(of("version_number", 6))
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    log.debug("Request is passing through filter: {}", request);
                    return Mono.just(request);
                }))
                .filter(ExchangeFilterFunction.ofResponseProcessor(response -> {
                    log.debug("Response is passing through filter: {}", response);
                    return Mono.just(response);
                }))
                .build();

        Router router = new WebFluxRouter(client);
        Mono<GatewayPojo> response = router.exchange(Routes.GATEWAY_GET);
        response.toFuture().join();
    }

    @Test
    public void postPayloadTest() {

        WebClient client = WebClient.create("https://sentry.quantic.top/api");

        DatadogPayload payload = new DatadogPayload();
        payload.setTitle("Running a test");
        payload.setBody("From webflux! @ " + Instant.now());

        String result = client.post()
                .uri("/webhooks/{key}/datadog", "webhook-test")
                .contentType("application/json")
                .body(BodyInserters.fromObject(payload))
                .exchange()
                .flatMap(response -> response.bodyToMono(String.class))
                .toFuture()
                .join();

        log.info("/post: {}", result);
    }
}
