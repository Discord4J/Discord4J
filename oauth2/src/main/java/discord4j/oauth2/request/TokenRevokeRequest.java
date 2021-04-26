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

import org.immutables.value.Value;

@Value.Immutable
public abstract class TokenRevokeRequest {

    public static ImmutableTokenRevokeRequest.Builder builder() {
        return ImmutableTokenRevokeRequest.builder();
    }

    public abstract long clientId();

    public abstract String clientSecret();

    public abstract String token();

    @Override
    public String toString() {
        return "token=" + token();
    }
}
