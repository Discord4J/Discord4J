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

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Message;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Objects;

public class ExampleForwardMessage {

    private static final Logger log = Loggers.getLogger(ExampleForwardMessage.class);

    public static void main(String[] args) {
        GatewayDiscordClient client = DiscordClient.create(System.getenv("token"))
            .gateway()
            .withEventDispatcher(d -> d.on(ReadyEvent.class)
                .doOnNext(readyEvent -> log.info("Ready: {}", readyEvent.getShardInfo())))
            .login()
            .block();

        getMessageAndForward(client);

        client.onDisconnect().block();
    }

    private static void getMessageAndForward(GatewayDiscordClient client) {
        Snowflake originMessageId = Snowflake.of(System.getenv("originMessageId"));
        Snowflake originMessageChannelId = Snowflake.of(System.getenv("originMessageChannelId"));

        Message message = client.getMessageById(originMessageChannelId, originMessageId).block();
        Objects.requireNonNull(message);

        Snowflake targetChannelId = Snowflake.of(System.getenv("targetChannelId"));

        message.forward(targetChannelId).block();
    }

}
