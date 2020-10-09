package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.MessageReactionAdd;

public class MessageReactionAddAction extends AbstractGatewayAction<Void> {

    private final MessageReactionAdd messageReactionAdd;

    public MessageReactionAddAction(int shardIndex, MessageReactionAdd messageReactionAdd) {
        super(shardIndex);
        this.messageReactionAdd = messageReactionAdd;
    }

    public MessageReactionAdd getMessageReactionAdd() {
        return messageReactionAdd;
    }
}
