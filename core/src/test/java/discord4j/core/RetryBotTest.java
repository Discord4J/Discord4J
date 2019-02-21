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
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.message.MessageCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.lifecycle.ResumeEvent;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.core.shard.ShardingClientBuilder;
import discord4j.gateway.IdentifyOptions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RetryBotTest {

    private static final Logger log = Loggers.getLogger(RetryBotTest.class);

    private static String token;
    private static Integer shardCount;
    private static Reflections reflections;

    @BeforeClass
    public static void initialize() {
        Hooks.onOperatorDebug();
        token = System.getenv("token");
        String shardCountValue = System.getenv("shardCount");
        if (shardCountValue != null) {
            shardCount = Integer.valueOf(shardCountValue);
        }
        reflections = new Reflections(Event.class);
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testWithSetShardCount() {
        new ShardingClientBuilder(token)
                .setShardCount(shardCount)
                .build()
                .map(DiscordClientBuilder::build)
                .flatMap(DiscordClient::login)
                .blockLast();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testWithRecommendedShardCount() {
        new ShardingClientBuilder(token)
                .build()
                .map(builder -> builder.setInitialPresence(Presence.invisible()))
                .map(DiscordClientBuilder::build)
                .flatMap(DiscordClient::login)
                .blockLast();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void test() {
        final Map<Integer, IdentifyOptions> optionsMap = loadResumeData();

        DiscordClient client = new DiscordClientBuilder(token)
                .setEventScheduler(Schedulers.fromExecutor(Executors.newWorkStealingPool()))
                .setIdentifyOptions(optionsMap.get(0))
                .setGatewayObserver((s, o) -> optionsMap.put(o.getShardIndex(), o))
                .build();

        Map<Integer, DiscordClient> clients = new ConcurrentHashMap<>();
        Map<String, AtomicLong> counts = new ConcurrentHashMap<>();

        clients.put(0, client);

        subscribeEventCounter(client, counts);

        startHttpServer(new ServerContext(clients, counts));

        AtomicLong ownerId = new AtomicLong();

        // Get the bot owner ID to filter commands
        Flux.first(client.getEventDispatcher().on(ReadyEvent.class),
                client.getEventDispatcher().on(ResumeEvent.class))
                .next()
                .flatMap(evt -> client.getApplicationInfo())
                .map(ApplicationInfo::getOwnerId)
                .map(Snowflake::asLong)
                .subscribe(ownerId::set);

        TestCommands testCommands = new TestCommands(client, ownerId);
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(testCommands::onMessageCreate)
                .onErrorContinue((t, o) -> log.error("Error", t))
                .subscribe();

        client.login().block();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testAndCancel() throws InterruptedException {
        DiscordClient client = new DiscordClientBuilder(token).build();
        CountDownLatch latch = new CountDownLatch(1);
        Disposable disposable = client.login()
                .doOnCancel(latch::countDown)
                .subscribe();
        Flux.interval(Duration.ofSeconds(19), Duration.ofSeconds(1))
                .take(1)
                .doOnNext(t -> disposable.dispose())
                .subscribe();
        latch.await();
        Thread.sleep(5000L);
    }

    public static class TestCommands {

        private final DiscordClient client;
        private final AtomicLong ownerId;

        public TestCommands(DiscordClient client, AtomicLong ownerId) {
            this.client = client;
            this.ownerId = ownerId;
        }

        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();

            message.getAuthor()
                    .filter(user -> ownerId.get() == user.getId().asLong()) // only accept bot owner messages
                    .flatMap(ignored -> message.getContent())
                    .ifPresent(content -> {
                        if ("!close".equals(content)) {
                            client.logout().subscribe();
                        } else if ("!retry".equals(content)) {
                            client.reconnect().subscribe();
                        } else if ("!online".equals(content)) {
                            client.updatePresence(Presence.online()).subscribe();
                        } else if ("!dnd".equals(content)) {
                            client.updatePresence(Presence.doNotDisturb()).subscribe();
                        } else if ("!raise".equals(content)) {
                            // exception if DM
                            Snowflake guildId = message.getGuild().block().getId();
                            log.info("Message came from guild: {}", guildId);
                        } else if (content.startsWith("!echo ")) {
                            Mono.justOrEmpty(message.getAuthor())
                                    .flatMap(User::getPrivateChannel)
                                    .flatMap(ch -> ch.createMessage(content.substring("!echo ".length())))
                                    .subscribe();
                        }
                    });
            return Mono.empty();
        }
    }

    /**
     * Allow resuming across JVM restarts by saving the last sequence and session ID for each shard on exit.
     *
     * @return a Map of IdentifyOptions across all shards to be used for resuming sessions.
     */
    private Map<Integer, IdentifyOptions> loadResumeData() {
        String resumePath = "resume.test";
        Map<Integer, IdentifyOptions> map = new ConcurrentHashMap<>();
        try {
            Path path = Paths.get(resumePath);
            if (Files.isRegularFile(path)
                    && Files.getLastModifiedTime(path).toInstant().plusSeconds(60).isAfter(Instant.now())) {
                for (String line : Files.readAllLines(path)) {
                    String[] tokens = line.split(";", 3);
                    Integer id = Integer.valueOf(tokens[0]);
                    IdentifyOptions options = new IdentifyOptions(id, shardCount, null);
                    options.setResumeSessionId(tokens[1]);
                    options.setResumeSequence(Integer.valueOf(tokens[2]));
                    map.put(id, options);
                }
            } else {
                log.debug("Not attempting to resume");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Persist our identify options
            try {
                List<String> lines = map.entrySet()
                        .stream()
                        .map(entry -> {
                            int shard = entry.getKey();
                            String sessionId = entry.getValue().getResumeSessionId();
                            Integer sequence = entry.getValue().getResumeSequence();
                            log.debug("Saving resume data for shard {}: {}, {}", shard, sessionId, sequence);
                            return shard + ";" + sessionId + ";" + sequence;
                        })
                        .filter(line -> !line.contains("null"))
                        .collect(Collectors.toList());
                Path saved = Files.write(Paths.get(resumePath), lines);
                log.info("File saved to {}", saved.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        if (map.isEmpty()) {
            // fallback to IDENTIFY case
            for (int i = 0; i < shardCount; i++) {
                map.put(i, new IdentifyOptions(i, shardCount, null));
            }
        } else if (map.size() < shardCount) {
            for (int i = 0; i < shardCount; i++) {
                map.computeIfAbsent(i, k -> new IdentifyOptions(k, shardCount, null));
            }
        }
        return map;
    }

    private void subscribeEventCounter(DiscordClient client, Map<String, AtomicLong> counts) {
        reflections.getSubTypesOf(Event.class)
                .forEach(type -> client.getEventDispatcher().on(type)
                        .map(event -> event.getClass().getSimpleName())
                        .map(name -> counts.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(1))
                        .subscribe(null, t -> log.error("Error", t)));
    }

    static class ServerContext {

        private final Map<Integer, DiscordClient> clientMap;
        private final Map<String, AtomicLong> eventCounts;

        ServerContext(Map<Integer, DiscordClient> clientMap, Map<String, AtomicLong> eventCounts) {
            this.clientMap = clientMap;
            this.eventCounts = eventCounts;
        }
    }

    private void startHttpServer(ServerContext context) {
        ObjectMapper mapper = new ObjectMapper();
        DisposableServer facade = HttpServer.create()
                .port(0)
                .route(routes -> routes
                        .get("/users",
                                (req, res) -> res.sendString(Flux.fromIterable(context.clientMap.values())
                                        .flatMap(DiscordClient::getUsers)
                                        .map(User::getId)
                                        .distinct()
                                        .count()
                                        .map(Object::toString))
                        )
                        .get("/events",
                                (req, res) -> {
                                    try {
                                        String json = mapper.writeValueAsString(context.eventCounts);
                                        return res.addHeader("content-type", "application/json")
                                                .chunkedTransfer(false)
                                                .sendString(Mono.just(json));
                                    } catch (JsonProcessingException e) {
                                        return res.status(500).send();
                                    }
                                }
                        )
                )
                .wiretap(true)
                .bindNow();

        log.info("Server started at {}:{}", facade.host(), facade.port());

        // kill the server on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(facade::disposeNow));
    }
}
