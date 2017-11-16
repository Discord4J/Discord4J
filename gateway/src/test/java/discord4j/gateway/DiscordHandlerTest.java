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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.Hello;
import discord4j.common.json.payload.Identify;
import discord4j.common.json.payload.IdentifyProperties;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.websocket.WebSocketClient;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscordHandlerTest {

	public static final String gatewayUrl = "wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream";
	private static final Logger log = LoggerFactory.getLogger(GatewayTest.class);

	private String token;

	@Before
	public void initialize() {
		token = System.getenv("token");

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger("discord4j.rest.http.client").setLevel(Level.TRACE);
	}

	@Test
	public void testGatewayConnect() throws Exception {
		WebSocketClient ws = new WebSocketClient();

		ObjectMapper mapper = getMapper();
		PayloadReader reader = new JacksonPayloadReader(mapper);
		PayloadWriter writer = new JacksonPayloadWriter(mapper);

		DiscordWebSocketHandler handler = new DiscordWebSocketHandler(reader, writer);

		AtomicInteger seq = new AtomicInteger(0);

		handler.inbound().subscribe(payload -> {

			log.debug("Received Payload: " + payload);

			if (payload.getSequence() != null) {
				seq.set(payload.getSequence());
			}

			if (payload.getOp() == 10) { // HELLO

				// TODO heartbeat
				//				Flux.interval(Duration.ofMillis(41250))
				//						.takeUntil(t -> handler.outbound().isTerminated())
				//						.subscribe(t -> {
				//							GatewayPayload heartbeat = GatewayPayload.of(1, null, seq.get(), null);
				//							handler.outbound().onNext(heartbeat);
				//						});

				if (payload.getData() instanceof Hello) {
					throw new RuntimeException("Type is HELLO!");
				}


//				Map<String, Object> identify = new HashMap<>();
//				Map<String, String> properties = new HashMap<>();
//				properties.put("os", "linux");
//				properties.put("browser", "disco");
//				properties.put("device", "disco");
//				d.put("token", token);
//				d.put("properties", properties);
//				d.put("large_threshold", 250);
//				*/

				IdentifyProperties properties = new IdentifyProperties("linux", "disco", "disco");
				GatewayPayload identify = new GatewayPayload(2, new Identify(token, properties, true, 250, Possible.absent(), Possible.absent()), null, null);

				handler.outbound().onNext(identify);
			}
		}, error -> {
			log.warn("Gateway connection terminated: {}", error.toString());
		});

		ws.execute(new URI(gatewayUrl), handler).block();
	}

	private ObjectMapper getMapper() {
		return new ObjectMapper()
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
				.registerModule(new PossibleModule());
	}

}
