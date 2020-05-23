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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.SimpleBucket;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.gateway.json.StatusUpdate;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExampleGatewayClientShards {

    private static final String gatewayUrl = "wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream";

    /*
     * Example code showcasing raw gateway module usage to launch an arbitrary number of shards, coordinating their
     * connection process using CountDownLatch objects.
     */
    public static void main(String[] args) throws InterruptedException {
        // need to set 2 env vars, token and shardCount
        String token = System.getenv("token");
        int shardCount = Integer.parseInt(System.getenv("shardCount"));
        ObjectMapper mapper = getMapper();

        CountDownLatch latch = new CountDownLatch(0);
        List<CountDownLatch> latches = new ArrayList<>();
        latches.add(latch);
        CountDownLatch exit = new CountDownLatch(shardCount);

        // we must share the PayloadTransformer across shards to coordinate IDENTIFY requests
        PayloadTransformer transformer = new RateLimiterTransformer(new SimpleBucket(1, Duration.ofSeconds(6)));
        for (int i = 0; i < shardCount; i++) {
            CountDownLatch next = new CountDownLatch(1);
            GatewayClient shard = new DefaultGatewayClient(
                    HttpClient.create().compress(true),
                    new JacksonPayloadReader(mapper),
                    new JacksonPayloadWriter(mapper),
                    // RetryOptions must not be shared as it tracks state for a single shard
                    new RetryOptions(Duration.ofSeconds(2), Duration.ofSeconds(120), Integer.MAX_VALUE,
                            Schedulers.elastic()),
                    token,
                    new IdentifyOptions(i, shardCount,
                            new StatusUpdate(null, "invisible")),
                    (s, o) -> {
                        if (s.equals(GatewayObserver.CONNECTED)) {
                            next.countDown();
                        }
                        if (s.equals(GatewayObserver.DISCONNECTED)) {
                            exit.countDown();
                        }
                    }, transformer);
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

    private static ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addHandler(new UnknownPropertyHandler(true))
                .registerModules(new PossibleModule(), new Jdk8Module());
    }
}
