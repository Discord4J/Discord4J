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
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link VoiceConnectionRegistry} that tracks local {@link VoiceConnection} instances.
 */
public class LocalVoiceConnectionRegistry implements VoiceConnectionRegistry {

    private static final Logger log = Loggers.getLogger(LocalVoiceConnectionRegistry.class);

    private final Map<Long, VoiceConnection> voiceConnections = new ConcurrentHashMap<>();

    @Override
    public Mono<VoiceConnection> getVoiceConnection(Snowflake guildId) {
        return Mono.fromCallable(() -> voiceConnections.get(guildId.asLong()))
                .flatMap(vc -> vc.stateEvents().next()
                        .doOnNext(state -> log.debug("Connection found to guild {} with state: {}",
                                guildId.asLong(), state))
                        .thenReturn(vc))
                .switchIfEmpty(Mono.<VoiceConnection>empty()
                        .doOnSubscribe(s -> log.debug("No connection in registry to guild {}", guildId.asLong())));
    }

    @Override
    public Mono<Void> registerVoiceConnection(Snowflake guildId, VoiceConnection voiceConnection) {
        return Mono.fromCallable(() -> voiceConnections.put(guildId.asLong(), voiceConnection))
                .flatMap(previous -> {
                    if (!previous.equals(voiceConnection)) {
                        log.debug("Removing previous guild {} connection from registry", guildId.asLong());
                        return previous.disconnect();
                    }
                    return Mono.empty();
                })
                .doFinally(signal -> log.debug("Connection registry to guild {} done after {}",
                        guildId.asLong(), signal));
    }

    @Override
    public Mono<Void> disconnect(Snowflake guildId) {
        return getVoiceConnection(guildId)
                .flatMap(connection -> connection.disconnect()
                        .doFinally(signal -> {
                            // TODO: verify if remove is the right approach on (complete|error|cancel)
                            voiceConnections.remove(guildId.asLong());
                            log.debug("Connection registry to guild {} removed after {}", guildId.asLong(), signal);
                        }));
    }
}
