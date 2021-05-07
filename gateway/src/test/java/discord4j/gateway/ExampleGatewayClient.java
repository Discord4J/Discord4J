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

package discord4j.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.common.retry.ReconnectOptions;
import discord4j.gateway.intent.IntentSet;
import discord4j.gateway.payload.JacksonPayloadReader;
import discord4j.gateway.payload.JacksonPayloadWriter;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import static discord4j.gateway.intent.Intent.*;

public class ExampleGatewayClient {

    private static final Logger log = Loggers.getLogger(ExampleGatewayClient.class);

    public static void main(String[] args) {
        ReactorResources reactorResources = ReactorResources.create();
        GatewayReactorResources gatewayReactorResources = new GatewayReactorResources(reactorResources);
        JacksonResources jacksonResources = JacksonResources.create();

        ObjectMapper objectMapper = jacksonResources.getObjectMapper();

        PayloadWriter payloadWriter = new JacksonPayloadWriter(objectMapper);
        PayloadReader payloadReader = new JacksonPayloadReader(objectMapper);

        ReconnectOptions reconnectOptions = ReconnectOptions.create();

        IdentifyOptions identifyOptions = IdentifyOptions.builder(0, 1)
                .intents(IntentSet.of(GUILDS, GUILD_MESSAGES, GUILD_MESSAGE_REACTIONS, GUILD_VOICE_STATES))
                .build();

        GatewayOptions gatewayOptions = new GatewayOptions(
                System.getenv("token"),
                gatewayReactorResources,
                payloadReader,
                payloadWriter,
                reconnectOptions,
                identifyOptions,
                GatewayObserver.NOOP_LISTENER,
                s -> s,
                1
        );

        GatewayClient gatewayClient = new DefaultGatewayClient(gatewayOptions);

        gatewayClient.receiver(
                buf -> Mono.fromCallable(() -> {
                    byte[] array = new byte[buf.readableBytes()];
                    buf.readBytes(array);
                    buf.release();
                    return array;
                }))
                .map(String::new)
                .doOnNext(str -> log.info("Received: {}", str))
                .subscribe();

        gatewayClient.execute("wss://gateway.discord.gg?v=6&encoding=json&compress=zlib-stream").block();
    }
}
