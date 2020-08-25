package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildMessageChannel;

public class ExampleWebhookCreate {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!webhookCreate"))
                        .flatMap(Message::getChannel)
                        .cast(GuildMessageChannel.class)
                        .flatMap(it -> it.createWebhook()
                                .withName("test webhook")
                                .withReason("test webhook reason")))
                .block();
    }
}
