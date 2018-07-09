package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EchoCommand implements BaseCommand {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                   .map(content -> content.substring(content.indexOf(" ")))
                   .zipWith(event.getMessage().getChannel(), (content, channel) -> channel.createMessage(content))
                   .then();
    }
}
