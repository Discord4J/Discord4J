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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.common.json.response.InviteResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A Discord invite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/invite">Invite Resource</a>
 */
public class Invite implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final InviteResponse invite;

    /**
     * Constructs a {@code Invite} with an associated serviceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param invite The raw data as represented by Discord, must be non-null.
     */
    public Invite(final ServiceMediator serviceMediator, final InviteResponse invite) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.invite = Objects.requireNonNull(invite);
    }

    @Override
    public final DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the invite code (unique ID).
     *
     * @return The invite code (unique ID).
     */
    public final String getCode() {
        return invite.getCode();
    }

    /**
     * Gets the ID of the guild this invite is associated to.
     *
     * @return The ID of the guild this invite is associated to.
     */
    public final Snowflake getGuildId() {
        return Snowflake.of(invite.getGuild().getId());
    }

    /**
     * Requests to retrieve the guild this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Guild> getGuild() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the ID of the channel this invite is associated to.
     *
     * @return The ID of the channel this invite is associated to.
     */
    public final Snowflake getChannelId() {
        return Snowflake.of(invite.getChannel().getId());
    }

    /**
     * Requests to retrieve the channel this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<TextChannel> getChannel() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    protected final InviteResponse getInvite() {
        return invite;
    }
}
