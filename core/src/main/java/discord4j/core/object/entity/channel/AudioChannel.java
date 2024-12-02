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

import discord4j.common.store.action.read.ReadActions;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Region;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.AudioChannelJoinMono;
import discord4j.core.spec.AudioChannelJoinSpec;
import discord4j.core.spec.legacy.LegacyVoiceChannelJoinSpec;
import discord4j.discordjson.json.gateway.VoiceStateUpdate;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnectionRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Discord audio channel. This can be either a {@link StageChannel} or a {@link VoiceChannel}.
 * This superclass contains the common audio methods for these two types.
 */
public interface AudioChannel extends CategorizableChannel {

    /**
     * Gets the bitrate (in bits) for this audio channel.
     *
     * @return Gets the bitrate (in bits) for this audio channel.
     */
    default int getBitrate() {
        return getData().bitrate().toOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the voice region id for the audio channel.
     *
     * @return The voice region id for the audio channel.
     */
    default Region.Id getRtcRegion() {
        return Possible.flatOpt(getData().rtcRegion()).map(Region.Id::of).orElse(Region.Id.AUTOMATIC);
    }

    /**
     * Requests to retrieve the voice states of this audio channel.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of this audio channel. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    default Flux<VoiceState> getVoiceStates() {
        return Flux.from(getClient().getGatewayResources().getStore()
                        .execute(ReadActions.getVoiceStatesInChannel(getGuildId().asLong(), getId().asLong())))
                .map(data -> new VoiceState(getClient(), data));
    }

    /**
     * Request to join this audio channel upon subscription. The resulting {@link VoiceConnection} will be available to
     * you from the {@code Mono} but also through a {@link VoiceConnectionRegistry} and can be obtained through {@link
     * GatewayDiscordClient#getVoiceConnectionRegistry()}. Additionally, the resulting {@code VoiceConnection} can be
     * retrieved from the associated guild through {@link Guild#getVoiceConnection()} and through {@link
     * #getVoiceConnection()}.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyVoiceChannelJoinSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits a {@link VoiceConnection}, indicating a
     * connection to the channel has been established. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #join(AudioChannelJoinSpec)} or {@link #join()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    default Mono<VoiceConnection> join(final Consumer<? super LegacyVoiceChannelJoinSpec> spec) {
        return Mono.defer(() -> {
            final LegacyVoiceChannelJoinSpec mutatedSpec = new LegacyVoiceChannelJoinSpec(getClient(), this);
            spec.accept(mutatedSpec);

            return mutatedSpec.asRequest();
        });
    }

    /**
     * Request to join this audio channel upon subscription. Properties specifying how to join this audio channel can be
     * set via the {@code withXxx} methods of the returned {@link AudioChannelJoinMono}. The resulting {@link
     * VoiceConnection} will be available to you from the {@code Mono} but also through a {@link
     * VoiceConnectionRegistry} and can be obtained through {@link GatewayDiscordClient#getVoiceConnectionRegistry()}.
     * Additionally, the resulting {@code VoiceConnection} can be retrieved from the associated guild through {@link
     * Guild#getVoiceConnection()} and through {@link #getVoiceConnection()}.
     *
     * @return A {@link AudioChannelJoinMono} where, upon successful completion, emits a {@link VoiceConnection},
     * indicating a connection to the channel has been established. If an error is received, it is emitted through the
     * {@code AudioChannelJoinMono}.
     */
    default AudioChannelJoinMono join() {
        return AudioChannelJoinMono.of(this);
    }

    /**
     * Request to join this audio channel upon subscription. The resulting {@link VoiceConnection} will be available to
     * you from the {@code Mono} but also through a {@link VoiceConnectionRegistry} and can be obtained through {@link
     * GatewayDiscordClient#getVoiceConnectionRegistry()}. Additionally, the resulting {@code VoiceConnection} can be
     * retrieved from the associated guild through {@link Guild#getVoiceConnection()} and through {@link
     * #getVoiceConnection()}.
     *
     * @param spec an immutable object that specifies how to join this audio channel
     * @return A {@link Mono} where, upon successful completion, emits a {@link VoiceConnection}, indicating a
     * connection to the channel has been established. If an error is received, it is emitted through the {@code Mono}.
     */
    default Mono<VoiceConnection> join(AudioChannelJoinSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> spec.asRequest().apply(this));
    }

    /**
     * Sends a join request to the gateway
     * <p>
     * This method does not trigger any logic and requires external state handling
     *
     * @param selfMute if the client should be mutes
     * @param selfDeaf if the client should be deaf
     * @return An empty mono which completes when the payload was sent to the gateway
     */
    default Mono<Void> sendConnectVoiceState(final boolean selfMute, final boolean selfDeaf) {
        final GatewayClientGroup clientGroup = getClient().getGatewayClientGroup();
        final int shardId = clientGroup.computeShardIndex(getGuildId());
        return clientGroup.unicast(ShardGatewayPayload.voiceStateUpdate(
                VoiceStateUpdate.builder()
                        .guildId(getGuildId().asString())
                        .channelId(getId().asString())
                        .selfMute(selfMute)
                        .selfDeaf(selfDeaf)
                        .build(), shardId));
    }

    /**
     * Sends a leave request to the gateway
     * <p>
     * This method does not replace {@link VoiceConnection#disconnect()} when the channel was joined by using
     * {@link AudioChannel#join(AudioChannelJoinSpec)}
     *
     * @return An empty mono which completes when the payload was sent to the gateway
     */
    default Mono<Void> sendDisconnectVoiceState() {
        final GatewayClientGroup clientGroup = getClient().getGatewayClientGroup();
        final int shardId = clientGroup.computeShardIndex(getGuildId());
        return clientGroup.unicast(ShardGatewayPayload.voiceStateUpdate(
                VoiceStateUpdate.builder()
                        .guildId(getGuildId().asString())
                        .selfMute(false)
                        .selfDeaf(false)
                        .build(), shardId));
    }

    /**
     * Requests to determine if the member represented by the provided {@link Snowflake} is connected to this audio
     * channel.
     *
     * @param memberId The ID of the member to check.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if the member represented by the
     * provided {@link Snowflake} is connected to this audio channel, {@code false} otherwise. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    default Mono<Boolean> isMemberConnected(final Snowflake memberId) {
        return getVoiceStates()
                .map(VoiceState::getUserId)
                .any(memberId::equals);
    }

    /**
     * Returns the current voice connection registered for this audio channel's guild.
     *
     * @return A {@link Mono} of {@link VoiceConnection} for this audio channel's guild if present, or empty otherwise.
     * The resulting {@code Mono} will also complete empty if the registered voice connection is not associated with
     * this audio channel.
     */
    default Mono<VoiceConnection> getVoiceConnection() {
        return getGuild()
                .flatMap(Guild::getVoiceConnection)
                .filterWhen(voiceConnection -> voiceConnection.getChannelId().map(channelId -> channelId.equals(getId())));
    }

}
