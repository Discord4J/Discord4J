package discord4j.common.store.action.read;

import discord4j.common.store.api.StoreAction;
import discord4j.discordjson.json.GuildScheduledEventData;

/**
 * Action representing a fetch from stores for scheduled events related to the given guildId in constructor
 */
public class GetGuildScheduledEventsAction implements StoreAction<GuildScheduledEventData> {

    private final long guildId;

    GetGuildScheduledEventsAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
