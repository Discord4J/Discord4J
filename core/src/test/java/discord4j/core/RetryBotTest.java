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
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.gateway.IdentifyOptions;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.Opcode;
import discord4j.gateway.retry.PartialDisconnectException;
import discord4j.rest.entity.data.ApplicationInfoData;
import io.netty.buffer.Unpooled;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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
        DiscordClient.builder(token)
                .build()
                .gateway()
                .setShardCount(shardCount)
                .setInitialPresence(shard -> Presence.invisible())
                .login(gateway -> Mono.empty())
                .block();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testWithConnect() throws InterruptedException {
        List<GatewayConnection> connections = DiscordClient.create(token)
                .gateway()
                .setShardCount(shardCount)
                .connect(gateway -> gateway.getEventDispatcher()
                        .on(MessageCreateEvent.class)
                        .filter(event -> event.getMessage().getContent().orElse("").equals("!exit"))
                        .doOnNext(event -> log.info("Proceeding to exit!!!"))
                        .flatMap(event -> event.getGateway().logout())
                        .then())
                .block();
        CountDownLatch latch = new CountDownLatch(1);
        for (GatewayConnection connection : connections) {
            log.info(connection.getIdentifyOptions().toString());
            connection.getGateway().getCloseProcessor().log("discord4j.close.processor.")
                    .subscribe(null, t -> {
                        log.info("Disconnected from {} with error", connection, t);
                        latch.countDown();
                    }, () -> {
                        log.info("Disconnected from {}", connection);
                        latch.countDown();
                    });
        }
        latch.await();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testWithRecommendedShardCount() {
        DiscordClient.builder(token)
                .build()
                .gateway()
                .login(gateway -> gateway.getEventDispatcher().on(MessageCreateEvent.class)
                        .filterWhen(e -> e.getMessage().getChannel().ofType(PrivateChannel.class).hasElement())
                        .doOnNext(event -> log.info("{}", event))
                        .then())
                .block();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void test() {
        String resumePath = "resume.test";
        Map<ShardInfo, IdentifyOptions> optionsMap = loadResumeData(resumePath);
        DiscordClient client = DiscordClient.create(token);
        Map<String, AtomicLong> counts = new ConcurrentHashMap<>();

        // Get the bot owner ID to filter commands
        Mono<Long> ownerId = client.getApplicationInfo()
                .map(ApplicationInfoData::getOwnerId)
                .cache();

        try {
            client.gateway()
                    .setGatewayObserver((newState, identifyOptions) -> {
                        optionsMap.put(identifyOptions.getShardInfo(), identifyOptions);
                    })
                    .setResumeOptions(shard -> {
                        IdentifyOptions loaded = optionsMap.get(shard);
                        String sessionId = loaded.getResumeSessionId() == null ? "" : loaded.getResumeSessionId();
                        Integer sequence = loaded.getResumeSequence() == null ? 0 : loaded.getResumeSequence();
                        return Tuples.of(sessionId, sequence);
                    })
                    .login(gateway -> {
                        subscribeEventCounter(gateway, counts);
                        startHttpServer(new ServerContext(gateway, counts));

                        TestCommands testCommands = new TestCommands(gateway);
                        return gateway.getEventDispatcher().on(MessageCreateEvent.class)
                                .filterWhen(event -> ownerId.map(owner -> {
                                    Long author = event.getMessage().getAuthor()
                                            .map(u -> u.getId().asLong())
                                            .orElse(null);
                                    return owner.equals(author);
                                }))
                                .flatMap(testCommands::onMessageCreate)
                                .onErrorContinue((t, o) -> log.error("Error", t))
                                .then();
                    })
                    .block();
        } catch (PartialDisconnectException e) {
            log.warn("This client can reconnect and RESUME");
        }

        saveResumeData(optionsMap, resumePath);
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testAndCancel() throws InterruptedException {
        DiscordClient client = DiscordClient.create(token);
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

        private final GatewayAggregate gateway;

        public TestCommands(GatewayAggregate gateway) {
            this.gateway = gateway;
        }

        public Mono<Void> onMessageCreate(MessageCreateEvent event) {
            Message message = event.getMessage();

            message.getAuthor()
                    .flatMap(__ -> message.getContent())
                    .ifPresent(content -> {
                        if ("!close".equals(content)) {
                            gateway.logout().subscribe();
                        } else if ("!retry".equals(content)) {
                            gateway.getGatewayClientMap().values()
                                    .forEach(gatewayClient -> gatewayClient.sender()
                                            .next(new GatewayPayload<>(Opcode.RECONNECT, null, null, null)));
                        } else if ("!disconnect".equals(content)) {
                            gateway.getGatewayClientMap().values()
                                    .forEach(gatewayClient -> gatewayClient.close(true).subscribe());
                        } else if ("!online".equals(content)) {
                            gateway.updatePresence(0, Presence.online()).subscribe();
                        } else if ("!dnd".equals(content)) {
                            gateway.updatePresence(0, Presence.doNotDisturb()).subscribe();
                        } else if (content.startsWith("!raw ")) {
                            gateway.getGatewayClientMap().get(0)
                                    .sendBuffer(Mono.just(
                                            Unpooled.wrappedBuffer(
                                                    content.substring("!raw ".length())
                                                            .getBytes(StandardCharsets.UTF_8))))
                                    .subscribe();
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
    private Map<ShardInfo, IdentifyOptions> loadResumeData(String resumePath) {
        Map<ShardInfo, IdentifyOptions> map = new ConcurrentHashMap<>();
        try {
            Path path = Paths.get(resumePath);
            if (Files.isRegularFile(path)
                    && Files.getLastModifiedTime(path).toInstant().plusSeconds(60).isAfter(Instant.now())) {
                for (String line : Files.readAllLines(path)) {
                    // id;count;sessionId;sequence
                    String[] tokens = line.split(";", 4);
                    int id = Integer.parseInt(tokens[0]);
                    int count = Integer.parseInt(tokens[1]);
                    ShardInfo shardInfo = new ShardInfo(id, count);
                    IdentifyOptions identifyOptions = new IdentifyOptions(id, count, null);
                    identifyOptions.setResumeSessionId(tokens[2]);
                    identifyOptions.setResumeSequence(Integer.valueOf(tokens[3]));
                    map.put(shardInfo, identifyOptions);
                }
            } else {
                log.debug("Not attempting to resume");
            }
        } catch (IOException e) {
            log.warn("Could not load resume data", e);
        }

        if (map.isEmpty()) {
            // fallback to IDENTIFY case
            for (int id = 0; id < shardCount; id++) {
                ShardInfo shardInfo = new ShardInfo(id, shardCount);
                map.put(shardInfo, new IdentifyOptions(id, shardCount, null));
            }
        } else if (map.size() < shardCount) {
            for (int id = 0; id < shardCount; id++) {
                ShardInfo shardInfo = new ShardInfo(id, shardCount);
                map.computeIfAbsent(shardInfo, k -> new IdentifyOptions(k.getIndex(), k.getCount(), null));
            }
        }
        return map;
    }

    private void saveResumeData(Map<ShardInfo, IdentifyOptions> map, String resumePath) {
        try {
            List<String> lines = map.entrySet()
                    .stream()
                    .map(entry -> {
                        ShardInfo shard = entry.getKey();
                        String sessionId = entry.getValue().getResumeSessionId();
                        Integer sequence = entry.getValue().getResumeSequence();
                        log.debug("Saving resume data for shard {}: {}, {}", shard, sessionId, sequence);
                        return shard.getIndex() + ";" + shard.getCount() + ";" + sessionId + ";" + sequence;
                    })
                    .filter(line -> !line.contains("null"))
                    .collect(Collectors.toList());
            Path saved = Files.write(Paths.get(resumePath), lines);
            log.info("File saved to {}", saved.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not save resume data", e);
        }
    }

    private void subscribeEventCounter(GatewayAggregate gateway, Map<String, AtomicLong> counts) {
        reflections.getSubTypesOf(Event.class)
                .forEach(type -> gateway.getEventDispatcher().on(type)
                        .map(event -> event.getClass().getSimpleName())
                        .map(name -> counts.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(1))
                        .subscribe(null, t -> log.error("Error", t)));
    }

    static class ServerContext {

        private final GatewayAggregate gateway;
        private final Map<String, AtomicLong> eventCounts;

        ServerContext(GatewayAggregate gateway, Map<String, AtomicLong> eventCounts) {
            this.gateway = gateway;
            this.eventCounts = eventCounts;
        }
    }

    private void startHttpServer(ServerContext context) {
        ObjectMapper mapper = new ObjectMapper();
        DisposableServer facade = HttpServer.create()
                .port(0)
                .route(routes -> routes
                        .get("/users",
                                (req, res) -> res.sendString(context.gateway.getUsers()
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
