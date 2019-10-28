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

import discord4j.rest.json.response.ErrorResponse;
import io.netty.util.IllegalReferenceCountException;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.util.annotation.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * Read a response as a {@code String} as a catch-all, unless the given response type is {@link ErrorResponse}, in which
 * case it will attempt to store the response into the {@link ErrorResponse} {@code body} field.
 */
public class FallbackReaderStrategy implements ReaderStrategy<Object> {

    @Override
    public boolean canRead(@Nullable Class<?> type, @Nullable String contentType) {
        return true;
    }

    @Override
    public Mono<Object> read(ByteBufMono content, Class<Object> responseType) {
        if (ErrorResponse.class.isAssignableFrom(responseType)) {
            return content.asString()
                    .map(body -> {
                        ErrorResponse response = new ErrorResponse();
                        response.getFields().put("body", body);
                        return response;
                    });
        }
        return content.handle((buf, sink) -> {
            try {
                sink.next(buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8).toString());
            } catch (IllegalReferenceCountException e) {
                sink.complete();
            }
        });
    }
}
