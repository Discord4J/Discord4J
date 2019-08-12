package discord4j.core.object.data.stored;

import discord4j.common.json.MessageReferenceResponse;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

public class MessageReferenceBean implements Serializable {

    private long channelId;
    private long guildId;
    @Nullable
    private long messageId;

    public MessageReferenceBean(final MessageReferenceResponse response) {
        channelId = response.getChannelId();
        guildId = response.getGuildId();
        messageId = response.getMessageId();
    }

    public MessageReferenceBean() {

    }

    public long getChannelId() {
        return channelId;
    }

    public long getGuildId() {
        return guildId;
    }

    @Nullable
    public long getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "MessageReferenceResponse{" +
            ", channelId=" + channelId +
            ", guildId=" + guildId +
            ", messageId=" + messageId +
            '}';
    }
}
