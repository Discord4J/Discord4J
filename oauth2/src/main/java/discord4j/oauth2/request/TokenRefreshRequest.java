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

package discord4j.oauth2.request;

import discord4j.oauth2.GrantType;
import org.immutables.value.Value;

@Value.Immutable
public abstract class TokenRefreshRequest {

    public static ImmutableTokenRefreshRequest.Builder builder() {
        return ImmutableTokenRefreshRequest.builder();
    }

    public abstract long clientId();

    public abstract String clientSecret();

    public abstract String refreshToken();

    @Override
    public String toString() {
        return "client_id=" + clientId() +
                "&client_secret=" + clientSecret() +
                "&grant_type=" + GrantType.REFRESH_TOKEN.getValue() +
                "&refresh_token=" + refreshToken();
    }
}
