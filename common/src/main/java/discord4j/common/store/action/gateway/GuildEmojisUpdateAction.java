package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.gateway.GuildEmojisUpdate;

public class GuildEmojisUpdateAction extends AbstractGatewayAction<EmojiData> {

    private final GuildEmojisUpdate guildEmojisUpdate;

    public GuildEmojisUpdateAction(int shardIndex, GuildEmojisUpdate guildEmojisUpdate) {
        super(shardIndex);
        this.guildEmojisUpdate = guildEmojisUpdate;
    }

    public GuildEmojisUpdate getGuildEmojisUpdate() {
        return guildEmojisUpdate;
    }
}
