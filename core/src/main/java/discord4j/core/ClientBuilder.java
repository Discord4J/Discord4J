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
package discord4j.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.payload.dispatch.Dispatch;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.dispatch.DispatchContext;
import discord4j.core.event.dispatch.DispatchHandlers;
import discord4j.core.event.domain.Event;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.IdentifyOptions;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import discord4j.rest.RestClient;
import discord4j.rest.http.*;
import discord4j.rest.http.client.SimpleHttpClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.store.noop.NoOpStoreService;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Objects;

public final class ClientBuilder {

    private String token;
    private int shardIndex;
    private int shardCount;

    public ClientBuilder(final String token) {
        this.token = Objects.requireNonNull(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = Objects.requireNonNull(token);
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public void setShardIndex(final int shardIndex) {
        this.shardIndex = shardIndex;
    }

    public int getShardCount() {
        return shardCount;
    }

    public void setShardCount(final int shardCount) {
        this.shardCount = shardCount;
    }

    public DiscordClient build() {
        final ClientConfig config = new ClientConfig(token, shardIndex, shardCount);

        final ObjectMapper mapper = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModules(new PossibleModule(), new Jdk8Module());

        final SimpleHttpClient httpClient = SimpleHttpClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bot " + token)
                .defaultHeader("User-Agent", "Discord4J")
                .readerStrategy(new JacksonReaderStrategy<>(mapper))
                .readerStrategy(new EmptyReaderStrategy())
                .writerStrategy(new MultipartWriterStrategy(mapper))
                .writerStrategy(new JacksonWriterStrategy(mapper))
                .writerStrategy(new EmptyWriterStrategy())
                .baseUrl(Routes.BASE_URL)
                .build();

        // TODO built-in store discovery or custom service override
        final StoreHolder storeHolder = new StoreHolder(new NoOpStoreService());
        final RestClient restClient = new RestClient(new Router(httpClient));

        // TODO custom retry parameters
        // TODO shard setup
        // TODO initial status
        final GatewayClient gatewayClient = new GatewayClient(
                new JacksonPayloadReader(mapper), new JacksonPayloadWriter(mapper),
                new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120)), token, new IdentifyOptions());

        // TODO custom processor and threading model
        final EmitterProcessor<Event> eventProcessor = EmitterProcessor.create(false);
        final EventDispatcher eventDispatcher = new EventDispatcher(eventProcessor, Schedulers.elastic());

        final ServiceMediator serviceMediator = new ServiceMediator(gatewayClient, restClient, storeHolder,
                eventDispatcher, config);

        serviceMediator.getGatewayClient().dispatch()
                .map(dispatch -> DispatchContext.of(dispatch, serviceMediator))
                .flatMap(DispatchHandlers::<Dispatch, Event>handle)
                .subscribeWith(eventProcessor);

        return new DiscordClient(serviceMediator);
    }
}
