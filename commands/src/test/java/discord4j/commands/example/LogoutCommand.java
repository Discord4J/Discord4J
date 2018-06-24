package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.commands.exceptions.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class LogoutCommand implements BaseCommand {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getClient()
                    .getApplicationInfo()
                    .filter(info -> info.getOwnerId().equals(event.getMessage().getAuthorId().get()))
                    .switchIfEmpty(Mono.fromRunnable(() -> {
                        throw new CommandException() {
                            @Override
                            public Optional<String> response() {
                                return Optional.of("You're not my owner!");
                            }
                        };
                    }))
                    .doOnSuccess(i -> event.getClient().logout())
                    .then();
    }
}
