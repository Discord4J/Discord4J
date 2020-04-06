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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.GatewayLifecycleEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.state.StateView;
import discord4j.discordjson.json.MessageData;
import discord4j.store.api.mapping.MappingStoreService;
import discord4j.store.api.noop.NoOpStoreService;
import discord4j.store.jdk.JdkStoreService;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.util.function.Tuple2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ExampleStore {

    private static final Logger log = LoggerFactory.getLogger(ExampleStore.class);
    private static final Reflections reflections = new Reflections(Event.class);

    public static void main(String[] args) {
        JacksonResources jackson = new JacksonResources();
        Map<String, AtomicLong> counts = new ConcurrentHashMap<>();
        DiscordClientBuilder.create(System.getenv("token"))
                .setJacksonResources(jackson)
                .build()
                .gateway()
                .setStoreService(MappingStoreService.create()
                        .setMapping(new NoOpStoreService(), MessageData.class)
                        .setFallback(new JdkStoreService()))
                .withGateway(gateway -> {
                    log.info("Start!");

                    Mono<Void> server = startHttpServer(gateway, counts, jackson.getObjectMapper()).then();

                    Mono<Void> listener = gateway.on(GatewayLifecycleEvent.class)
                            .filter(e -> !e.getClass().equals(ReadyEvent.class))
                            .doOnNext(e -> log.info("[shard={}] {}", e.getShardInfo().format(), e.toString()))
                            .then();

                    Mono<Void> eventCounter = Flux.fromIterable(reflections.getSubTypesOf(Event.class))
                            .filter(cls -> reflections.getSubTypesOf(cls).isEmpty())
                            .flatMap(type -> gateway.on(type).doOnNext(event -> {
                                String key = event.getClass().getSimpleName();
                                counts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(1);
                            }))
                            .then();

                    return Mono.when(server, listener, eventCounter);
                })
                .block();
    }

    private static Mono<? extends DisposableServer> startHttpServer(GatewayDiscordClient gateway,
                                                                    Map<String, AtomicLong> counts,
                                                                    ObjectMapper mapper) {
        return HttpServer.create()
                .port(0) // use an ephemeral port
                .route(routes -> routes
                        .get("/counts",
                                (req, res) -> {
                                    StateView stores = gateway.getGatewayResources().getStateView();
                                    Mono<String> result = Flux.merge(
                                            Mono.just("users").zipWith(stores.getUserStore().count()),
                                            Mono.just("guilds").zipWith(stores.getGuildStore().count()),
                                            Mono.just("messages").zipWith(stores.getMessageStore().count()))
                                            .collectMap(Tuple2::getT1, Tuple2::getT2)
                                            .map(map -> {
                                                try {
                                                    return mapper.writeValueAsString(map);
                                                } catch (JsonProcessingException e) {
                                                    throw Exceptions.propagate(e);
                                                }
                                            });
                                    return res.addHeader("content-type", "application/json")
                                            .chunkedTransfer(false)
                                            .sendString(result);
                                }
                        )
                        .get("/events",
                                (req, res) -> Mono.fromCallable(() -> mapper.writeValueAsString(counts))
                                        .flatMap(json -> res.addHeader("content-type", "application/json")
                                                .chunkedTransfer(false)
                                                .sendString(Mono.just(json))
                                                .then()
                                                .onErrorResume(t -> res.status(500).send()))
                        )
                )
                .bind()
                .doOnNext(facade -> {
                    log.info("*************************************************************");
                    log.info("Server started at {}:{}", facade.host(), facade.port());
                    log.info("*************************************************************");
                    // kill the server on JVM exit
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> facade.disposeNow()));
                });
    }
}
