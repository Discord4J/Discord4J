package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.MessageCreate;

public class MessageCreateAction extends AbstractGatewayAction<Void> {

    private final MessageCreate messageCreate;

    public MessageCreateAction(int shardIndex, MessageCreate messageCreate) {
        super(shardIndex);
        this.messageCreate = messageCreate;
    }

    public MessageCreate getMessageCreate() {
        return messageCreate;
    }
}
