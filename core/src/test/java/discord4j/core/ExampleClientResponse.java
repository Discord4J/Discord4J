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

package discord4j.core;

import discord4j.core.support.BotSupport;
import discord4j.core.support.ExtraBotSupport;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.request.RouterOptions;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class ExampleClientResponse {

    public static void main(String[] args) {
        DiscordClient client = new DiscordClientBuilder(System.getenv("token"))
                .setRouterOptions(RouterOptions.builder()
                        // globally suppress any not found (404) error
                        //.onClientResponse(ResponseFunction.emptyIfNotFound())
                        // bad requests (400) while adding reactions will be suppressed
                        .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
                        .build())
                .build();
        Mono.zip(
                BotSupport.create(client).eventHandlers(),
                ExtraBotSupport.create(client).eventHandlers(),
                client.login()
        ).block();
    }
}
