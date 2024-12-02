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

import discord4j.rest.json.response.ErrorResponse;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Route;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.annotation.Nullable;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Exception that contains information about a failed request containing HTTP response data.
 * <p>
 * The original request can be retrieved through {@link #getRequest()}. HTTP response status can be retrieved by
 * {@link #getStatus()}, headers using {@link #getHeaders()}, while the body can be retrieved through
 * {@link #getErrorResponse()} provided Discord has supplied a body along with the error.
 * <p>
 * It is possible to modify the behavior of a reactive sequence that has failed with this error, using operators like
 * {@link Mono#onErrorResume(Predicate, Function)}, {@link Mono#onErrorContinue(Predicate, BiConsumer)} among others. In
 * cases where a {@link Predicate} is accepted, you can use one of the provided static methods like
 * {@link #isStatusCode(int)}
 * to further filter by HTTP status code.
 * <p>
 * The following example would retry a request if it has failed with an HTTP 500 error:
 * <pre>
 * client.getEventDispatcher().on(MessageCreateEvent.class)
 *     .map(MessageCreateEvent::getMessage)
 *     .filter(msg -&gt; msg.getContent().map("!ping"::equals).orElse(false))
 *     .flatMap(Message::getChannel)
 *     .flatMap(channel -&gt; channel.createMessage("Pong!")
 *         .transform(ClientException.retryOnceOnStatus(500)))
 *     .subscribe();
 * </pre>
 * While the following one would transform a not found user into an empty sequence:
 * <pre>
 * client.getUserById(Snowflake.of(userLongId))
 *     .onErrorResume(ClientException.isStatusCode(404), error -&gt; Mono.empty())
 *     .subscribe(user -&gt; System.out.println("Found: " + user.getUsername()));
 * </pre>
 * For global or {@link Route} based error handling, refer to the {@link ResponseFunction} class.
 */
public class ClientException extends RuntimeException {

    private final ClientRequest request;
    private final HttpClientResponse response;
    private final ErrorResponse errorResponse;

    /**
     * Create a new {@link ClientException} with the given HTTP request and response details.
     *
     * @param request the original {@link ClientRequest} that caused this exception
     * @param response the failing {@link HttpClientResponse}
     * @param errorResponse the response body converted to an {@link ErrorResponse}, or {@code null} if not available
     */
    public ClientException(ClientRequest request, HttpClientResponse response, @Nullable ErrorResponse errorResponse) {
        super(request.getMethod().toString() + " " + request.getUrl() + " returned " + response.status().toString() +
                (errorResponse != null ? " with response " + errorResponse.getFields() : ""));
        this.request = request;
        this.response = response;
        this.errorResponse = errorResponse;
    }

    /**
     * Return the {@link ClientRequest} encapsulating a Discord API request.
     *
     * @return the request that caused this exception
     */
    public ClientRequest getRequest() {
        return request;
    }

    /**
     * Return the {@link HttpClientResponse} encapsulating a low-level Discord API response.
     *
     * @return the low-level response that caused this exception
     */
    public HttpClientResponse getResponse() {
        return response;
    }

    /**
     * Return the {@link HttpResponseStatus} with information related to the HTTP error. The actual status code can be
     * obtained through {@link HttpResponseStatus#code()}.
     *
     * @return the HTTP error associated to this exception
     */
    public HttpResponseStatus getStatus() {
        return getResponse().status();
    }

    /**
     * Return the {@link HttpHeaders} from the error <strong>response</strong>. To get request headers refer to
     * {@link #getRequest()} and then {@link ClientRequest#getHeaders()}.
     *
     * @return the HTTP response headers
     */
    public HttpHeaders getHeaders() {
        return getResponse().responseHeaders();
    }

    /**
     * Return the HTTP response body in the form of a Discord {@link ErrorResponse}, if present. {@link ErrorResponse}
     * is a common object that contains an internal status code and messages, and could be used to further clarify
     * the source of the API error.
     *
     * @return the Discord error response, if present
     */
    public Optional<ErrorResponse> getErrorResponse() {
        return Optional.ofNullable(errorResponse);
    }

    /**
     * {@link Predicate} helper to further classify a {@link ClientException} depending on the underlying HTTP status
     * code.
     *
     * @param code the status code for which this {@link Predicate} should return {@code true}
     * @return a {@link Predicate} that returns {@code true} if the given {@link Throwable} is a {@link ClientException}
     * containing the given HTTP status code
     */
    public static Predicate<Throwable> isStatusCode(int code) {
        return t -> {
            if (t instanceof ClientException) {
                ClientException e = (ClientException) t;
                return e.getStatus().code() == code;
            }
            return false;
        };
    }

    /**
     * {@link Predicate} helper to further classify a {@link ClientException} depending on the underlying HTTP status
     * code.
     *
     * @param codes the status codes for which this {@link Predicate} should return {@code true}
     * @return a {@link Predicate} that returns {@code true} if the given {@link Throwable} is a {@link ClientException}
     * containing the given HTTP status code
     */
    public static Predicate<Throwable> isStatusCode(Integer... codes) {
        return t -> {
            if (t instanceof ClientException) {
                ClientException e = (ClientException) t;
                return Arrays.asList(codes).contains(e.getStatus().code());
            }
            return false;
        };
    }

    /**
     * Transformation function that can be used within an operator such as {@link Mono#transform(Function)} or
     * {@link Mono#transformDeferred(Function)} to turn an error sequence matching the given HTTP status code, into
     * an empty
     * sequence, effectively suppressing the original error.
     *
     * @param code the status code that should be transformed into empty sequences
     * @param <T> the type of the response
     * @return a transformation function that converts error sequences into empty sequences
     */
    public static <T> Function<Mono<T>, Publisher<T>> emptyOnStatus(int code) {
        return mono -> mono.onErrorResume(isStatusCode(code), t -> Mono.empty());
    }

    /**
     * Transformation function that can be used within an operator such as {@link Mono#transform(Function)} or
     * {@link Mono#transformDeferred(Function)} to apply a retrying strategy in case of an error matching the given
     * HTTP status
     * code. The provided retrying strategy will wait 1 second, and then retry once.
     *
     * @param code the status code that should be retried
     * @param <T> the type of the response
     * @return a transformation function that retries error sequences
     */
    public static <T> Function<Mono<T>, Publisher<T>> retryOnceOnStatus(int code) {
        return mono -> mono.retryWhen(Retry.backoff(1, Duration.ofSeconds(1)).filter(isStatusCode(code)));
    }
}
