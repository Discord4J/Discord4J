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
import discord4j.discordjson.json.MessageSnapshotsData;

/**
 * Represent the message associated with the {@link Message#getMessageReference()}.
 * This is a minimal subset of fields in a message.
 *
 * @see <a href="https://discord.com/developers/docs/resources/message#message-snapshot-object">
 * Message Snapshot Object</a>
 */
public class MessageSnapshot implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageSnapshotsData data;

    public MessageSnapshot(GatewayDiscordClient gateway, MessageSnapshotsData data) {
        this.gateway = gateway;
        this.data = data;
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Returns a partial message with minimal subset of fields in the forwarded message.
     *
     * @return A partial message with minimal subset of fields in the forwarded message.
     */
    public PartialMessage getMessage() {
        return new PartialMessage(this.gateway, this.data.message());
    }
}
