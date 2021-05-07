package discord4j.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.discordjson.json.AccessTokenData;
import discord4j.oauth2.service.OAuth2Service;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;

public class ExampleOAuth2CodeFlow {

    private static final Logger log = Loggers.getLogger(ExampleOAuth2CodeFlow.class);
    private static final long clientId = Long.parseLong(System.getenv("id"));

    public static void main(String[] args) {
        ObjectMapper mapper = JacksonResources.create().getObjectMapper();
        ExchangeStrategies strategies = ExchangeStrategies.jackson(mapper);
        Router router = new DefaultRouter(new RouterOptions(Mono.empty(), AuthorizationScheme.NONE,
                ReactorResources.create(), strategies, Collections.emptyList(),
                BucketGlobalRateLimiter.create(), RequestQueueFactory.buffering(), Routes.BASE_URL));
        OAuth2Server server = OAuth2Server.builder(new OAuth2Service(router))
                .clientId(clientId)
                .clientSecret(System.getenv("secret"))
                .addRedirectUri("http://localhost")
                .objectMapper(mapper)
                .build();

        server.getHttpServer().bindUntilJavaShutdown(Duration.ofMillis(Long.MAX_VALUE), facade -> {
                log.info("*************************************************************");
                log.info("Server started at {}:{}", facade.host(), facade.port());
                log.info("*************************************************************");
                try {
                    AccessTokenData data = mapper.readValue(new URL(String.format("http://localhost:%d/?code=%s",
                            facade.port(), System.getenv("code"))), AccessTokenData.class);

                    OAuth2Client.createFromData(data)
                            .setClientId(clientId)
                            .setClientSecret(System.getenv("secret"))
                            .build();
                    facade.dispose();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
        });

    }
}
