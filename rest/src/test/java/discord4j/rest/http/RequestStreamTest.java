package discord4j.rest.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.pojo.MessagePojo;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.StreamPuller;
import discord4j.rest.route.Routes;
import org.junit.Test;

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
		streamPuller.from(Routes.MESSAGE_CREATE)
				.push(route -> route.newRequest(channelId)
						.body(new MessagePojo("hello at " + Instant.now())))
				.subscribe(response -> System.out.println("sent a message with content: " + response.content));
		TimeUnit.SECONDS.sleep(10);
	}
}
