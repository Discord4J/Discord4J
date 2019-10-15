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

/**
 * Provides Reactor Netty resources like an {@link HttpClient} and {@link Scheduler} instances that can be customized
 * and reused across the application.
 * <p>
 * Allow a user to externally manage the connection pool through a custom {@link ConnectionProvider}, and custom
 * event loop threads using a {@link LoopResources}.
 */
public class ReactorResources {

    private final HttpClient httpClient;
    private final Scheduler timerTaskScheduler;

    /**
     * Create with a default {@link HttpClient} and {@link Scheduler} for timed tasks instances.
     */
    public ReactorResources() {
        this.httpClient = HttpClient.create().compress(true);
        this.timerTaskScheduler = Schedulers.parallel();
    }

    /**
     * Create with a pre-configured {@link HttpClient} and {@link Scheduler} suited for timed tasks instances.
     *
     * @param httpClient the underlying {@link HttpClient} to use
     * @param timerTaskScheduler the time-capable {@link Scheduler} to use
     */
    public ReactorResources(HttpClient httpClient, Scheduler timerTaskScheduler) {
        this.httpClient = httpClient;
        this.timerTaskScheduler = timerTaskScheduler;
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
     * Create a Reactor Netty {@link HttpClient} using the given connection pool and event loop threads.
     * <p>Use this in case you want dedicated resources for a particular client or clients instead of the global
     * default. Requires externally disposing of the given parameters on
     * application shutdown, through {@link ConnectionProvider#dispose()} and
     * {@link LoopResources#dispose()}.
     *
     * @param provider the connection pool provider to use
     * @param resources the set of event loop threads to use
     */
    public static HttpClient newHttpClient(ConnectionProvider provider, LoopResources resources) {
        return HttpClient.create(provider).tcpConfiguration(tcpClient -> tcpClient.runOn(resources));
    }
}
