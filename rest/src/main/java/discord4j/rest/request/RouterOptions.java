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

package discord4j.rest.request;

import discord4j.rest.response.ResponseFunction;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class RouterOptions {

    private final Scheduler responseScheduler;
    private final Scheduler rateLimitScheduler;
    private final List<ResponseFunction> responseTransformers;

    public RouterOptions(Builder builder) {
        this.responseScheduler = builder.responseScheduler;
        this.rateLimitScheduler = builder.rateLimitScheduler;
        this.responseTransformers = builder.responseTransformers;
    }

    public static RouterOptions.Builder builder() {
        return new RouterOptions.Builder();
    }

    public static RouterOptions create() {
        return builder().build();
    }

    public static class Builder {

        private Scheduler responseScheduler = Schedulers.elastic();
        private Scheduler rateLimitScheduler = Schedulers.elastic();
        private final List<ResponseFunction> responseTransformers = new ArrayList<>();

        protected Builder() {
        }

        public Builder responseScheduler(Scheduler responseScheduler) {
            this.responseScheduler = responseScheduler;
            return this;
        }

        public Builder rateLimitScheduler(Scheduler rateLimitScheduler) {
            this.rateLimitScheduler = rateLimitScheduler;
            return this;
        }

        public Builder onClientResponse(ResponseFunction errorHandler) {
            responseTransformers.add(errorHandler);
            return this;
        }

        public RouterOptions build() {
            return new RouterOptions(this);
        }
    }

    public Scheduler getResponseScheduler() {
        return responseScheduler;
    }

    public Scheduler getRateLimitScheduler() {
        return rateLimitScheduler;
    }

    public List<ResponseFunction> getResponseTransformers() {
        return responseTransformers;
    }
}
