package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a {@link StageInstance} is deleted.
 * <p>
 * The old stage instance may not be present if stage instances are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#stage-instance-delete">Stage Instance Delete</a>
 */
public class StageInstanceDeleteEvent extends Event {

    private final StageInstance old;

    public StageInstanceDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, @Nullable final StageInstance old) {
        super(gateway, shardInfo);
        this.old = old;
    }

    /**
     * Get the deleted {@link StageInstance}, if present
     *
     * @return The deleted {@link StageInstance}, if present
     */
    public Optional<StageInstance> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "StageInstanceDeleteEvent{" +
                "old=" + old +
                '}';
    }
}
