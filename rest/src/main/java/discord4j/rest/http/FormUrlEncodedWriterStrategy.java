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

import io.netty.buffer.ByteBufAllocator;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.nio.charset.StandardCharsets;

/**
 * Write to a request from a {@code String} using reactor-netty's {@link
 * reactor.netty.http.client.HttpClient.RequestSender#send(Publisher)}.
 */
public class FormUrlEncodedWriterStrategy implements WriterStrategy<String> {

    private static final Logger log = Loggers.getLogger(FormUrlEncodedWriterStrategy.class);

    @Override
    public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
        return type != null && contentType != null && contentType.startsWith("application/x-www-form-urlencoded") &&
                String.class.isAssignableFrom(type);
    }

    @Override
    public Mono<HttpClient.ResponseReceiver<?>> write(HttpClient.RequestSender sender, @Nullable String body) {
        if (body == null) {
            return Mono.error(new RuntimeException("Missing body"));
        }
        Mono<String> source = Mono.just(body).doOnNext(payload -> {
            if (log.isTraceEnabled()) {
                log.trace("{}", body);
            }
        });
        return Mono.fromCallable(() -> sender.send(
                ByteBufFlux.fromString(source, StandardCharsets.UTF_8, ByteBufAllocator.DEFAULT)));
    }
}
