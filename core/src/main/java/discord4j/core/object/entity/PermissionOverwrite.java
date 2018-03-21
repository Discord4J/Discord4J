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

import discord4j.core.Client;
import discord4j.core.Shard;
import discord4j.core.object.PermissionSet;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.bean.PermissionOverwriteBean;

import java.util.Arrays;
import java.util.Objects;

/**
 * A Discord permission overwrite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#overwrite-object">Overwrite Object</a>
 */
public final class PermissionOverwrite implements Entity {

    /** The Client associated to this object. */
    private final Client client;

    /** The raw data as represented by Discord, must be non-null. */
    private final PermissionOverwriteBean data;

    /**
     * Constructs a {@code PermissionOverwrite} with an associated client and Discord data.
     *
     * @param client The Client associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public PermissionOverwrite(final Client client, final PermissionOverwriteBean data) {
        this.client = Objects.requireNonNull(client);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public Shard getShard() {
        return client.getShard();
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    /**
     * Gets the type of entity this overwrite is for.
     *
     * @return The type of entity this overwrite is for.
     */
    public Type getType() {
        return Arrays.stream(Type.values())
                .filter(type -> Objects.equals(data.getType(), type.value))
                .findFirst() // If this throws Discord added something
                .orElseThrow(UnsupportedOperationException::new);
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
    }
}
