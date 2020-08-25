package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;

import java.time.Instant;

public class ExampleEmbedCreate {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
            .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(msg -> msg.getContent().equals("!embed"))
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createEmbed()
                    .withColor(Color.CYAN)
                    .withDescription("Cyan is a cool color!")
                    .withTimestamp(Instant.now())))
            .block();
    }
}
