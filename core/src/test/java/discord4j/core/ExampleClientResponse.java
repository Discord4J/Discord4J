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

import discord4j.core.command.CommandListener;
import discord4j.core.support.Commands;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;

/**
 * An example bot showcasing how to implement global handlers against some API responses. See
 * {@link DiscordClientBuilder#onClientResponse(ResponseFunction)} docs for more details.
 */
public class ExampleClientResponse {

    public static void main(String[] args) {
        DiscordClient.builder(System.getenv("token"))
                // globally suppress any not found (404) error
                //.onClientResponse(ResponseFunction.emptyIfNotFound())
                // bad requests (400) while adding reactions will be suppressed
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 400))
                .build()
                .withGateway(client -> client.on(CommandListener.createWithPrefix("!!")
                        .on("echo", Commands::echo)
                        .on("exit", ctx -> ctx.getClient().logout())
                        .on("status", Commands::status)));
    }
}
