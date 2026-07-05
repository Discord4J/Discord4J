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
package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageActivityData;

/**
 * Represent a partial message where just a few elements from a {@link Message} are present.
 *
 * @see <a href="https://docs.discord.com/developers/resources/message#message-object-message-activity-structure">
 * Activity Message Object</a>
 */
public class ActivityMessage implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageActivityData data;

    public ActivityMessage(final GatewayDiscordClient gateway, final MessageActivityData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Gets the data of the message activity.
     *
     * @return The data of the message activity.
     */
    public MessageActivityData getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "ActivityMessage{" +
                "data=" + this.data +
                '}';
    }

    /**
     * Represents the various types of messages activities.
     */
    public enum Type {
        /**
         * Unknown type.
         */
        UNKNOWN(-1),

        JOIN(1),

        SPECTATE(2),

        LISTEN(3),

        JOIN_REQUEST(5);

        /**
         * The underlying value as represented by Discord.
         */
        private final int value;

        Type(final int value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Gets the type of the activity message. It is guaranteed that invoking {@link #getValue()} from the returned enum will be
         * equal ({@code ==}) to the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of activity message.
         */
        public static Type of(final int value) {
            switch (value) {
                case 1: return JOIN;
                case 2: return SPECTATE;
                case 3: return LISTEN;
                case 5: return JOIN_REQUEST;
                default: return UNKNOWN;
            }
        }
    }
}
