package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.gateway.GuildMemberAdd;

public class GuildMemberAddAction extends AbstractGatewayAction<Void> {

    private final GuildMemberAdd guildMemberAdd;

    public GuildMemberAddAction(int shardIndex, GuildMemberAdd guildMemberAdd) {
        super(shardIndex);
        this.guildMemberAdd = guildMemberAdd;
    }

    public GuildMemberAdd getGuildMemberAdd() {
        return guildMemberAdd;
    }
}
