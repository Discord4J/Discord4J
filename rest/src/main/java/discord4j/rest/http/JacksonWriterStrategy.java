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
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Write to a request from an {@code Object} to a JSON {@code String} using Jackson.
 */
public class JacksonWriterStrategy implements WriterStrategy<Object> {

    private static final Logger log = Loggers.getLogger(JacksonWriterStrategy.class);

    private final ObjectMapper objectMapper;

    public JacksonWriterStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
        if (type == null || contentType == null || !contentType.startsWith("application/json")) {
            return false;
        }
        Class<?> rawClass = getJavaType(type).getRawClass();

        return (Object.class == rawClass)
                || !String.class.isAssignableFrom(rawClass) && objectMapper.canSerialize(rawClass);
    }

    @Override
    public Mono<HttpClient.ResponseReceiver<?>> write(HttpClient.RequestSender sender, @Nullable Object body) {
        if (body == null) {
            return Mono.error(new RuntimeException("Missing body"));
        }
        Mono<String> source = Mono.fromCallable(() -> mapToString(body))
                .doOnNext(json -> {
                    if (log.isTraceEnabled()) {
                        log.trace("{}", json);
                    }
                });
        return Mono.fromCallable(() -> sender.send(
                ByteBufFlux.fromString(source, StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT)));
    }

    private String mapToString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw Exceptions.propagate(e);
        }
    }

    private JavaType getJavaType(Type type) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(type);
    }
}
