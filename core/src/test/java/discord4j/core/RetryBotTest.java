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
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import discord4j.gateway.IdentifyOptions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

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
    public void test() {
        IdentifyOptions options = new IdentifyOptions(shardId, shardCount, null);

        try {
            Path path = Paths.get("resume.dat");
            if (Files.isRegularFile(path)
                    && Files.getLastModifiedTime(path).toInstant().plusSeconds(60).isAfter(Instant.now())) {
                for (String line : Files.readAllLines(path)) {
                    String[] tokens = line.split(";", 2);
                    options.setResumeSessionId(tokens[0]);
                    options.setResumeSequence(Integer.valueOf(tokens[1]));
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
                String sessionId = options.getResumeSessionId();
                Integer sequence = options.getResumeSequence();
                log.debug("Resuming data: {}, {}", sessionId, sequence);
                Path saved = Files.write(Paths.get("resume.dat"),
                        Collections.singletonList(sessionId + ";" + sequence));
                log.info("File saved to {}", saved.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        DiscordClient client = new ClientBuilder(token)
                .setIdentifyOptions(options)
                .setInitialPresence(Presence.doNotDisturb())
                .build();

        CommandListener commandListener = new CommandListener(client);
        commandListener.configure();

        LifecycleListener lifecycleListener = new LifecycleListener(client);
        lifecycleListener.configure();

        client.login().block();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testNoCommands() {
        IdentifyOptions options = new IdentifyOptions(shardId, shardCount, null);

        DiscordClient client = new ClientBuilder(token)
                .setIdentifyOptions(options)
                .setInitialPresence(Presence.doNotDisturb())
                .build();

        LifecycleListener lifecycleListener = new LifecycleListener(client);
        lifecycleListener.configure();

        client.login().block();
    }

    public static class CommandListener {

        private final DiscordClient client;
        private final AtomicLong ownerId = new AtomicLong();

        public CommandListener(DiscordClient client) {
            this.client = client;
        }

        void configure() {
            Flux.first(client.getEventDispatcher().on(ReadyEvent.class), client.getEventDispatcher().on(ResumeEvent.class))
                    .next()
                    .flatMap(evt -> client.getApplicationInfo())
                    .map(ApplicationInfo::getOwnerId)
                    .map(Snowflake::asLong)
                    .subscribe(ownerId::set);

            client.getEventDispatcher().on(MessageCreateEvent.class)
                    .doOnNext(event -> {
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
                                    }
                                });
                    })
                    .retry() // retry if above block throws
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
