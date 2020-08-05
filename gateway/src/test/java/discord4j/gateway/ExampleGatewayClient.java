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
import discord4j.common.ReactorResources;
import discord4j.common.SimpleBucket;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.gateway.json.dispatch.MessageCreate;
import discord4j.gateway.json.dispatch.Ready;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;

public class ExampleGatewayClient {

    private static final String gatewayUrl = "wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream";

    public static void main(String[] args) {
        // need to set 1 env vars, token
        String token = System.getenv("token");
        ObjectMapper mapper = getMapper();
        PayloadReader reader = new JacksonPayloadReader(mapper);
        PayloadWriter writer = new JacksonPayloadWriter(mapper);
        RetryOptions retryOptions = new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120),
                Integer.MAX_VALUE, Schedulers.elastic());
        GatewayClient gatewayClient = new DefaultGatewayClient(ReactorResources.DEFAULT_HTTP_CLIENT.get(),
                reader, writer, retryOptions, token,
                new IdentifyOptions(0, 1, null), null,
                new RateLimiterTransformer(new SimpleBucket(1, Duration.ofSeconds(6))));

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
                    String content = message.getContent();
                    System.out.println(content);
                    if ("!close".equals(content)) {
                        gatewayClient.close(false).block();
                    } else if ("!retry".equals(content)) {
                        gatewayClient.close(true).block();
                    }
                });

        gatewayClient.execute(gatewayUrl).block();
    }

    private static ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addHandler(new UnknownPropertyHandler(true))
                .registerModules(new PossibleModule(), new Jdk8Module());
    }
}
