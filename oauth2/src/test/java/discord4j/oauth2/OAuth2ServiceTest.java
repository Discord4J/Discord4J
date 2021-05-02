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

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.discordjson.json.AccessTokenData;
import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.discordjson.json.ClientCredentialsGrantRequest;
import discord4j.discordjson.json.TokenRefreshRequest;
import discord4j.discordjson.json.TokenRevokeRequest;
import discord4j.oauth2.service.OAuth2Service;
import discord4j.oauth2.spec.AuthorizationCodeGrantSpec;
import discord4j.oauth2.spec.ClientCredentialsGrantSpec;
import discord4j.oauth2.spec.TokenRefreshSpec;
import discord4j.oauth2.spec.TokenRevokeSpec;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.route.Routes;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Mono;

import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OAuth2ServiceTest {

    private static final long clientId = Long.parseLong(System.getenv("id"));
    private static final String clientSecret = System.getenv("secret");

    private OAuth2Service oAuth2Service;
    private AccessTokenData tokenData;

    @BeforeAll
    public void setup() {
        ExchangeStrategies strategies = ExchangeStrategies.jackson(JacksonResources.create().getObjectMapper());
        Router router = new DefaultRouter(new RouterOptions(Mono.empty(), AuthorizationScheme.NONE,
                ReactorResources.create(), strategies, Collections.emptyList(),
                BucketGlobalRateLimiter.create(), RequestQueueFactory.buffering(), Routes.BASE_URL));

        oAuth2Service = new OAuth2Service(router);
        AuthorizationCodeGrantRequest req = new AuthorizationCodeGrantSpec()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setCode(System.getenv("code"))
                .setRedirectUri("http://localhost")
                .asRequest();
        tokenData = oAuth2Service.exchangeAuthorizationCode(req).block();
    }

    @Test
    @Order(1)
    public void testGetAuthorizationInfo() {
        oAuth2Service.getAuthorizationInfo(tokenData.accessToken()).block();
    }

    @Test
    @Order(2)
    public void testRefreshToken() {
        TokenRefreshRequest req = new TokenRefreshSpec()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(tokenData.refreshToken().get())
                .asRequest();
        tokenData = oAuth2Service.refreshToken(req).block();
    }

    @Test
    @Order(3)
    public void testRevokeToken() {
        TokenRevokeRequest req = new TokenRevokeSpec()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setToken(tokenData.accessToken())
                .asRequest();
        oAuth2Service.revokeToken(req).block();
    }

    @Test
    @Order(4)
    public void testExchangeClientCredentials() {
        ClientCredentialsGrantRequest req = new ClientCredentialsGrantSpec()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .addScope(Scope.IDENTIFY)
            .asRequest();
        oAuth2Service.exchangeClientCredentials(req).block();
    }
}
