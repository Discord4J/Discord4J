/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.json.response.GatewayResponse;
import discord4j.gateway.WebSocketMessage;
import discord4j.gateway.client.WebSocketClient;
import discord4j.rest.http.EmptyWriterStrategy;
import discord4j.rest.http.JacksonReaderStrategy;
import discord4j.rest.http.JacksonWriterStrategy;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
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

import static org.junit.Assert.assertTrue;

public class GatewayTest {

	private static final Logger log = LoggerFactory.getLogger(GatewayTest.class);

	private Router router;

	@Before
	public void initialize() {
		String token = System.getenv("token");
		ObjectMapper mapper = new ObjectMapper();
		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl(Routes.BASE_URL)
				.defaultHeader("Authorization", "Bot " + token)
				.defaultHeader("Content-Type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.writerStrategy(new EmptyWriterStrategy())
				.build();
		router = new Router(httpClient);

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger("discord4j.rest.http.client").setLevel(Level.TRACE);
	}

	@Test
	public void testGatewayConnect() throws URISyntaxException, InterruptedException {
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
				.subscribe(message -> {
					log.info("[Inbound Message] {}", message);
				});

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
		GatewayResponse response = Routes.GATEWAY_GET.newRequest().exchange(router).toFuture().join();
		assertTrue(response != null);
		return response.getUrl();
	}
}
