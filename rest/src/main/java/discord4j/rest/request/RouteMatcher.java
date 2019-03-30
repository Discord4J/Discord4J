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

import discord4j.rest.route.Route;
import discord4j.rest.route.Routes;
import reactor.util.annotation.Nullable;

/**
 * A predicate that can match a given {@link DiscordRequest}.
 */
public class RouteMatcher {

    @Nullable
    private final DiscordRequest<?> request;

    public RouteMatcher(DiscordRequest<?> request) {
        this.request = request;
    }

    /**
     * Create a new {@link RouteMatcher} that returns true for every request.
     *
     * @return a new {@link RouteMatcher}
     */
    public static RouteMatcher any() {
        return new RouteMatcher(null);
    }

    /**
     * Create a new {@link RouteMatcher} that matches any request made for the given {@link Route}. A list of
     * {@link Route} objects exist in the {@link Routes} class.
     *
     * @param route the {@link Route} to be matched by this instance
     * @return a new {@link RouteMatcher}
     */
    public static RouteMatcher route(Route<?> route) {
        return new RouteMatcher(route.newRequest());
    }

    /**
     * Tests this matcher against the given {@link DiscordRequest}.
     *
     * @param otherRequest the {@link DiscordRequest} argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    public boolean matches(DiscordRequest<?> otherRequest) {
        return request == null || request.getRoute().getResponseType().isInstance(otherRequest.getRoute().getResponseType());
    }
}
