package discord4j.core;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;

import java.util.Objects;

public class ExampleRoleEdit {

    public static void main(String[] args) {
        Snowflake roleId = Snowflake.of(Objects.requireNonNull(System.getenv("roleId")));

        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!editRole"))
                        .flatMap(Message::getGuild)
                        .flatMap(guild -> guild.getRoleById(roleId))
                        .flatMap(role -> role.edit()
                                .withName("New Role Name")
                                .withColor(Color.MOON_YELLOW)))
                .block();
    }
}
