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
import discord4j.common.annotations.Experimental;
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A client registration capable of making requests to the Discord API on behalf of a <strong>single user</strong> using
 * their OAuth2 access token.
 * <p>
 * If you use a {@link DiscordOAuth2Server}, you can retrieve working instances by defining a
 * {@link DiscordOAuth2SuccessHandler}. In other cases use a factory method depending on how your OAuth2 application
 * is structured:
 * <ul>
 *     <li>{@link #createFromCode(RestClient, AuthorizationCodeGrantRequest)} if you want to defer the token fetching
 *     process until the first authorized request</li>
 *     <li>{@link #createFromToken(RestClient, long, String, AccessTokenData)} if access token data is already
 *     available, like after calling {@link OAuth2Service#exchangeAuthorizationCode(AuthorizationCodeGrantRequest)}</li>
 *     <li>{@link #createFromCredentials(RestClient, ClientCredentialsGrantRequest)} if you are a bot developer and
 *     want to issue a Bearer token on your behalf for testing purposes</li>
 * </ul>
 *
 * @see <a href="https://discord.com/developers/docs/topics/oauth2#oauth2">Discord Docs</a>
 */
@Experimental
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

    /**
     * Create an OAuth2 client by completing an authorization code grant flow. This is useful if you have a custom
     * HTTP server that receives the {@code code} parameter. Build a
     * {@link AuthorizationCodeGrantRequest} making sure to include the code parameter and the access token with refresh
     * capabilities will be stored once the first request is made using one of the API methods in this class or
     * {@link #withAuthorizedClient(DiscordWebRequest)}.
     * <p>
     * For an example server implementation, check {@link DiscordOAuth2Server}, which uses this method.
     *
     * @param restClient a Discord REST API client for performing requests
     * @param request an object with all parameters required to complete an authorization code grant request
     * @return a client that can work with a valid user token to perform API requests
     */
    public static DiscordOAuth2Client createFromCode(RestClient restClient, AuthorizationCodeGrantRequest request) {
        return new DiscordOAuth2Client(restClient, request.clientId(), request.clientSecret(),
                service -> service.exchangeAuthorizationCode(request).map(AccessToken::new));
    }

    /**
     * Create an OAuth2 client by performing a client credentials flow. This is a quick and easy way as a bot developer
     * to get an access token for testing purposes. Build a {@link ClientCredentialsGrantRequest} using your
     * application parameters and intended {@link Scope} values, separated by spaces.
     *
     * @param restClient a Discord REST API client for performing requests
     * @param request an object with all parameters required to complete a client credentials grant request
     * @return a client that can work with a valid token for your user to perform API requests
     */
    public static DiscordOAuth2Client createFromCredentials(RestClient restClient,
                                                            ClientCredentialsGrantRequest request) {
        return new DiscordOAuth2Client(restClient, request.clientId(), request.clientSecret(),
                service -> service.exchangeClientCredentials(request).map(AccessToken::new));
    }

    /**
     * Create an OAuth2 client with the raw {@link AccessTokenData} returned from
     * {@link OAuth2Service#exchangeAuthorizationCode(AuthorizationCodeGrantRequest)}. Useful if a token is requested
     * directly by your HTTP server.
     *
     * @param restClient a Discord REST API client for performing requests
     * @param clientId your application's client ID
     * @param clientSecret your application's client secret
     * @param data the access token object
     * @return a client that can work with a valid token to perform API requests
     */
    public static DiscordOAuth2Client createFromToken(RestClient restClient, long clientId, String clientSecret,
                                                      AccessTokenData data) {
        return new DiscordOAuth2Client(restClient, clientId, clientSecret, __ -> Mono.just(new AccessToken(data)));
    }

    /**
     * Return the Jackson {@link ObjectMapper} tied to this instance for JSON handling purposes.
     *
     * @return an object that can provide JSON processing
     */
    public ObjectMapper getObjectMapper() {
        return restClient.getRestResources().getJacksonResources().getObjectMapper();
    }

    /**
     * Return the {@link Router} tied to this instance to execute requests to Discord API.
     *
     * @return an abstraction to perform API requests
     */
    private Router getRouter() {
        return restClient.getRestResources().getRouter();
    }

    /**
     * Returns info about the current authorization. Uses {@link #withAuthorizedClient(DiscordWebRequest)} to retrieve
     * and use the Bearer token tied to this client.
     *
     * @return a Mono with authorization details given by this token in this client, or an
     * error Mono in case any request fails
     */
    public Mono<AuthorizationInfoData> getAuthorizationInfo() {
        return withAuthorizedClient(Routes.AUTHORIZATION_INFO_GET.newRequest())
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(AuthorizationInfoData.class));
    }

    /**
     * Returns the user object of the requesting account. For OAuth2, this requires the {@link Scope#IDENTIFY}
     * scope, which will return the object without an email, and optionally the {@link Scope#EMAIL} scope, which returns
     * the object with an email.
     *
     * @return a Mono with user details if successful, otherwise an error Mono
     */
    public Mono<UserData> getCurrentUser() {
        return withAuthorizedClient(Routes.CURRENT_USER_GET.newRequest())
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(UserData.class));
    }

    /**
     * Returns a list of partial guild objects the requesting account is a member of. Requires the
     * {@link Scope#GUILDS} scope.
     *
     * @param queryParams optional query parameters for this endpoint, allowing pagination using {@code before},
     * {@code after} and {@code limit} (defaults to 200)
     * @return a Flux with partial guild information for the user, otherwise an error Flux
     */
    public Flux<UserGuildData> getCurrentUserGuilds(Map<String, Object> queryParams) {
        return withAuthorizedClient(Routes.CURRENT_USER_GUILDS_GET.newRequest()
                .query(queryParams))
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(UserGuildData[].class))
                .flatMapMany(Flux::fromArray);
    }

    /**
     * Returns a member object from the current user in the given guild. Request the
     * {@link Scope#GUILDS_MEMBERS_READ} scope.
     *
     * @param guildId the guild to query the current user member object
     * @return a Mono with member information, otherwise an error Mono
     */
    public Mono<MemberData> getCurrentUserGuildMember(long guildId) {
        return withAuthorizedClient(Routes.CURRENT_USER_GUILD_MEMBER_GET.newRequest(guildId))
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(MemberData.class));
    }

    /**
     * Return a list of {@link ConnectionData} objects. Requires this client was authorized to use the
     * {@link Scope#CONNECTIONS} scope. Uses {@link #withAuthorizedClient(DiscordWebRequest)} to retrieve and use the
     * Bearer token tied to this client.
     *
     * @return a Mono with user connections, or an error Mono in case any request fails
     */
    public Flux<ConnectionData> getUserConnections() {
        return withAuthorizedClient(Routes.USER_CONNECTIONS_GET.newRequest())
                .map(request -> request.exchange(getRouter()))
                .flatMap(response -> response.bodyToMono(ConnectionData[].class))
                .flatMapMany(Flux::fromArray);
    }

    /**
     * Prepare a given {@link DiscordWebRequest} on behalf of a user, using the credentials stored under this client.
     * The token fetching, refreshing (if required) and API request are run once this Mono is subscribed.
     *
     * @param request the compiled Discord REST API request to be run on behalf of a user
     * @return a Mono of a request including required steps to include proper authorization
     */
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
