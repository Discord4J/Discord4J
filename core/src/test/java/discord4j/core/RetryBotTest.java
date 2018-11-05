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

import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.gateway.GatewayObserver;
import discord4j.gateway.IdentifyOptions;
import discord4j.gateway.TokenBucket;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RetryBotTest {

    private static final Logger log = Loggers.getLogger(RetryBotTest.class);

    private static String token;
    private static Integer shardId;
    private static Integer shardCount;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
        String shardIdValue = System.getenv("shardId");
        String shardCountValue = System.getenv("shardCount");
        if (shardIdValue != null && shardCountValue != null) {
            shardId = Integer.valueOf(shardIdValue);
            shardCount = Integer.valueOf(shardCountValue);
        }
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testShards() {
        final Map<Integer, IdentifyOptions> optionsMap = initResumeOptions();
        final DiscordClientBuilder builder = new DiscordClientBuilder(token)
                .setGatewayLimiter(new TokenBucket(1, Duration.ofSeconds(5)))
                .setShardCount(shardCount);

        Flux.range(0, shardCount)
                .flatMap(index -> {
                    DiscordClient client = builder.setIdentifyOptions(optionsMap.get(index))
                            .setInitialPresence(Presence.online(Activity.playing("with " + index)))
                            .setGatewayObserver((s, o) -> {
                                optionsMap.put(o.getShardIndex(), o);
                                if (s.equals(GatewayObserver.CONNECTED)) {
                                    log.info("Shard {} connected", o.getShardIndex());
                                }
                            })
                            .build();
                    return client.login();
                })
                .blockLast();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void test() {
        final Map<Integer, IdentifyOptions> optionsMap = initResumeOptions();

        DiscordClient client = new DiscordClientBuilder(token)
                .setIdentifyOptions(optionsMap.get(0))
                .setGatewayObserver((s, o) -> optionsMap.put(o.getShardIndex(), o))
                .build();

        CommandListener commandListener = new CommandListener(client);
        commandListener.configure();

        LifecycleListener lifecycleListener = new LifecycleListener(client);
        lifecycleListener.configure();

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

    @Test
    @Ignore("Example code excluded from CI")
    public void testNoCommands() {
        IdentifyOptions options = new IdentifyOptions(shardId, shardCount, null);

        DiscordClient client = new DiscordClientBuilder(token)
                .setIdentifyOptions(options)
                .setInitialPresence(Presence.doNotDisturb())
                .build();

        LifecycleListener lifecycleListener = new LifecycleListener(client);
        lifecycleListener.configure();

        client.login().block();
    }

    private Map<Integer, IdentifyOptions> initResumeOptions() {
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

    public static class CommandListener {

        private final DiscordClient client;
        private final AtomicLong ownerId = new AtomicLong();

        public CommandListener(DiscordClient client) {
            this.client = client;
        }

        void configure() {
            Flux.first(client.getEventDispatcher().on(ReadyEvent.class),
                    client.getEventDispatcher().on(ResumeEvent.class))
                    .next()
                    .flatMap(evt -> client.getApplicationInfo())
                    .map(ApplicationInfo::getOwnerId)
                    .map(Snowflake::asLong)
                    .subscribe(ownerId::set);

            client.getEventDispatcher().on(MessageCreateEvent.class)
                    .flatMap(event -> {
                        Message message = event.getMessage();

                        message.getAuthorId()
                                .filter(id -> ownerId.get() == id.asLong()) // only accept bot owner messages
                                .flatMap(id -> message.getContent())
                                .ifPresent(content -> {
                                    if ("!close".equals(content)) {
                                        client.logout();
                                    } else if ("!retry".equals(content)) {
                                        client.reconnect();
                                    } else if ("!online".equals(content)) {
                                        client.updatePresence(Presence.online()).subscribe();
                                    } else if ("!dnd".equals(content)) {
                                        client.updatePresence(Presence.doNotDisturb()).subscribe();
                                    } else if ("!raise".equals(content)) {
                                        // exception if DM
                                        Snowflake guildId = message.getGuild().block().getId();
                                        log.info("Message came from guild: {}", guildId);
                                    } else if (content.startsWith("!echo ")) {
                                        message.getAuthor()
                                                .flatMap(User::getPrivateChannel)
                                                .flatMap(ch -> ch.createMessage(content.substring("!echo ".length())))
                                                .subscribe();
                                    }
                                });
                        return Mono.just(event);
                    })
                    .onErrorContinue((t, o) -> log.error("Error", t))
                    .subscribe();
        }
    }

    public static class LifecycleListener {

        private final DiscordClient client;

        public LifecycleListener(DiscordClient client) {
            this.client = client;
        }

        void configure() {
            client.getEventDispatcher().on(ConnectEvent.class).subscribe();
            client.getEventDispatcher().on(DisconnectEvent.class).subscribe();
            client.getEventDispatcher().on(ReconnectStartEvent.class).subscribe();
            client.getEventDispatcher().on(ReconnectEvent.class).subscribe();
            client.getEventDispatcher().on(ReconnectFailEvent.class).subscribe();
        }

    }

}
