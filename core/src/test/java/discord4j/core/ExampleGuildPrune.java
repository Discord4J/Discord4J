package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class ExampleGuildPrune {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!prune"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.prune()
                                .withDays(1)
                                .withComputePruneCount(true)))
                .block();
    }
}
