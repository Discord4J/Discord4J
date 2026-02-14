/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.event.domain.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.entity.Guild;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when an application command relevant to the current user is updated.
 * This event is dispatched by Discord.
 *
 * @see
 * <a href="https://discord.com/developers/docs/topics/gateway#application-command-update">Application Command Update</a>
 */
public class ApplicationCommandUpdateEvent extends ApplicationCommandEvent {

    private final ApplicationCommand command;
    @Nullable
    private final Long guildId;

    public ApplicationCommandUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo,
                                         ApplicationCommand command, @Nullable Long guildId) {
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
     * Requests to retrieve the {@link Guild} that had an application command updated in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the command updated in this event.
     *
     * @return The command updated in this event.
     */
    public ApplicationCommand getCommand() {
        return command;
    }

}
