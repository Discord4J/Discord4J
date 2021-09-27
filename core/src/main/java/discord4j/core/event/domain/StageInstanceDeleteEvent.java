package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class StageInstanceDeleteEvent extends Event {

    private final StageInstance old;

    public StageInstanceDeleteEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, @Nullable final StageInstance old) {
        super(gateway, shardInfo);
        this.old = old;
    }

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
