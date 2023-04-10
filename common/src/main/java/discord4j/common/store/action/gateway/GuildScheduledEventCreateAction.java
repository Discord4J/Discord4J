package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.gateway.GuildScheduledEventCreate;

public class GuildScheduledEventCreateAction extends ShardAwareAction<Void> {

    private final GuildScheduledEventCreate guildScheduledEventCreate;

    GuildScheduledEventCreateAction(int shardIndex, GuildScheduledEventCreate guildScheduledEventCreate) {
        super(shardIndex);
        this.guildScheduledEventCreate = guildScheduledEventCreate;
    }

    public GuildScheduledEventCreate getGuildScheduledEventCreate() {
        return guildScheduledEventCreate;
    }

}
