package discord4j.common.store.layout.action.read;

import discord4j.discordjson.json.PresenceData;

public class GetPresenceByIdAction implements ReadAction<PresenceData> {

    private final long guildId;
    private final long userId;

    public GetPresenceByIdAction(long guildId, long userId) {
        this.guildId = guildId;
        this.userId = userId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getUserId() {
        return userId;
    }
}
