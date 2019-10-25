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
import discord4j.gateway.GatewayOptions;
import org.junit.Test;

public class CustomOptionsBot {

    static class CustomOptions extends GatewayOptions {

        protected CustomOptions(Builder builder) {
            super(builder);
        }

        public static CustomOptions.Builder builder() {
            return new CustomOptions.Builder();
        }

        public CustomOptions.Builder mutate() {
            Builder builder = new Builder();
            builder.setIdentifyLimiter(getIdentifyLimiter())
                    .setInitialObserver(getInitialObserver())
                    .setToken(getToken())
                    .setReconnectOptions(getReconnectOptions())
                    .setIdentifyOptions(getIdentifyOptions())
                    .setPayloadWriter(getPayloadWriter())
                    .setPayloadReader(getPayloadReader())
                    .setReactorResources(getReactorResources());
            return builder;
        }

        static class Builder extends GatewayOptions.Builder {

            public CustomOptions build() {
                return new CustomOptions(this);
            }

        }
    }

    static class CustomGatewayClient extends DefaultGatewayClient {

        public CustomGatewayClient(CustomOptions options) {
            super(options);
        }
    }

    @Test
    public void customBot() {
        CustomOptions custom = CustomOptions.builder().build();
        DiscordClient.create(System.getenv("token"))
                .gateway()
                .setExtraOptions(options -> {
                    CustomOptions.Builder builder = custom.mutate();

                    return (CustomOptions) builder.setIdentifyLimiter(options.getIdentifyLimiter())
                            .setInitialObserver(options.getInitialObserver())
                            .setToken(options.getToken())
                            .setReconnectOptions(options.getReconnectOptions())
                            .setIdentifyOptions(options.getIdentifyOptions())
                            .setPayloadWriter(options.getPayloadWriter())
                            .setPayloadReader(options.getPayloadReader())
                            .setReactorResources(options.getReactorResources())
                            .build();
                })
                .connect(CustomGatewayClient::new)
                .block();
    }
}
