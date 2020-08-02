package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;

public class ExampleRoleManagement {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!createRole"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.createRole()
                                .withName("My role")
                                .withHoist(true)
                                .withMentionable(false))
                        .flatMap(role -> client.getEventDispatcher().on(MessageCreateEvent.class)
                                .map(MessageCreateEvent::getMessage)
                                .filter(msg -> msg.getContent().equals("!editRole"))
                                .next()
                                .then(role.edit()
                                        .withName("New Role Name")
                                        .withColor(Color.MOON_YELLOW))))
                .block();
    }
}
