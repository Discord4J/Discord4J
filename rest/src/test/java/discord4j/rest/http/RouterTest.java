package discord4j.rest.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.pojo.MessagePojo;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class RouterTest {

	@Before
	public void disableSomeLogs() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger("reactor.ipc.netty").setLevel(Level.INFO);
		context.getLogger("io.netty").setLevel(Level.INFO);
	}

	@Test
	public void test() throws Exception {
		String token = System.getenv("token");
		String channelId = System.getenv("channel");

		ObjectMapper mapper = new ObjectMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl("https://discordapp.com/api/v6")
				.defaultHeader("Authorization", "Bot " + token)
				.defaultHeader("Content-Type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.build();

		Router router = new Router(httpClient);

		Routes.MESSAGE_CREATE.newRequest(channelId)
				.body(new MessagePojo("hello at " + Instant.now()))
				.exchange(router)
				.subscribe(response -> System.out.println("complete response"));


		TimeUnit.SECONDS.sleep(2);
	}

	@Test
	public void orderingTest() throws Exception {
		String token = System.getenv("token");
		String channelId = System.getenv("channel");

		ObjectMapper mapper = new ObjectMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl("https://discordapp.com/api/v6")
				.defaultHeader("Authorization", "Bot " + token)
				.defaultHeader("Content-Type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.build();

		Router router = new Router(httpClient);

		for (int i = 0; i < 6; i++) {
			final int a = i;
			Routes.MESSAGE_CREATE.newRequest(channelId)
					.body(new MessagePojo("hi " + a))
					.exchange(router)
					.subscribe(response -> System.out.println("response " + a + ": " + response.content));
		}

		TimeUnit.SECONDS.sleep(60);
	}

	@Test
	public void testMultiSub() throws Exception {
		String token = System.getenv("token");
		String channelId = System.getenv("channel");

		ObjectMapper mapper = new ObjectMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl("https://discordapp.com/api/v6")
				.defaultHeader("Authorization", "Bot " + token)
				.defaultHeader("Content-Type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.build();

		Router router = new Router(httpClient);

		Mono<MessagePojo> mono = Routes.MESSAGE_CREATE.newRequest(channelId)
				.body(new MessagePojo("hi"))
				.exchange(router);

		mono.subscribe();
		mono.subscribe();

		TimeUnit.SECONDS.sleep(2);
	}
}
