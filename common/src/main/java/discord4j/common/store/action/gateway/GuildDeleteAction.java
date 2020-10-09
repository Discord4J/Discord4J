package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.gateway.GuildDelete;

public class GuildDeleteAction extends AbstractGatewayAction<GuildData> {

    private final GuildDelete guildDelete;

    public GuildDeleteAction(int shardIndex, GuildDelete guildDelete) {
        super(shardIndex);
        this.guildDelete = guildDelete;
    }

    public GuildDelete getGuildDelete() {
        return guildDelete;
    }
}
