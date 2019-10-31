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

package discord4j.rest.request;

import discord4j.rest.http.client.ClientException;
import discord4j.rest.http.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * Wrapper for a {@link Mono} of {@link ClientResponse} to condense post-exchange calls.
 */
public class DiscordWebResponse {

    private final Mono<ClientResponse> responseMono;

    DiscordWebResponse(Mono<ClientResponse> responseMono) {
        this.responseMono = responseMono;
    }

    /**
     * Read the response body and extract it to a single object according to the {@code responseType} given. If the
     * response has an HTTP error (status codes 4xx and 5xx) the produced object will be a {@link ClientException}.
     *
     * @param responseClass the target type this response body should be converted into
     * @param <T> the response type
     * @return a {@link Mono} containing the response body extracted into the given {@code T} type. If a network or
     * read error had occurred, it will be emitted through the {@link Mono}.
     */
    public <T> Mono<T> bodyToMono(Class<T> responseClass) {
        return responseMono.flatMap(res -> res.bodyToMono(responseClass));
    }

    /**
     * Consume and release the response body then return and empty {@link Mono}.
     *
     * @return an empty {@link Mono} indicating response body consumption and release
     */
    public Mono<Void> skipBody() {
        return responseMono.flatMap(ClientResponse::skipBody);
    }

    /**
     * Return the underlying {@link Mono} of {@link ClientResponse}.
     *
     * @return the original {@link Mono} this response wrapper accesses
     */
    public Mono<ClientResponse> mono() {
        return responseMono;
    }
}
