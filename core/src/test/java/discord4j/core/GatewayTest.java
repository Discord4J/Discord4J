package discord4j.core;

import discord4j.http.function.client.ExchangeFilterFunction;
import discord4j.http.function.client.WebClient;
import discord4j.pojo.GatewayPojo;
import discord4j.route.Router;
import discord4j.route.Routes;
import discord4j.route.WebFluxRouter;
import discord4j.socket.WebSocketMessage;
import discord4j.socket.client.WebSocketClient;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.WorkQueueProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GatewayTest {

    private static final Logger log = LoggerFactory.getLogger(GatewayTest.class);

    private String token;

    @Before
    public void initialize() {
        token = System.getProperty("token");
    }

    @Test
    public void testGatewayConnect() throws URISyntaxException, InterruptedException {
        WebClient webClient = buildWebClient();
        Router router = new WebFluxRouter(webClient);
        String gateway = getGatewayUrl(router) + "?v=6&encoding=json";

        AtomicBoolean closed = new AtomicBoolean();
        SynchronousQueue<String> outboundMessages = new SynchronousQueue<>();

        Flux<String> outboundExchange = Flux.create(emitter -> {
            while (!closed.get()) {
                try {
                    String message = outboundMessages.poll(1, TimeUnit.SECONDS);
                    if (message != null) {
                        log.debug("Emitting {}", message);
                        emitter.next(message);
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrupted", e);
                }
            }
            emitter.complete();
        }, FluxSink.OverflowStrategy.BUFFER);
        WorkQueueProcessor<String> inboundExchange = WorkQueueProcessor.create();

        WebSocketClient client = new WebSocketClient();

        inboundExchange.log()
                .subscribe(message -> {
                    log.info("[Inbound Message] {}", message);
                });

        client.execute(new URI(gateway),
                session -> {
                    log.debug("Starting to send messages");

                    session.send(outboundExchange.map(session::textMessage))
                            .then();

                    return session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .subscribeWith(inboundExchange)
                            .then();
                }).block();
    }

    private String getGatewayUrl(Router router) {
        GatewayPojo response = router.exchange(Routes.GATEWAY_GET)
                .toFuture()
                .join();
        return response.url;
    }

    private static Map<String, ?> of(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private WebClient buildWebClient() {
        return WebClient.builder()
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
    }
}
