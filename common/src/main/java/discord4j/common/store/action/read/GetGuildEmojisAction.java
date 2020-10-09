package discord4j.common.store.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.EmojiData;

public class GetGuildEmojisAction implements ReadAction<PossiblyIncompleteList<EmojiData>> {

    private final long guildId;

    public GetGuildEmojisAction(long guildId) {
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }
}
