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

import discord4j.common.ReactorResources;
import discord4j.gateway.limiter.PayloadTransformer;
import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.common.retry.ReconnectOptions;

import java.util.Objects;

/**
 * A set of options targeting the configuration of {@link GatewayClient} implementations.
 */
public class GatewayOptions {

    private final String token;
    private final ReactorResources reactorResources;
    private final PayloadReader payloadReader;
    private final PayloadWriter payloadWriter;
    private final ReconnectOptions reconnectOptions;
    private final IdentifyOptions identifyOptions;
    private final GatewayObserver initialObserver;
    private final PayloadTransformer identifyLimiter;

    public GatewayOptions(String token, ReactorResources reactorResources, PayloadReader payloadReader,
                          PayloadWriter payloadWriter, ReconnectOptions reconnectOptions,
                          IdentifyOptions identifyOptions, GatewayObserver initialObserver,
                          PayloadTransformer identifyLimiter) {
        this.token = Objects.requireNonNull(token, "token");
        this.reactorResources = Objects.requireNonNull(reactorResources, "reactorResources");
        this.payloadReader = Objects.requireNonNull(payloadReader, "payloadReader");
        this.payloadWriter = Objects.requireNonNull(payloadWriter, "payloadWriter");
        this.reconnectOptions = Objects.requireNonNull(reconnectOptions, "reconnectOptions");
        this.identifyOptions = Objects.requireNonNull(identifyOptions, "identifyOptions");
        this.initialObserver = Objects.requireNonNull(initialObserver, "initialObserver");
        this.identifyLimiter = Objects.requireNonNull(identifyLimiter, "identifyLimiter");
    }

    public String getToken() {
        return token;
    }

    public ReactorResources getReactorResources() {
        return reactorResources;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    public PayloadWriter getPayloadWriter() {
        return payloadWriter;
    }

    public ReconnectOptions getReconnectOptions() {
        return reconnectOptions;
    }

    public IdentifyOptions getIdentifyOptions() {
        return identifyOptions;
    }

    public GatewayObserver getInitialObserver() {
        return initialObserver;
    }

    public PayloadTransformer getIdentifyLimiter() {
        return identifyLimiter;
    }
}
