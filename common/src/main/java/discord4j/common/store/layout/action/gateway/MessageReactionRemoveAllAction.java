package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.gateway.MessageReactionRemoveAll;

public class MessageReactionRemoveAllAction extends AbstractGatewayAction<Void> {

    private final MessageReactionRemoveAll messageReactionRemoveAll;

    public MessageReactionRemoveAllAction(int shardIndex, MessageReactionRemoveAll messageReactionRemoveAll) {
        super(shardIndex);
        this.messageReactionRemoveAll = messageReactionRemoveAll;
    }

    public MessageReactionRemoveAll getMessageReactionRemoveAll() {
        return messageReactionRemoveAll;
    }
}
