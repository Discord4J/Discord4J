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

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

/**
 * Strategy for reading from a {@link reactor.netty.http.client.HttpClientResponse} and decoding the stream of bytes
 * to an Object of type {@code <Res>}.
 *
 * @param <T> the type of object in the read response
 */
public interface ReaderStrategy<T> {

    /**
     * Whether the given object type is supported by this reader.
     *
     * @param type the type of object to check
     * @param contentType the content type for the read
     * @return {@code true} if readable, {@code false} otherwise
     */
    boolean canRead(@Nullable Class<?> type, @Nullable String contentType);

    /**
     * Read from the input message and encode to a single object.
     *
     * @param content the response contents
     * @param responseType the type of object in the response which must have been previously checked via {@link
     * #canRead(Class, String)}
     * @return a Mono for the resolved response, according to the given response type
     */
    Mono<T> read(Mono<ByteBuf> content, Class<T> responseType);
}
