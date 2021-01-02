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
package discord4j.gateway.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.gateway.json.GatewayPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class JacksonPayloadWriter implements PayloadWriter {

    private final ObjectMapper mapper;

    public JacksonPayloadWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<ByteBuf> write(GatewayPayload<?> payload) {
        return Mono.create(sink -> sink.onRequest(__ -> {
            try {
                sink.success(Unpooled.wrappedBuffer(mapper.writeValueAsBytes(payload)));
            } catch (JsonProcessingException e) {
                sink.error(Exceptions.propagate(e));
            }
        }));
    }
}
