package discord4j.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.pojo.GatewayPojo;
import discord4j.gateway.WebSocketMessage;
import discord4j.gateway.client.WebSocketClient;
import discord4j.rest.http.*;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.route.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.route.SimpleRouter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.WorkQueueProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GatewayTest {

	private static final Logger log = LoggerFactory.getLogger(GatewayTest.class);

	private String token;

	@Before
	public void initialize() {
		token = System.getProperty("token");
	}

	@Test
	public void testGatewayConnect() throws URISyntaxException, InterruptedException {
		ObjectMapper mapper = new ObjectMapper();
		SimpleHttpClient httpClient = createHttpClient(mapper);
		Router router = new SimpleRouter(httpClient);
		String gateway = getGatewayUrl(router) + "?v=6&encoding=json";

		AtomicBoolean closed = new AtomicBoolean();
		SynchronousQueue<String> outboundMessages = new SynchronousQueue<>();

		Flux<String> outboundExchange = Flux.create(emitter -> {
			while (!closed.get()) {
				try {
					String message = outboundMessages.poll(1, TimeUnit.SECONDS);
					if (message != null) {
						log.debug("Emitting {}", message);
						emitter.next(message);
					}
				} catch (InterruptedException e) {
					log.warn("Interrupted", e);
				}
			}
			emitter.complete();
		}, FluxSink.OverflowStrategy.BUFFER);
		WorkQueueProcessor<String> inboundExchange = WorkQueueProcessor.create();

		WebSocketClient client = new WebSocketClient();

		inboundExchange.log()
				.subscribe(message -> log.info("[Inbound Message] {}", message));

		client.execute(new URI(gateway),
				session -> {
					log.debug("Starting to send messages");

					session.send(outboundExchange.map(session::textMessage))
							.then();

					return session.receive()
							.map(WebSocketMessage::getPayloadAsText)
							.subscribeWith(inboundExchange)
							.then();
				}).block();
	}

	private String getGatewayUrl(Router router) {
		GatewayPojo response = router.exchange(Routes.GATEWAY_GET.complete())
				.toFuture()
				.join();
		return response.url;
	}

	private SimpleHttpClient createHttpClient(ObjectMapper mapper) {
		return SimpleHttpClient.builder()
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
	}
}
