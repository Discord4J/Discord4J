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

package discord4j.oauth2;

import discord4j.common.annotations.Experimental;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * An I/O handler to customize the response given by a {@link DiscordOAuth2Server} after a login process is completed.
 */
@FunctionalInterface
@Experimental
public interface DiscordOAuth2ResponseHandler {

    /**
     * Handle an incoming request to provide a response after an OAuth2 login is completed.
     *
     * @param client an authenticated client to perform API operations on behalf of the user
     * @param req a reactor-netty HTTP request accessors
     * @param res a reactor-netty HTTP response accessors
     * @return the response sent to the user, typically derived from one of the {@code send*} methods in
     * {@link HttpServerResponse}
     */
    Publisher<Void> handle(DiscordOAuth2Client client, HttpServerRequest req, HttpServerResponse res);

}
