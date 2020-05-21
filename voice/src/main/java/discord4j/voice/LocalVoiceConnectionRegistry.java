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

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link VoiceConnectionRegistry} that tracks local {@link VoiceConnection} instances.
 */
public class LocalVoiceConnectionRegistry implements VoiceConnectionRegistry {

    private final Map<Long, VoiceConnection> voiceConnections = new ConcurrentHashMap<>();

    @Override
    public Mono<VoiceConnection> getVoiceConnection(Snowflake guildId) {
        return Mono.fromCallable(() -> voiceConnections.get(guildId.asLong()));
    }

    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    @Override
    public Mono<Void> registerVoiceConnection(Snowflake guildId, VoiceConnection voiceConnection) {
        return Mono.fromCallable(() -> voiceConnections.put(guildId.asLong(), voiceConnection))
                .flatMap(previous -> {
                    if (previous != null && !previous.equals(voiceConnection)) {
                        return previous.disconnect();
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> disconnect(Snowflake guildId) {
        return getVoiceConnection(guildId)
                .flatMap(connection -> connection.disconnect().doFinally(s -> voiceConnections.remove(guildId.asLong())));
    }
}
