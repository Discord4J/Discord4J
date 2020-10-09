package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.MessageReactionRemoveEmoji;

public class MessageReactionRemoveEmojiAction extends AbstractGatewayAction<Void> {

    private final MessageReactionRemoveEmoji messageReactionRemoveEmoji;

    public MessageReactionRemoveEmojiAction(int shardIndex, MessageReactionRemoveEmoji messageReactionRemoveEmoji) {
        super(shardIndex);
        this.messageReactionRemoveEmoji = messageReactionRemoveEmoji;
    }

    public MessageReactionRemoveEmoji getMessageReactionRemoveEmoji() {
        return messageReactionRemoveEmoji;
    }
}
