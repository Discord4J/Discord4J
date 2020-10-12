package discord4j.common.store.action.read;

import discord4j.discordjson.json.ChannelData;

public class GetGuildChannelsAction implements ReadAction<ChannelData> {

    private final long guildId;
    private final boolean requireComplete;

    public GetGuildChannelsAction(long guildId, boolean requireComplete) {
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
