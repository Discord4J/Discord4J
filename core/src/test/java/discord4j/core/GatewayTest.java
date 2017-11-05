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
import discord4j.gateway.WebSocketHandler;
import discord4j.gateway.WebSocketMessage;
import discord4j.gateway.adapter.WebSocketSession;
import discord4j.gateway.client.WebSocketClient;
import discord4j.rest.http.EmptyWriterStrategy;
import discord4j.rest.http.JacksonReaderStrategy;
import discord4j.rest.http.JacksonWriterStrategy;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import io.netty.buffer.ByteBuf;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

import static org.junit.Assert.assertTrue;

public class GatewayTest {

	private static final Logger log = LoggerFactory.getLogger(GatewayTest.class);

	private Router router;
	private String token;

	@Before
	public void initialize() {
		token = System.getenv("token");
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

		EmitterProcessor<String> outboundExchange = EmitterProcessor.create();
		EmitterProcessor<String> inboundExchange = EmitterProcessor.create();

		WebSocketClient client = new WebSocketClient();

		client.execute(new URI(gateway), new WebSocketHandler() {
			@Override
			public String[] getSubProtocols() {
				return new String[]{};
			}

			@Override
			public Mono<Void> handle(WebSocketSession session) {
				log.debug("Starting handshake");

				inboundExchange.log()
						.subscribe(message -> {
							if (message.contains("\"op\":10") || message.contains("\"op\":9")) {
								outboundExchange.onNext("{\n" +
										"  \"op\": 2,\n" +
										"  \"d\": {\n" +
										"    \"token\": \"" + token + "\",\n" +
										"    \"properties\": {\n" +
										"      \"$os\": \"linux\",\n" +
										"      \"$browser\": \"disco\",\n" +
										"      \"$device\": \"disco\"\n" +
										"    },\n" +
										"    \"compress\": true,\n" +
										"    \"large_threshold\": 250\n" +
										"  }\n" +
										"}");
							}
						});

				session.receive()
						.map(message -> {
							if (message.getType().equals(WebSocketMessage.Type.BINARY)) {
								ByteBuf payload = message.getPayload();
								byte[] bytes = new byte[payload.readableBytes()];
								payload.readBytes(bytes);

								try (BufferedReader reader = new BufferedReader(new InputStreamReader(new
										InflaterInputStream(new ByteArrayInputStream(bytes, 0, bytes.length))))) {
									return reader.lines().collect(Collectors.joining());
								} catch (IOException e) {
									throw Exceptions.propagate(e);
								}
							} else {
								return message.getPayloadAsText();
							}
						})
						.log("session-inbound")
						.subscribeWith(inboundExchange);

				return session.send(outboundExchange.log("session-outbound").map(session::textMessage));
			}
		}).block();
	}

	private String getGatewayUrl(Router router) {
		GatewayResponse response = Routes.GATEWAY_GET.newRequest().exchange(router).toFuture().join();
		assertTrue(response != null);
		return response.getUrl();
	}
}
