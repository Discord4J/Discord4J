package discord4j.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.discordjson.json.UserData;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example code showcasing a Discord OAuth2 authorization code flow.
 * <p>
 * This code will spin up a {@link DiscordOAuth2Server} with three endpoints:
 * <ul>
 *     <li>{@code /} or root path, initiating an authorization code flow to login a user using Discord</li>
 *     <li>{@code /@me} to return logged in user information</li>
 *     <li>{@code /@me/connections} to return a payload of user connections</li>
 * </ul>
 * <p>
 * A map is created to store OAuth2 clients, and populated on successful authorizations. For this to work, supply the
 * following environment variables:
 * <ul>
 *     <li>{@code CLIENT_ID} - your app client id</li>
 *     <li>{@code CLIENT_SECRET} - your app client secret</li>
 *     <li>{@code SERVER_PORT} - the server port where this will run</li>
 * </ul>
 * Your OAuth2 application must also allow a redirect URI like {@code http://localhost:SERVER_PORT}
 */
public class ExampleOAuth2CodeFlow {

    private static final Logger log = Loggers.getLogger(ExampleOAuth2CodeFlow.class);

    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String SERVER_PORT = System.getenv("SERVER_PORT");

    private static final Map<String, DiscordOAuth2Client> CLIENTS = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // a Discord OAuth2 application
        // starts a server to redirect users for authorization
        // allow getting discord authenticated user info through endpoints
        DiscordOAuth2Server.builder()
                .clientId(Long.parseLong(CLIENT_ID))
                .clientSecret(CLIENT_SECRET)
                .addScope(Scope.IDENTIFY)
                .addScope(Scope.CONNECTIONS)
                // uncomment to prompt a user to join your bot to a user's guild
                //.addScope(Scope.BOT)
                // uncomment to prompt a user to select a channel and create a webhook to it
                //.addScope(Scope.WEBHOOK_INCOMING)
                .addRedirectUri("http://localhost:" + SERVER_PORT)
                .onAuthSuccess((client, sessionId) -> {
                    CLIENTS.put(sessionId, client);
                    return Mono.empty();
                })
                .responseHandler((client, req, res) -> client.getCurrentUser()
                        .map(UserData::username)
                        .flatMapMany(name -> res.sendString(Mono.just(
                                "<!doctype html>" +
                                        "<html lang=en>" +
                                        "<head>" +
                                        "<meta charset=utf-8>" +
                                        "<title>Discord4J</title>" +
                                        "</head>" +
                                        "<body>" +
                                        "<p>Hello, " + name + "!</p>" +
                                        "</body>" +
                                        "</html>"))))
                .route(r -> r
                        .get("/@me", (req, res) -> {
                            DiscordOAuth2Client client = getClient(req);
                            if (client == null) {
                                return res.status(HttpResponseStatus.UNAUTHORIZED);
                            }
                            ObjectMapper mapper = client.getObjectMapper();
                            return res.sendString(client.getAuthorizationInfo()
                                    .flatMap(data -> Mono.fromCallable(() -> mapper.writeValueAsString(data))));
                        })
                        .get("/@me/connections", (req, res) -> {
                            DiscordOAuth2Client client = getClient(req);
                            if (client == null) {
                                return res.status(HttpResponseStatus.UNAUTHORIZED);
                            }
                            ObjectMapper mapper = client.getObjectMapper();
                            return res.sendString(client.getUserConnections()
                                    .collectList()
                                    .flatMap(data -> Mono.fromCallable(() -> mapper.writeValueAsString(data))));
                        }))
                .build()
                .getHttpServer()
                .port(Integer.parseInt(SERVER_PORT))
                .bindUntilJavaShutdown(Duration.ofSeconds(30), facade -> {
                    log.info("******************************************************************");
                    log.info("Server started at {}:{}", facade.host(), facade.port());
                    log.info("******************************************************************");
                });
    }

    @Nullable
    private static DiscordOAuth2Client getClient(HttpServerRequest request) {
        String key = request.cookies().getOrDefault(DiscordOAuth2Server.SESSION_KEY, Collections.emptySet())
                .stream().map(Cookie::value).findFirst().orElse("");
        return CLIENTS.get(key);
    }
}
