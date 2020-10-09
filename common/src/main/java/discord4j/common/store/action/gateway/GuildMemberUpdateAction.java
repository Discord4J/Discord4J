package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.MemberData;
import discord4j.discordjson.json.gateway.GuildMemberUpdate;

public class GuildMemberUpdateAction extends AbstractGatewayAction<MemberData> {

    private final GuildMemberUpdate guildMemberUpdate;

    public GuildMemberUpdateAction(int shardIndex, GuildMemberUpdate guildMemberUpdate) {
        super(shardIndex);
        this.guildMemberUpdate = guildMemberUpdate;
    }

    public GuildMemberUpdate getGuildMemberUpdate() {
        return guildMemberUpdate;
    }
}
