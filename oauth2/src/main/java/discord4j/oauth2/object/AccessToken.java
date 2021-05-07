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

import discord4j.common.util.Token;
import discord4j.discordjson.json.AccessTokenData;
import discord4j.discordjson.json.WebhookData;
import discord4j.oauth2.OAuth2Client;
import discord4j.oauth2.Scope;
import discord4j.oauth2.service.OAuth2Service;
import discord4j.oauth2.spec.TokenRefreshSpec;
import discord4j.oauth2.spec.TokenRevokeSpec;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An OAuth2 bearer token used to authenticate requests to the Discord API on behalf of a normal user.
 */
public class AccessToken extends Token {

    private final long clientId;
    private final OAuth2Service service;
    private final String clientSecret;
    private final Instant created = Instant.now();

    /**
     * Constructs an {@code AccessToken} from the given {@link OAuth2Service}, client ID, client secret, and Discord data.
     *
     * @param service The REST service that makes requests to Discord's OAuth2 API.
     * @param clientId The client ID of the Discord application.
     * @param clientSecret The client secret of the Discord application.
     * @param data The raw data as represented by Discord.
     */
    public AccessToken(final OAuth2Service service, final long clientId,
                       final String clientSecret,
                       final AccessTokenData data) {
        super(data);
        this.clientId = Objects.requireNonNull(clientId == 0 ? null : clientId, "clientId");;
        this.service = Objects.requireNonNull(service);
        this.clientSecret = Objects.requireNonNull(clientSecret);
    }

    /**
     * Constructs an {@code AccessToken} with an associated {@link OAuth2Client} and Discord data.
     *
     * @param client The {@link OAuth2Client} associated with this {@code AccessToken}.
     * @param data The raw data as represented by Discord.
     */
    public AccessToken(final OAuth2Client client, final AccessTokenData data) {
        this(client.getOAuth2Service(), client.getClientId(), client.getClientSecret(), data);
    }

    private Mono<Void> revoke(String token) {
        return service.revokeToken(new TokenRevokeSpec()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setToken(token)
                .asRequest());
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data of this {@code AccessToken}.
     */
    public final AccessTokenData getData() {
        return data;
    }

    /**
     * Gets when this {@code AccessToken} expires.
     *
     * @return When this {@code AccessToken} expires.
     */
    public final Instant getExpiration() {
        return created.plusSeconds(data.expiresIn());
    }

    /**
     * Gets whether this {@code AccessToken} has expired.
     *
     * @return Whether this {@code AccessToken} has expired.
     */
    public final boolean hasExpired() {
        return Instant.now().isAfter(getExpiration());
    }

    /**
     * Gets the refresh token of this {@code AccessToken}, if present.
     *
     * @return This {@code AccessToken}'s refresh token, if present.
     */
    public final Optional<String> getRefreshToken() {
        return data.refreshToken().toOptional();
    }

    /**
     * Gets the scopes this {@code AccessToken} has been granted.
     *
     * @return The scopes this {@code AccessToken} has been granted.
     */
    public final Set<Scope> getScopes() {
        return Arrays.stream(data.scope().split(" "))
                .map(Scope::of)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the webhook associated with this {@code AccessToken}, if present.
     *
     * @return The webhook of this {@code AccessToken}, if present.
     */
    public final Optional<WebhookData> getWebhook() {
        return data.webhook().toOptional();
    }

    /**
     * Exchanges this {@code AccessToken} for a new {@code AccessToken} using its refresh token.
     *
     * @return A {@link Mono} that emits the exchanged {@code AccessToken} upon successful completion. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public final Mono<AccessToken> refresh() {
        return service.refreshToken(new TokenRefreshSpec()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(getRefreshToken().orElseThrow(UnsupportedOperationException::new))
                .asRequest())
                .map(data -> new AccessToken(service, clientId, clientSecret, data));
    }

    /**
     * Revokes this {@code AccessToken}.
     *
     * @return A {@link Mono} that emits nothing upon successful completion, indicating that this {@code AccessToken}
     * has been revoked. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> revoke() {
        return revoke(asString());
    }

    /**
     * Revokes the refresh token of this {@code AccessToken}.
     *
     * @return A {@link Mono} that emits nothing upon successful completion, indicating that this {@code AccessToken}'s
     * refresh token has been revoked. If an error is received, it is emitted through the {@code Mono}.
     */
    public final Mono<Void> revokeRefreshToken() {
        return revoke(getRefreshToken().orElseThrow(UnsupportedOperationException::new));
    }

    /**
     * Gets the authorization information of this {@code AccessToken}.
     *
     * @return A {@link Mono} that emits the {@link AuthorizationInfo} upon successful completion. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public final Mono<AuthorizationInfo> getAuthorizationInfo() {
        return service.getAuthorizationInfo(asString()).map(AuthorizationInfo::new);
    }

    @Override
    public String toString() {
        return "AccessToken{} " + super.toString();
    }
}
