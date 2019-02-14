package discord4j.command.example;

import discord4j.command.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class LogoutCommand implements Command<Void> {

    private final Snowflake ownerId;

    public LogoutCommand(Snowflake ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event, @Nullable Void context) {
        return Mono.just(event)
                .map(MessageCreateEvent::getMember)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Member::getId)
                .filter(memberId -> memberId.equals(ownerId))
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    throw new IllegalStateException("You're not my owner!");
                }))
                .doOnSuccess(i -> event.getClient().logout())
                .then();
    }
}
