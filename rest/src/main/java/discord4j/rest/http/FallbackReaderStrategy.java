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

import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Read a response as a {@code String}, regardless of its type and response Content-Type. It serves as a "catch-all"
 * reader.
 */
public class FallbackReaderStrategy implements ReaderStrategy<String> {

    @Override
    public boolean canRead(@Nullable Class<?> type, @Nullable String contentType) {
        return true;
    }

    @Override
    public Mono<String> read(HttpClientResponse response, Class<String> responseType) {
        Objects.requireNonNull(response);
        return response.receive().aggregate().asString();
    }
}
