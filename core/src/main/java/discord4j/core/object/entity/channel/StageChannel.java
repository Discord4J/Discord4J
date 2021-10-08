/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity.channel;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.StageInstance;
import discord4j.core.spec.StageChannelEditMono;
import discord4j.core.spec.StageChannelEditSpec;
import discord4j.core.spec.legacy.LegacyStageChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.StageInstanceCreateRequest;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/** A Discord stage channel. */
public final class StageChannel extends BaseTopLevelGuildChannel implements AudioChannel, CategorizableChannel {

    /**
     * Constructs a {@code StageChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public StageChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        super(gateway, data);
    }

    /**
     * Requests to edit a stage channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyStageChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StageChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(StageChannelEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<StageChannel> edit(final Consumer<? super LegacyStageChannelEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyStageChannelEditSpec mutatedSpec = new LegacyStageChannelEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(StageChannel.class);
    }

    /**
     * Requests to edit this stage channel. Properties specifying how to edit this stage channel can be set via the
     * {@code withXxx} methods of the returned {@link StageChannelEditMono}.
     *
     * @return A {@link StageChannelEditMono} where, upon successful completion, emits the edited {@link StageChannel}.
     * If an error is received, it is emitted through the {@code StageChannelEditMono}.
     */
    public StageChannelEditMono edit() {
        return StageChannelEditMono.of(this);
    }

    /**
     * Requests to edit this stage channel.
     *
     * @param spec an immutable object that specifies how to edit this stage channel
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StageChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<StageChannel> edit(StageChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                        () -> getClient().getRestClient().getChannelService()
                                .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(StageChannel.class);
    }

    /**
     * Requests to determine if the member represented by the provided {@link Snowflake} is a speaker of this stage
     * channel.
     *
     * @param memberId The ID of the member to check.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if the member represented by the
     * provided {@link Snowflake} is a speaker of this stage channel, {@code false} otherwise. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> isSpeaker(final Snowflake memberId) {
        return getSpeakers()
                .map(VoiceState::getUserId)
                .any(memberId::equals);
    }

    /**
     * Returns the current voice connection registered for this stage channel's guild.
     *
     * @return A {@link Mono} of {@link VoiceConnection} for this stage channel's guild if present, or empty otherwise.
     * The resulting {@code Mono} will also complete empty if the registered voice connection is not associated with
     * this stage channel.
     */
    public Mono<VoiceConnection> getVoiceConnection() {
        return getGuild()
                .flatMap(Guild::getVoiceConnection)
                .filterWhen(voiceConnection -> voiceConnection.getChannelId().map(channelId -> channelId.equals(getId())));
    }

    /**
     * Requests to retrieve the voice states of this channel with a request to speak.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of this stage channel with a
     * request to speak. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<VoiceState> getRequestsToSpeak() {
        return getVoiceStates()
                .filter(voiceState -> voiceState.getRequestedToSpeakAt().isPresent());
    }

    /**
     * Requests to retrieve the voice states of this channel associated to speaker users.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of this stage channel
     * associated to speaker users. If an error is received, it is emitted through the {@code Flux}.
     */
    public Flux<VoiceState> getSpeakers() {
        return getVoiceStates()
                .filter(voiceState -> !voiceState.isSuppressed() && !voiceState.getRequestedToSpeakAt().isPresent());
    }

    /**
     * Requests to retrieve if there is a {@link StageInstance} for this channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if this stage channel
     *         is associated with a {@link StageInstance}, {@code false} otherwise. If an error is received,
     *         it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> isStageLive() {
        return getClient()
                .getRestClient()
                .getStageInstanceService()
                .getStageInstance(this.getId().asLong()).map(Objects::nonNull);
    }

    /**
     * Requests to start a {@link StageInstance} on this channel.
     *
     * @param topic The topic of this {@link StageInstance}
     * @param reason The reason, if present
     * @return A {@link Mono} where, upon successful completion, emits a {@link StageInstance} created
     *         for this channel with the specified {@param topic}. If an error is received, it is emitted
     *         through the {@code Mono}
     */
    public Mono<StageInstance> startStageLive(String topic, @Nullable String reason) {
        return getClient()
                .getRestClient()
                .getStageInstanceService()
                .createStageInstance(StageInstanceCreateRequest.builder().channelId(this.getId().asLong()).topic(topic).build(), reason)
                .map(stageInstanceData -> new StageInstance(getClient(), stageInstanceData));
    }

    @Override
    public String toString() {
        return "StageChannel{} " + super.toString();
    }

}
