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

import discord4j.gateway.DefaultGatewayClient;
import discord4j.gateway.GatewayClient;
import discord4j.gateway.GatewayOptions;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Advanced example showcasing how to customize the set of options passed to a potentially custom {@link GatewayClient}.
 */
public class ExampleCustomOptionsBot {

    static class CustomOptions extends GatewayOptions {

        private final String foo;

        public CustomOptions(GatewayOptions parent, String foo) {
            super(parent.getToken(), parent.getReactorResources(), parent.getPayloadReader(),
                    parent.getPayloadWriter(), parent.getReconnectOptions(), parent.getIdentifyOptions(),
                    parent.getInitialObserver(), parent.getIdentifyLimiter(), parent.getMaxMissedHeartbeatAck());
            this.foo = foo;
        }

        public String getFoo() {
            return foo;
        }
    }

    static class CustomGatewayClient extends DefaultGatewayClient {

        private static final Logger log = Loggers.getLogger(CustomGatewayClient.class);

        private final String foo;

        public CustomGatewayClient(CustomOptions options) {
            super(options);
            this.foo = options.getFoo();
        }

        @Override
        public Mono<Void> execute(String gatewayUrl) {
            log.info("Connecting with foo value: {}", foo);
            return super.execute(gatewayUrl);
        }
    }

    public static void main(String[] args) {
        DiscordClient.create(System.getenv("token"))
                .gateway()
                .setExtraOptions(options -> new CustomOptions(options, "bar"))
                .login(CustomGatewayClient::new)
                .blockOptional()
                .orElseThrow(RuntimeException::new)
                .onDisconnect()
                .block();
    }
}
