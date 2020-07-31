package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;

public class ExampleGuildEdit {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!editGuild"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.edit()
                                .withVerificationLevel(Guild.VerificationLevel.VERY_HIGH)
                                .withReason("Raid!"))
                )
                .block();
    }
}
