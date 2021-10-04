package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;

/**
 * Dispatched when a {@link StageInstance} is created.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#stage-instance-create">Stage Instance Create</a>
 */
public class StageInstanceCreateEvent extends Event {

    private final StageInstance stageInstance;

    public StageInstanceCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, final StageInstance stageInstance) {
        super(gateway, shardInfo);
        this.stageInstance = stageInstance;
    }

    /**
     * Get the created {@link StageInstance}
     *
     * @return The created {@link StageInstance}
     */
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
