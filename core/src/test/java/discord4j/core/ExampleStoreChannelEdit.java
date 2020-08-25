package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.StoreChannel;

import java.util.Objects;

public class ExampleStoreChannelEdit {

    public static void main(String[] args) {
        Snowflake toEdit = Snowflake.of(Objects.requireNonNull(System.getenv("toEdit")));

        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!storeChannelEdit"))
                        .flatMap(it -> it.getClient().getChannelById(toEdit))
                        .cast(StoreChannel.class)
                        .flatMap(channel -> channel.edit().withName("edited store channel")))
                .block();
    }
}
