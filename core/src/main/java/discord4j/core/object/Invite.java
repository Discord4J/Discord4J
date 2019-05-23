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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.InviteBean;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.CategorizableChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

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
    private final InviteBean data;

    /**
     * Constructs a {@code Invite} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Invite(final ServiceMediator serviceMediator, final InviteBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
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
        return data.getCode();
    }

    /**
     * Gets the ID of the guild this invite is associated to.
     *
     * @return The ID of the guild this invite is associated to.
     */
    public final Snowflake getGuildId() {
        return Snowflake.of(data.getGuildId());
    }

    /**
     * Requests to retrieve the guild this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the ID of the channel this invite is associated to.
     *
     * @return The ID of the channel this invite is associated to.
     */
    public final Snowflake getChannelId() {
        return Snowflake.of(data.getChannelId());
    }

    /**
     * Requests to retrieve the channel this invite is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link CategorizableChannel channel} this invite is
     * associated to. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<CategorizableChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(CategorizableChannel.class);
    }

    /**
     * Requests to delete this invite.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the invite has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this invite while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the invite has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> delete(@Nullable final String reason) {
        return serviceMediator.getRestClient().getInviteService()
                .deleteInvite(getCode(), reason)
                .then()
                .subscriberContext(ctx -> ctx.put("shard", serviceMediator.getClientConfig().getShardIndex()));
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    InviteBean getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Invite{" +
                "data=" + data +
                '}';
    }
}
