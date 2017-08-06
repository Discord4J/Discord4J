package discord4j.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.pojo.GatewayPojo;
import discord4j.route.Router;
import discord4j.route.Routes;
import discord4j.route.SimpleRouter;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.ipc.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;

public class SimpleClientTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleClientTest.class);

    @Test
    public void testGetGateway() {
        String token = System.getProperty("token");

        HttpHeaders defaultHeaders = new DefaultHttpHeaders();
        defaultHeaders.add("user-agent", "DiscordBot (http://discord4j.com, Discord4J/3.0.0)");
        defaultHeaders.add("authorization", "Bot " + token);
        defaultHeaders.add("content-type", "application/json");

        ObjectMapper mapper = new ObjectMapper();

        List<WriterStrategy<?>> writerStrategies = new ArrayList<>();
        writerStrategies.add(new JacksonWriterStrategy(mapper));
        writerStrategies.add(new EmptyWriterStrategy());
        List<ReaderStrategy<?>> readerStrategies = new ArrayList<>();
        readerStrategies.add(new JacksonReaderStrategy(mapper));

        SimpleHttpClient httpClient = new SimpleHttpClient(HttpClient.create(), Routes.BASE_URL,
                defaultHeaders, writerStrategies, readerStrategies);
        Router router = new SimpleRouter(httpClient);
        GatewayPojo result = router.exchange(Routes.GATEWAY_GET).toFuture().join();
        log.info("Result: " + result.url);
    }
}
