package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.StageInstanceEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.StageInstanceData;
import discord4j.discordjson.json.UpdateUserVoiceStateRequest;
import discord4j.rest.entity.RestStageInstance;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/** A Discord stage instance. */
public final class StageInstance implements Entity {

    private final GatewayDiscordClient gateway;
    private final StageInstanceData data;
    private final RestStageInstance rest;

    /**
     * Constructs a {@code StageInstance} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw stage instance data as represented by Discord, must be non-null.
     */
    public StageInstance(final GatewayDiscordClient gateway, final StageInstanceData data) {
        this.gateway = gateway;
        this.data = data;
        this.rest = RestStageInstance.create(gateway.rest(), Snowflake.of(data.channelId()));
    }

    /**
     * Requests to edit this stage instance.
     *
     * @param spec an immutable object that specifies how to edit this stage instance
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StageInstance}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<StageInstance> edit(StageInstanceEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> rest.edit(spec.asRequest(), spec.reason())
            .map(bean -> new StageInstance(gateway, bean)));
    }

    /**
     * Requests to invite the specified {@param member} to the speakers of the stage channel associated with this
     * stage instance.
     *
     * @param member The member to invite to the stage speakers
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating that the specified
     * {@param member} has been invited to the speakers of the stage channel associated with this stage instance.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> inviteMemberToStageSpeakers(Member member) {
        return Mono.defer(() -> gateway.getRestClient().getGuildService().modifyOthersVoiceState(member.getGuildId().asLong(), member.getId().asLong(), UpdateUserVoiceStateRequest.builder().suppress(false).build()));
    }

    /**
     * Requests to move the specified {@param member} to the audience of the stage channel associated with this
     * stage instance.
     *
     * @param member The member to move to the stage audience
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating that the specified
     * {@param member} has been moved to the audience of the stage channel associated with this stage instance.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> moveMemberToStageAudience(Member member) {
        return Mono.defer(() -> gateway.getRestClient().getGuildService().modifyOthersVoiceState(member.getGuildId().asLong(), member.getId().asLong(), UpdateUserVoiceStateRequest.builder().suppress(true).build()));
    }

    @Override
    public GatewayDiscordClient getClient() {
        return this.gateway;
    }

    /**
     * Gets the data of the stage instance.
     *
     * @return The data of the stage instance.
     */
    public StageInstanceData getData() {
        return this.data;
    }

    /**
     * Gets the stage instance ID.
     *
     * @return The stage instance ID.
     */
    @Override
    public Snowflake getId() {
        return Snowflake.of(data.id());
    }

    /**
     * Gets the channel ID associated to this stage instance.
     *
     * @return The channel ID associated to this stage instance.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(data.channelId());
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
}
