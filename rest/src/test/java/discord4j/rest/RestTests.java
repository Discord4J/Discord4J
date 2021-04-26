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

package discord4j.rest;

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.common.util.Token;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

public abstract class RestTests {

    private static Router DEFAULT_ROUTER;

    public static Router defaultRouter() {
        if (DEFAULT_ROUTER == null) {
            DEFAULT_ROUTER = createDefaultRouter(Objects.requireNonNull(System.getenv("token")));
        }
        return DEFAULT_ROUTER;
    }

    private static Router createDefaultRouter(String token) {
        return new DefaultRouter(new RouterOptions(Mono.just(Token.of(token)), AuthorizationScheme.BOT,
                ReactorResources.create(), ExchangeStrategies.jackson(JacksonResources.create().getObjectMapper()),
                Collections.emptyList(), BucketGlobalRateLimiter.create(), RequestQueueFactory.buffering(),
                Routes.BASE_URL));
    }
}
