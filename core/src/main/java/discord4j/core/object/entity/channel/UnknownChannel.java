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
package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.ChannelData;

/**
 * A {@link Channel} implementation representing an unsupported or not implemented channel type. Use {@link #getData()}
 * to retrieve entity information and {@link #getRestChannel()} to obtain an instance to perform REST API operations
 * on this channel.
 */
public class UnknownChannel extends BaseChannel {

    /**
     * Constructs an {@code UnknownChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public UnknownChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        super(gateway, data);
    }

    @Override
    public String toString() {
        return "UnknownChannel{" +
                "data=" + getData() +
                '}';
    }
}
