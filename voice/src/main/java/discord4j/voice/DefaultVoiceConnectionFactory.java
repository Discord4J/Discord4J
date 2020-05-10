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

public class DefaultVoiceConnectionFactory implements VoiceConnectionFactory {

    private final Map<Long, Mono<VoiceConnection>> onHandshake = new ConcurrentHashMap<>();

    @Override
    public Mono<VoiceConnection> create(VoiceGatewayOptions options) {
        return Mono.defer(
                () -> onHandshake.compute(options.getGuildId(), (id, existing) -> {
                    if (existing != null) {
                        return existing;
                    }
                    return Mono.fromCallable(() -> new DefaultVoiceGatewayClient(options))
                            .flatMap(client -> client.start(options.getVoiceServerOptions()))
                            .doFinally(s -> onHandshake.remove(options.getGuildId()))
                            .cache()
                            .publish(mono -> mono.flatMap(vc -> vc.onConnectOrDisconnect().thenReturn(vc)));
                }));
    }
}
