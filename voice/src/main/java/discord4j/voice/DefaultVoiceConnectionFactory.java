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
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation for a {@link VoiceConnectionFactory}. It uses a {@link DefaultVoiceGatewayClient} to create
 * {@link VoiceConnection} instances. Protects against concurrent {@link #create(VoiceGatewayOptions)} calls by sharing
 * the {@link Mono} subscription that actually establishes a voice connection.
 */
public class DefaultVoiceConnectionFactory implements VoiceConnectionFactory {

    private static final Logger log = Loggers.getLogger(DefaultVoiceConnectionFactory.class);

    private final Map<Long, Mono<VoiceConnection>> onHandshake = new ConcurrentHashMap<>();

    @Override
    public Mono<VoiceConnection> create(VoiceGatewayOptions options) {
        return Mono.defer(
                () -> onHandshake.compute(options.getGuildId().asLong(), (id, existing) -> {
                    if (existing != null) {
                        // another join request was issued while another one has not completed, avoid duplication
                        log.debug("Concurrent handshake detected for guild {}: returning existing", id);
                        return existing;
                    }
                    return Mono.usingWhen(
                            new DefaultVoiceGatewayClient(options)
                                    .start(options.getVoiceServerOptions(), options.getSession())
                                    // publish to avoid re-subscription: await until connected or disconnect and emit
                                    .publish(mono -> mono.flatMap(vc -> vc.onConnectOrDisconnect().thenReturn(vc))
                                            .doFinally(signal -> onHandshake.remove(options.getGuildId().asLong()))),
                            Mono::just, // produce a voice connection
                            vc -> Mono.empty(), // no cleanup procedure required if successful
                            (vc, t) -> vc.disconnect(), // disconnect on error
                            VoiceConnection::disconnect); // disconnect on cancel (i.e. join timeout)
                }));
    }
}
