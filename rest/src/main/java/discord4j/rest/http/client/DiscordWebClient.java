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

package discord4j.rest.http.client;

import discord4j.common.GitProperties;
import discord4j.common.util.Token;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.WriterStrategy;
import discord4j.rest.request.AuthorizationScheme;
import discord4j.rest.response.ResponseFunction;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static discord4j.common.LogUtil.format;

/**
 * Reactor Netty based HTTP client dedicated to Discord REST API requests.
 */
public class DiscordWebClient {

    private static final Logger log = Loggers.getLogger(DiscordWebClient.class);

    public static final String KEY_REQUEST_TIMESTAMP = "discord4j.request.timestamp";

    private final AtomicReference<Mono<? extends Token>> token;
    private final HttpClient httpClient;
    private final Mono<HttpHeaders> defaultHeaders;
    private final ExchangeStrategies exchangeStrategies;
    private final List<ResponseFunction> responseFunctions;

    /**
     * Create a new {@link DiscordWebClient} wrapping HTTP, Discord and encoding/decoding resources.
     *
     * @param httpClient a Reactor Netty HTTP client
     * @param exchangeStrategies a strategy to transform requests and responses
     * @param authorizationScheme scheme to use with the authorization header
     * @param token a Discord token for API authorization
     * @param responseFunctions a list of {@link ResponseFunction} transformations
     */
    public DiscordWebClient(HttpClient httpClient, ExchangeStrategies exchangeStrategies,
                            AuthorizationScheme authorizationScheme, Mono<Token> token,
                            List<ResponseFunction> responseFunctions, String discordBaseUrl) {
        final Properties properties = GitProperties.getProperties();
        final String version = properties.getProperty(GitProperties.APPLICATION_VERSION, "3");
        final String url = properties.getProperty(GitProperties.APPLICATION_URL, "https://discord4j.com");

        final HttpHeaders defaultHeaders = new DefaultHttpHeaders();
        defaultHeaders.add(HttpHeaderNames.CONTENT_TYPE, "application/json");
        defaultHeaders.add(HttpHeaderNames.USER_AGENT, "DiscordBot(" + url + ", " + version + ")");

        token = token.switchIfEmpty(Mono.just(Token.of("")));
        this.token = new AtomicReference<>(token);
        this.httpClient = configureHttpClient(httpClient.baseUrl(discordBaseUrl));
        this.defaultHeaders = token.map(bearerToken -> {
            if (authorizationScheme != AuthorizationScheme.NONE) {
                defaultHeaders.add(HttpHeaderNames.AUTHORIZATION, authorizationScheme.getValue() + ' ' + bearerToken.asString());
            }
            return defaultHeaders;
        });
        this.exchangeStrategies = exchangeStrategies;
        this.responseFunctions = responseFunctions;
    }

    private HttpClient configureHttpClient(HttpClient httpClient) {
        if (log.isTraceEnabled()) {
            return httpClient.observe((connection, state) -> {
                if (connection instanceof ConnectionObserver) {
                    ConnectionObserver observer = (ConnectionObserver) connection;
                    log.trace(format(observer.currentContext(), "{} {}"), state, connection);
                } else if (connection instanceof HttpClientRequest) {
                    HttpClientRequest httpClientRequest = (HttpClientRequest) connection;
                    log.trace(format(httpClientRequest.currentContextView(), "{} {}"), state, connection);
                }
            });
        }
        return httpClient;
    }

    /**
     * Return the underlying Reactor Netty HTTP client.
     *
     * @return the HTTP client used by this {@link DiscordWebClient}
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Return the default headers used in every request.
     *
     * @return the {@link HttpHeaders} used by this {@link DiscordWebClient} in every request
     */
    public Mono<HttpHeaders> getDefaultHeaders() {
        return defaultHeaders;
    }

    /**
     * Return the strategy used for request and response conversion.
     *
     * @return the {@link ExchangeStrategies} used by this {@link DiscordWebClient} in every request
     */
    public ExchangeStrategies getExchangeStrategies() {
        return exchangeStrategies;
    }

    /**
     * Exchange a request for a {@link Mono} response.
     * <p>
     * The request will be processed according to the writer strategies available.
     *
     * @param request the client HTTP request
     * @return a {@link Mono} with the response in the form of {@link ClientResponse}
     */
    public Mono<ClientResponse> exchange(ClientRequest request) {
        return Mono.defer(
                () -> {
                    Mono<HttpHeaders> requestHeaders = buildHttpHeaders(request);
                    Mono<String> contentTypeMono = requestHeaders.map(headers -> {
                    String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
                    return contentType != null ? contentType : "";
                });
                Mono<HttpClient.RequestSender> sender = requestHeaders.map(reqHeaders -> httpClient.headers(headers ->
                        headers.setAll(reqHeaders)).request(request.getMethod()).uri(request.getUrl()));
                Object body = request.getBody();

                return contentTypeMono.zipWith(sender).flatMap(t2 -> {
                    String contentType = t2.getT1();
                    return exchangeStrategies.writers().stream()
                            .filter(s -> s.canWrite(body != null ? body.getClass() : null, !contentType.isEmpty() ? contentType : null))
                            .findFirst()
                            .map(DiscordWebClient::cast)
                            .map(writer -> writer.write(t2.getT2(), body))
                            .orElseGet(() -> Mono.error(noWriterException(body, contentType)));
                });
            })
            .flatMap(receiver -> receiver.responseConnection((response, connection) ->
                    Mono.just(new ClientResponse(response, connection.inbound(),
                            exchangeStrategies, request, responseFunctions))).next())
            .contextWrite(ctx -> ctx.put(KEY_REQUEST_TIMESTAMP, Instant.now().toEpochMilli()));
    }

    private Mono<HttpHeaders> buildHttpHeaders(ClientRequest request) {
        return defaultHeaders.zipWith(token.get()).flatMap(t2 -> {
            Token token = t2.getT2();

            HttpHeaders requestHeaders = new DefaultHttpHeaders().add(t2.getT1()).setAll(request.getHeaders());
            if (request.getBody() == null) {
                requestHeaders.remove(HttpHeaderNames.CONTENT_TYPE);
            }
            if (token.hasExpired()) {
                Mono<? extends Token> refreshedTokenMono = token.refresh();
                this.token.set(refreshedTokenMono);
                return refreshedTokenMono.map(refreshedToken -> requestHeaders.set(HttpHeaderNames.AUTHORIZATION,
                        AuthorizationScheme.BEARER.getValue() + ' ' + refreshedToken.asString())).map(__ -> requestHeaders);
            }
            return Mono.just(requestHeaders);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> WriterStrategy<T> cast(WriterStrategy<?> strategy) {
        return (WriterStrategy<T>) strategy;
    }

    private static RuntimeException noWriterException(@Nullable Object body, String contentType) {
        return new RuntimeException("No strategies to write this request: " + body + " - " + contentType);
    }
}
