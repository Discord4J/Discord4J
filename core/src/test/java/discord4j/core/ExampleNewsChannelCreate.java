package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class ExampleNewsChannelCreate {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!newsChannel"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.createNewsChannel()
                                .withName("test news")
                                .withTopic("test topic")
                                .withNsfw(true)))
                .block();
    }
}
