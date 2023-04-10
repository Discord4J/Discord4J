package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.GuildScheduledEventData;
import discord4j.discordjson.json.gateway.GuildScheduledEventDelete;

public class GuildScheduledEventDeleteAction extends ShardAwareAction<GuildScheduledEventData> {

    private final GuildScheduledEventDelete guildScheduledEventDelete;

    GuildScheduledEventDeleteAction(int shardIndex, GuildScheduledEventDelete guildScheduledEventDelete) {
        super(shardIndex);
        this.guildScheduledEventDelete = guildScheduledEventDelete;
    }

    public GuildScheduledEventDelete getGuildScheduledEventDelete() {
        return guildScheduledEventDelete;
    }

}
