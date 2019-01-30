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

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.event.domain.VoiceServerUpdateEvent;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.VoiceStateUpdate;
import discord4j.voice.AudioProvider;
import discord4j.voice.AudioReceiver;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class VoiceChannelJoinSpec implements Spec<Mono<VoiceConnection>> {

    private AudioProvider provider = AudioProvider.NO_OP;
    private AudioReceiver receiver = AudioReceiver.NO_OP;
    private boolean selfDeaf;
    private boolean selfMute;

    private final ServiceMediator serviceMediator;
    private final VoiceChannel voiceChannel;

    public VoiceChannelJoinSpec(final ServiceMediator serviceMediator, final VoiceChannel voiceChannel) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.voiceChannel = voiceChannel;
    }

    public VoiceChannelJoinSpec setProvider(final AudioProvider provider) {
        this.provider = provider;
        return this;
    }

    @Deprecated
    public VoiceChannelJoinSpec setReceiver(final AudioReceiver receiver) {
        this.receiver = receiver;
        return this;
    }

    public VoiceChannelJoinSpec setSelfDeaf(final boolean selfDeaf) {
        this.selfDeaf = selfDeaf;
        return this;
    }

    public VoiceChannelJoinSpec setSelfMute(final boolean selfMute) {
        this.selfMute = selfMute;
        return this;
    }

    @Override
    public Mono<VoiceConnection> asRequest() {
        final DiscordClient client = voiceChannel.getClient();
        final long guildId = voiceChannel.getGuildId().asLong();
        final long channelId = voiceChannel.getId().asLong();
        final long selfId = serviceMediator.getStateHolder().getSelfId().get();

        final Mono<Void> sendVoiceStateUpdate = Mono.fromRunnable(() -> {
            final VoiceStateUpdate voiceStateUpdate = new VoiceStateUpdate(guildId, channelId, selfMute, selfDeaf);
            serviceMediator.getGatewayClient().sender().next(GatewayPayload.voiceStateUpdate(voiceStateUpdate));
        });

        final Mono<VoiceStateUpdateEvent> waitForVoiceStateUpdate = client.getEventDispatcher()
                .on(VoiceStateUpdateEvent.class)
                .filter(vsu -> {
                    final long vsuUser = vsu.getCurrent().getUserId().asLong();
                    final long vsuGuild = vsu.getCurrent().getGuildId().asLong();
                    // this update is for the bot (current) user in this guild
                    return (vsuUser == selfId) && (vsuGuild == guildId);
                }).next();

        final Mono<VoiceServerUpdateEvent> waitForVoiceServerUpdate = client.getEventDispatcher()
                .on(VoiceServerUpdateEvent.class)
                .filter(vsu -> vsu.getGuildId().asLong() == guildId)
                .filter(vsu -> vsu.getEndpoint() != null) // sometimes Discord sends null here. If so, another VSU should arrive afterwards
                .next();

        return sendVoiceStateUpdate
                .then(Mono.zip(waitForVoiceStateUpdate, waitForVoiceServerUpdate))
                .flatMap(t -> {
                    final String endpoint = t.getT2().getEndpoint().replace(":80", ""); // discord sends wrong port...
                    final String session = t.getT1().getCurrent().getSessionId();
                    final String token = t.getT2().getToken();

                    return serviceMediator.getVoiceClient()
                            .newConnection(guildId, selfId, session, token, endpoint, provider, receiver);
                });
    }
}
