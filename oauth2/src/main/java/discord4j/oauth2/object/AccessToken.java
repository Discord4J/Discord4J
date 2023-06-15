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
import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.WebhookData;
import discord4j.oauth2.Scope;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Access token details for a single OAuth2 authorized client.
 */
public class AccessToken {

    private final AccessTokenData data;
    private final Instant created = Instant.now();

    public AccessToken(AccessTokenData data) {
        this.data = data;
    }

    /**
     * Return the raw data object for this token
     *
     * @return a data object with the token details
     */
    public AccessTokenData getData() {
        return data;
    }

    /**
     * Return the OAuth2 access token.
     *
     * @return the OAuth2 access token
     */
    public String getAccessToken() {
        return data.accessToken();
    }

    /**
     * Return whether this token has expired according to the token expires_in field and this object creation date.
     *
     * @return {@code true} if this token has expired and a refresh token flow must be used, {@code false} otherwise
     */
    public boolean hasExpired() {
        return Instant.now().isAfter(created.plusSeconds(data.expiresIn()));
    }

    /**
     * Return the OAuth2 refresh token, if available.
     *
     * @return an Optional with the OAuth2 refresh token or an empty optional if not present
     */
    public Optional<String> getRefreshToken() {
        return data.refreshToken().toOptional();
    }

    /**
     * Return the OAuth2 scopes in this access token.
     *
     * @return an EnumSet of the scopes in the access token
     */
    public EnumSet<Scope> getScopes() {
        EnumSet<Scope> scopes = EnumSet.noneOf(Scope.class);
        List<String> values = Arrays.asList(data.scope().split(" "));
        for (Scope scope : Scope.values()) {
            if (values.contains(scope.getValue())) {
                scopes.add(scope);
            }
        }
        return scopes;
    }

    /**
     * Return information about the guild to which your bot was added, available when authorizing a user with a
     * {@link Scope#BOT} scope.
     *
     * @return a data object with the guild joined by your bot
     * @see <a href="https://discord.com/developers/docs/topics/oauth2#advanced-bot-authorization">Discord</a>
     */
    public Optional<GuildUpdateData> getGuild() {
        return data.guild().toOptional();
    }

    /**
     * Return webhook information if this client was authorized using a {@link Scope#WEBHOOK_INCOMING} scope.
     * By fetching {@link WebhookData#token()} and {@link WebhookData#id()} you can send messages with the webhook.
     *
     * @return webhook details tied to this access token, if present
     */
    public Optional<WebhookData> getWebhook() {
        return data.webhook().toOptional();
    }
}
