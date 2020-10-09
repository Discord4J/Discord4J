package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.gateway.MessageReactionRemove;

public class MessageReactionRemoveAction extends AbstractGatewayAction<Void> {

    private final MessageReactionRemove messageReactionRemove;

    public MessageReactionRemoveAction(int shardIndex, MessageReactionRemove messageReactionRemove) {
        super(shardIndex);
        this.messageReactionRemove = messageReactionRemove;
    }

    public MessageReactionRemove getMessageReactionRemove() {
        return messageReactionRemove;
    }
}
