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
import discord4j.oauth2.service.OAuth2Service;
import discord4j.oauth2.spec.AuthorizationCodeGrantSpec;
import discord4j.rest.interaction.Interactions.ReactorNettyServerHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A server capable of exchanging an OAuth2 authorization code received by the frontend via a query parameter for an
 * {@link discord4j.oauth2.object.AccessToken} encoded as JSON. Assumes that any
 * <a href="https://discord.com/developers/docs/topics/oauth2#state-and-security">state</a> parameter provided in the
 * OAuth2 flow has already been validated on the frontend.
 */
public class OAuth2Server {

    private final OAuth2Service service;
    private final long clientId;
    private final String clientSecret;
    private final List<String> redirectUris;
    private final ObjectMapper objectMapper;
    private final HttpServer httpServer;

    /**
     * Initialize a new builder with the given {@link OAuth2Service}.
     *
     * @param service the service used to make REST requests to Discord's OAuth2 API
     * @return a {@code Builder} capable of constructing instances of {@code OAuth2Server}
     */
    public static Builder builder(OAuth2Service service) {
        return new Builder(service);
    }

    private OAuth2Server(Builder builder) {
        this.service = Objects.requireNonNull(builder.service, "service");
        this.clientId = Objects.requireNonNull(builder.clientId == 0 ? null : builder.clientId, "clientId");
        this.clientSecret = Objects.requireNonNull(builder.clientSecret, "clientSecret");
        this.redirectUris = Collections.unmodifiableList(builder.redirectUris);
        this.objectMapper = Objects.requireNonNull(builder.objectMapper, "objectMapper");
        Function<HttpServer, HttpServer> initializer = httpServer ->
                httpServer.route(route -> route.get("/", new OAuth2ServerHandler()));
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

        private final OAuth2Service service;
        private final List<String> redirectUris = new ArrayList<>();
        private long clientId;
        private String clientSecret;
        private HttpServer httpServer = HttpServer.create();
        private ObjectMapper objectMapper = new ObjectMapper();

        protected Builder(OAuth2Service service) {
            this.service = service;
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
         * Create the {@code OAuth2Server}
         *
         * @return an {@code OAuth2Server} with the configured parameters.
         */
        public OAuth2Server build() {
            return new OAuth2Server(this);
        }
    }

    private class OAuth2ServerHandler implements ReactorNettyServerHandler {

        @Override
        public Publisher<Void> apply(HttpServerRequest serverRequest, HttpServerResponse serverResponse) {
            String query = serverRequest.uri();
            int i = query.indexOf("code=");
            String code = query.substring(i == -1 ? 0 : i + "code=".length(), i == -1 ? query.length() :
                    (i = query.indexOf("&", i)) == -1 ? query.length() : i);
            if (query.trim().isEmpty() || code.trim().isEmpty() || code.equals(query)) {
                return serverResponse.status(HttpResponseStatus.BAD_REQUEST).send();
            }
            String origin = serverRequest.requestHeaders().get("origin");
            if (origin != null && redirectUris.contains(origin)) {
                serverResponse.addHeader("access-control-allow-origin", origin);
            }

            return serverResponse.addHeader("content-type", "application/json")
                    .addHeader("vary", "origin")
                    .addHeader("access-control-allow-methods", "GET")
                    .chunkedTransfer(false)
                    .sendString(service.exchangeAuthorizationCode(new AuthorizationCodeGrantSpec()
                            .setClientId(clientId)
                            .setClientSecret(clientSecret)
                            .setCode(code)
                            .setRedirectUri(origin == null ? redirectUris.get(0) : origin).asRequest())
                            .flatMap(data -> Mono.fromCallable(() -> objectMapper.writeValueAsString(data))));
        }
    }
}
