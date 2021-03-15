package discord4j.core.event.domain.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.entity.Guild;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when an application command relevant to the current user is deleted.
 * This event is dispatched by Discord.
 *
 * @see
 * <a href="https://discord.com/developers/docs/topics/gateway#application-command-delete">Application Command Delete</a>
 */
public class ApplicationCommandDeleteEvent extends ApplicationCommandEvent {

    private final ApplicationCommand command;
    @Nullable
    private final Long guildId;

    public ApplicationCommandDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ApplicationCommand command,
                                         @Nullable Long guildId) {
        super(gateway, shardInfo);
        this.command = command;
        this.guildId = guildId;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event, if present.
     *
     * @return The ID of the guild involved, if present.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Requests to retrieve the {@link Guild} that had an application command deleted in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the command deleted in this event.
     *
     * @return The command deleted in this event.
     */
    public ApplicationCommand getCommand() {
        return command;
    }

}
