package discord4j.common.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import reactor.util.annotation.Nullable;

public class MessageReferenceResponse {

    @JsonProperty("channel_id")
    @UnsignedJson
    private long channelId;
    @JsonProperty("guild_id")
    @UnsignedJson
    @Nullable
    private Long guildId;
    @JsonProperty("message_id")
    @UnsignedJson
    @Nullable
    private Long messageId;

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
            ", channelId=" + channelId +
            ", guildId=" + guildId +
            ", messageId=" + messageId +
            '}';
    }
}
