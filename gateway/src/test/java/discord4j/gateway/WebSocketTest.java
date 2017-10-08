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

import discord4j.gateway.client.WebSocketClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class WebSocketTest {

	private static final Logger log = LoggerFactory.getLogger(WebSocketTest.class);

	@Test
	public void connectTest() throws URISyntaxException, InterruptedException {
		int count = 10;
		Flux<String> input = Flux.range(1, count).map(index -> "msg-" + index);
		ReplayProcessor<Object> output = ReplayProcessor.create(count);

		WebSocketClient client = new WebSocketClient();

		client.execute(new URI("wss://echo.websocket.org/"),
				session -> {
					log.debug("Starting to send messages");
					return session
							.send(input.doOnNext(s -> log.debug("outbound " + s)).map(session::textMessage))
							.thenMany(session.receive().take(count).map(WebSocketMessage::getPayloadAsText))
							.subscribeWith(output)
							.doOnNext(s -> log.debug("inbound " + s))
							.then()
							.doOnSuccessOrError((aVoid, ex) ->
									log.debug("Done with " + (ex != null ? ex.getMessage() : "success")));
				})
				.block(Duration.ofMillis(5000));

		assertEquals(input.collectList().block(Duration.ofMillis(5000)),
				output.collectList().block(Duration.ofMillis(5000)));
	}
}
