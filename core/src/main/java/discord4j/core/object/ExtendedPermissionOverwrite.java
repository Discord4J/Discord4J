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

import discord4j.discordjson.json.OverwriteData;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A Discord permission overwrite.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#overwrite-object">Overwrite Object</a>
 */
public final class ExtendedPermissionOverwrite extends PermissionOverwrite implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The ID of the guild associated to this overwrite. */
    private final long guildId;

    /** The ID of the channel associated to this overwrite. */
    private final long channelId;

    /**
     * Constructs a {@code ExtendedPermissionOverwrite} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild associated to this overwrite.
     * @param channelId The ID of the channel associated to this overwrite.
     */
    public ExtendedPermissionOverwrite(final GatewayDiscordClient gateway, final OverwriteData data,
                                       final long guildId, final long channelId) {
        super(data.allow(), data.deny(), Snowflake.asLong(data.id()), Type.of(data.type()));
        this.gateway = Objects.requireNonNull(gateway);
        this.guildId = guildId;
        this.channelId = channelId;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Requests to retrieve the role this overwrite is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} this overwrite is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRole() {
        return Mono.justOrEmpty(getRoleId()).flatMap(id -> gateway.getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the role this overwrite is associated to, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the role
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} this overwrite is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRole(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getRoleId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy)
                        .getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the user this overwrite is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this overwrite is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return Mono.justOrEmpty(getMemberId()).flatMap(gateway::getUserById);
    }

    /**
     * Requests to retrieve the user this overwrite is associated to, if present, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the user
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this overwrite is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getMemberId())
                .flatMap(id -> gateway.withRetrievalStrategy(retrievalStrategy).getUserById(id));
    }

    /**
     * Gets the ID of the guild associated to this overwrite.
     *
     * @return The ID of the guild associated to this overwrite.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the guild associated to this overwrite.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} associated to this overwrite.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return gateway.getGuildById(getGuildId());
    }

    /**
     * Requests to retrieve the guild associated to this overwrite, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the guild
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} associated to this overwrite.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy).getGuildById(getGuildId());
    }

    /**
     * Gets the ID of the channel associated to this overwrite.
     *
     * @return The ID of the channel associated to this overwrite.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Requests to retrieve the channel associated to this overwrite.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} associated to this
     * overwrite. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getChannel() {
        return gateway.getChannelById(getChannelId()).cast(GuildChannel.class);
    }

    /**
     * Requests to retrieve the channel associated to this overwrite, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the channel
     * @return A {@link Mono} where, upon successful completion, emits the {@link GuildChannel} associated to this
     * overwrite. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<GuildChannel> getChannel(EntityRetrievalStrategy retrievalStrategy) {
        return gateway.withRetrievalStrategy(retrievalStrategy)
                .getChannelById(getChannelId())
                .cast(GuildChannel.class);
    }

    /**
     * Requests to delete this permission overwrite.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the permission overwrite has
     * been deleted. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return delete(null);
    }

    /**
     * Requests to delete this permission overwrite while optionally specifying a reason.
     *
     * @param reason The reason, if present.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the permission overwrite has
     * been deleted. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getChannelService()
                .deleteChannelPermission(channelId, getTargetId().asLong(), reason);
    }
}
