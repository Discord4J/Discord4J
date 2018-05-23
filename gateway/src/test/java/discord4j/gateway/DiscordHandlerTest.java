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
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleModule;
import discord4j.gateway.json.*;
import discord4j.gateway.json.dispatch.Dispatch;
import discord4j.gateway.json.dispatch.Ready;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscordHandlerTest {

    public static final String gatewayUrl = "wss://gateway.discord.gg/?v=6&encoding=json&compress=zlib-stream";
    private static final Logger log = Loggers.getLogger(DiscordHandlerTest.class);

    private String token;

    @Before
    public void initialize() {
        token = System.getenv("token");
    }

    @Test
    @Ignore("Example code not under CI")
    public void testGatewayConnect() {
        ObjectMapper mapper = getMapper();
        PayloadReader reader = new JacksonPayloadReader(mapper);
        PayloadWriter writer = new JacksonPayloadWriter(mapper);

        DiscordWebSocketHandler handler = new DiscordWebSocketHandler(reader, writer);

        EmitterProcessor<Dispatch> dispatch = EmitterProcessor.create();
        AtomicInteger seq = new AtomicInteger(0);

        handler.inbound().subscribe(payload -> {
            if (payload.getData() instanceof Hello) {
                IdentifyProperties properties = new IdentifyProperties("linux", "disco", "disco");
                GatewayPayload<Identify> identify = GatewayPayload.identify(new Identify(token, properties, true, 250,
                        Possible.absent(), Possible.absent()));

                handler.outbound().onNext(identify);
            } else if (payload.getData() instanceof Dispatch) {
                seq.set(payload.getSequence());
                dispatch.onNext((Dispatch) payload.getData());
            }
        }, error -> {
            log.warn("Gateway connection terminated: {}", error.toString());
        });

        Flux.interval(Duration.ofMillis(45250))
                .map(l -> new Heartbeat(seq.get()))
                .map(GatewayPayload::heartbeat)
                .subscribe(handler.outbound()::onNext);

        dispatch.ofType(Ready.class).subscribe(ready -> {
            log.info("Gateway received READY!");
        });

        HttpClient.create()
                .websocket()
                .uri(gatewayUrl)
                .handle(handler::handle)
                .then()
                .publishOn(Schedulers.elastic())
                .block();
    }

    private ObjectMapper getMapper() {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .registerModule(new PossibleModule());
    }

}
