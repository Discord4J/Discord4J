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

import discord4j.gateway.json.GatewayPayload;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

/**
 * Strategy for reading from a {@link ByteBuf} and decoding its contents to a {@link Publisher} of
 * {@link GatewayPayload}.
 */
public interface PayloadReader {

    /**
     * Read from the input buffer and encode to a single object.
     *
     * @param payload the input byte buffer
     * @return a publisher of {@code GatewayPayload} representing the inbound payload
     */
    Publisher<GatewayPayload<?>> read(ByteBuf payload);

    /**
     * Decode {@link GatewayPayload} instances from a {@link Flux} of input buffers.
     *
     * @param input the input stream of {@link ByteBuf} instances
     * @return a publisher of {@code GatewayPayload} representing the inbound payloads result
     */
    Publisher<GatewayPayload<?>> decode(Flux<ByteBuf> input);

}
