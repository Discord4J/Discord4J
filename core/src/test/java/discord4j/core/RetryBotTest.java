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
import discord4j.core.event.function.DispatchContext;
import discord4j.core.event.function.DispatchHandlers;
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

	private static final Logger log = Loggers.getLogger(RetryBotTest.class);

	private String token;

	@Before
	public void initialize() {
		token = System.getenv("token");
	}

	@Test
	public void test() {
		ObjectMapper mapper = getMapper();

		SimpleHttpClient httpClient = SimpleHttpClient.builder()
				.baseUrl(Routes.BASE_URL)
				.defaultHeader("authorization", "Bot " + token)
				.defaultHeader("content-type", "application/json")
				.readerStrategy(new JacksonReaderStrategy<>(mapper))
				.readerStrategy(new EmptyReaderStrategy())
				.writerStrategy(new JacksonWriterStrategy(mapper))
				.writerStrategy(new MultipartWriterStrategy(mapper))
				.writerStrategy(new EmptyWriterStrategy())
				.build();
		Router router = new Router(httpClient);
		GatewayService gatewayService = new GatewayService(router);
		ApplicationService applicationService = new ApplicationService(router);

		PayloadReader reader = new JacksonLenientPayloadReader(mapper);
		PayloadWriter writer = new JacksonPayloadWriter(mapper);
		GatewayClient gatewayClient = new GatewayClient(reader, writer, token);

		Object client = new Object();

		EmitterProcessor<Event> eventProcessor = EmitterProcessor.create(false);
		EventDispatcher dispatcher = new EventDispatcher(eventProcessor, Schedulers.elastic());
		gatewayClient.dispatch()
				.compose(flux -> wiretap(flux, Loggers.getLogger("discord4j.event.dispatcher")))
				.map(dispatch -> DispatchContext.of(dispatch, client))
				.flatMap(context -> Mono.justOrEmpty(DispatchHandlers.<Dispatch, Event>handle(context)))
				.subscribeWith(eventProcessor);

		RetryCommand command = new RetryCommand(dispatcher, applicationService, gatewayClient);
		command.configure();

		gatewayService.getGateway()
				.flatMap(res -> gatewayClient.execute(res.getUrl() + "?v=6&encoding=json&compress=zlib-stream"))
				.block();
	}

	private static <V> Flux<V> wiretap(Flux<V> flux, Logger logger) {
		return flux.log(logger, Level.FINE, false, SignalType.ON_NEXT);
	}

	static class RetryCommand {

		private final EventDispatcher dispatcher;
		private final ApplicationService applicationService;
		private final GatewayClient gatewayClient;
		private final AtomicLong ownerId = new AtomicLong();

		RetryCommand(EventDispatcher dispatcher, ApplicationService applicationService, GatewayClient
				gatewayClient) {
			this.dispatcher = dispatcher;
			this.applicationService = applicationService;
			this.gatewayClient = gatewayClient;
		}

		void configure() {
			FluxSink<GatewayPayload<?>> outboundSink = gatewayClient.sender();

			dispatcher.on(ReadyEvent.class)
					.next()
					.flatMap(ready -> applicationService.getCurrentApplicationInfo())
					.map(res -> res.getOwner().getId())
					.subscribe(ownerId::set);

			dispatcher.on(MessageCreatedEvent.class)
					.subscribe(wrappedEvent -> {
						MessageCreate event = wrappedEvent.getMessageCreate();
						MessageResponse message = event.getMessage();
						if (ownerId.get() == message.getAuthor().getId()) {
							String content = message.getContent();
							if ("!close".equals(content)) {
								gatewayClient.close(false);
							} else if ("!retry".equals(content)) {
								gatewayClient.close(true);
							} else if ("!fail".equals(content)) {
								outboundSink.next(new GatewayPayload<>());
							}
						}
					});
		}
	}

	private ObjectMapper getMapper() {
		return new ObjectMapper()
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
				.registerModule(new PossibleModule());
	}


}
