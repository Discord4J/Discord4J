package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Image;

public class ExampleEmojiCreate {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!createEmoji"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild ->
                                Image.ofUrl("https://cdn.discordapp.com/emojis/546687597246939136.png")
                                        .flatMap(image ->
                                                guild.createEmoji()
                                                        .withName("d4j")
                                                        .withImage(image)))
                )
                .block();
    }
}
