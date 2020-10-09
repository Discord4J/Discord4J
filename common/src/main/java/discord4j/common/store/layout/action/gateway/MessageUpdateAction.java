package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.gateway.MessageUpdate;

public class MessageUpdateAction extends AbstractGatewayAction<MessageData> {

    private final MessageUpdate messageUpdate;

    public MessageUpdateAction(int shardIndex, MessageUpdate messageUpdate) {
        super(shardIndex);
        this.messageUpdate = messageUpdate;
    }

    public MessageUpdate getMessageUpdate() {
        return messageUpdate;
    }
}
