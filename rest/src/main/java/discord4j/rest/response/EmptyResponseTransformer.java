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
import java.util.function.Predicate;

/**
 * A {@link ResponseFunction} that is able to transform an error sequence with a HTTP 404 status, into an empty
 * sequence.
 *
 * @see ResponseFunction#emptyIfNotFound()
 * @see ResponseFunction#emptyIfNotFound(RouteMatcher)
 * @see ResponseFunction#emptyOnErrorStatus(RouteMatcher, Integer...)
 */
@Experimental
public class EmptyResponseTransformer implements ResponseFunction {

    private final RouteMatcher routeMatcher;
    private final Predicate<Throwable> predicate;

    public EmptyResponseTransformer(RouteMatcher routeMatcher, Predicate<Throwable> predicate) {
        this.routeMatcher = routeMatcher;
        this.predicate = predicate;
    }

    @Override
    public Function<Mono<ClientResponse>, Mono<ClientResponse>> transform(DiscordWebRequest request) {
        if (routeMatcher.matches(request)) {
            return mono -> mono.onErrorResume(predicate, t -> Mono.empty());
        }
        return mono -> mono;
    }
}
