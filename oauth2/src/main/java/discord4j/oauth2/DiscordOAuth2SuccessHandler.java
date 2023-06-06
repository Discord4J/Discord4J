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

import reactor.core.publisher.Mono;

/**
 * An interface to work with authorized users within a {@link DiscordOAuth2Server}. Allows retrieving details once
 * a user completes an authorization code grant request.
 */
@FunctionalInterface
public interface DiscordOAuth2SuccessHandler {

    /**
     * Invoked once a user completes an authorization code grant request for your OAuth2 application.
     *
     * @param client the authorized user client registration, allowing to perform API request on behalf of this user
     * @param sessionId a session identifier used by the server
     * @return a Mono signaling completion of the success handler, errors thrown are logged and discarded by the server
     */
    Mono<?> onAuthSuccess(DiscordOAuth2Client client, String sessionId);

}
