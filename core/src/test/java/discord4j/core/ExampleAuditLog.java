package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.entity.Message;

import java.util.stream.Collectors;

public class ExampleAuditLog {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!auditlog"))
                        .flatMap(msg -> msg.getGuild()
                                .flatMapMany(guild -> guild.getAuditLog()
                                        .withActionType(ActionType.MEMBER_BAN_ADD)
                                        .take(20))
                                .map(Object::toString)
                                .collect(Collectors.joining("\n"))
                                .flatMap(msg.getRestChannel()::createMessage)))
                .block();
    }
}
