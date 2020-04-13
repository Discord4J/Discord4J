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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.common.retry.ReconnectOptions;
import discord4j.discordjson.json.gateway.ImmutableStatusUpdate;
import discord4j.discordjson.json.gateway.MessageCreate;
import discord4j.discordjson.json.gateway.Ready;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.limiter.RateLimitTransformer;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class GatewayClientTest {

    private static final String gatewayUrl = "wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream";

    @Test
    @Ignore("Example code not under CI")
    public void test() {
        // need to set 1 env vars, token
        String token = System.getenv("token");
        ObjectMapper mapper = new JacksonResources().getObjectMapper();
        PayloadReader reader = new JacksonPayloadReader(mapper);
        PayloadWriter writer = new JacksonPayloadWriter(mapper);
        ReconnectOptions reconnectOptions = ReconnectOptions.create();
        GatewayOptions gatewayOptions = new GatewayOptions(
                token,
                GatewayReactorResources.create(),
                reader,
                writer,
                reconnectOptions,
                new IdentifyOptions(new ShardInfo(0, 1), null, Possible.absent(), true),
                GatewayObserver.NOOP_LISTENER,
                new RateLimitTransformer(1, Duration.ofSeconds(6)),
                1
        );
        GatewayClient gatewayClient = new DefaultGatewayClient(gatewayOptions);
        gatewayClient.dispatch().subscribe(dispatch -> {
            if (dispatch instanceof Ready) {
                System.out.println("Test received READY!");
            }
        });

        gatewayClient.receiver(byteBuf -> Mono.fromRunnable(() -> {
            try {
                String json = mapper.writeValueAsString(mapper.readTree(byteBuf.array()));
                System.out.println(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).subscribe();

        gatewayClient.dispatch().ofType(MessageCreate.class)
                .subscribe(message -> {
                    String content = message.message().content();
                    System.out.println(content);
                    if ("!close".equals(content)) {
                        gatewayClient.close(false).block();
                    } else if ("!retry".equals(content)) {
                        gatewayClient.close(true).block();
                    }
                });

        gatewayClient.execute(gatewayUrl).block();
    }

    /*
     * Example code showcasing raw gateway module usage to launch an arbitrary number of shards, coordinating their
     * connection process using CountDownLatch objects.
     */
    @Test
    @Ignore("Example code not under CI")
    public void testShards() throws InterruptedException {
        // need to set 2 env vars, token and shardCount
        String token = System.getenv("token");
        int shardCount = Integer.parseInt(System.getenv("shardCount"));
        ObjectMapper mapper = new JacksonResources().getObjectMapper();

        CountDownLatch latch = new CountDownLatch(0);
        List<CountDownLatch> latches = new ArrayList<>();
        latches.add(latch);
        CountDownLatch exit = new CountDownLatch(shardCount);

        // we must share the PayloadTransformer across shards to coordinate IDENTIFY requests
        PayloadTransformer transformer = new RateLimitTransformer(1, Duration.ofSeconds(6));
        for (int i = 0; i < shardCount; i++) {
            CountDownLatch next = new CountDownLatch(1);
            ReconnectOptions reconnectOptions = ReconnectOptions.create();
            GatewayOptions gatewayOptions = new GatewayOptions(
                    token,
                    GatewayReactorResources.create(),
                    new JacksonPayloadReader(mapper),
                    new JacksonPayloadWriter(mapper),
                    reconnectOptions,
                    new IdentifyOptions(new ShardInfo(i, shardCount), ImmutableStatusUpdate.of(Optional.empty(), Optional.empty(), "invisible", false), Possible.absent(), true),
                    (s, o) -> {
                        if (s.equals(GatewayObserver.CONNECTED)) {
                            next.countDown();
                        }
                        if (s.equals(GatewayObserver.DISCONNECTED)) {
                            exit.countDown();
                        }
                    },
                    transformer,
                    1
            );
            GatewayClient shard = new DefaultGatewayClient(gatewayOptions);
            latches.add(next);
            int shardIndex = i;
            Schedulers.elastic().schedule(() -> {
                try {
                    latches.get(shardIndex).await();
                    System.out.println("Running shard " + shardIndex);
                    shard.execute(gatewayUrl).block();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        exit.await();
    }
}
