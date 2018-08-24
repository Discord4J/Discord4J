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
import discord4j.core.object.data.stored.PermissionOverwriteBean;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord permission overwrite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#overwrite-object">Overwrite Object</a>
 */
public final class ExtendedPermissionOverwrite extends TargetedPermissionOverwrite implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The ID of the guild associated to this overwrite. */
    private final long guildId;

    /** The ID of the channel associated to this overwrite. */
    private final long channelId;

    /**
     * Constructs a {@code ExtendedPermissionOverwrite} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild associated to this overwrite.
     * @param channelId The ID of the channel associated to this overwrite.
     */
    public ExtendedPermissionOverwrite(final ServiceMediator serviceMediator, final PermissionOverwriteBean data,
                                       final long guildId, final long channelId) {
        super(data.getAllow(), data.getDeny(), data.getId(), data.getType());
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.guildId = guildId;
        this.channelId = channelId;
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Requests to retrieve the role this overwrite is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Role} this overwrite is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Role> getRole() {
        return Mono.justOrEmpty(getRoleId()).flatMap(id -> getClient().getRoleById(getGuildId(), id));
    }

    /**
     * Requests to retrieve the user this overwrite is associated to, if present.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} this overwrite is associated to,
     * if present. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return Mono.justOrEmpty(getUserId()).flatMap(getClient()::getUserById);
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
        return getClient().getGuildById(getGuildId());
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
        return getClient().getGuildChannelById(getChannelId());
    }

    /**
     * Requests to delete this permission overwrite.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the permission overwrite has
     * been deleted. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> delete() {
        return serviceMediator.getRestClient().getChannelService()
            .deleteChannelPermission(channelId, getTargetId().asLong());
    }
}
