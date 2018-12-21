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
package discord4j.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VoiceClient {

    private final Scheduler scheduler;
    private final ObjectMapper mapper;

    public VoiceClient(Scheduler scheduler, ObjectMapper mapper) {
        this.scheduler = scheduler;
        this.mapper = mapper;
    }

    public Mono<VoiceConnection> newConnection(long guildId, long selfId, String session, String token, AudioProvider provider, String gatewayUrl) {
        return Mono.create(sink -> {
            VoiceGatewayClient vgw = new VoiceGatewayClient(guildId, selfId, session, token, mapper, scheduler, provider);
            vgw.start(gatewayUrl, () -> {
                VoiceConnection connection = new VoiceConnection(vgw);
                sink.success(connection);
            });
            sink.onCancel(vgw::stop);
        });
    }
}
