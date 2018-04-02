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
import discord4j.core.object.bean.PermissionOverwriteBean;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord permission overwrite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#overwrite-object">Overwrite Object</a>
 */
public final class PermissionOverwrite implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord, must be non-null. */
    private final PermissionOverwriteBean data;

    /** The ID of the guild associated to this overwrite. */
    private final long guildId;

    /**
     * Constructs a {@code PermissionOverwrite} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     * @param guildId The ID of the guild associated to this overwrite.
     */
    public PermissionOverwrite(final ServiceMediator serviceMediator, final PermissionOverwriteBean data, final long guildId) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
        this.guildId = guildId;
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the ID of the role or user this overwrite is associated to.
     *
     * @return The ID of the role or user this overwrite is associated to.
     */
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the ID of the role this overwrite is associated to, if present.
     *
     * @return The ID of the role this overwrite is associated to, if present.
     */
    public Optional<Snowflake> getRoleId() {
        return Optional.of(getId()).filter(ignored -> getType() == Type.ROLE);
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
     * Gets the ID of the user this overwrite is associated to, if present.
     *
     * @return The ID of the user this overwrite is associated to, if present.
     */
    public Optional<Snowflake> getUserId() {
        return Optional.of(getId()).filter(ignored -> getType() == Type.MEMBER);
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
     * Gets the type of entity this overwrite is for.
     *
     * @return The type of entity this overwrite is for.
     */
    public Type getType() {
        return Type.of(data.getType());
    }

    /**
     * Gets the permissions explicitly allowed for this overwrite.
     *
     * @return The permissions explicitly allowed for this overwrite.
     */
    public PermissionSet getAllowed() {
        return PermissionSet.of(data.getAllow());
    }

    /**
     * Gets the permissions explicitly denied for this overwrite.
     *
     * @return The permissions explicitly denied for this overwrite.
     */
    public PermissionSet getDenied() {
        return PermissionSet.of(data.getDeny());
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

    /** The type of entity a {@link PermissionOverwrite} is explicitly for. */
    public enum Type {

        /** The {@link Role} entity. */
        ROLE("role"),

        /** The {@link Member} entity. */
        MEMBER("member");

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs a {@code PermissionOverwrite.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the type of permission overwrite. It is guaranteed that invoking {@link #getValue()} from the returned
         * enum will equal ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of permission overwrite.
         */
        public static Type of(final String value) {
            switch (value) {
                case "role": return ROLE;
                case "member": return MEMBER;
                default: throw new UnsupportedOperationException("Unknown Value: " + value);
            }
        }
    }
}
