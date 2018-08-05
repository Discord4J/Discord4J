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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;

public class ClientException extends RuntimeException {

    private final HttpResponseStatus status;
    private final HttpHeaders headers;
    private final ErrorResponse errorResponse;

    public ClientException(HttpResponseStatus status, HttpHeaders headers, @Nullable ErrorResponse errorResponse) {
        this.status = status;
        this.headers = headers;
        this.errorResponse = errorResponse;
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
        return "ClientException[" +
                "status=" + status +
                ", headers=" + headers +
                ", errorResponse=" + errorResponse +
                ']';
    }
}
