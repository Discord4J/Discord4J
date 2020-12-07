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

package discord4j.core.shard;

import discord4j.common.close.CloseStatus;
import discord4j.discordjson.json.gateway.Dispatch;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.GatewayConnection;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.ShardGatewayPayload;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

class SingleGatewayClientGroup implements GatewayClientGroupManager {

    private final AtomicReference<GatewayClient> client = new AtomicReference<>();

    @Override
    public void add(int key, GatewayClient gatewayClient) {
        client.set(gatewayClient);
    }

    @Override
    public void remove(int key) {
        client.set(null);
    }

    private Optional<GatewayClient> instance() {
        return Optional.ofNullable(client.get());
    }

    @Override
    public Optional<GatewayClient> find(int index) {
        return instance().map(client -> new RoutableGatewayClient(client, index));
    }

    @Override
    public int getShardCount() {
        // in a distributed architecture, this number varies according to the latest gateway packet a worker reads
        return instance()
                .map(GatewayClient::getShardCount)
                .orElseThrow(() -> new IllegalStateException("Missing shard count information"));
    }

    @Override
    public Mono<Void> multicast(GatewayPayload<?> payload) {
        return Mono.defer(() -> Mono.just(getShardCount()))
                .flatMapMany(count -> Flux.range(0, count))
                .flatMap(index -> unicast(makeShardAware(payload, index)))
                .then();
    }

    private static ShardGatewayPayload<?> makeShardAware(GatewayPayload<?> payload, int shardIndex) {
        if (payload instanceof ShardGatewayPayload) {
            ShardGatewayPayload<?> shardedPayload = (ShardGatewayPayload<?>) payload;
            if (shardedPayload.getShardIndex() != shardIndex) {
                return new ShardGatewayPayload<>(payload, shardIndex);
            }
        }
        return new ShardGatewayPayload<>(payload, shardIndex);
    }

    @Override
    public Mono<Void> unicast(ShardGatewayPayload<?> payload) {
        return Mono.defer(() -> Mono.justOrEmpty(instance())
                .switchIfEmpty(Mono.error(new IllegalStateException("Missing gateway client"))))
                .flatMap(client -> client.send(Mono.just(payload)));
    }

    @Override
    public Mono<Void> logout() {
        return Mono.defer(() -> Mono.justOrEmpty(instance()))
                .flatMap(client -> client.close(false).then());
    }

    private static class RoutableGatewayClient implements GatewayClient {

        private final GatewayClient client;
        private final int shardIndex;

        private RoutableGatewayClient(GatewayClient client, int shardIndex) {
            this.client = client;
            this.shardIndex = shardIndex;
        }

        @Override
        public Mono<Void> execute(String gatewayUrl) {
            return client.execute(gatewayUrl);
        }

        @Override
        public Mono<CloseStatus> close(boolean allowResume) {
            return client.close(allowResume);
        }

        @Override
        public Flux<Dispatch> dispatch() {
            return client.dispatch();
        }

        @Override
        public Flux<GatewayPayload<?>> receiver() {
            return client.receiver();
        }

        @Override
        public <T> Flux<T> receiver(Function<ByteBuf, Publisher<? extends T>> mapper) {
            return client.receiver(mapper);
        }

        @Override
        public Mono<Void> send(Publisher<? extends GatewayPayload<?>> publisher) {
            return Flux.from(publisher)
                    .doOnNext(payload -> sender().emitNext(makeShardAware(payload, shardIndex), FAIL_FAST))
                    .then();
        }

        @Override
        public Sinks.Many<GatewayPayload<?>> sender() {
            return client.sender();
        }

        @Override
        public Mono<Void> sendBuffer(Publisher<ByteBuf> publisher) {
            return client.sendBuffer(publisher);
        }

        @Override
        public int getShardCount() {
            return client.getShardCount();
        }

        @Override
        public String getSessionId() {
            return client.getSessionId();
        }

        @Override
        public int getSequence() {
            return client.getSequence();
        }

        @Override
        public Flux<GatewayConnection.State> stateEvents() {
            return client.stateEvents();
        }

        @Override
        public Mono<Boolean> isConnected() {
            return client.isConnected();
        }

        @Override
        public Duration getResponseTime() {
            return client.getResponseTime();
        }
    }
}
