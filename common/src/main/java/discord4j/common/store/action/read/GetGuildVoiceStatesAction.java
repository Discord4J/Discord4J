package discord4j.common.store.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.VoiceStateData;

public class GetGuildVoiceStatesAction implements ReadAction<PossiblyIncompleteList<VoiceStateData>> {

    private final long guildId;

    public GetGuildVoiceStatesAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
