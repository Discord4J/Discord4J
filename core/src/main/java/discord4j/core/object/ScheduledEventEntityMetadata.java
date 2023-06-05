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
import discord4j.discordjson.json.GuildScheduledEventEntityMetadataData;

import java.util.Objects;
import java.util.Optional;

public class ScheduledEventEntityMetadata implements DiscordObject {

    /** The gateway associated with this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final GuildScheduledEventEntityMetadataData data;

    /**
     * Constructs a {@code ScheduledEventEntityMetadata} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated with this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public ScheduledEventEntityMetadata(GatewayDiscordClient gateway, GuildScheduledEventEntityMetadataData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the data of the entity metadata.
     *
     * @return The data of the entity metadata.
     */
    public GuildScheduledEventEntityMetadataData getData() {
        return data;
    }

    /**
     * Gets the location of the entity metadata, if present.
     *
     * @return The location of the entity metadata, or {@code Optional#empty()} if not present.
     */
    public Optional<String> getLocation() {
        return data.location().toOptional();
    }

    @Override
    public String toString() {
        return "ScheduledEventEntityMetadata{"+
            "data=" + data +
            '}';
    }
}
