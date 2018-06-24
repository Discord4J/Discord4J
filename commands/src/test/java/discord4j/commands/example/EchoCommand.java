package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EchoCommand implements BaseCommand {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        if (!event.getMessage().getContent().isPresent())
            return Mono.empty();

        return Flux.fromArray(event.getMessage().getContent().get().split(" ")).skip(1)
                .collectList()
                .map(l -> String.join(" ", l))
                .zipWith(event.getMessage().getChannel())
                .flatMap(t -> t.getT2().createMessage(new MessageCreateSpec().setContent(t.getT1())))
                .then();
    }
}
