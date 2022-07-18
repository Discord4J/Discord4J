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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.discordjson.json.*;
import discord4j.oauth2.object.AccessToken;
import discord4j.oauth2.service.OAuth2Service;
import discord4j.rest.RestClient;
import discord4j.rest.request.DiscordWebRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A client registration capable of making requests to the Discord API on behalf of a single user using their OAuth2
 * access token.
 */
public class DiscordOAuth2Client {

    private static final Logger log = Loggers.getLogger(DiscordOAuth2Client.class);

    private final RestClient restClient;
    private final OAuth2Service oAuth2Service;
    private final long clientId;
    private final String clientSecret;
    private final Function<OAuth2Service, Mono<AccessToken>> tokenFactory;
    private final AtomicReference<AccessToken> accessToken;

    DiscordOAuth2Client(RestClient restClient, long clientId, String clientSecret,
                        Function<OAuth2Service, Mono<AccessToken>> tokenFactory) {
        this.restClient = restClient;
        this.oAuth2Service = new OAuth2Service(restClient.getRestResources().getRouter());
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenFactory = tokenFactory;
        this.accessToken = new AtomicReference<>();
    }

    public static DiscordOAuth2Client createFromCode(RestClient restClient, long clientId, String clientSecret,
                                                     AuthorizationCodeGrantRequest request) {
        return new DiscordOAuth2Client(restClient, clientId, clientSecret,
                service -> service.exchangeAuthorizationCode(request).map(AccessToken::new));
    }

    public static DiscordOAuth2Client createFromCredentials(RestClient restClient, long clientId, String clientSecret,
                                                            ClientCredentialsGrantRequest request) {
        return new DiscordOAuth2Client(restClient, clientId, clientSecret,
                service -> service.exchangeClientCredentials(request).map(AccessToken::new));
    }

    public static DiscordOAuth2Client createFromData(RestClient restClient, long clientId, String clientSecret,
                                                     AccessTokenData data) {
        return new DiscordOAuth2Client(restClient, clientId, clientSecret, __ -> Mono.just(new AccessToken(data)));
    }

    public ObjectMapper getObjectMapper() {
        return restClient.getRestResources().getJacksonResources().getObjectMapper();
    }

    private Router getRouter() {
        return restClient.getRestResources().getRouter();
    }

    public Mono<AuthorizationInfoData> getAuthorizationInfo() {
        return withAuthorizedClient(Routes.AUTHORIZATION_INFO_GET.newRequest())
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(AuthorizationInfoData.class));
    }

    public Flux<ConnectionData> getUserConnections() {
        return withAuthorizedClient(Routes.USER_CONNECTIONS_GET.newRequest())
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(ConnectionData[].class))
                .flatMapMany(Flux::fromArray);
    }

    public Mono<DiscordWebRequest> withAuthorizedClient(DiscordWebRequest request) {
        return Mono.fromCallable(accessToken::get)
                .flatMap(token -> {
                    if (token.hasExpired()) {
                        return oAuth2Service.refreshToken(TokenRefreshRequest.builder()
                                        .clientId(clientId)
                                        .clientSecret(clientSecret)
                                        .refreshToken(token.getRefreshToken().orElseThrow(UnsupportedOperationException::new))
                                        .build())
                                .map(data -> {
                                    accessToken.set(new AccessToken(data));
                                    return request.copy().bearerAuth(data.accessToken());
                                });
                    }
                    return Mono.just(request.copy().bearerAuth(token.getAccessToken()));
                })
                .switchIfEmpty(Mono.defer(() -> tokenFactory.apply(oAuth2Service)
                        .map(newToken -> {
                            accessToken.set(newToken);
                            return request.copy().bearerAuth(newToken.getAccessToken());
                        })))
                .onErrorResume(ex -> {
                    accessToken.set(null);
                    return Mono.error(ex);
                });
    }

}
