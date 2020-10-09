package discord4j.common.store.layout.action.read;

import discord4j.discordjson.json.VoiceStateData;

public class GetVoiceStateByIdAction implements ReadAction<VoiceStateData> {

    private final long guildId;
    private final long userId;

    public GetVoiceStateByIdAction(long guildId, long userId) {
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
