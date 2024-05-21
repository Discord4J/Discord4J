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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core.spec;

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
import discord4j.voice.AudioProvider;
import discord4j.voice.AudioReceiver;
import discord4j.voice.LocalVoiceReceiveTaskFactory;
import discord4j.voice.LocalVoiceSendTaskFactory;
import discord4j.voice.VoiceChannelRetrieveTask;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceDisconnectTask;
import discord4j.voice.VoiceGatewayOptions;
import discord4j.voice.VoiceReceiveTaskFactory;
import discord4j.voice.VoiceSendTaskFactory;
import discord4j.voice.VoiceServerOptions;
import discord4j.voice.VoiceServerUpdateTask;
import discord4j.voice.VoiceStateUpdateTask;
import org.immutables.value.Value;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static discord4j.common.LogUtil.format;

@Value.Immutable(singleton = true)
interface AudioChannelJoinSpecGenerator extends Spec<Function<AudioChannel, Mono<VoiceConnection>>> {

    /**
     * Default maximum amount of time in seconds to wait before the connection to the voice channel times out.
     */
    int DEFAULT_TIMEOUT = 10;

    /**
     * Default maximum amount of time in seconds to wait before a single IP discovery attempt times out.
     */
    int DEFAULT_DISCOVERY_TIMEOUT = 5;

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

    @Value.Default
    default Duration timeout() {
        return Duration.ofSeconds(DEFAULT_TIMEOUT);
    }

    @Value.Default
    default AudioProvider provider() {
        return AudioProvider.NO_OP;
    }

    @Value.Default
    @Deprecated
    default AudioReceiver receiver() {
        return AudioReceiver.NO_OP;
    }

    @Value.Default
    default VoiceSendTaskFactory sendTaskFactory() {
        return new LocalVoiceSendTaskFactory();
    }

    @Value.Default
    @Deprecated
    default VoiceReceiveTaskFactory receiveTaskFactory() {
        return new LocalVoiceReceiveTaskFactory();
    }

    @Value.Default
    default boolean selfDeaf() {
        return false;
    }

    @Value.Default
    default boolean selfMute() {
        return false;
    }

    @Value.Default
    default Duration ipDiscoveryTimeout() {
        return Duration.ofSeconds(DEFAULT_DISCOVERY_TIMEOUT);
    }

    @Value.Default
    default RetrySpec ipDiscoveryRetrySpec() {
        return RetrySpec.maxInARow(1);
    }

    @Override
    default Function<AudioChannel, Mono<VoiceConnection>> asRequest() {
        return voiceChannel -> {
            final Logger log = Loggers.getLogger(AudioChannelJoinSpec.class);
            final GatewayDiscordClient gateway = voiceChannel.getClient();
            if (!gateway.getGatewayResources().getIntents().contains(Intent.GUILD_VOICE_STATES)) {
                return Mono.error(new IllegalArgumentException(
                        "GUILD_VOICE_STATES intent is required to establish a voice connection"));
            }

            final Snowflake guildId = voiceChannel.getGuildId();
            final Snowflake channelId = voiceChannel.getId();
            final GatewayClientGroup clientGroup = voiceChannel.getClient().getGatewayClientGroup();
            final int shardId = clientGroup.computeShardIndex(guildId);
            final Mono<Void> sendVoiceStateUpdate = clientGroup.unicast(ShardGatewayPayload.voiceStateUpdate(
                    VoiceStateUpdate.builder()
                            .guildId(guildId.asString())
                            .channelId(channelId.asString())
                            .selfMute(selfMute())
                            .selfDeaf(selfDeaf())
                            .build(), shardId));

            final Mono<VoiceStateUpdateEvent> waitForVoiceStateUpdate = onVoiceStateUpdates(gateway, guildId).next();
            final Mono<VoiceServerUpdateEvent> waitForVoiceServerUpdate = onVoiceServerUpdate(gateway, guildId);

            final VoiceDisconnectTask disconnectTask = id -> voiceChannel.sendDisconnectVoiceState()
                    .then(gateway.getVoiceConnectionRegistry().disconnect(id));
            final VoiceServerUpdateTask serverUpdateTask = new DefaultVoiceServerUpdateTask(gateway);
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
                                provider(), receiver(), sendTaskFactory(), receiveTaskFactory(), disconnectTask,
                                serverUpdateTask, stateUpdateTask, channelRetrieveTask,
                                ipDiscoveryTimeout(), ipDiscoveryRetrySpec());

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
                    .timeout(timeout())
                    .doFinally(signal -> log.debug("Voice connection handshake to guild {} done after {}",
                            guildId.asLong(), signal))
                    // send disconnecting voice state to Discord and forward the timeout error signal
                    .onErrorResume(TimeoutException.class,
                            t -> gateway.getVoiceConnectionRegistry().getVoiceConnection(guildId)
                                    .switchIfEmpty(voiceChannel.sendDisconnectVoiceState().then(Mono.error(t))));

            return gateway.getVoiceConnectionRegistry().getVoiceConnection(guildId)
                    .flatMap(existing -> sendVoiceStateUpdate.then(waitForVoiceStateUpdate).thenReturn(existing))
                    .switchIfEmpty(newConnection);
        };
    }
}

@SuppressWarnings("immutables:subtype")
@Value.Immutable(builder = false)
abstract class AudioChannelJoinMonoGenerator extends Mono<VoiceConnection> implements AudioChannelJoinSpecGenerator {

    abstract AudioChannel channel();

    @Override
    public void subscribe(CoreSubscriber<? super VoiceConnection> actual) {
        channel().join(AudioChannelJoinSpec.copyOf(this)).subscribe(actual);
    }

    @Override
    public abstract String toString();
}
