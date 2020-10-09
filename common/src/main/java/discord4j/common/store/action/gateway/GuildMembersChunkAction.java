package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.GuildMembersChunk;

public class GuildMembersChunkAction extends AbstractGatewayAction<Void> {

    private final GuildMembersChunk guildMembersChunk;

    public GuildMembersChunkAction(int shardIndex, GuildMembersChunk guildMembersChunk) {
        super(shardIndex);
        this.guildMembersChunk = guildMembersChunk;
    }

    public GuildMembersChunk getGuildMembersChunk() {
        return guildMembersChunk;
    }
}
