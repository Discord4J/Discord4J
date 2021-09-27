package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;

public class StageInstanceCreateEvent extends Event {

    private final StageInstance stageInstance;

    public StageInstanceCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, final StageInstance stageInstance) {
        super(gateway, shardInfo);
        this.stageInstance = stageInstance;
    }

    public StageInstance getStageInstance() {
        return stageInstance;
    }

    @Override
    public String toString() {
        return "StageInstanceCreateEvent{" +
                "stageInstance=" + stageInstance +
                '}';
    }
}
