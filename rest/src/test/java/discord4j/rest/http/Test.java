package discord4j.rest.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.pojo.MessagePojo;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.RequestStream;
import discord4j.rest.request.StreamPuller;
import discord4j.rest.route.Routes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class Test {
	@org.junit.Test
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


		RequestStream<MessagePojo> messageStream = new RequestStream<>();

		Collection<RequestStream<?>> streams = new ArrayList<>();
		streams.add(messageStream);
		StreamPuller streamPuller = new StreamPuller(httpClient, streams);
		streamPuller.start();

		messageStream.push(Routes.MESSAGE_CREATE.newRequest(212695582064508928L).body(new MessagePojo("hello"))).subscribe(response -> {
			System.out.println("sent a message with content: " + response.content);
		});

		TimeUnit.SECONDS.sleep(10);
	}
}
