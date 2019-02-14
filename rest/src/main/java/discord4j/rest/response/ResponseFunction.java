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

package discord4j.rest.response;

import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.DiscordRequest;
import discord4j.rest.request.RouteMatcher;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

public interface ResponseFunction {

    <T> Function<Mono<T>, Mono<T>> transform(DiscordRequest<T> request);

    static EmptyResponseTransformer emptyWhenNotFound() {
        return new EmptyResponseTransformer(RouteMatcher.any(), ClientException.isStatusCode(404));
    }

    static EmptyResponseTransformer emptyWhenNotFound(RouteMatcher routeMatcher) {
        return new EmptyResponseTransformer(routeMatcher, ClientException.isStatusCode(404));
    }

    static EmptyResponseTransformer emptyOnErrorStatus(RouteMatcher routeMatcher, Integer... codes) {
        return new EmptyResponseTransformer(routeMatcher, ClientException.isStatusCode(codes));
    }

    static RetryingTransformer retryOnceOnErrorStatus(Integer... codes) {
        return new RetryingTransformer(RouteMatcher.any(),
                Retry.onlyIf(ClientException.isRetryContextStatusCode(codes))
                        .fixedBackoff(Duration.ofSeconds(1))
                        .retryOnce());
    }

    static RetryingTransformer retryWhen(RouteMatcher routeMatcher, Retry<?> retry) {
        return new RetryingTransformer(routeMatcher, retry);
    }
}
