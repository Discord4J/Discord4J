package discord4j.commands.example;

import discord4j.commands.BaseCommand;
import discord4j.commands.CommandException;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class LogoutCommand implements BaseCommand {

    private final Snowflake ownerId;

    public LogoutCommand(Snowflake ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.just(event)
                .map(MessageCreateEvent::getMember)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Member::getId)
                .filter(memberId -> memberId.equals(ownerId))
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    throw new CommandException("You're not my owner!");
                }))
                .doOnSuccess(i -> event.getClient().logout())
                .then();
    }
}
