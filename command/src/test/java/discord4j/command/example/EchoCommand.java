package discord4j.command.example;

import discord4j.command.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class EchoCommand implements Command<Void> {

    private final int startIndex;
    private final int endIndex;

    public EchoCommand(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event, @Nullable Void context) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.substring(startIndex, endIndex))
                .zipWith(event.getMessage().getChannel())
                .flatMap(t -> t.getT2().createMessage(t.getT1()))
                .then();
    }
}
