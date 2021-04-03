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
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.VoiceChannelEditSpec;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.gateway.VoiceStateUpdate;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnectionRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/** A Discord voice channel. */
public final class VoiceChannel extends BaseCategorizableChannel {

    /**
     * Constructs an {@code VoiceChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public VoiceChannel(final GatewayDiscordClient gateway, final ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the bitrate (in bits) for this voice channel.
     *
     * @return Gets the bitrate (in bits) for this voice channel.
     */
    public int getBitrate() {
        return getData().bitrate().toOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the user limit of this voice channel.
     *
     * @return The user limit of this voice channel.
     */
    public int getUserLimit() {
        return getData().userLimit().toOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Requests to edit a voice channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link VoiceChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceChannel> edit(final Consumer<? super VoiceChannelEditSpec> spec) {
        return Mono.defer(
                () -> {
                    VoiceChannelEditSpec mutatedSpec = new VoiceChannelEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(VoiceChannel.class);
    }

    /**
     * Requests to retrieve the voice states of this voice channel.
     *
     * @return A {@link Flux} that continually emits the {@link VoiceState voice states} of this voice channel. If an
     * error is received, it is emitted through the {@code Flux}.
     */
    public Flux<VoiceState> getVoiceStates() {
        return Flux.from(getClient().getGatewayResources().getStore()
                .execute(ReadActions.getVoiceStatesInChannel(getGuildId().asLong(), getId().asLong())))
                .map(data -> new VoiceState(getClient(), data));
    }

    /**
     * Request to join this voice channel upon subscription. The resulting {@link VoiceConnection} will be available
     * to you from the {@code Mono} but also through a {@link VoiceConnectionRegistry} and can be obtained through
     * {@link GatewayDiscordClient#getVoiceConnectionRegistry()}. Additionally, the resulting {@code VoiceConnection}
     * can be retrieved from the associated guild through {@link Guild#getVoiceConnection()} and through
     * {@link #getVoiceConnection()}.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link VoiceChannelJoinSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits a {@link VoiceConnection}, indicating a
     * connection to the channel has been established. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<VoiceConnection> join(final Consumer<? super VoiceChannelJoinSpec> spec) {
        return Mono.defer(() -> {
            final VoiceChannelJoinSpec mutatedSpec = new VoiceChannelJoinSpec(getClient(), this);
            spec.accept(mutatedSpec);

            return mutatedSpec.asRequest();
        });
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
    public Mono<Void> sendConnectVoiceState(final boolean selfMute, final boolean selfDeaf) {
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
     * {@link VoiceChannel#join(Consumer)}
     *
     * @return An empty mono which completes when the payload was sent to the gateway
     */
    public Mono<Void> sendDisconnectVoiceState() {
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
     * Requests to determine if the member represented by the provided {@link Snowflake} is connected to this voice
     * channel.
     *
     * @param memberId The ID of the member to check.
     * @return A {@link Mono} where, upon successful completion, emits {@code true} if the member represented by the
     * provided {@link Snowflake} is connected to this voice channel, {@code false} otherwise. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Boolean> isMemberConnected(final Snowflake memberId) {
        return getVoiceStates()
                .map(VoiceState::getUserId)
                .any(memberId::equals);
    }

    /**
     * Returns the current voice connection registered for this voice channel's guild.
     *
     * @return A {@link Mono} of {@link VoiceConnection} for this voice channel's guild if present, or empty otherwise.
     * The resulting {@code Mono} will also complete empty if the registered voice connection is not associated with
     * this voice channel.
     */
    public Mono<VoiceConnection> getVoiceConnection() {
        return getGuild()
                .flatMap(Guild::getVoiceConnection)
                .filterWhen(voiceConnection -> voiceConnection.getChannelId().map(channelId -> channelId.equals(getId())));
    }

    @Override
    public String toString() {
        return "VoiceChannel{} " + super.toString();
    }
}
