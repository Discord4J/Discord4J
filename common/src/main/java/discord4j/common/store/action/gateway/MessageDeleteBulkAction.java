package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.gateway.MessageDeleteBulk;

import java.util.List;

public class MessageDeleteBulkAction extends AbstractGatewayAction<List<MessageData>> {

    private final MessageDeleteBulk messageDeleteBulk;

    public MessageDeleteBulkAction(int shardIndex, MessageDeleteBulk messageDeleteBulk) {
        super(shardIndex);
        this.messageDeleteBulk = messageDeleteBulk;
    }

    public MessageDeleteBulk getMessageDeleteBulk() {
        return messageDeleteBulk;
    }
}
