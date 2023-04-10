package discord4j.common.store.action.gateway;

import discord4j.discordjson.json.GuildScheduledEventData;
import discord4j.discordjson.json.gateway.GuildScheduledEventUpdate;

public class GuildScheduledEventUpdateAction extends ShardAwareAction<GuildScheduledEventData> {

    private final GuildScheduledEventUpdate guildScheduledEventUpdate;

    GuildScheduledEventUpdateAction(int shardIndex, GuildScheduledEventUpdate guildScheduledEventUpdate) {
        super(shardIndex);
        this.guildScheduledEventUpdate = guildScheduledEventUpdate;
    }

    public GuildScheduledEventUpdate getGuildScheduledEventUpdate() {
        return guildScheduledEventUpdate;
    }

}
