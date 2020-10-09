package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.gateway.MessageDelete;

public class MessageDeleteAction extends AbstractGatewayAction<MessageData> {

    private final MessageDelete messageDelete;

    public MessageDeleteAction(int shardIndex, MessageDelete messageDelete) {
        super(shardIndex);
        this.messageDelete = messageDelete;
    }

    public MessageDelete getMessageDelete() {
        return messageDelete;
    }
}
