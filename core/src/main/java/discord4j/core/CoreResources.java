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

import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.rest.RestClient;

import java.util.Objects;

/**
 * A set of resources required to build {@link DiscordClient} instances and are used for core Discord4J operations
 * like entity manipulation and API communication.
 */
public class CoreResources {

    private final String token;
    private final RestClient restClient;
    private final ReactorResources reactorResources;
    private final JacksonResources jacksonResources;

    protected CoreResources(Builder builder) {
        this.token = Objects.requireNonNull(builder.token);
        this.restClient = Objects.requireNonNull(builder.restClient);
        this.reactorResources = Objects.requireNonNull(builder.reactorResources);
        this.jacksonResources = Objects.requireNonNull(builder.jacksonResources);
    }

    public static CoreResources.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String token;
        private RestClient restClient;
        private ReactorResources reactorResources;
        private JacksonResources jacksonResources;

        protected Builder() {
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setRestClient(RestClient restClient) {
            this.restClient = restClient;
            return this;
        }

        public Builder setReactorResources(ReactorResources reactorResources) {
            this.reactorResources = reactorResources;
            return this;
        }

        public Builder setJacksonResources(JacksonResources jacksonResources) {
            this.jacksonResources = jacksonResources;
            return this;
        }

        public CoreResources build() {
            return new CoreResources(this);
        }
    }

    public String getToken() {
        return token;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public ReactorResources getReactorResources() {
        return reactorResources;
    }

    public JacksonResources getJacksonResources() {
        return jacksonResources;
    }
}
