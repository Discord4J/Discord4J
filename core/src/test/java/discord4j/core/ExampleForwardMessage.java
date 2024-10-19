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
