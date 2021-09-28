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
package discord4j.core.spec.legacy;

import discord4j.common.LogUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.discordjson.json.gateway.VoiceStateUpdate;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.rest.util.Permission;
import discord4j.voice.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static discord4j.common.LogUtil.format;

/**
 * LegacySpec used to request a connection to a {@link AudioChannel} and handle the initialization of the resulting
 * {@link VoiceConnection}.
 */
public class LegacyAudioChannelJoinSpec implements LegacySpec<Mono<VoiceConnection>> {

    private static final Logger log = Loggers.getLogger(LegacyAudioChannelJoinSpec.class);

    /** Default maximum amount of time in seconds to wait before the connection to the voice channel times out. */
    private static final int DEFAULT_TIMEOUT = 10;

    /** Default maximum amount of time in seconds to wait before a single IP discovery attempt times out. */
    private static final int DEFAULT_DISCOVERY_TIMEOUT = 5;

    private Duration timeout = Duration.ofSeconds(DEFAULT_TIMEOUT);
    private AudioProvider provider = AudioProvider.NO_OP;
    private AudioReceiver receiver = AudioReceiver.NO_OP;
    private VoiceSendTaskFactory sendTaskFactory = new LocalVoiceSendTaskFactory();
    private VoiceReceiveTaskFactory receiveTaskFactory = new LocalVoiceReceiveTaskFactory();
    private boolean selfDeaf;
    private boolean selfMute;
    private Duration ipDiscoveryTimeout = Duration.ofSeconds(DEFAULT_DISCOVERY_TIMEOUT);
    private RetrySpec LegacyipDiscoveryRetrySpec = RetrySpec.maxInARow(1);

    private final GatewayDiscordClient gateway;
    private final AudioChannel audioChannel;

    public LegacyAudioChannelJoinSpec(final GatewayDiscordClient gateway, final AudioChannel audioChannel) {
        this.gateway = Objects.requireNonNull(gateway);
        this.audioChannel = audioChannel;
    }

    /**
     * Configure the {@link AudioProvider} to use in the created {@link VoiceConnection}.
     *
     * @param provider Used to send audio.
     * @return This spec.
     */
    public LegacyAudioChannelJoinSpec setProvider(final AudioProvider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Configure the {@link AudioReceiver} to use in the created {@link VoiceConnection}.
     *
     * @param receiver Used to receive audio.
     * @return This spec.
     * @deprecated Discord does not officially support bots receiving audio. It is not guaranteed that this
     * functionality works properly. Use at your own risk.
     */
    @Deprecated
    public LegacyAudioChannelJoinSpec setReceiver(final AudioReceiver receiver) {
        this.receiver = receiver;
        return this;
    }

    /**
     * Configure the {@link VoiceSendTaskFactory} to use in the created {@link VoiceConnection}. A send task is created
     * when establishing a Voice Gateway session and is torn down when disconnecting.
     *
     * @param sendTaskFactory provides an audio send system that process outbound packets
     * @return this spec
     */
    public LegacyAudioChannelJoinSpec setSendTaskFactory(VoiceSendTaskFactory sendTaskFactory) {
        this.sendTaskFactory = sendTaskFactory;
        return this;
    }

    /**
     * Configure the {@link VoiceReceiveTaskFactory} to use in the created {@link VoiceConnection}. A receive task is
     * created when establishing a Voice Gateway session and is torn down when disconnecting.
     *
     * @param receiveTaskFactory provides an audio receive system to process inbound packets
     * @return this spec
     * @deprecated Discord does not officially support bots receiving audio. It is not guaranteed that this
     * functionality works properly. Use at your own risk.
     */
    @Deprecated
    public LegacyAudioChannelJoinSpec setReceiveTaskFactory(VoiceReceiveTaskFactory receiveTaskFactory) {
        this.receiveTaskFactory = receiveTaskFactory;
        return this;
    }

    /**
     * Set whether to deafen this client when establishing a {@link VoiceConnection}.
     *
     * @param selfDeaf if this client is deafened
     * @return this spec
     */
    public LegacyAudioChannelJoinSpec setSelfDeaf(final boolean selfDeaf) {
        this.selfDeaf = selfDeaf;
        return this;
    }

    /**
     * Set whether to mute this client when establishing a {@link VoiceConnection}.
     *
     * @param selfMute if this client is muted
     * @return this spec
     */
    public LegacyAudioChannelJoinSpec setSelfMute(final boolean selfMute) {
        this.selfMute = selfMute;
        return this;
    }

    /**
     * Set the maximum amount of time to wait before the connection to the voice channel timeouts.
     * For example, the connection may get stuck when the bot does not have {@link Permission#VIEW_CHANNEL} or
     * when the voice channel is full.
     * The default value is {@value #DEFAULT_TIMEOUT} seconds.
     *
     * @param timeout the maximum amount of time to wait before the connection to the voice channel timeouts
     * @return this spec
     */
    public LegacyAudioChannelJoinSpec setTimeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout);
        return this;
    }

    /**
     * Set the maximum amount of time to wait for a single attempt at performing the IP discovery procedure. For more
     * information about this procedure check
     * <a href="https://discord.com/developers/docs/topics/voice-connections#ip-discovery">IP discovery</a>.
     * The default value is {@link #DEFAULT_DISCOVERY_TIMEOUT} seconds.
     *
     * @param ipDiscoveryTimeout the maximum amount of time to wait in a single attempt at IP discovery
     * @return this spec
     */
    public LegacyAudioChannelJoinSpec setIpDiscoveryTimeout(Duration ipDiscoveryTimeout) {
        this.ipDiscoveryTimeout = Objects.requireNonNull(ipDiscoveryTimeout);
        return this;
    }

    /**
     * Set the retry policy to apply when performing IP discovery. For more
     * information about this procedure check
     * <a href="https://discord.com/developers/docs/topics/voice-connections#ip-discovery">IP discovery</a>.
     * The default value is retrying once before exiting.
     *
     * @param LegacyipDiscoveryRetrySpec the maximum amount of time to wait in a single attempt at IP discovery
     * @return this spec
     */
    public LegacyAudioChannelJoinSpec LegacysetIpDiscoveryRetrySpec(RetrySpec LegacyipDiscoveryRetrySpec) {
        this.LegacyipDiscoveryRetrySpec = Objects.requireNonNull(LegacyipDiscoveryRetrySpec);
        return this;
    }

    @Override
    public Mono<VoiceConnection> asRequest() {
        if (!gateway.getGatewayResources().getIntents().contains(Intent.GUILD_VOICE_STATES)) {
            return Mono.error(new IllegalArgumentException(
                    "GUILD_VOICE_STATES intent is required to establish a voice connection"));
        }

        final Snowflake guildId = audioChannel.getGuildId();
        final Snowflake channelId = audioChannel.getId();
        final GatewayClientGroup clientGroup = audioChannel.getClient().getGatewayClientGroup();
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

        final VoiceDisconnectTask disconnectTask = id -> audioChannel.sendDisconnectVoiceState()
                .then(gateway.getVoiceConnectionRegistry().disconnect(id));
        //noinspection ConstantConditions
        final VoiceServerUpdateTask serverUpdateTask = id -> onVoiceServerUpdate(gateway, id)
                .map(vsu -> new VoiceServerOptions(vsu.getToken(), vsu.getEndpoint()));
        final VoiceStateUpdateTask stateUpdateTask = id -> onVoiceStateUpdates(gateway, id)
                .map(stateUpdateEvent -> stateUpdateEvent.getCurrent().getSessionId());
        final VoiceChannelRetrieveTask channelRetrieveTask = () -> gateway
                .getMemberById(audioChannel.getGuildId(), gateway.getSelfId())
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
                            ipDiscoveryTimeout, LegacyipDiscoveryRetrySpec);

                    return gateway.getVoiceConnectionFactory()
                            .create(voiceGatewayOptions)
                            .flatMap(vc -> gateway.getVoiceConnectionRegistry()
                                    .registerVoiceConnection(guildId, vc)
                                    .thenReturn(vc))
                            .doOnEach(signal -> {
                                if (signal.isOnSubscribe()) {
                                    log.debug(format(signal.getContextView(), "Creating voice connection"));
                                }
                            })
                            .contextWrite(ctx ->
                                    ctx.put(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(gateway.hashCode()))
                                            .put(LogUtil.KEY_SHARD_ID, shardId)
                                            .put(LogUtil.KEY_GUILD_ID, guildId.asLong()));
                }))
                .timeout(timeout)
                .doFinally(signal -> log.debug("Voice connection handshake to guild {} done after {}",
                        guildId.asLong(), signal))
                // send disconnecting voice state to Discord and forward the timeout error signal
                .onErrorResume(TimeoutException.class,
                        t -> gateway.getVoiceConnectionRegistry().getVoiceConnection(guildId)
                                .switchIfEmpty(audioChannel.sendDisconnectVoiceState().then(Mono.error(t))));

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
                .filter(vsu -> vsu.getGuildId().equals(guildId) && vsu.getEndpoint() != null)
                .next();
    }
}
