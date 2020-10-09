package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.RoleData;
import discord4j.discordjson.json.gateway.GuildRoleUpdate;

public class GuildRoleUpdateAction extends AbstractGatewayAction<RoleData> {

    private final GuildRoleUpdate guildRoleUpdate;

    public GuildRoleUpdateAction(int shardIndex, GuildRoleUpdate guildRoleUpdate) {
        super(shardIndex);
        this.guildRoleUpdate = guildRoleUpdate;
    }

    public GuildRoleUpdate getGuildRoleUpdate() {
        return guildRoleUpdate;
    }
}
