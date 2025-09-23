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

import discord4j.core.event.domain.InviteCreateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class TestInvite {

    private static final Logger log = Loggers.getLogger(TestInvite.class);

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
            .withGateway(client -> {
                client.getEventDispatcher().on(ReadyEvent.class)
                    .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));


                client.getInvite(System.getenv("inviteCode")).subscribe(invite -> log.info("Invite: {}", invite));

                // Take every invite created in the guild
                client.getEventDispatcher().on(GuildCreateEvent.class)
                    .map(GuildCreateEvent::getGuild)
                    .filter(guild -> guild.getId().asString().equals(System.getenv("guildId")))
                    .flatMap(Guild::getInvites)
                    .flatMap(extendedInvite -> Mono.fromRunnable(() -> log.info("ExtendedInvite {}", extendedInvite)))
                    .subscribe();

                client.getEventDispatcher().on(InviteCreateEvent.class)
                    .subscribe(event -> log.info(event.toString()));

                return client.onDisconnect();
            })
            .block();
    }
}
