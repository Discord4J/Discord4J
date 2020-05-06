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

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link VoiceConnectionRegistry} that tracks local {@link VoiceConnection} instances.
 */
public class LocalVoiceConnectionRegistry implements VoiceConnectionRegistry {

    private final Map<Long, VoiceConnection> voiceConnections = new ConcurrentHashMap<>();

    @Override
    public Mono<VoiceConnection> getVoiceConnection(long guildId) {
        return Mono.justOrEmpty(voiceConnections.get(guildId));
    }

    @Override
    public Mono<Void> registerVoiceConnection(long guildId, VoiceConnection voiceConnection) {
        VoiceConnection connection = voiceConnections.put(guildId, voiceConnection);
        return connection == null ? Mono.empty() : connection.disconnect();
    }

    @Override
    public Mono<Void> disconnect(long guildId) {
        VoiceConnection connection = voiceConnections.get(guildId);
        return connection == null ? Mono.empty() :
                connection.disconnect().doFinally(s -> voiceConnections.remove(guildId));
    }
}
