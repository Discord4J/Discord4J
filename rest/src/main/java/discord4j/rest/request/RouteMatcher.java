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

import discord4j.common.annotations.Experimental;
import discord4j.rest.route.Route;
import discord4j.rest.route.Routes;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A predicate that can match a given {@link DiscordWebRequest}. You can create instances of this class using the
 * {@link #route(Route)} factory, or through {@link #any()} to provide a catch-all matcher.
 */
@Experimental
public class RouteMatcher {

    @Nullable
    private final DiscordWebRequest request;

    @Nullable
    private final Predicate<Map<String, String>> requestVariableMatcher;

    private RouteMatcher(@Nullable DiscordWebRequest request) {
        this(request, null);
    }

    public RouteMatcher(@Nullable DiscordWebRequest request, @Nullable Predicate<Map<String, String>> requestVariableMatcher) {
        this.request = request;
        this.requestVariableMatcher = requestVariableMatcher;
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
    public static RouteMatcher route(Route route) {
        return new RouteMatcher(route.newRequest());
    }

    /**
     * Create a new {@link RouteMatcher} that matches every request made for the given {@link Route} that also match
     * a given {@link Predicate} of URI variables.
     * <p>
     * The given predicate will receive a {@link Map} of {@code String} URI template parameters as keys and {@code
     * String} values used to compile the URI for a {@link DiscordWebRequest}. This means you would expect keys as
     * {@code guild.id}, {@code channel.id}, {@code message.id}, {@code user.id}, among others. Refer to the actual
     * {@link Route} instances declared in the {@link Routes} class for the exact template keys used in the requests
     * you want to match.
     *
     * @param route the {@link Route} to be matched by this instance
     * @param requestVariableMatcher a {@link Map} of {@code String} keys and values representing the URI template and
     * the completed value for a given {@link DiscordWebRequest}, respectively
     * @return a new {@link RouteMatcher}
     */
    public static RouteMatcher route(Route route, Predicate<Map<String, String>> requestVariableMatcher) {
        return new RouteMatcher(route.newRequest(), requestVariableMatcher);
    }

    /**
     * Tests this matcher against the given {@link DiscordWebRequest}.
     *
     * @param otherRequest the {@link DiscordWebRequest} argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    public boolean matches(DiscordWebRequest otherRequest) {
        return matchesRoute(otherRequest) && matchesVariables(otherRequest);
    }

    private boolean matchesRoute(DiscordWebRequest otherRequest) {
        return request == null || request.getRoute().equals(otherRequest.getRoute());
    }

    private boolean matchesVariables(DiscordWebRequest otherRequest) {
        return requestVariableMatcher == null || otherRequest.matchesVariables(requestVariableMatcher);
    }
}
