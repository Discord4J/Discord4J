package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

import java.util.Objects;

public class ExampleBan {

    public static void main(String[] args) {
        Snowflake toBan = Objects.requireNonNull(Snowflake.of(System.getenv("toBan")));

        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!ban"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.ban(toBan)
                                .withReason("rekt")
                                .withDeleteMessageDays(5)))
                .block();
    }
}
