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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.core.event.DispatchContext;
import discord4j.core.event.DispatchHandlers;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import discord4j.rest.RestClient;
import discord4j.rest.http.*;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.RouteUtils;
import discord4j.store.noop.NoOpStoreService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An example bot showing gateway and rest operations without involving core module user-facing constructs.
 */
public class RetryBotTest {

    private static String token;
    private static Integer shardId;
    private static Integer shardCount;

    @BeforeClass
    public static void initialize() {
        token = System.getenv("token");
        String shardIdValue = System.getenv("shardId");
        String shardCountValue = System.getenv("shardCount");
        if (shardIdValue != null && shardCountValue != null) {
            shardId = Integer.valueOf(shardIdValue);
            shardCount = Integer.valueOf(shardCountValue);
        }
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void test() {
        Bootstrap bootstrap = new Bootstrap(token);

        CommandListener commandListener = new CommandListener(bootstrap.serviceMediator, bootstrap.eventDispatcher);
        commandListener.configure();

        LifecycleListener lifecycleListener = new LifecycleListener(bootstrap.eventDispatcher);
        lifecycleListener.configure();

        bootstrap.blockingLogin();
    }

    @Test
    @Ignore("Example code excluded from CI")
    public void testNoCommands() {
        Bootstrap bootstrap = new Bootstrap(token);

        LifecycleListener lifecycleListener = new LifecycleListener(bootstrap.eventDispatcher);
        lifecycleListener.configure();

        bootstrap.blockingLogin();
    }

    public static class Bootstrap {

        private final ServiceMediator serviceMediator;
        private final EventDispatcher eventDispatcher;

        Bootstrap(String token) {
            ObjectMapper mapper = new ObjectMapper()
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .registerModules(new PossibleModule(), new Jdk8Module());

            SimpleHttpClient httpClient = SimpleHttpClient.builder()
                    .baseUrl(Routes.BASE_URL)
                    .defaultHeader("authorization", "Bot " + token)
                    .defaultHeader("content-type", "application/json")
                    .defaultHeader("user-agent", "Discord4J")
                    .readerStrategy(new JacksonReaderStrategy<>(mapper))
                    .readerStrategy(new EmptyReaderStrategy())
                    .writerStrategy(new JacksonWriterStrategy(mapper))
                    .writerStrategy(new MultipartWriterStrategy(mapper))
                    .writerStrategy(new EmptyWriterStrategy())
                    .build();

            StoreHolder storeHolder = new StoreHolder(new NoOpStoreService());
            RestClient restClient = new RestClient(new Router(httpClient));
            ClientConfig config = new ClientConfig(token);

            GatewayClient gatewayClient = new GatewayClient(
                    new JacksonPayloadReader(mapper), new JacksonPayloadWriter(mapper),
                    new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120)), token);

            EmitterProcessor<Event> eventProcessor = EmitterProcessor.create(false);
            eventDispatcher = new EventDispatcher(eventProcessor, Schedulers.elastic());

            serviceMediator = new ServiceMediator(gatewayClient, restClient, storeHolder, eventDispatcher, config);

            gatewayClient.dispatch()
                    .map(dispatch -> DispatchContext.of(dispatch, serviceMediator))
                    .flatMap(DispatchHandlers::<Dispatch, Event>handle)
                    .subscribeWith(eventProcessor);
        }

        void blockingLogin() {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("v", 6);
            queryParams.put("encoding", "json");
            queryParams.put("compress", "zlib-stream");
            serviceMediator.getRestClient().getGatewayService().getGateway()
                    .flatMap(res -> serviceMediator.getGatewayClient()
                            .execute(RouteUtils.expandQuery(res.getUrl(), queryParams),
                                    shardId != null ? new int[]{shardId, shardCount} : null, null))
                    .block();
        }
    }

    public static class CommandListener {

        private final ServiceMediator services;
        private final EventDispatcher dispatcher;
        private final AtomicLong ownerId = new AtomicLong();

        CommandListener(ServiceMediator services, EventDispatcher dispatcher) {
            this.services = services;
            this.dispatcher = dispatcher;
        }

        void configure() {
            FluxSink<GatewayPayload<?>> outboundSink = services.getGatewayClient().sender();

            dispatcher.on(ReadyEvent.class)
                    .next()
                    .flatMap(ready -> services.getRestClient().getApplicationService().getCurrentApplicationInfo())
                    .map(res -> res.getOwner().getId())
                    .subscribe(ownerId::set);

            dispatcher.on(MessageCreateEvent.class)
                    .subscribe(event -> {
                        Message message = event.getMessage();

                        message.getAuthorId()
                                .filter(id -> ownerId.get() == id.asLong()) // only accept bot owner messages
                                .flatMap(id -> message.getContent())
                                .ifPresent(content -> {
                                    if ("!close".equals(content)) {
                                        services.getGatewayClient().close(false);
                                    } else if ("!retry".equals(content)) {
                                        services.getGatewayClient().close(true);
                                    } else if ("!fail".equals(content)) {
                                        outboundSink.next(new GatewayPayload<>());
                                    }
                                });
                    });
        }
    }

    public static class LifecycleListener {

        private final EventDispatcher dispatcher;

        LifecycleListener(EventDispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        void configure() {
            dispatcher.on(ConnectEvent.class).subscribe();
            dispatcher.on(DisconnectEvent.class).subscribe();
            dispatcher.on(ReconnectStartEvent.class).subscribe();
            dispatcher.on(ReconnectEvent.class).subscribe();
            dispatcher.on(ReconnectFailEvent.class).subscribe();
        }

    }

}
