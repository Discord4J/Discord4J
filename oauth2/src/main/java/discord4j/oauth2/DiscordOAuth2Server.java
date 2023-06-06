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
import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.oauth2.service.OAuth2Service;
import discord4j.rest.RestClient;
import discord4j.rest.RestClientBuilder;
import discord4j.rest.interaction.Interactions.ReactorNettyServerHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A simple server capable of exchanging an OAuth2 authorization code received by the frontend via a query parameter for
 * an {@link discord4j.oauth2.object.AccessToken} encoded as JSON.
 */
@Experimental
public class DiscordOAuth2Server {

    private static final Logger log = Loggers.getLogger(DiscordOAuth2Server.class);

    public static final String SESSION_KEY = "D4J-SESSION";

    private final RestClient restClient;
    private final OAuth2Service service;
    private final long clientId;
    private final String clientSecret;
    private final List<String> redirectUris;
    private final String scope;
    private final ObjectMapper objectMapper;
    private final DiscordOAuth2SuccessHandler successHandler;
    private final DiscordOAuth2ResponseHandler responseHandler;
    private final HttpServer httpServer;

    /**
     * Initialize a new builder.
     *
     * @return a builder capable of constructing instances of {@link DiscordOAuth2Server}
     */
    public static Builder builder() {
        return new Builder(RestClientBuilder.createRestApplication().build());
    }

    public static Builder builder(RestClient restClient) {
        return new Builder(restClient);
    }

    private DiscordOAuth2Server(Builder builder) {
        this.restClient = builder.restClient;
        this.service = new OAuth2Service(restClient.getRestResources().getRouter());
        this.clientId = Objects.requireNonNull(builder.clientId == 0 ? null : builder.clientId, "clientId");
        this.clientSecret = Objects.requireNonNull(builder.clientSecret, "clientSecret");
        this.redirectUris = Collections.unmodifiableList(builder.redirectUris);
        this.scope = builder.scope.toString();
        this.objectMapper = Objects.requireNonNull(builder.objectMapper, "objectMapper");
        this.successHandler = builder.successHandler == null ? (client, session) -> Mono.empty() :
                builder.successHandler;
        this.responseHandler = builder.responseHandler == null ?
                (client, req, res) -> res.addHeader("content-type", "application/json")
                        .addHeader("vary", "origin")
                        .addHeader("access-control-allow-methods", "GET")
                        .chunkedTransfer(false)
                        .sendString(Mono.just("{\"ok\": true}")) : builder.responseHandler;
        Consumer<HttpServerRoutes> loginRoute = route -> route.get(builder.loginPath, new OAuth2ServerHandler());
        Function<HttpServer, HttpServer> initializer = httpServer ->
                httpServer.route(builder.routesCustomizer == null ? loginRoute :
                        loginRoute.andThen(builder.routesCustomizer));
        this.httpServer = initializer.apply(Objects.requireNonNull(builder.httpServer, "httpServer"));
    }

    /**
     * Gets the configured {@link HttpServer}.
     *
     * @return the configured {@link HttpServer}
     */
    public final HttpServer getHttpServer() {
        return httpServer;
    }

    /** Builder suited for creating an {@code OAuth2Server}. */
    public static class Builder {

        private final RestClient restClient;
        private final List<String> redirectUris = new ArrayList<>();
        private long clientId;
        private String clientSecret;
        private HttpServer httpServer = HttpServer.create();
        private ObjectMapper objectMapper = new ObjectMapper();
        private final StringBuilder scope = new StringBuilder();
        private DiscordOAuth2SuccessHandler successHandler;
        private DiscordOAuth2ResponseHandler responseHandler;
        private Consumer<HttpServerRoutes> routesCustomizer;
        private String loginPath = "/";

        protected Builder(RestClient restClient) {
            this.restClient = restClient;
        }

        /**
         * Set the client ID of the Discord application associated with the resulting server.
         *
         * @param clientId the client ID obtained from the Discord developer portal
         * @return this builder
         */
        public Builder clientId(long clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Set the client secret of the Discord application associated with the resulting server.
         *
         * @param clientSecret the client secret obtained from the Discord developer portal
         * @return this builder
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Add the given redirect URI to the list of URIs this server should receive requests from; must be a URI
         * defined in the OAuth2 section of the Discord developer portal of the Discord application associated with the
         * resulting server.
         *
         * @param redirectUri the redirect URI
         * @return this builder
         */
        public Builder addRedirectUri(String redirectUri) {
            redirectUris.add(redirectUri);
            return this;
        }

        /**
         * Set the {@link HttpServer} this OAuth2 server should use to handle HTTP requests.
         *
         * @param httpServer the HTTP server
         * @return this builder
         */
        public Builder httpServer(HttpServer httpServer) {
            this.httpServer = httpServer;
            return this;
        }

        /**
         * Set the {@link ObjectMapper} this server should use to encode JSON responses.
         *
         * @param objectMapper the object mapper
         * @return this builder
         */
        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Add the given scope to the list of scopes that the exchanged access token will grant.
         *
         * @param scope the scope to grant
         * @return this builder
         */
        public Builder addScope(Scope scope) {
            this.scope.append(' ').append(scope.getValue());
            return this;
        }

        /**
         * Add a handler to be called once authorization succeeds allowing a {@link DiscordOAuth2Client} to be
         * saved.
         *
         * @param successHandler a handler invoked every time a user authorization succeeds
         * @return this builder
         */
        public Builder onAuthSuccess(DiscordOAuth2SuccessHandler successHandler) {
            this.successHandler = successHandler;
            return this;
        }

        /**
         * Add a handler called once an OAuth2 login completes, allowing to transform the response sent by the server.
         *
         * @param responseHandler a handler invoked every time a user authorization succeeds
         * @return this builder
         */
        public Builder responseHandler(DiscordOAuth2ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        /**
         * Override the route path this server uses to register the authorization handler. By default, it's the root
         * path ('/')
         *
         * @param path the new login path to use
         * @return the builder
         */
        public Builder loginPath(String path) {
            this.loginPath = Objects.requireNonNull(path);
            return this;
        }

        /**
         * Set the route customization for the reactor-netty HTTP server. By default, this server only registers a path
         * to process incoming authorization requests.
         *
         * @param routesCustomizer a lambda to include additional HTTP routes to the server
         * @return this builder
         */
        public Builder route(Consumer<HttpServerRoutes> routesCustomizer) {
            if (this.routesCustomizer == null) {
                this.routesCustomizer = routesCustomizer;
            } else {
                this.routesCustomizer = this.routesCustomizer.andThen(routesCustomizer);
            }
            return this;
        }

        /**
         * Create the {@code OAuth2Server}
         *
         * @return an {@code OAuth2Server} with the configured parameters.
         */
        public DiscordOAuth2Server build() {
            return new DiscordOAuth2Server(this);
        }
    }

    public class OAuth2ServerHandler implements ReactorNettyServerHandler {

        private final SecureRandom random = new SecureRandom();
        private final Map<String, String> sessionToState = new ConcurrentHashMap<>();

        @Override
        public Publisher<Void> apply(HttpServerRequest req, HttpServerResponse res) {
            QueryStringDecoder query = new QueryStringDecoder(req.uri());
            String code = query.parameters().getOrDefault("code", Collections.emptyList())
                    .stream().findFirst().orElse(null);
            String origin = req.requestHeaders().get("origin");
            if (origin != null && redirectUris.contains(origin)) {
                res.addHeader("access-control-allow-origin", origin);
            }
            if (code == null) {
                QueryStringEncoder encoder = new QueryStringEncoder("https://discord.com/api/oauth2/authorize");
                encoder.addParam("client_id", String.valueOf(clientId));
                encoder.addParam("redirect_uri", origin == null ? redirectUris.get(0) : origin);
                encoder.addParam("response_type", "code");
                encoder.addParam("scope", scope);
                String sessionId = UUID.randomUUID().toString();
                String state = initState();
                encoder.addParam("state", state);
                sessionToState.put(sessionId, state);
                return res.addCookie(initSessionCookie(req, sessionId)).sendRedirect(encoder.toString());
            } else {
                String sessionId = req.cookies().getOrDefault(SESSION_KEY, Collections.emptySet())
                        .stream().map(Cookie::value).findFirst().orElse("");
                String state = query.parameters().getOrDefault("state", Collections.emptyList())
                        .stream().findFirst().orElse(null);
                if (!Objects.equals(state, sessionToState.getOrDefault(sessionId, ""))) {
                    return res.status(HttpResponseStatus.BAD_REQUEST);
                }
                sessionToState.remove(sessionId);

                Mono<DiscordOAuth2Client> exchange =
                        service.exchangeAuthorizationCode(AuthorizationCodeGrantRequest.builder()
                                .clientId(clientId)
                                .clientSecret(clientSecret)
                                .code(code)
                                .redirectUri(origin == null ? redirectUris.get(0) : origin)
                                .build())
                        .flatMap(data -> {
                            DiscordOAuth2Client client = DiscordOAuth2Client.createFromToken(
                                    restClient, clientId, clientSecret, data);

                            return Mono.defer(() -> successHandler.onAuthSuccess(client, sessionId))
                                    .onErrorResume(e -> {
                                        log.error("Unable to run success handler", e);
                                        return Mono.empty();
                                    })
                                    .thenReturn(client);
                        });

                return exchange.flatMapMany(client -> Flux.defer(() -> responseHandler.handle(client, req, res)));
            }
        }

        private String initState() {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            byte[] encoded = Base64.getEncoder().encode(bytes);
            return new String(encoded);
        }

        private Cookie initSessionCookie(HttpServerRequest req, String value) {
            DefaultCookie cookie = new DefaultCookie(SESSION_KEY, value);
            cookie.setPath(req.path() + "/");
            cookie.setHttpOnly(true);
            try {
                cookie.setSecure("https".equalsIgnoreCase(new URI(req.uri()).getScheme()));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            cookie.setSameSite(CookieHeaderNames.SameSite.Lax);
            return cookie;
        }
    }
}
