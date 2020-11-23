package discord4j.core.spec;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ImmutableMessageReferenceData;
import discord4j.discordjson.json.MessageReferenceData;

/**
 * Spec used to create a message reference.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#message-object-message-reference-structure">Message Reference</a>
 */
public class MessageReferenceSpec implements Spec<MessageReferenceData> {

    private final ImmutableMessageReferenceData.Builder requestBuilder = MessageReferenceData.builder();

    /**
     * Sets the ID of the originating message.
     *
     * @param messageId The ID of the originating message.
     * @return This spec.
     */
    public MessageReferenceSpec setMessageId(Snowflake messageId) {
        requestBuilder.messageId(messageId.asString());
        return this;
    }

    /**
     * Sets the ID of the originating message's channel.
     *
     * @param channelId The ID of the originating message's channel.
     * @return This spec.
     */
    public MessageReferenceSpec setChannelId(Snowflake channelId) {
        requestBuilder.channelId(channelId.asString());
        return this;
    }

    /**
     * Sets the ID of the originating message's guild.
     *
     * @param guildId The ID of the originating message's guild.
     * @return This spec.
     */
    public MessageReferenceSpec setGuildId(Snowflake guildId) {
        requestBuilder.guildId(guildId.asString());
        return this;
    }

    @Override
    public MessageReferenceData asRequest() {
        return requestBuilder.build();
    }
}
