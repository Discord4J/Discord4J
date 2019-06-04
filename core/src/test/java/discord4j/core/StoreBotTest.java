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
import discord4j.common.JacksonResourceProvider;
import discord4j.core.event.domain.Event;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.ShardingClientBuilder;
import discord4j.core.shard.ShardingJdkStoreRegistry;
import discord4j.core.shard.ShardingStoreRegistry;
import discord4j.store.api.mapping.MappingStoreService;
import discord4j.store.jdk.JdkStoreService;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.util.function.Tuple2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StoreBotTest {

    private static final Logger log = LoggerFactory.getLogger(StoreBotTest.class);

    private static String token;
    private static Reflections reflections;

    @BeforeClass
    public static void initialize() {
        Hooks.onOperatorDebug();
        token = System.getenv("token");
        reflections = new Reflections(Event.class);
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testStoreBot() {
        Map<Integer, DiscordClient> clients = new ConcurrentHashMap<>();
        Map<String, AtomicLong> counts = new ConcurrentHashMap<>();
        JacksonResourceProvider jackson = new JacksonResourceProvider();
        startHttpServer(clients, counts, jackson.getObjectMapper());
        ShardingStoreRegistry registry = new ShardingJdkStoreRegistry();
        new ShardingClientBuilder(token)
                .setShardingStoreRegistry(registry)
                // showcase disabling the cache for messages
                .setStoreService(MappingStoreService.create()
                        //.setMapping(new NoOpStoreService(), MessageBean.class)
                        .setFallback(new JdkStoreService()))
                .build()
                .map(builder -> builder.setJacksonResourceProvider(jackson)
                        .setInitialPresence(Presence.invisible()))
                .map(DiscordClientBuilder::build)
                .doOnNext(client -> clients.put(client.getConfig().getShardIndex(), client))
                .doOnNext(client -> subscribeEventCounter(client, counts))
                .flatMap(DiscordClient::login)
                .blockLast();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> clients.forEach((id, client) -> client.logout().block())));
    }

    private void subscribeEventCounter(DiscordClient client, Map<String, AtomicLong> counts) {
        reflections.getSubTypesOf(Event.class)
                .stream()
                .filter(cls -> reflections.getSubTypesOf(cls).isEmpty())
                .forEach(type -> {
                    client.getEventDispatcher().on(type)
                            .map(event -> event.getClass().getSimpleName())
                            .map(name -> counts.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(1))
                            .subscribe(null, t -> log.error("Error", t));
                });
    }

    private void startHttpServer(Map<Integer, DiscordClient> shards, Map<String, AtomicLong> counts,
                                 ObjectMapper mapper) {
        DisposableServer facade = HttpServer.create()
                .port(0) // use an ephemeral port
                .route(routes -> routes
                        .get("/counts",
                                (req, res) -> {
                                    DiscordClient client = shards.get(0);
                                    StateHolder stores = client.getServiceMediator().getStateHolder();
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
                                (req, res) -> {
                                    try {
                                        String json = mapper.writeValueAsString(counts);
                                        return res.addHeader("content-type", "application/json")
                                                .chunkedTransfer(false)
                                                .sendString(Mono.just(json));
                                    } catch (JsonProcessingException e) {
                                        return res.status(500).send();
                                    }
                                }
                        )
                )
                .bindNow();

        log.info("*************************************************************");
        log.info("Server started at {}:{}", facade.host(), facade.port());
        log.info("*************************************************************");

        // kill the server on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(facade::disposeNow));
    }
}
