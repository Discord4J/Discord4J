package discord4j.common.store.layout.action.read;

import discord4j.discordjson.json.MessageData;

public class GetMessageByIdAction implements ReadAction<MessageData> {

    private final long channelId;
    private final long messageId;

    public GetMessageByIdAction(long channelId, long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getMessageId() {
        return messageId;
    }
}
