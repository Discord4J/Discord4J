package discord4j.common.store.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.ChannelData;

public class GetGuildChannelsAction implements ReadAction<PossiblyIncompleteList<ChannelData>> {

    private final long guildId;

    public GetGuildChannelsAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
