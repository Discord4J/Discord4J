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

import discord4j.discordjson.json.AuthorizationInfoData;
import discord4j.discordjson.json.PartialApplicationInfoData;
import discord4j.discordjson.json.UserData;
import discord4j.oauth2.Scope;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Information about the current authorization.
 *
 * @see <a href="https://discord.com/developers/docs/topics/oauth2#get-current-authorization-information-response-structure">
 * Current Authorization Information</a>
 */
public class AuthorizationInfo {

    private final AuthorizationInfoData data;

    /**
     * Constructs a {@code AuthorizationInfo} from the given Discord data.
     *
     * @param data The raw data as represented by Discord.
     */
    public AuthorizationInfo(final AuthorizationInfoData data) {
        this.data = data;
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data of this {@code AuthorizationInfo}.
     */
    public final AuthorizationInfoData getData() {
        return data;
    }

    /**
     * Gets basic information about the application associated with this {@code AuthorizationInfo}.
     *
     * @return Basic information about the application of this {@code AuthorizationInfo}.
     */
    public final PartialApplicationInfoData getApplication() {
        return data.application();
    }

    /**
     * Gets the scopes the access token associated with this {@code AuthorizationInfo} has been granted.
     *
     * @return The scopes that the access token has been granted.
     */
    public final Set<Scope> getScopes() {
        return data.scopes().stream()
                .map(Scope::of)
                .collect(Collectors.toSet());
    }

    /**
     * Gets when the access token associated with this {@code AuthorizationInfo} expires.
     *
     * @return When the access token expires.
     */
    public final Instant getExpiration() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(data.expires(), Instant::from);
    }

    /**
     * Gets whether the access token associated with this {@code AuthorizationInfo} has expired.
     *
     * @return Whether the access token has expired.
     */
    public final boolean hasExpired() {
        return Instant.now().isAfter(getExpiration());
    }

    /**
     * Gets the user associated with this {@code AuthorizationInfo}, if present.
     *
     * @return The user who has authorized, if present.
     */
    public final Optional<UserData> getUser() {
        return data.user().toOptional();
    }

    @Override
    public String toString() {
        return "AuthorizationInfo{" +
                "data=" + data +
                '}';
    }
}
