package discord4j.core;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ExampleUserEdit {

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .withGateway(client -> client.getEventDispatcher().on(MessageCreateEvent.class)
                        .map(MessageCreateEvent::getMessage)
                        .filter(msg -> msg.getContent().equals("!userEdit"))
                        .flatMap(it -> client.edit().withUsername("test name")))
                .block();
    }
}
