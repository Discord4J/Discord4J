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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.common.json.payload.dispatch.MessageCreate;
import discord4j.common.json.response.MessageResponse;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.*;
import discord4j.core.event.DispatchContext;
import discord4j.core.event.DispatchHandlers;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import discord4j.rest.http.*;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.service.ApplicationService;
import discord4j.rest.service.GatewayService;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class RetryBotTest {

	private String token;

	@Before
	public void initialize() {
		token = System.getenv("token");
	}

	@Test
	public void test() {
		FakeClient client = new FakeClient(token);

		client.gatewayClient.dispatch()
				.compose(flux -> wiretap(flux, Loggers.getLogger("discord4j.event.dispatcher")))
				.map(dispatch -> DispatchContext.of(dispatch, client))
				.flatMap(context -> Mono.justOrEmpty(DispatchHandlers.<Dispatch, Event>handle(context)))
				.subscribeWith(client.eventProcessor);

		CommandListener commandListener = new CommandListener(client);
		commandListener.configure();

		LifecycleListener lifecycleListener = new LifecycleListener(client);
		lifecycleListener.configure();

		client.gatewayService.getGateway()
				.flatMap(res -> client.gatewayClient.execute(res.getUrl() + "?v=6&encoding=json&compress=zlib-stream"))
				.block();
	}

	private static <V> Flux<V> wiretap(Flux<V> flux, Logger logger) {
		return flux.log(logger, Level.FINE, false, SignalType.ON_NEXT);
	}

	static class FakeClient {

		private final ObjectMapper mapper;

		private final SimpleHttpClient httpClient;
		private final Router router;
		private final GatewayService gatewayService;
		private final ApplicationService applicationService;

		private final PayloadReader reader;
		private final PayloadWriter writer;
		private final RetryOptions retryOptions;
		private final GatewayClient gatewayClient;

		private final EmitterProcessor<Event> eventProcessor;
		private final EventDispatcher dispatcher;

		FakeClient(String token) {
			mapper = new ObjectMapper()
					.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
					.registerModule(new PossibleModule());

			httpClient = SimpleHttpClient.builder()
					.baseUrl(Routes.BASE_URL)
					.defaultHeader("authorization", "Bot " + token)
					.defaultHeader("content-type", "application/json")
					.readerStrategy(new JacksonReaderStrategy<>(mapper))
					.readerStrategy(new EmptyReaderStrategy())
					.writerStrategy(new JacksonWriterStrategy(mapper))
					.writerStrategy(new MultipartWriterStrategy(mapper))
					.writerStrategy(new EmptyWriterStrategy())
					.build();

			router = new Router(httpClient);
			gatewayService = new GatewayService(router);
			applicationService = new ApplicationService(router);
			reader = new JacksonPayloadReader(mapper);
			writer = new JacksonPayloadWriter(mapper);
			retryOptions = new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120));
			gatewayClient = new GatewayClient(reader, writer, retryOptions, token);

			eventProcessor = EmitterProcessor.create(false);
			dispatcher = new EventDispatcher(eventProcessor, Schedulers.elastic());
		}
	}

	static class CommandListener {

		private final FakeClient client;
		private final AtomicLong ownerId = new AtomicLong();

		CommandListener(FakeClient client) {
			this.client = client;
		}

		void configure() {
			FluxSink<GatewayPayload<?>> outboundSink = client.gatewayClient.sender();

			client.dispatcher.on(ReadyEvent.class)
					.next()
					.flatMap(ready -> client.applicationService.getCurrentApplicationInfo())
					.map(res -> res.getOwner().getId())
					.subscribe(ownerId::set);

			client.dispatcher.on(MessageCreatedEvent.class)
					.subscribe(wrappedEvent -> {
						MessageCreate event = wrappedEvent.getMessageCreate();
						MessageResponse message = event.getMessage();
						if (ownerId.get() == message.getAuthor().getId()) {
							String content = message.getContent();
							if ("!close".equals(content)) {
								client.gatewayClient.close(false);
							} else if ("!retry".equals(content)) {
								client.gatewayClient.close(true);
							} else if ("!fail".equals(content)) {
								outboundSink.next(new GatewayPayload<>());
							}
						}
					});
		}
	}

	static class LifecycleListener {

		private static final Logger log = Loggers.getLogger(LifecycleListener.class);

		private final FakeClient client;

		LifecycleListener(FakeClient client) {
			this.client = client;
		}

		void configure() {
			client.dispatcher.on(ConnectedEvent.class)
					.subscribe(event -> log.info("Connected!"));

			client.dispatcher.on(DisconnectedEvent.class)
					.subscribe(event -> log.info("Disconnected!"));

			client.dispatcher.on(ReconnectStartedEvent.class)
					.subscribe(event -> log.info("Reconnects started..."));

			client.dispatcher.on(ReconnectedEvent.class)
					.subscribe(event -> log.info("Reconnected"));

			client.dispatcher.on(ReconnectFailedEvent.class)
					.subscribe(event -> log.info("Reconnect failed!"));
		}

	}


}
