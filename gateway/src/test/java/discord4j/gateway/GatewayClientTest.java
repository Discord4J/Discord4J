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
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.StatusUpdate;
import discord4j.gateway.json.dispatch.MessageCreate;
import discord4j.gateway.json.dispatch.Ready;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GatewayClientTest {

    private static final String gatewayUrl = "wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream";

    @Test
    @Ignore("Example code not under CI")
    public void test() {
        // need to set 1 env vars, token
        String token = System.getenv("token");
        ObjectMapper mapper = getMapper();
        PayloadReader reader = new JacksonPayloadReader(mapper);
        PayloadWriter writer = new JacksonPayloadWriter(mapper);
        RetryOptions retryOptions = new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120),
                Integer.MAX_VALUE, Schedulers.elastic());
        GatewayClient gatewayClient = new DefaultGatewayClient(HttpClient.create(),
                reader, writer, retryOptions, token,
                new IdentifyOptions(0, 1, null), null,
                new SimpleBucket(1, Duration.ofSeconds(6)));

        gatewayClient.dispatch().subscribe(dispatch -> {
            if (dispatch instanceof Ready) {
                System.out.println("Test received READY!");
            }
        });

        // sink to manually produce individual values
        FluxSink<GatewayPayload<?>> outboundSink = gatewayClient.sender();

        gatewayClient.dispatch().ofType(MessageCreate.class)
                .subscribe(message -> {
                    String content = message.getContent();
                    if ("!close".equals(content)) {
                        gatewayClient.close(false);
                    } else if ("!retry".equals(content)) {
                        gatewayClient.close(true);
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
        ObjectMapper mapper = getMapper();

        CountDownLatch latch = new CountDownLatch(0);
        List<CountDownLatch> latches = new ArrayList<>();
        latches.add(latch);
        CountDownLatch exit = new CountDownLatch(shardCount);

        for (int i = 0; i < shardCount; i++) {
            CountDownLatch next = new CountDownLatch(1);
            GatewayClient shard = new DefaultGatewayClient(
                    HttpClient.create().compress(true),
                    new JacksonPayloadReader(mapper),
                    new JacksonPayloadWriter(mapper),
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
                    },
                    new SimpleBucket(1, Duration.ofSeconds(6)));
            latches.add(next);
            int shardIndex = i;
            Schedulers.elastic().schedule(() -> {
                try {
                    latches.get(shardIndex).await();
                    System.out.println("Running shard " + shardIndex);
                    shard.execute("wss://gateway.discord.gg/?v=6&encoding=json&compress=zlib-stream").block();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        exit.await();
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addHandler(new UnknownPropertyHandler(true))
                .registerModules(new PossibleModule(), new Jdk8Module());
    }
}
