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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.json.payload.GatewayPayload;
import discord4j.common.json.payload.dispatch.MessageCreate;
import discord4j.common.json.payload.dispatch.Ready;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.RetryOptions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.FluxSink;

import java.time.Duration;

public class GatewayClientTest {

    private static final String gatewayUrl = "wss://gateway.discord.gg/?v=6&encoding=json&compress=zlib-stream";

    private String token;

    @Before
    public void initialize() {
        token = System.getenv("token");
    }

    @Test
    @Ignore("Example code not under CI")
    public void test() {
        ObjectMapper mapper = getMapper();
        PayloadReader reader = new JacksonPayloadReader(mapper);
        PayloadWriter writer = new JacksonPayloadWriter(mapper);
        RetryOptions retryOptions = new RetryOptions(Duration.ofSeconds(5), Duration.ofSeconds(120), Integer.MAX_VALUE);
        GatewayClient gatewayClient = new GatewayClient(reader, writer, retryOptions, token, new IdentifyOptions());

        gatewayClient.dispatch().subscribe(dispatch -> {
            if (dispatch instanceof Ready) {
                System.out.println("Test received READY!");
            }
        });

        FluxSink<GatewayPayload<?>> outboundSink = gatewayClient.sender();

        gatewayClient.dispatch().ofType(MessageCreate.class)
                .subscribe(message -> {
                    String content = message.getMessage().getContent();
                    if ("!close".equals(content)) {
                        gatewayClient.close(false);
                    } else if ("!retry".equals(content)) {
                        gatewayClient.close(true);
                    } else if ("!fail".equals(content)) {
                        outboundSink.next(new GatewayPayload<>());
                    }
                });

        gatewayClient.execute(gatewayUrl).block();
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }
}
