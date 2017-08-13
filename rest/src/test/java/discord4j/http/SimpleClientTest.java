package discord4j.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.http.client.SimpleHttpClient;
import discord4j.pojo.GatewayPojo;
import discord4j.pojo.MessagePojo;
import discord4j.route.Router;
import discord4j.route.Routes;
import discord4j.route.SimpleRouter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.ipc.netty.http.client.HttpClientRequest;

import java.util.function.Consumer;

public class SimpleClientTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleClientTest.class);

    @Test
    public void testGetGateway() {
        String token = System.getProperty("token");

        ObjectMapper mapper = new ObjectMapper();

        SimpleHttpClient httpClient = SimpleHttpClient.builder()
                .baseUrl(Routes.BASE_URL)
                .defaultHeader("user-agent", "DiscordBot (http://discord4j.com, Discord4J/3.0.0)")
                .defaultHeader("authorization", "Bot " + token)
                .defaultHeader("content-type", "application/json")
                .writerStrategy(new JacksonWriterStrategy(mapper))
                .writerStrategy(new MultipartWriterStrategy())
                .writerStrategy(new EmptyWriterStrategy())
                .readerStrategy(new JacksonReaderStrategy(mapper))
                .readerStrategy(new EmptyReaderStrategy())
                .build();
        Router router = new SimpleRouter(httpClient);
        GatewayPojo result = router.exchange(Routes.GATEWAY_GET).toFuture().join();
        log.info("Result: " + result.url);

        // Expects 401 error
        Consumer<HttpClientRequest.Form> consumer = form -> form.attr("test", "value");
        MessagePojo result2 = router.exchange(Routes.MESSAGE_CREATE.complete(100000), consumer,
                Router.Context.ofContentType("multipart/form-data"))
                .toFuture()
                .join();
    }
}
