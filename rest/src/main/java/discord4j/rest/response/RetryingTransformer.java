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

import discord4j.common.annotations.Experimental;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.request.DiscordWebRequest;
import discord4j.rest.request.RouteMatcher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * A {@link ResponseFunction} that is able to transform an error sequence into a retrying one.
 */
@Experimental
public class RetryingTransformer implements ResponseFunction {

    private final RouteMatcher routeMatcher;
    private final reactor.util.retry.Retry retryStrategy;

    public RetryingTransformer(RouteMatcher routeMatcher, reactor.util.retry.Retry retryStrategy) {
        this.routeMatcher = routeMatcher;
        this.retryStrategy = retryStrategy;
    }

    @Override
    public Function<Mono<ClientResponse>, Mono<ClientResponse>> transform(DiscordWebRequest request) {
        if (routeMatcher.matches(request)) {
            return mono -> mono.retryWhen(retryStrategy);
        }
        return mono -> mono;
    }
}
