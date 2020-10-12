package discord4j.common.store.action.read;

import discord4j.discordjson.json.PresenceData;

public class GetGuildPresencesAction implements ReadAction<PresenceData> {

    private final long guildId;
    private final boolean requireComplete;

    public GetGuildPresencesAction(long guildId, boolean requireComplete) {
        this.guildId = guildId;
        this.requireComplete = requireComplete;
    }

    public long getGuildId() {
        return guildId;
    }

    public boolean requireComplete() {
        return requireComplete;
    }
}
