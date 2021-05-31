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

package discord4j.core.spec.legacy;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ImmutableMessageReferenceData;
import discord4j.discordjson.json.MessageReferenceData;

/**
 * LegacySpec used to create a message reference.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#message-object-message-reference-structure">Message Reference</a>
 */
public class LegacyMessageReferenceSpec implements LegacySpec<MessageReferenceData> {

    private final ImmutableMessageReferenceData.Builder requestBuilder = MessageReferenceData.builder();

    /**
     * Sets the ID of the originating message.
     *
     * @param messageId The ID of the originating message.
     * @return This spec.
     */
    public LegacyMessageReferenceSpec setMessageId(Snowflake messageId) {
        requestBuilder.messageId(messageId.asString());
        return this;
    }

    /**
     * Sets the ID of the originating message's channel.
     *
     * @param channelId The ID of the originating message's channel.
     * @return This spec.
     */
    public LegacyMessageReferenceSpec setChannelId(Snowflake channelId) {
        requestBuilder.channelId(channelId.asString());
        return this;
    }

    /**
     * Sets the ID of the originating message's guild.
     *
     * @param guildId The ID of the originating message's guild.
     * @return This spec.
     */
    public LegacyMessageReferenceSpec setGuildId(Snowflake guildId) {
        requestBuilder.guildId(guildId.asString());
        return this;
    }

    /**
     * Sets to error if the referenced message doesn't exist instead of sending as a normal (non-reply) message,
     * default true.
     *
     * @param failIfNotExists Whether to error if the referenced message doesn't exist instead of sending as a normal
     *                        (non-reply) message.
     * @return This spec.
     */
    public LegacyMessageReferenceSpec setFailIfNotExists(boolean failIfNotExists) {
        requestBuilder.failIfNotExists(failIfNotExists);
        return this;
    }

    @Override
    public MessageReferenceData asRequest() {
        return requestBuilder.build();
    }
}
