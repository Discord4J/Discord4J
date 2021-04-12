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

import discord4j.discordjson.json.MessageReferenceData;
import discord4j.common.annotations.Experimental;
import discord4j.core.GatewayDiscordClient;
import discord4j.common.util.Snowflake;

import java.util.Objects;
import java.util.Optional;

/**
 * A Message Reference used by the Server Following feature.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#message-object-message-structure">
 * MessageReference Object</a>
 */
@Experimental
public class MessageReference implements DiscordObject {

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final MessageReferenceData data;

    /**
     * Constructs a {@code MessageReference} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public MessageReference(final GatewayDiscordClient gateway, final MessageReferenceData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets the data of the message reference.
     *
     * @return The data of the message reference.
     */
    public MessageReferenceData getData() {
        return data;
    }

    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId().toOptional().orElseThrow(IllegalStateException::new));
    }

    public Optional<Snowflake> getGuildId() {
        return data.guildId().toOptional()
                .map(Snowflake::of);
    }

    public Optional<Snowflake> getMessageId() {
        return data.messageId().toOptional()
                .map(Snowflake::of);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    @Override
    public String toString() {
        return "MessageReference{" +
                "data=" + data +
                '}';
    }
}
