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

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MigrationBot {

    private static final Logger log = Loggers.getLogger(MigrationBot.class);

    @Test
    @Ignore
    public void withGatewayInfinite() {
        String token = System.getenv("token");
        DiscordClient client = DiscordClient.create(token);
        // withGateway builds a transactional Gateway pipeline: after the inner completes, resources are released
        // This can only complete on disconnect
        // as we're building an infinite pipeline (Flux of events) that never completes
        client.withGateway(
                gateway -> gateway.on(MessageCreateEvent.class)
                        .filter(event -> event.getMessage().getContent().orElse("").equals("exit"))
                        .flatMap(event -> event.getGateway().logout())
                        .then())
                .block();
    }

    @Test
    @Ignore
    public void withGatewayFinite() {
        String token = System.getenv("token");
        DiscordClient client = DiscordClient.create(token);
        // if our inner pipeline is finite, we can potentially use it to terminate the bot
        // and extract the result outside
        MessageCreateEvent last = client.withGateway(
                gateway -> gateway.on(MessageCreateEvent.class)
                        .filter(event -> event.getMessage().getContent().orElse("").equals("hello"))
                        .next())
                .block();
        assert last != null;
        log.info("This message stopped me: {}", last.getMessage());
    }

    @Test
    @Ignore
    public void immediateDisconnect() {
        String token = System.getenv("token");
        DiscordClient client = DiscordClient.create(token);
        // This should immediately disconnect as we're completing the inner pipeline right after it connects
        client.withGateway(gateway -> Mono.empty()).block();
        // check below for an alternative method...
    }

    @Test
    @Ignore
    public void manageGatewayExternally() {
        String token = System.getenv("token");
        DiscordClient client = DiscordClient.create(token);
        // If you want to manually acquire a Gateway, you will become responsible for its release
        Gateway gateway = client.login().block();
        assert gateway != null;
        // let's add something to stop the bot from a command
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().orElse("").equals("exit"))
                .flatMap(event -> event.getGateway().logout())
                .subscribe();
        // we should block until it disconnects
        gateway.onDisconnect().block();
    }
}
