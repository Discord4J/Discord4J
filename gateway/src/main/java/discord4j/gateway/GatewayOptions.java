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

import discord4j.gateway.payload.PayloadReader;
import discord4j.gateway.payload.PayloadWriter;
import discord4j.gateway.retry.ReconnectOptions;
import reactor.netty.http.client.HttpClient;

public class GatewayOptions {

    private final String token;
    private final HttpClient httpClient;
    private final PayloadReader payloadReader;
    private final PayloadWriter payloadWriter;
    private final ReconnectOptions reconnectOptions;
    private final IdentifyOptions identifyOptions;
    private final GatewayObserver initialObserver;
    private final PayloadTransformer identifyLimiter;

    protected GatewayOptions(Builder builder) {
        this.token = builder.token;
        this.httpClient = builder.httpClient;
        this.payloadReader = builder.payloadReader;
        this.payloadWriter = builder.payloadWriter;
        this.reconnectOptions = builder.reconnectOptions;
        this.identifyOptions = builder.identifyOptions;
        this.initialObserver = builder.initialObserver;
        this.identifyLimiter = builder.identifyLimiter;
    }

    public static Builder builder() {
        return new GatewayOptions.Builder();
    }

    public static class Builder {

        private String token;
        private HttpClient httpClient;
        private PayloadReader payloadReader;
        private PayloadWriter payloadWriter;
        private ReconnectOptions reconnectOptions;
        private IdentifyOptions identifyOptions;
        private GatewayObserver initialObserver;
        private PayloadTransformer identifyLimiter;

        protected Builder() {
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder setPayloadReader(PayloadReader payloadReader) {
            this.payloadReader = payloadReader;
            return this;
        }

        public Builder setPayloadWriter(PayloadWriter payloadWriter) {
            this.payloadWriter = payloadWriter;
            return this;
        }

        public Builder setReconnectOptions(ReconnectOptions reconnectOptions) {
            this.reconnectOptions = reconnectOptions;
            return this;
        }

        public Builder setIdentifyOptions(IdentifyOptions identifyOptions) {
            this.identifyOptions = identifyOptions;
            return this;
        }

        public Builder setInitialObserver(GatewayObserver initialObserver) {
            this.initialObserver = initialObserver;
            return this;
        }

        public Builder setIdentifyLimiter(PayloadTransformer identifyLimiter) {
            this.identifyLimiter = identifyLimiter;
            return this;
        }

        public GatewayOptions build() {
            return new GatewayOptions(this);
        }
    }

    public String getToken() {
        return token;
    }

    public HttpClient getHttpClient() {
        return httpClient;
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
