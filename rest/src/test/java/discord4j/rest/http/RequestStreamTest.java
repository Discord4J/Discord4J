package discord4j.rest.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.pojo.MessagePojo;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.DiscordRequest;
import discord4j.rest.request.RequestStream;
import discord4j.rest.request.StreamPuller;
import discord4j.rest.route.Routes;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class RequestStreamTest {

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

		StreamPuller streamPuller = new StreamPuller(httpClient);

		DiscordRequest<MessagePojo> request = Routes.MESSAGE_CREATE.newRequest(channelId).body(new MessagePojo("hello at " + Instant.now()));
		streamPuller.push(request)
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

		StreamPuller streamPuller = new StreamPuller(httpClient);

		for (int i = 0; i < 5; i++) {
			final int a = i;
			DiscordRequest<MessagePojo> request = Routes.MESSAGE_CREATE.newRequest(channelId).body(new MessagePojo("hi " + a));
			streamPuller.push(request)
					.subscribe(response -> System.out.println("response " + a + ": " + response.content));
		}

		TimeUnit.SECONDS.sleep(2);
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

		StreamPuller streamPuller = new StreamPuller(httpClient);

		DiscordRequest<MessagePojo> request = Routes.MESSAGE_CREATE.newRequest(channelId).body(new MessagePojo("hi"));
		Mono<MessagePojo> mono = streamPuller.push(request);

		mono.subscribe();
		mono.subscribe();

		TimeUnit.SECONDS.sleep(2);
	}
}
