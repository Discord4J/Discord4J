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

package discord4j.common;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Provides Reactor Netty resources like an {@link HttpClient} and {@link Scheduler} instances that can be customized
 * and reused across the application.
 * <p>
 * Allow a user to externally manage the connection pool through a custom {@link ConnectionProvider}, and custom
 * event loop threads using a {@link LoopResources}.
 */
public class ReactorResources {

    private static final AtomicInteger ID = new AtomicInteger();

    public static final Supplier<HttpClient> DEFAULT_HTTP_CLIENT =
            () -> HttpClient.create().compress(true).followRedirect(true).secure();
    public static final Supplier<Scheduler> DEFAULT_TIMER_TASK_SCHEDULER = () ->
            Schedulers.newParallel("d4j-parallel-" + ID.incrementAndGet(), Schedulers.DEFAULT_POOL_SIZE, true);
    public static final Supplier<Scheduler> DEFAULT_BLOCKING_TASK_SCHEDULER = Schedulers::boundedElastic;

    private final HttpClient httpClient;
    private final Scheduler timerTaskScheduler;
    private final Scheduler blockingTaskScheduler;

    /**
     * Create with a default {@link HttpClient} and {@link Scheduler}s for timed and blocking tasks.
     */
    public ReactorResources() {
        this.httpClient = DEFAULT_HTTP_CLIENT.get();
        this.timerTaskScheduler = DEFAULT_TIMER_TASK_SCHEDULER.get();
        this.blockingTaskScheduler = DEFAULT_BLOCKING_TASK_SCHEDULER.get();
    }

    /**
     * Create with a pre-configured {@link HttpClient} and {@link Scheduler}s for timed and blocking tasks.
     *
     * @param httpClient the underlying {@link HttpClient} to use
     * @param timerTaskScheduler the time-capable {@link Scheduler} to use
     * @param blockingTaskScheduler the {@link Scheduler} to use for potentially blocking tasks
     */
    public ReactorResources(HttpClient httpClient, Scheduler timerTaskScheduler, Scheduler blockingTaskScheduler) {
        this.httpClient = httpClient;
        this.timerTaskScheduler = timerTaskScheduler;
        this.blockingTaskScheduler = blockingTaskScheduler;
    }

    protected ReactorResources(Builder builder) {
        this.httpClient = builder.httpClient == null ? DEFAULT_HTTP_CLIENT.get() : builder.httpClient;
        this.timerTaskScheduler = builder.timerTaskScheduler == null ?
                DEFAULT_TIMER_TASK_SCHEDULER.get() : builder.timerTaskScheduler;
        this.blockingTaskScheduler = builder.blockingTaskScheduler == null ?
                DEFAULT_BLOCKING_TASK_SCHEDULER.get() : builder.blockingTaskScheduler;
    }

    public static ReactorResources create() {
        return new Builder().build();
    }

    public static ReactorResources.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private HttpClient httpClient;
        private Scheduler timerTaskScheduler;
        private Scheduler blockingTaskScheduler;

        protected Builder() {
        }

        /**
         * Sets the underlying {@link HttpClient} to use. A default can be created from
         * {@link ReactorResources#DEFAULT_HTTP_CLIENT}.
         *
         * @return This builder, for chaining.
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Sets the time-capable {@link Scheduler} to use. A default can be created from
         * {@link ReactorResources#DEFAULT_TIMER_TASK_SCHEDULER}.
         *
         * @return This builder, for chaining.
         */
        public Builder timerTaskScheduler(Scheduler timerTaskScheduler) {
            this.timerTaskScheduler = timerTaskScheduler;
            return this;
        }

        /**
         * Sets the {@link Scheduler} to use for potentially blocking tasks. A default can be created from
         * {@link ReactorResources#DEFAULT_BLOCKING_TASK_SCHEDULER}.
         *
         * @return This builder, for chaining.
         */
        public Builder blockingTaskScheduler(Scheduler blockingTaskScheduler) {
            this.blockingTaskScheduler = blockingTaskScheduler;
            return this;
        }

        /**
         * Create the {@link ReactorResources}.
         *
         * @return A custom {@link ReactorResources}.
         */
        public ReactorResources build() {
            return new ReactorResources(this);
        }

    }

    /**
     * Get the {@link HttpClient} configured by this provider.
     *
     * @return a Reactor Netty HTTP client ready to perform requests
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Get the {@link Scheduler} configured by this provider to be used in timed tasks.
     *
     * @return a time-capable {@link Scheduler}
     */
    public Scheduler getTimerTaskScheduler() {
        return timerTaskScheduler;
    }

    /**
     * Get the {@link Scheduler} configured by this provider to be used in blocking tasks.
     *
     * @return a blocking-capable {@link Scheduler}
     */
    public Scheduler getBlockingTaskScheduler() {
        return blockingTaskScheduler;
    }

    /**
     * Create a Reactor Netty {@link HttpClient} using the given connection pool.
     * <p>Use this in case you want dedicated resources for a particular client or clients instead of the global
     * default.
     *
     * @param provider the connection pool provider to use
     * @return an {@link HttpClient} configured with custom resources
     */
    public static HttpClient newHttpClient(ConnectionProvider provider) {
        return HttpClient.create(provider).compress(true).followRedirect(true).secure();
    }

    /**
     * Create a Reactor Netty {@link HttpClient} using the given connection pool and event loop threads.
     * <p>Use this in case you want dedicated resources for a particular client or clients instead of the global
     * default.
     *
     * @param provider the connection pool provider to use
     * @param resources the set of event loop threads to use
     * @return an {@link HttpClient} configured with custom resources
     */
    public static HttpClient newHttpClient(ConnectionProvider provider, LoopResources resources) {
        return HttpClient.create(provider).runOn(resources).compress(true).followRedirect(true).secure();
    }
}
