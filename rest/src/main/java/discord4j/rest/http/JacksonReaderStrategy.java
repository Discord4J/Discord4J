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
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Read a response into JSON and convert to an Object of type {@code <Res>} using Jackson 2.9.
 *
 * @param <Res> the type of object in the read response
 */
public class JacksonReaderStrategy<Res> implements ReaderStrategy<Res> {

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

    @Override
    public Mono<Res> read(HttpClientResponse response, Class<Res> responseType) {
        Objects.requireNonNull(response);
        Objects.requireNonNull(responseType);
        return response.receive().aggregate().asByteArray().map(bytes -> {
            try {
                return objectMapper.readValue(bytes, responseType);
            } catch (JsonProcessingException e) {
                throw Exceptions.propagate(new RuntimeException(e.toString()
                        .replaceAll("(\"token\": \")([A-Za-z0-9.-]*)(\")", "$1hunter2$3")));
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private JavaType getJavaType(Type type) {
        return objectMapper.getTypeFactory().constructType(type);
    }
}
