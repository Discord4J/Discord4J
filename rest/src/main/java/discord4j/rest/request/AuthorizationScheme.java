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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.request;

/**
 * The type of authentication that precedes the {@link discord4j.common.util.Token} when authenticating to the Discord
 * API via the HTTP authorization header.
 *
 * @see <a href="https://discord.com/developers/docs/reference#authentication">Authentication</a>
 */
public enum AuthorizationScheme {

    /** Bot token authorization **/
    BOT("Bot"),

    /** OAuth2 bearer token authorization **/
    BEARER("Bearer"),

    /** No authorization **/
    NONE("");

    private final String value;

    AuthorizationScheme(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
