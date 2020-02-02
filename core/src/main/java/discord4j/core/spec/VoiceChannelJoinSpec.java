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

import com.darichey.discordjson.json.gateway.ImmutableVoiceStateUpdate;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.gateway.GatewayClientGroup;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.voice.AudioProvider;
import discord4j.voice.AudioReceiver;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Objects;
import java.util.Optional;

/**
 * Spec used to request a connection to a {@link VoiceChannel} and handle the initialization of the resulting
 * {@link VoiceConnection}.
 */
public class VoiceChannelJoinSpec implements Spec<Mono<VoiceConnection>> {

    private AudioProvider provider = AudioProvider.NO_OP;
    private AudioReceiver receiver = AudioReceiver.NO_OP;
    private boolean selfDeaf;
    private boolean selfMute;

    private final GatewayDiscordClient gateway;
    private final VoiceChannel voiceChannel;

    public VoiceChannelJoinSpec(final GatewayDiscordClient gateway, final VoiceChannel voiceChannel) {
        this.gateway = Objects.requireNonNull(gateway);
        this.voiceChannel = voiceChannel;
    }

    /**
     * Configure the {@link AudioProvider} to use in the created {@link VoiceConnection}.
     *
     * @param provider Used to send audio.
     * @return This spec.
     */
    public VoiceChannelJoinSpec setProvider(final AudioProvider provider) {
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
    public VoiceChannelJoinSpec setReceiver(final AudioReceiver receiver) {
        this.receiver = receiver;
        return this;
    }

    /**
     * Sets whether to deafen this client when establishing a {@link VoiceConnection}.
     *
     * @param selfDeaf If this client is deafened.
     * @return This spec.
     */
    public VoiceChannelJoinSpec setSelfDeaf(final boolean selfDeaf) {
        this.selfDeaf = selfDeaf;
        return this;
    }

    /**
     * Sets whether to mute this client when establishing a {@link VoiceConnection}.
     *
     * @param selfMute If this client is muted.
     * @return This spec.
     */
    public VoiceChannelJoinSpec setSelfMute(final boolean selfMute) {
        this.selfMute = selfMute;
        return this;
    }

    @Override
    public Mono<VoiceConnection> asRequest() {
        final long guildId = voiceChannel.getGuildId().asLong();
        final long channelId = voiceChannel.getId().asLong();
        final Mono<Long> selfIdMono = gateway.getGatewayResources().getStateView().getSelfId()
                .switchIfEmpty(Mono.just(0L))
                .cache();

        final GatewayClientGroup clientGroup = voiceChannel.getClient().getGatewayClientGroup();
        final int shardId = (int) ((voiceChannel.getGuildId().asLong() >> 22) % clientGroup.getShardCount());
        final Mono<Void> sendVoiceStateUpdate = clientGroup.unicast(ShardGatewayPayload.voiceStateUpdate(
                ImmutableVoiceStateUpdate.of(Long.toUnsignedString(guildId), Optional.of(Long.toUnsignedString(channelId)), selfMute, selfDeaf), shardId));

        final Mono<VoiceStateUpdateEvent> waitForVoiceStateUpdate = gateway.getEventDispatcher()
                .on(VoiceStateUpdateEvent.class)
                .zipWith(selfIdMono)
                .filter(t2 -> {
                    VoiceStateUpdateEvent vsu = t2.getT1();
                    Long selfId = t2.getT2();
                    final long vsuUser = vsu.getCurrent().getUserId().asLong();
                    final long vsuGuild = vsu.getCurrent().getGuildId().asLong();
                    // this update is for the bot (current) user in this guild
                    return (vsuUser == selfId) && (vsuGuild == guildId);
                })
                .map(Tuple2::getT1)
                .next();

        final Mono<VoiceServerUpdateEvent> waitForVoiceServerUpdate = gateway.getEventDispatcher()
                .on(VoiceServerUpdateEvent.class)
                .filter(vsu -> vsu.getGuildId().asLong() == guildId)
                .filter(vsu -> vsu.getEndpoint() != null) // sometimes Discord sends null here. If so, another VSU
                // should arrive afterwards
                .next();

        return sendVoiceStateUpdate
                .then(Mono.zip(waitForVoiceStateUpdate, waitForVoiceServerUpdate, selfIdMono))
                .flatMap(t3 -> {
                    final String endpoint = t3.getT2().getEndpoint().replace(":80", ""); // discord sends wrong port...
                    final String session = t3.getT1().getCurrent().getSessionId();
                    final String token = t3.getT2().getToken();

                    return gateway.getVoiceConnectionFactory().create(guildId, t3.getT3(), session, token, endpoint,
                            provider, receiver);
                });
    }
}
