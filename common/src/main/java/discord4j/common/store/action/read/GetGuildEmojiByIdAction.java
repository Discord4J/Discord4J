package discord4j.common.store.action.read;

import discord4j.discordjson.json.EmojiData;

public class GetGuildEmojiByIdAction implements ReadAction<EmojiData> {

    private final long guildId;
    private final long emojiId;

    public GetGuildEmojiByIdAction(long guildId, long emojiId) {
        this.guildId = guildId;
        this.emojiId = emojiId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getEmojiId() {
        return emojiId;
    }
}
