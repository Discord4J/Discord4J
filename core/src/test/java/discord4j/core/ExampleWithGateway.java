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

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import reactor.util.Logger;
import reactor.util.Loggers;

public class ExampleWithGateway {

    private static final Logger log = Loggers.getLogger(ExampleWithGateway.class);

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .gateway()
                .setInitialPresence(s -> ClientPresence.online(
                        ClientActivity.playing("with a new feature").withState("and an extra state")))
                .withGateway(client -> client.on(ReadyEvent.class)
                        .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
                        .then())
                .block();
    }
}
