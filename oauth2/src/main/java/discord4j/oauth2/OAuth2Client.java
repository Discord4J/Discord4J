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

import discord4j.discordjson.json.AccessTokenData;
import discord4j.oauth2.OAuth2ClientBuilder.Resources;
import discord4j.oauth2.object.AccessToken;
import discord4j.oauth2.spec.AuthorizationCodeGrantSpec;
import discord4j.oauth2.spec.ClientCredentialsGrantSpec;
import discord4j.rest.RestClient;
import discord4j.rest.RestResources;
import discord4j.rest.request.RouterOptions;
import discord4j.oauth2.service.OAuth2Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link RestClient} capable of making requests to the Discord API on behalf of a user using their {@link AccessToken}.
 */
public class OAuth2Client extends RestClient {

    private final OAuth2Service oAuth2Service;
    private final long clientId;
    private final String clientSecret;

    /**
     * Obtain an {@link OAuth2ClientBuilder} able to create {@link OAuth2Client} instances, using an {@link AccessToken}
     * exchanged via an authorization code, produced from the modified spec for authentication.
     *
     * @param spec a {@link Consumer} that provides a "blank" {@link AuthorizationCodeGrantSpec} to be operated on
     * @return an {@link OAuth2ClientBuilder}
     */
    public static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B> createFromCode(Consumer<? super AuthorizationCodeGrantSpec> spec) {
        return OAuth2ClientBuilder.createFromCode(spec);
    }

    /**
     * Obtain an {@link OAuth2ClientBuilder} able to create {@link OAuth2Client} instances, using an {@link AccessToken}
     * exchanged via client credentials, produced from the modified spec for authentication.
     *
     * @param spec a {@link Consumer} that provides a "blank" {@link ClientCredentialsGrantSpec} to be operated on
     * @return an {@link OAuth2ClientBuilder}
     */
    public static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B> createFromCredentials(Consumer<? super ClientCredentialsGrantSpec> spec) {
        return OAuth2ClientBuilder.createFromCredentials(spec);
    }

    /**
     * Obtain an {@link OAuth2ClientBuilder} able to create {@link OAuth2Client} instances, using the {@link AccessTokenData}
     * retrieved from an {@link OAuth2Server}
     *
     * @param data access token data obtained from Discord via an {@link OAuth2Server}
     * @return an {@link OAuth2ClientBuilder}
     */
    public static <B extends OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B>> OAuth2ClientBuilder<OAuth2Client, Resources, RouterOptions, B> createFromData(AccessTokenData data) {
        return OAuth2ClientBuilder.createFromData(data);
    }

    protected OAuth2Client(RestResources restResources,
                           OAuth2Service oAuth2Service,
                           long clientId, String clientSecret) {
        super(restResources);
        this.oAuth2Service = Objects.requireNonNull(oAuth2Service, "oAuth2Service");
        this.clientId = Objects.requireNonNull(clientId == 0 ? null : clientId, "clientId");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret");
    }

    /**
     * Access a low-level representation of the API endpoints for the OAuth2 resource.
     *
     * @return a handle to perform low-level requests to the API
     */
    public final OAuth2Service getOAuth2Service() {
        return oAuth2Service;
    }

    /**
     * Obtain the client ID of the Discord application associated with this {@code OAuth2Client}.
     *
     * @return the client ID for this client
     */
    public final long getClientId() {
        return clientId;
    }

    public final Mono<Long> getApplicationId() {
        return Mono.just(clientId);
    }

    /**
     * Obtain the client secret of the Discord application associated with this {@code OAuth2Client}.
     *
     * @return the client secret for this client
     */
    public final String getClientSecret() {
        return clientSecret;
    }

    /**
     * Requests to retrieve an {@link AccessToken} exchanged via an authorization code, produced from the modified spec.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link AuthorizationCodeGrantSpec} to be operated on.
     * @return An {@link AccessToken} produced from the modified spec.
     */
    public final Mono<AccessToken> getToken(final Consumer<? super AuthorizationCodeGrantSpec> spec) {
        return Mono.defer(
                () -> {
                    AuthorizationCodeGrantSpec mutatedSpec = new AuthorizationCodeGrantSpec();
                    spec.accept(mutatedSpec);
                    return oAuth2Service.exchangeAuthorizationCode(mutatedSpec
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .asRequest());
                }).map(data -> new AccessToken(this, data));
    }

    /**
     * Requests to retrieve an {@link AccessToken} of the owner of the Discord application associated with this client,
     * exchanged via client credentials with the scopes supplied to the spec.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link ClientCredentialsGrantSpec} to be operated on.
     * @return An {@link AccessToken} produced from the modified spec.
     */
    public final Mono<AccessToken> getApplicationOwnerToken(final Consumer<? super ClientCredentialsGrantSpec> spec) {
        return Mono.defer(
                () -> {
                    ClientCredentialsGrantSpec mutatedSpec = new ClientCredentialsGrantSpec();
                    spec.accept(mutatedSpec);
                    return oAuth2Service.exchangeClientCredentials(mutatedSpec
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .asRequest());
                })
                .map(data -> new AccessToken(this, data));
    }
}
