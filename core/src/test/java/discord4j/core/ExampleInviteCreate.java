package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.CategorizableChannel;

public class ExampleInviteCreate {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!createInvite"))
                        .flatMap(Message::getChannel)
                        .ofType(CategorizableChannel.class)
                        .flatMap(channel -> channel.createInvite()
                                .withMaxAge(1)
                                .withMaxUses(5))
                )
                .block();
    }
}
