package discord4j.commands.example;

import discord4j.commands.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class EchoCommand implements Command {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                   .map(content -> content.substring(content.indexOf(" ")))
                   .zipWith(event.getMessage().getChannel(), (content, channel) -> channel.createMessage(content))
                   .then();
    }
}
