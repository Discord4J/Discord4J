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
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.IntentSet;
import reactor.util.Logger;
import reactor.util.Loggers;

public class ExampleGatewayErrors {

    private static final Logger log = Loggers.getLogger(ExampleGatewayErrors.class);

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClient.create(System.getenv("token"))
                .gateway()
                .setEnabledIntents(IntentSet.all())
                .setInitialStatus(s -> ClientPresence.invisible())
                .setAwaitConnections(false)
                .login()
                .doOnError(e -> log.error("Failed to authenticate with Discord", e))
                .doOnSuccess(result -> log.info("Connected to Discord"))
                .block();
        assert client != null;
        client.on(ReadyEvent.class)
                .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
                .subscribe();
        client.onDisconnect().block();
    }
}
