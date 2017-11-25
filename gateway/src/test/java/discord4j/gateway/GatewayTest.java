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

package discord4j.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import discord4j.gateway.websocket.CloseStatus;
import discord4j.gateway.websocket.WebSocketClient;
import discord4j.gateway.websocket.WebSocketMessage;
import io.netty.buffer.ByteBuf;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.EmitterProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class GatewayTest {

	public static final String gatewayUrl = "wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream";
	private static final Logger log = LoggerFactory.getLogger(GatewayTest.class);

	private String token;
	private Inflater zlibContext;

	@Before
	public void initialize() {
		zlibContext = new Inflater();
		token = System.getenv("token");

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger("discord4j.rest.http.client").setLevel(Level.TRACE);
	}

	@Test
	public void testGatewayConnect() throws URISyntaxException, InterruptedException {
		EmitterProcessor<String> outboundExchange = EmitterProcessor.create();
		EmitterProcessor<String> inboundExchange = EmitterProcessor.create();

		WebSocketClient client = new WebSocketClient();

		client.execute(gatewayUrl, session -> {
			WebSocketMessageSubscriber subscriber = new WebSocketMessageSubscriber(inboundExchange, outboundExchange,
					token);
			session.closeFuture().subscribe(subscriber::onClose);

			session.receive()
					.map(message -> {
						if (WebSocketMessage.Type.BINARY.equals(message.getType())) {
							ByteBuf payload = message.getPayload();
							byte[] bytes = new byte[payload.readableBytes()];
							payload.readBytes(bytes);

							ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length * 2);
							try (InflaterOutputStream inflater = new InflaterOutputStream(out, zlibContext)) {
								inflater.write(bytes);
								return out.toString("UTF-8");
							} catch (IOException e) {
								throw Exceptions.propagate(e);
							}
						} else {
							return message.getPayloadAsText();
						}
					})
					.log("session-inbound")
					.subscribe(subscriber::onNext, subscriber::onError, subscriber::onComplete);

			return session.send(outboundExchange
					.log("session-outbound")
					.doOnError(t -> log.info("outbound error", t))
					.map(WebSocketMessage::fromText));
		}).block();
	}

	private static class WebSocketMessageSubscriber {

		private final EmitterProcessor<String> inboundExchange; // towards our users (eventDispatcher)
		private final EmitterProcessor<String> outboundExchange; // towards discord (wire)
		private final String token;

		public WebSocketMessageSubscriber(EmitterProcessor<String> inboundExchange,
		                                  EmitterProcessor<String> outboundExchange, String token) {
			this.inboundExchange = inboundExchange;
			this.outboundExchange = outboundExchange;
			this.token = token;
		}

		public void onNext(String message) {
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
						"    \"large_threshold\": 250\n" +
						"  }\n" +
						"}");
			} else {
				inboundExchange.onNext(message);
			}
		}

		public void onError(Throwable error) {
			log.error("Error", error);
		}

		public void onComplete() {
			inboundExchange.onComplete();
			outboundExchange.onComplete();
		}

		public void onClose(CloseStatus closeStatus) {
			log.info("Connection was CLOSED with status code {}", closeStatus.getStatusCode());
		}
	}
}
