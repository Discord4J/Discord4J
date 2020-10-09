package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.gateway.GuildCreate;

public class GuildCreateAction extends AbstractGatewayAction<Void> {

    private final GuildCreate guildCreate;

    public GuildCreateAction(int shardIndex, GuildCreate guildCreate) {
        super(shardIndex);
        this.guildCreate = guildCreate;
    }

    public GuildCreate getGuildCreate() {
        return guildCreate;
    }
}
