/**
 * Copyright 2002-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
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
							.doOnTerminate((aVoid, ex) ->
									log.debug("Done with " + (ex != null ? ex.getMessage() : "success")));
				})
				.block(Duration.ofMillis(5000));

		assertEquals(input.collectList().block(Duration.ofMillis(5000)),
				output.collectList().block(Duration.ofMillis(5000)));
	}
}
