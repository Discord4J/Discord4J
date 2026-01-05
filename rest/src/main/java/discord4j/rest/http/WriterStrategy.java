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

import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Strategy for encoding an object of type {@code <Req>} and writing the encoded stream of bytes to an {@link
 * reactor.netty.http.client.HttpClientRequest}.
 *
 * @param <R> the type of object in the body
 */
public interface WriterStrategy<R> {

    /**
     * Whether the given object type is supported by this writer.
     *
     * @param type the type of object to check
     * @param contentType the content type for the write
     * @return {@code true} if writable, {@code false} otherwise
     */
    boolean canWrite(@Nullable Class<?> type, @Nullable String contentType);

    /**
     * Write a given object to the output message.
     *
     * @param sender the http request sender
     * @param body the object to write
     * @return indicates completion or error
     */
    Mono<HttpClient.ResponseReceiver<?>> write(HttpClient.RequestSender sender, @Nullable R body);
}
