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

package discord4j.rest.http.client;

import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.ReaderStrategy;
import discord4j.rest.json.response.ErrorResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.ReferenceCounted;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An HTTP response encapsulating status, headers and accessors to the response body for consumption.
 * <p>
 * Make sure you call {@link #bodyToMono(Class)}, {@link #skipBody()} or similar and actually consume the response
 * body. It is possible to use {@code bodyToMono(Void.class)} if the response is empty.
 */
public class ClientResponse {

    private static final Logger log = Loggers.getLogger(ClientResponse.class);

    private final HttpClientResponse response;
    private final NettyInbound inbound;
    private final ExchangeStrategies exchangeStrategies;
    private final ClientRequest clientRequest;
    private final AtomicBoolean reject = new AtomicBoolean();

    ClientResponse(HttpClientResponse response, NettyInbound inbound, ExchangeStrategies exchangeStrategies,
                   ClientRequest clientRequest) {
        this.response = response;
        this.inbound = inbound;
        this.exchangeStrategies = exchangeStrategies;
        this.clientRequest = clientRequest;
    }

    /**
     * Return the underlying {@link HttpClientResponse} from where you can access the response headers, status and
     * context.
     *
     * @return this response {@link HttpClientResponse}
     */
    public HttpClientResponse getResponse() {
        return response;
    }

    /**
     * Return the body of this response as a {@link Mono} of {@link ByteBuf}. If this {@link Mono} is cancelled, then
     * it will not be possible to consume the body again.
     *
     * @return the response body contents
     */
    public Mono<ByteBuf> getBody() {
        return inbound.receive()
                .aggregate()
                .doOnSubscribe(s -> {
                    if (reject.get()) {
                        throw new IllegalStateException("Response body can only be consumed once");
                    }
                })
                .doOnCancel(() -> reject.set(true))
                .map(ByteBuf::retain)
                .doOnNext(buf -> buf.touch("discord4j.client.response"));
    }

    /**
     * Read the response body and extract it to a single object according to the {@code responseType} given. If the
     * response has an HTTP error (status codes 4xx and 5xx) the produced object will be a {@link ClientException}.
     *
     * @param responseType the target type this response body should be converted into
     * @param <T> the response type
     * @return a {@link Mono} containing the response body extracted into the given {@code T} type. If a network or
     * read error had occurred, it will be emitted through the {@link Mono}.
     */
    public <T> Mono<T> bodyToMono(Class<T> responseType) {
        String responseContentType = response.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
        Optional<ReaderStrategy<?>> readerStrategy = exchangeStrategies.readers().stream()
                .filter(s -> s.canRead(responseType, responseContentType))
                .findFirst();
        Mono<ByteBuf> content = getBody().doOnDiscard(ByteBuf.class, buf -> {
            log.info("Releasing buffer on element discard");
            buf.release();
        });
        if (response.status().code() >= 400) {
            return Mono.justOrEmpty(readerStrategy)
                    .map(ClientResponse::<ErrorResponse>cast)
                    .flatMap(s -> s.read(content, ErrorResponse.class))
                    .flatMap(s -> Mono.<T>error(clientException(clientRequest, response, s)))
                    .switchIfEmpty(Mono.error(clientException(clientRequest, response, null)));
        } else {
            return readerStrategy.map(ClientResponse::<T>cast)
                    .map(s -> s.read(content, responseType))
                    .orElseGet(() -> Mono.error(noReaderException(responseType, responseContentType)));
        }
    }

    /**
     * Consume and release the response body then return and empty {@link Mono}.
     *
     * @return an empty {@link Mono} indicating response body consumption and release
     */
    public Mono<Void> skipBody() {
        return getBody().map(ReferenceCounted::release).then();
    }

    @SuppressWarnings("unchecked")
    private static <T> ReaderStrategy<T> cast(ReaderStrategy<?> strategy) {
        return (ReaderStrategy<T>) strategy;
    }

    private static ClientException clientException(ClientRequest request, HttpClientResponse response,
                                                   @Nullable ErrorResponse errorResponse) {
        return new ClientException(request, response, errorResponse);
    }

    private static RuntimeException noReaderException(Object body, String contentType) {
        return new RuntimeException("No strategies to read this response: " + body + " - " + contentType);
    }

    @Override
    public String toString() {
        return "ClientResponse{" +
                "response=" + response +
                '}';
    }
}
