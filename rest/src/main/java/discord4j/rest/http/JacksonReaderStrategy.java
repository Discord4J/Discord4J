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
package discord4j.rest.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import org.jspecify.annotations.Nullable;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Read a response into JSON and convert to an Object of type {@code <Res>} using Jackson.
 *
 * @param <T> the type of object in the read response
 */
public class JacksonReaderStrategy<T> implements ReaderStrategy<T> {

    private static final Logger log = Loggers.getLogger(JacksonReaderStrategy.class);

    private final ObjectMapper objectMapper;

    public JacksonReaderStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canRead(@Nullable Class<?> type, @Nullable String contentType) {
        if (type == null || contentType == null || !contentType.startsWith("application/json")) {
            return false;
        }

        // A Route<String> should be read by the FallbackReader
        return !CharSequence.class.isAssignableFrom(type) && objectMapper.canDeserialize(getJavaType(type));
    }

    private JavaType getJavaType(Type type) {
        return objectMapper.getTypeFactory().constructType(type);
    }

    @Override
    public Mono<T> read(Mono<ByteBuf> content, Class<T> responseType) {
        return content.<Mono<byte[]>>as(JacksonReaderStrategy::byteArray)
                .map(bytes -> {
                    if (log.isTraceEnabled()) {
                        log.trace("{}", new String(bytes, StandardCharsets.UTF_8));
                    }
                    try {
                        return objectMapper.readValue(bytes, responseType);
                    } catch (JsonProcessingException e) {
                        throw Exceptions.propagate(new RuntimeException(e.toString()
                                .replaceAll("(\"token\": ?\")([A-Za-z0-9._-]*)(\")", "$1hunter2$3")));
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    private static Mono<byte[]> byteArray(Mono<ByteBuf> byteBufMono) {
        return byteBufMono.handle((buf, sink) -> {
            try {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                sink.next(bytes);
            } catch (IllegalReferenceCountException e) {
                sink.complete();
            }
        });
    }
}
