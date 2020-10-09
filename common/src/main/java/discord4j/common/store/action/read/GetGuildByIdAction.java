package discord4j.common.store.action.read;

import discord4j.discordjson.json.GuildData;

public class GetGuildByIdAction implements ReadAction<GuildData> {

    private final long guildId;

    public GetGuildByIdAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
