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

package discord4j.oauth2.object;

import discord4j.discordjson.json.AccessTokenData;

import java.time.Instant;
import java.util.Optional;

public class AccessToken {

    private final AccessTokenData data;
    private final Instant created = Instant.now();

    public AccessToken(AccessTokenData data) {
        this.data = data;
    }

    public String getAccessToken() {
        return data.accessToken();
    }

    public boolean hasExpired() {
        return Instant.now().isAfter(created.plusSeconds(data.expiresIn()));
    }

    public Optional<String> getRefreshToken() {
        return data.refreshToken().toOptional();
    }
}
