package discord4j.common.store.action.read;

import discord4j.discordjson.json.EmojiData;

public class GetGuildEmojisAction implements ReadAction<EmojiData> {

    private final long guildId;
    private final boolean requireComplete;

    public GetGuildEmojisAction(long guildId, boolean requireComplete) {
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
