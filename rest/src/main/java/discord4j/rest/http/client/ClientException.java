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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientResponse;
import reactor.retry.Retry;
import reactor.retry.RetryContext;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public class ClientException extends RuntimeException {

    private final ClientRequest request;
    private final HttpResponseStatus status;
    private final HttpHeaders headers;
    private final ErrorResponse errorResponse;

    public ClientException(ClientRequest request, HttpClientResponse response, @Nullable ErrorResponse errorResponse) {
        super(request.method().toString() + " " + request.url() + " returned " + response.status().toString() +
                (errorResponse != null ? " with response " + errorResponse.getFields() : ""));
        this.request = request;
        this.status = response.status();
        this.headers = response.responseHeaders();
        this.errorResponse = errorResponse;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    @Nullable
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "request=" + request +
                ", status=" + status +
                ", headers=" + headers.copy().remove(HttpHeaderNames.AUTHORIZATION).toString() +
                ", errorResponse=" + errorResponse +
                "}";
    }

    public static Predicate<Throwable> isStatusCode(int code) {
        return t -> {
            if (t instanceof ClientException) {
                ClientException e = (ClientException) t;
                return e.getStatus().code() == code;
            }
            return false;
        };
    }

    public static Predicate<Throwable> isStatusCode(Integer... codes) {
        return t -> {
            if (t instanceof ClientException) {
                ClientException e = (ClientException) t;
                return Arrays.asList(codes).contains(e.getStatus().code());
            }
            return false;
        };
    }

    public static Predicate<RetryContext<?>> isRetryContextStatusCode(int code) {
        return ctx -> {
            if (ctx.exception() instanceof ClientException) {
                ClientException e = (ClientException) ctx.exception();
                return e.getStatus().code() == code;
            }
            return false;
        };
    }

    public static Predicate<RetryContext<?>> isRetryContextStatusCode(Integer... codes) {
        return ctx -> {
            if (ctx.exception() instanceof ClientException) {
                ClientException e = (ClientException) ctx.exception();
                return Arrays.asList(codes).contains(e.getStatus().code());
            }
            return false;
        };
    }

    public static <T> Function<Mono<T>, Publisher<T>> emptyOnStatus(int code) {
        return mono -> mono.onErrorResume(isStatusCode(code), t -> Mono.empty());
    }

    public static <T> Function<Mono<T>, Publisher<T>> retryOnceOnStatus(int code) {
        return mono -> mono.retryWhen(Retry.onlyIf(isRetryContextStatusCode(code)).retryOnce());
    }
}
