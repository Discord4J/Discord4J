package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.GuildRoleCreate;

public class GuildRoleCreateAction extends AbstractGatewayAction<Void> {

    private final GuildRoleCreate guildRoleCreate;

    public GuildRoleCreateAction(int shardIndex, GuildRoleCreate guildRoleCreate) {
        super(shardIndex);
        this.guildRoleCreate = guildRoleCreate;
    }

    public GuildRoleCreate getGuildRoleCreate() {
        return guildRoleCreate;
    }
}
