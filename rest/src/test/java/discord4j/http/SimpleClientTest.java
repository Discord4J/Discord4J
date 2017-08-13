package discord4j.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.http.client.ExchangeFilter;
import discord4j.http.client.SimpleHttpClient;
import discord4j.pojo.GatewayPojo;
import discord4j.pojo.MessagePojo;
import discord4j.route.Route;
import discord4j.route.Router;
import discord4j.route.Routes;
import discord4j.route.SimpleRouter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.ipc.netty.http.client.HttpClientRequest;

import java.io.IOException;
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
				.readerStrategy(new FallbackReaderStrategy())
				.build();
		Router router = new SimpleRouter(httpClient);
		GatewayPojo result = router.exchange(Routes.GATEWAY_GET.complete()).toFuture().join();
		log.info("Result: " + result.url);
	}

	@Test
	public void testSendMultipart() {
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
				.readerStrategy(new FallbackReaderStrategy())
				.build();
		Router router = new SimpleRouter(httpClient);

		Consumer<HttpClientRequest.Form> consumer = form -> form.attr("test", "value");
		MessagePojo result2 = router.exchange(Routes.MESSAGE_CREATE.complete(100000), consumer, ExchangeFilter
				.requestContentType("multipart/form-data"))
				.toFuture()
				.join();
	}

	@Test
	public void postFormTest() throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl("https://httpbin.org")
				.defaultHeader("content-type", "multipart/form-data")
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.writerStrategy(new MultipartWriterStrategy())
				.writerStrategy(new EmptyWriterStrategy())
				.readerStrategy(new JacksonReaderStrategy(mapper))
				.readerStrategy(new EmptyReaderStrategy())
				.readerStrategy(new FallbackReaderStrategy())
				.build();
		Router router = new SimpleRouter(httpClient);

		Consumer<HttpClientRequest.Form> formConsumer = form -> form
				.attr("comments", "ABCD")
				.attr("custemail", "C@D.com")
				.attr("custname", "A")
				.attr("custtel", "B")
				.attr("delivery", "12:30")
				.attr("size", "small")
				.attr("topping", "bacon")
				.attr("topping", "onion");

		String result = router.exchange(Route.post("/post", String.class).complete(), formConsumer,
				ExchangeFilter.builder()
						.requestFilter(req -> req.requestHeaders()
								.set("content-type", "multipart/form-data")
								.set("content-length", 0))
						.build())
				.toFuture()
				.join();
		log.info("Result: " + result);
	}
}
