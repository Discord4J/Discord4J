package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;

public class ExampleWebhookEdit {

    public static void main(String[] args) {
        Snowflake toEdit = Snowflake.of(System.getenv("toEdit"));

        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!webhookEdit"))
                        .flatMap(Message::getChannel)
                        .cast(GuildMessageChannel.class)
                        .flatMap(GuildMessageChannel::getWebhooks)
                        .filter(it -> it.getId().equals(toEdit))
                        .flatMap(it -> it.edit().withName("edited webhook name")))
                .block();
    }
}
