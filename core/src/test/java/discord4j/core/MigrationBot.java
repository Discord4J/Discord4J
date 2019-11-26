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

public class MigrationBot {

    @Test
    @Ignore
    public void transactionalGateway() {
        String token = System.getenv("token");
        DiscordClient client = DiscordClient.create(token);
        // builds a transactional Gateway pipeline: after the inner completes, release resources
        client.withGateway(
                gateway -> gateway.on(MessageCreateEvent.class)
                        .filter(event -> event.getMessage().getContent().orElse("").equals("exit"))
                        .flatMap(event -> event.getClient().logout())
                        .then())
                .block();
    }

    @Test
    @Ignore
    public void externalGateway() {
        String token = System.getenv("token");
        DiscordClient client = DiscordClient.create(token);
        // If you want to manually acquire a Gateway, you will become responsible for its release
        GatewayDiscordClient gateway = client.login().blockOptional().orElseThrow(RuntimeException::new);
        // let's add something to stop the bot from a command
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getContent().orElse("").equals("exit"))
                .flatMap(event -> event.getClient().logout())
                .subscribe();
        // we should block until it disconnects
        gateway.onDisconnect().block();
    }
}
