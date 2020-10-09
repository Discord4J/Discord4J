package discord4j.common.store.action.read;

import discord4j.common.store.util.PossiblyIncompleteList;
import discord4j.discordjson.json.VoiceStateData;

public class GetChannelVoiceStatesAction implements ReadAction<PossiblyIncompleteList<VoiceStateData>> {

    private final long channelId;

    public GetChannelVoiceStatesAction(long channelId) {
        this.channelId = channelId;
    }

    public long getChannelId() {
        return channelId;
    }
}
