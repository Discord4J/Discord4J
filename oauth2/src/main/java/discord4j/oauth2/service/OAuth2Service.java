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

package discord4j.oauth2.service;

import discord4j.discordjson.json.AccessTokenData;
import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.discordjson.json.AuthorizationInfoData;
import discord4j.discordjson.json.ClientCredentialsGrantRequest;
import discord4j.discordjson.json.TokenRefreshRequest;
import discord4j.discordjson.json.TokenRevokeRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.service.RestService;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OAuth2Service extends RestService {

    private static final Base64.Encoder BASE64 = Base64.getEncoder();

    public OAuth2Service(Router router) {
        super(router);
    }

    public Mono<AccessTokenData> exchangeAuthorizationCode(AuthorizationCodeGrantRequest request) {
        return Routes.TOKEN.newRequest()
                .body(request.toString())
                .header("content-type", "application/x-www-form-urlencoded")
                .exchange(getRouter())
                .bodyToMono(AccessTokenData.class);
    }

    public Mono<AccessTokenData> exchangeClientCredentials(ClientCredentialsGrantRequest request) {
        return Routes.TOKEN.newRequest()
                .body(request.toString())
                .header("content-type", "application/x-www-form-urlencoded")
                .header("authorization", "Basic " + BASE64.encodeToString((request.clientId() + ":" +
                        request.clientSecret()).getBytes(StandardCharsets.UTF_8)))
                .exchange(getRouter())
                .bodyToMono(AccessTokenData.class);
    }

    public Mono<AccessTokenData> refreshToken(TokenRefreshRequest request) {
        return Routes.TOKEN.newRequest()
                .body(request.toString())
                .header("content-type", "application/x-www-form-urlencoded")
                .exchange(getRouter())
                .bodyToMono(AccessTokenData.class);
    }

    public Mono<Void> revokeToken(TokenRevokeRequest request) {
        return Routes.TOKEN_REVOKE.newRequest()
                .body(request.toString())
                .header("content-type", "application/x-www-form-urlencoded")
                .header("authorization", "Basic " + BASE64.encodeToString((request.clientId() + ":" +
                        request.clientSecret()).getBytes(StandardCharsets.UTF_8)))
                .exchange(getRouter())
                // Must skip body here - the authorization server returns a response of 200 with an empty body rather
                // than 204 with no body. An NPE is therefore thrown by JacksonReaderStrategy when the ObjectMapper
                // tries to serialize the null ByteBuf into Void
                .skipBody();
    }

    public Mono<AuthorizationInfoData> getAuthorizationInfo(String accessToken) {
        return Routes.AUTHORIZATION_INFO_GET.newRequest()
                .header("authorization", "Bearer " + accessToken)
                .exchange(getRouter())
                .bodyToMono(AuthorizationInfoData.class);
    }
}
