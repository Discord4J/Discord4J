package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.StageInstanceEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.StageInstanceData;
import discord4j.rest.entity.RestRole;
import discord4j.rest.entity.RestStageInstance;
import reactor.core.CorePublisher;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

public final class StageInstance implements Entity {

    private final GatewayDiscordClient gateway;

    private final StageInstanceData data;

    private final RestStageInstance rest;

    public StageInstance(final GatewayDiscordClient gateway, final StageInstanceData data) {
        this.gateway = gateway;
        this.data = data;
        this.rest = RestStageInstance.create(gateway.rest(), Snowflake.of(data.channelId()));
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    public StageInstanceData getData() {
        return this.data;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return EntityUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "StageInstance{" +
                "gateway=" + gateway +
                ", data=" + data +
                '}';
    }

    public Mono<StageInstance> edit(StageInstanceEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> rest.edit(spec.asRequest(), spec.reason())
                .map(bean -> new StageInstance(gateway, bean)));
    }
}
