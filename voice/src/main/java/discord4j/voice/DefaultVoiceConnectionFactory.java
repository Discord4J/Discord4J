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

package discord4j.voice;

import discord4j.common.JacksonResources;
import discord4j.common.retry.ReconnectOptions;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultVoiceConnectionFactory implements VoiceConnectionFactory {

    private final Map<Long, Subscription> onHandshake = new ConcurrentHashMap<>();

    @Override
    public Mono<VoiceConnection> create(long guildId,
                                        long selfId,
                                        String session,
                                        VoiceServerOptions voiceServerOptions,
                                        JacksonResources jacksonResources,
                                        VoiceReactorResources reactorResources,
                                        ReconnectOptions reconnectOptions,
                                        AudioProvider provider,
                                        AudioReceiver receiver,
                                        VoiceSendTaskFactory sendTaskFactory,
                                        VoiceReceiveTaskFactory receiveTaskFactory,
                                        VoiceDisconnectTask onDisconnectTask,
                                        VoiceServerUpdateTask serverUpdateTask) {
        return Mono.fromCallable(() -> new DefaultVoiceGatewayClient(guildId, selfId, session,
                jacksonResources.getObjectMapper(), reactorResources, reconnectOptions,
                provider, receiver, sendTaskFactory, receiveTaskFactory, onDisconnectTask, serverUpdateTask))
                .flatMap(client -> client.start(voiceServerOptions))
                .doOnSubscribe(s -> onHandshake.compute(guildId, (id, existing) -> {
                    if (existing != null) {
                        throw new IllegalStateException("Concurrent voice handshakes are not allowed for guild " + guildId);
                    }
                    return s;
                }))
                .doOnNext(vc -> onHandshake.remove(guildId));
    }
}
