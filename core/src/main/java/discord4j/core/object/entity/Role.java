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
package discord4j.core.object.entity;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.entity.bean.RoleBean;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import discord4j.core.trait.Positionable;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Objects;

/**
 * A Discord role.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/permissions#role-object">Role Object</a>
 */
public final class Role implements Entity, Positionable {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final RoleBean data;

    /** The ID of the guild this role is associated to. */
    private final long guildId;

    /**
     * Constructs a {@code Role} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild this role is associated to.
     */
    public Role(final ServiceMediator serviceMediator, final RoleBean data, final long guildId) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    @Override
    public Mono<Integer> getPosition() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    /**
     * Gets the role name.
     *
     * @return The role name.
     */
    public String getName() {
        return data.getName();
    }

    /**
     * Gets the color assigned to this role.
     *
     * @return The color assigned to this role.
     */
    public Color getColor() {
        return new Color(data.getColor(), true);
    }

    /**
     * Gets whether if this role is pinned in the user listing.
     *
     * @return {@code true} if this role is pinned in the user listing, {@code false} otherwise.
     */
    public boolean isHoisted() {
        return data.isHoist();
    }

    /**
     * Gets the permissions assigned to this role.
     *
     * @return The permissions assigned to this role.
     */
    public PermissionSet getPermissions() {
        return PermissionSet.of(data.getPermissions());
    }

    /**
     * Gets whether this role is managed by an integration.
     *
     * @return {@code true} if this role is managed by an integration, {@code false} otherwise.
     */
    public boolean isManaged() {
        return data.isManaged();
    }

    /**
     * Gets whether this role is mentionable.
     *
     * @return {@code true} if this role is mentionable, {@code false} otherwise.
     */
    public boolean isMentionable() {
        return data.isMentionable();
    }

    /**
     * Gets the ID of the guild this role is associated to.
     *
     * @return The ID of the guild this role is associated to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retireve the guild this role is associated to.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this role is associated
     * to. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the <i>raw</i> mention. This is the format utilized to directly mention another role (assuming the role
     * exists in context of the mention).
     *
     * @return The <i>raw</i> mention.
     */
    public String getMention() {
        return "<@&" + getId().asString() + ">";
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }
}
