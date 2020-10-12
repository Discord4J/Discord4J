package discord4j.common.store.action.read;

import discord4j.discordjson.json.VoiceStateData;

public class GetChannelVoiceStatesAction implements ReadAction<VoiceStateData> {

    private final long channelId;

    public GetChannelVoiceStatesAction(long channelId) {
        this.channelId = channelId;
    }

    public long getChannelId() {
        return channelId;
    }
}
