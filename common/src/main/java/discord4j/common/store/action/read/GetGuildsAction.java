package discord4j.common.store.action.read;

import discord4j.discordjson.json.GuildData;

public class GetGuildsAction implements ReadAction<GuildData> {

    private final boolean requireComplete;

    public GetGuildsAction(boolean requireComplete) {
        this.requireComplete = requireComplete;
    }

    public boolean requireComplete() {
        return requireComplete;
    }
}
