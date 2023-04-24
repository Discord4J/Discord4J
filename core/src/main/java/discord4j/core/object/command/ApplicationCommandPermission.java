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

package discord4j.core.object.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.ApplicationCommandPermissionsData;

/**
 * Represents an individual application command permission, allowing you to enable or disable commands for specific
 * users, roles, or channels within a guild.
 */
public class ApplicationCommandPermission implements DiscordObject {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;
    /**
     * The guild this command permission belongs to.
     */
    private final long guildId;
    /**
     * The raw data as represented by Discord.
     */
    private final ApplicationCommandPermissionsData data;

    /**
     * Constructs an {@code ApplicationCommandPermission} with an associated
     * {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway the {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data the raw data as represented by Discord, must be non-null.
     */
    public ApplicationCommandPermission(GatewayDiscordClient gateway, Snowflake guildId,
                                        ApplicationCommandPermissionsData data) {
        this.gateway = gateway;
        this.guildId = guildId.asLong();
        this.data = data;
    }

    /**
     * Return the ID of the role, user, or channel. It can also be a permission constant which can be detected by
     * {@link #appliesToEveryone()} or {@link #appliesToAllChannels()}.
     *
     * @return the permission target as a Snowflake ID
     */
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Returns whether this role permission uses a constant representing it applies to everyone.
     *
     * @return {@code true} if this permission applies to all members in a guild, {@code false} if otherwise
     */
    public boolean appliesToEveryone() {
        return guildId == data.id().asLong();
    }

    /**
     * Returns whether this channel permission uses a constant representing it applies to all channels.
     *
     * @return {@code true} if this permission applies to all channels in a guild, {@code false} if otherwise
     */
    public boolean appliesToAllChannels() {
        return guildId - 1 == data.id().asLong();
    }

    /**
     * Returns the type of this permission.
     *
     * @return the type of this permission
     */
    public Type getType() {
        return Type.of(data.type());
    }

    /**
     * Returns whether this permission allows or disallows a command for the target given by {@link #getType()} and
     * {@link #getId()}.
     *
     * @return {@code true} if this permission is allowing the command, {@code false} if it disallows it.
     */
    public boolean isAllowed() {
        return data.permission();
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Represents the various types of application command permissions.
     */
    public enum Type {

        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        /**
         * A role permission, can also target all members in the guild.
         */
        ROLE(1),

        /**
         * A user permission.
         */
        USER(2),

        /**
         * A channel permission, can also target all channels in the guild.
         */
        CHANNEL(3);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        /**
         * Constructs an {@code ApplicationCommandPermission.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the type of permission. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * be equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of permission.
         */
        public static Type of(final int value) {
            switch (value) {
                case 1: return ROLE;
                case 2: return USER;
                case 3: return CHANNEL;
                default: return UNKNOWN;
            }
        }
    }

    @Override
    public String toString() {
        return "ApplicationCommandPermission{" +
                "data=" + data +
                '}';
    }
}
