package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.gateway.GuildMemberRemove;

public class GuildMemberRemoveAction extends AbstractGatewayAction<MemberData> {

    private final GuildMemberRemove guildMemberRemove;

    public GuildMemberRemoveAction(int shardIndex, GuildMemberRemove guildMemberRemove) {
        super(shardIndex);
        this.guildMemberRemove = guildMemberRemove;
    }

    public GuildMemberRemove getGuildMemberRemove() {
        return guildMemberRemove;
    }
}
