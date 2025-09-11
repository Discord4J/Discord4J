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
package discord4j.core.object.entity;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.discordjson.json.PinnedMessageData;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class PinnedMessageReference implements DiscordObject {

    /**
     * The gateway associated to this object.
     */
    private final GatewayDiscordClient gateway;

    /**
     * The raw data as represented by Discord.
     */
    private final PinnedMessageData data;

    public PinnedMessageReference(GatewayDiscordClient gateway, PinnedMessageData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Gets the raw data of this entity.
     *
     * @return The raw data of this entity
     */
    public PinnedMessageData getData() {
        return this.data;
    }

    /**
     * Gets the message referenced.
     *
     * @return The message referenced
     */
    public Message getMessage() {
        return new Message(this.getClient(), this.data.message());
    }

    /**
     * Gets when the message was pinned.
     *
     * @return The instant when the message was pinned
     */
    public Instant getPinnedAt() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this.data.pinnedAt(), Instant::from);
    }
}
