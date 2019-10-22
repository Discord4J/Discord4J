package discord4j.rest.entity.data;

import discord4j.common.json.MessageReferenceResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public class MessageReferenceData implements Serializable {

    private final long channelId;
    @Nullable
    private final Long guildId;
    @Nullable
    private final Long messageId;

    public MessageReferenceData(final MessageReferenceResponse response) {
        channelId = response.getChannelId();
        guildId = response.getGuildId();
        messageId = response.getMessageId();
    }

    public long getChannelId() {
        return channelId;
    }

    @Nullable
    public Long getGuildId() {
        return guildId;
    }

    @Nullable
    public Long getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "MessageReferenceResponse{" +
            "channelId=" + channelId +
            ", guildId=" + guildId +
            ", messageId=" + messageId +
            '}';
    }
}
