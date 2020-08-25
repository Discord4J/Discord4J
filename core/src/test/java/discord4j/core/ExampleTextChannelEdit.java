package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.Objects;

public class ExampleTextChannelEdit {

    public static void main(String[] args) {
        Snowflake toEdit = Snowflake.of(Objects.requireNonNull(System.getenv("toEdit")));

        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!textChannelEdit"))
                        .flatMap(it -> it.getClient().getChannelById(toEdit))
                        .cast(TextChannel.class)
                        .flatMap(channel -> channel.edit().withName("edited text channel")
                                .withTopic("edited text channel topic")
                                .withNsfw(false)))
                .block();
    }
}
