package discord4j.common.store.layout.action.gateway;

import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.json.gateway.VoiceStateUpdateDispatch;

public class VoiceStateUpdateDispatchAction extends AbstractGatewayAction<VoiceStateData> {

    private final VoiceStateUpdateDispatch voiceStateUpdateDispatch;

    public VoiceStateUpdateDispatchAction(int shardIndex, VoiceStateUpdateDispatch voiceStateUpdateDispatch) {
        super(shardIndex);
        this.voiceStateUpdateDispatch = voiceStateUpdateDispatch;
    }

    public VoiceStateUpdateDispatch getVoiceStateUpdateDispatch() {
        return voiceStateUpdateDispatch;
    }
}
