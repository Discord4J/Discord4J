package discord4j.rest.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.pojo.MessagePojo;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.DiscordRequest;
import discord4j.rest.request.StreamPuller;
import discord4j.rest.request.StreamStore;
import discord4j.rest.route.Routes;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class RequestStreamTest {
	@Test
	public void test() throws Exception {
		String token = System.getProperty("token");

		ObjectMapper mapper = new ObjectMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl("https://discordapp.com/api/v6")
				.defaultHeader("Authorization", "Bot " + token)
				.defaultHeader("Content-Type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.build();

		StreamStore streamStore = new StreamStore(new StreamPuller(httpClient));
		DiscordRequest<MessagePojo> request = Routes.MESSAGE_CREATE.newRequest(212695582064508928L).body(new MessagePojo("hello"));
		streamStore.getStream(Routes.MESSAGE_CREATE).push(request).subscribe(m -> {
			System.out.println("Message sent");
		});

		TimeUnit.SECONDS.sleep(10);
	}
}
