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
package discord4j.core.spec;

import discord4j.common.LogUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.gateway.VoiceStateUpdate;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.rest.util.Permission;
import discord4j.voice.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.annotation.Nullable;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.UnaryOperator;

/**
 * Mono used to request a connection to a {@link VoiceChannel} and handle the initialization of the resulting
 * {@link VoiceConnection}.
 */
public class VoiceChannelJoinMono extends AuditableRequest<VoiceConnection, Void, VoiceChannelJoinMono> {

    /**
     * Default maximum amount of time in seconds to wait before the connection to the voice channel times out.
     */
    private static final int DEFAULT_TIMEOUT = 10;

    /**
     * Default maximum amount of time in seconds to wait before a single IP discovery attempt times out.
     */
    private static final int DEFAULT_DISCOVERY_TIMEOUT = 5;

    private final Duration timeout;
    private final AudioProvider provider;
    private final AudioReceiver receiver;
    private final VoiceSendTaskFactory sendTaskFactory;
    private final VoiceReceiveTaskFactory receiveTaskFactory;
    private final boolean selfDeaf;
    private final boolean selfMute;
    private final Duration ipDiscoveryTimeout;
    private final RetrySpec ipDiscoveryRetrySpec;

    private final GatewayDiscordClient gateway;
    private final VoiceChannel voiceChannel;

    public VoiceChannelJoinMono(@Nullable String reason, Duration timeout, AudioProvider provider, AudioReceiver receiver,
                                VoiceSendTaskFactory sendTaskFactory, VoiceReceiveTaskFactory receiveTaskFactory,
                                boolean selfDeaf, boolean selfMute, Duration ipDiscoveryTimeout, RetrySpec ipDiscoveryRetrySpec,
                                GatewayDiscordClient gateway, VoiceChannel voiceChannel) {
        super(() -> {
            throw new UnsupportedOperationException("VoiceChannelJoinMono has no internal builder.");
        }, reason);
        this.timeout = timeout;
        this.provider = provider;
        this.receiver = receiver;
        this.sendTaskFactory = sendTaskFactory;
        this.receiveTaskFactory = receiveTaskFactory;
        this.selfDeaf = selfDeaf;
        this.selfMute = selfMute;
        this.ipDiscoveryTimeout = ipDiscoveryTimeout;
        this.ipDiscoveryRetrySpec = ipDiscoveryRetrySpec;
        this.gateway = gateway;
        this.voiceChannel = voiceChannel;
    }

    public VoiceChannelJoinMono(GatewayDiscordClient gateway, VoiceChannel voiceChannel) {
        this(null, Duration.ofSeconds(DEFAULT_TIMEOUT), AudioProvider.NO_OP, AudioReceiver.NO_OP,
            new LocalVoiceSendTaskFactory(), new LocalVoiceReceiveTaskFactory(), false, false,
            Duration.ofSeconds(DEFAULT_DISCOVERY_TIMEOUT),
            RetrySpec.maxInARow(1), gateway, voiceChannel);
    }

    @Override
    public VoiceChannelJoinMono withReason(String reason) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    @Override
    VoiceChannelJoinMono withBuilder(UnaryOperator<Void> f) {
        throw new UnsupportedOperationException("VoiceChannelJoinMono has no internal builder.");
    }

    /**
     * Configure the {@link AudioProvider} to use in the created {@link VoiceConnection}.
     *
     * @param provider Used to send audio.
     * @return This mono.
     */
    public VoiceChannelJoinMono withProvider(AudioProvider provider) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Configure the {@link AudioReceiver} to use in the created {@link VoiceConnection}.
     *
     * @param receiver Used to receive audio.
     * @return This mono.
     * @deprecated Discord does not officially support bots receiving audio. It is not guaranteed that this
     * functionality works properly. Use at your own risk.
     */
    @Deprecated
    public VoiceChannelJoinMono withReceiver(AudioReceiver receiver) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Configure the {@link VoiceSendTaskFactory} to use in the created {@link VoiceConnection}. A send task is created
     * when establishing a Voice Gateway session and is torn down when disconnecting.
     *
     * @param sendTaskFactory provides an audio send system that process outbound packets
     * @return This mono.
     */
    public VoiceChannelJoinMono withSendTaskFactory(VoiceSendTaskFactory sendTaskFactory) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Configure the {@link VoiceReceiveTaskFactory} to use in the created {@link VoiceConnection}. A receive task is
     * created when establishing a Voice Gateway session and is torn down when disconnecting.
     *
     * @param receiveTaskFactory provides an audio receive system to process inbound packets
     * @return This mono.
     * @deprecated Discord does not officially support bots receiving audio. It is not guaranteed that this
     * functionality works properly. Use at your own risk.
     */
    @Deprecated
    public VoiceChannelJoinMono withReceiveTaskFactory(VoiceReceiveTaskFactory receiveTaskFactory) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Set whether to deafen this client when establishing a {@link VoiceConnection}.
     *
     * @param selfDeaf if this client is deafened
     * @return This mono.
     */
    public VoiceChannelJoinMono withSelfDeaf(boolean selfDeaf) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Set whether to mute this client when establishing a {@link VoiceConnection}.
     *
     * @param selfMute if this client is muted
     * @return This mono.
     */
    public VoiceChannelJoinMono withSelfMute(boolean selfMute) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Set the maximum amount of time to wait before the connection to the voice channel timeouts.
     * For example, the connection may get stuck when the bot does not have {@link Permission#VIEW_CHANNEL} or
     * when the voice channel is full.
     * The default value is {@value #DEFAULT_TIMEOUT} seconds.
     *
     * @param timeout the maximum amount of time to wait before the connection to the voice channel timeouts
     * @return This mono.
     */
    public VoiceChannelJoinMono withTimeout(Duration timeout) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Set the maximum amount of time to wait for a single attempt at performing the IP discovery procedure. For more
     * information about this procedure check
     * <a href="https://discord.com/developers/docs/topics/voice-connections#ip-discovery">IP discovery</a>.
     * The default value is {@link #DEFAULT_DISCOVERY_TIMEOUT} seconds.
     *
     * @param ipDiscoveryTimeout the maximum amount of time to wait in a single attempt at IP discovery
     * @return This mono.
     */
    public VoiceChannelJoinMono withIpDiscoveryTimeout(Duration ipDiscoveryTimeout) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    /**
     * Set the retry policy to apply when performing IP discovery. For more
     * information about this procedure check
     * <a href="https://discord.com/developers/docs/topics/voice-connections#ip-discovery">IP discovery</a>.
     * The default value is retrying once before exiting.
     *
     * @param ipDiscoveryRetrySpec the maximum amount of time to wait in a single attempt at IP discovery
     * @return this mono
     */
    public VoiceChannelJoinMono withIpDiscoveryRetrySpec(RetrySpec ipDiscoveryRetrySpec) {
        return new VoiceChannelJoinMono(reason, timeout, provider, receiver, sendTaskFactory,
            receiveTaskFactory, selfDeaf, selfMute, ipDiscoveryTimeout,
            ipDiscoveryRetrySpec, gateway, voiceChannel);
    }

    @Override
    Mono<VoiceConnection> getRequest() {
        final Snowflake guildId = voiceChannel.getGuildId();
        final Snowflake channelId = voiceChannel.getId();
        final GatewayClientGroup clientGroup = voiceChannel.getClient().getGatewayClientGroup();
        final int shardId = clientGroup.computeShardIndex(guildId);
        final Mono<Void> sendVoiceStateUpdate = clientGroup.unicast(ShardGatewayPayload.voiceStateUpdate(
            VoiceStateUpdate.builder()
                .guildId(guildId.asString())
                .channelId(channelId.asString())
                .selfMute(selfMute)
                .selfDeaf(selfDeaf)
                .build(), shardId));

        final Mono<VoiceStateUpdateEvent> waitForVoiceStateUpdate = onVoiceStateUpdates(gateway, guildId).next();
        final Mono<VoiceServerUpdateEvent> waitForVoiceServerUpdate = onVoiceServerUpdate(gateway, guildId);

        final VoiceDisconnectTask disconnectTask = id -> voiceChannel.sendDisconnectVoiceState()
            .then(gateway.getVoiceConnectionRegistry().disconnect(id));
        //noinspection ConstantConditions
        final VoiceServerUpdateTask serverUpdateTask = id -> onVoiceServerUpdate(gateway, id)
            .map(vsu -> new VoiceServerOptions(vsu.getToken(), vsu.getEndpoint()));
        final VoiceStateUpdateTask stateUpdateTask = id -> onVoiceStateUpdates(gateway, id)
            .map(stateUpdateEvent -> stateUpdateEvent.getCurrent().getSessionId());
        final VoiceChannelRetrieveTask channelRetrieveTask = () -> gateway
            .getMemberById(voiceChannel.getGuildId(), gateway.getSelfId())
            .flatMap(Member::getVoiceState)
            .flatMap(voiceState -> Mono.justOrEmpty(voiceState.getChannelId()));

        Mono<VoiceConnection> newConnection = sendVoiceStateUpdate
            .then(Mono.zip(waitForVoiceStateUpdate, waitForVoiceServerUpdate))
            .flatMap(TupleUtils.function((voiceState, voiceServer) -> {
                final String session = voiceState.getCurrent().getSessionId();
                //noinspection ConstantConditions
                final VoiceServerOptions voiceServerOptions = new VoiceServerOptions(
                    voiceServer.getToken(), voiceServer.getEndpoint());
                final VoiceGatewayOptions voiceGatewayOptions = new VoiceGatewayOptions(
                    guildId, gateway.getSelfId(), session, voiceServerOptions,
                    gateway.getCoreResources().getJacksonResources(),
                    gateway.getGatewayResources().getVoiceReactorResources(),
                    gateway.getGatewayResources().getVoiceReconnectOptions(),
                    provider, receiver, sendTaskFactory, receiveTaskFactory, disconnectTask,
                    serverUpdateTask, stateUpdateTask, channelRetrieveTask,
                    ipDiscoveryTimeout, ipDiscoveryRetrySpec);

                return gateway.getVoiceConnectionFactory()
                    .create(voiceGatewayOptions)
                    .flatMap(vc -> gateway.getVoiceConnectionRegistry().registerVoiceConnection(guildId, vc).thenReturn(vc))
                    .subscriberContext(ctx ->
                        ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(gateway.hashCode()))
                            .put(LogUtil.KEY_SHARD_ID, shardId));
            }))
            .timeout(timeout)
            .onErrorResume(TimeoutException.class,
                t -> gateway.getVoiceConnectionRegistry().getVoiceConnection(guildId)
                    .switchIfEmpty(voiceChannel.sendDisconnectVoiceState().then(Mono.error(t))));

        return gateway.getVoiceConnectionRegistry().getVoiceConnection(guildId)
            .flatMap(existing -> sendVoiceStateUpdate.then(waitForVoiceStateUpdate).thenReturn(existing))
            .switchIfEmpty(newConnection);
    }

    static Flux<VoiceStateUpdateEvent> onVoiceStateUpdates(GatewayDiscordClient gateway, Snowflake guildId) {
        return gateway.getEventDispatcher()
            .on(VoiceStateUpdateEvent.class)
            .filter(vsu -> {
                final Snowflake vsuUser = vsu.getCurrent().getUserId();
                final Snowflake vsuGuild = vsu.getCurrent().getGuildId();
                // this update is for the bot (current) user in this guild
                return vsuUser.equals(gateway.getSelfId()) && vsuGuild.equals(guildId);
            });
    }

    static Mono<VoiceServerUpdateEvent> onVoiceServerUpdate(GatewayDiscordClient gateway, Snowflake guildId) {
        return gateway.getEventDispatcher()
            .on(VoiceServerUpdateEvent.class)
            .filter(vsu -> vsu.getGuildId().equals(guildId))
            .filter(vsu -> vsu.getEndpoint() != null) // sometimes Discord sends null here. If so, another VSU
            // should arrive afterwards
            .next();
    }
}
