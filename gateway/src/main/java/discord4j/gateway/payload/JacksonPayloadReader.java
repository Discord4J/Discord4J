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
package discord4j.gateway.payload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.gateway.json.GatewayPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JacksonPayloadReader implements PayloadReader {

    private static final Logger log = Loggers.getLogger(JacksonPayloadReader.class);

    private final ObjectMapper mapper;
    private final boolean lenient;

    public JacksonPayloadReader(ObjectMapper mapper) {
        this(mapper, true);
    }

    @Deprecated
    public JacksonPayloadReader(ObjectMapper mapper, boolean lenient) {
        this.mapper = mapper;
        this.lenient = lenient;
    }

    @Override
    public Mono<GatewayPayload<?>> read(ByteBuf buf) {
        return Mono.create(sink -> {
            sink.onDispose(() -> ReferenceCountUtil.release(buf));
            try {
                GatewayPayload<?> value = mapper.readValue(
                        ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes(), false),
                        new TypeReference<GatewayPayload<?>>() {});
                sink.success(value);
            } catch (IllegalArgumentException e) {
                sink.success();
            } catch (IOException e) {
                if (lenient) {
                    // if eof input - just ignore
                    if (buf.readableBytes() > 0) {
                        log.warn("Error while decoding JSON ({}): {}", e.toString(),
                                new String(ByteBufUtil.getBytes(buf), StandardCharsets.UTF_8));
                    }
                    sink.success();
                } else {
                    sink.error(Exceptions.propagate(e));
                }
            }
        });
    }
}
