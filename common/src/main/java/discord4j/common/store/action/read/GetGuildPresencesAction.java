package discord4j.common.store.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.PresenceData;

public class GetGuildPresencesAction implements ReadAction<PossiblyIncompleteList<PresenceData>> {

    private final long guildId;

    public GetGuildPresencesAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
