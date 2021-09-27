package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.StageInstance;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class StageInstanceUpdateEvent extends Event {

    private final StageInstance current;
    private final StageInstance old;

    public StageInstanceUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, final StageInstance current, @Nullable final StageInstance old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    public StageInstance getCurrent() {
        return current;
    }

    public Optional<StageInstance> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "StageInstanceUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
