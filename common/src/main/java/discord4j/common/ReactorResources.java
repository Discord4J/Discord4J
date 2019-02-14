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

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.util.function.Function;

/**
 * Provides Reactor Netty resources like an {@link HttpClient} that can be customized and
 * reused across the application.
 * <p>
 * Allow a user to externally manage the connection pool through a custom
 * {@link ConnectionProvider}, and custom event loop threads using a
 * {@link LoopResources}.
 */
public class ReactorResources {

    private final static Function<HttpClient, HttpClient> initializer = client -> client.compress(true);

    private final HttpClient httpClient;

    /**
     * Create with a default {@link HttpClient} instance.
     */
    public ReactorResources() {
        this.httpClient = initializer.apply(HttpClient.create());
    }

    /**
     * Create with a set of external Reactor Netty connection pool and event loop threads.
     * <p>Use this in case you want dedicated resources for a particular client or clients instead of the global
     * default. Requires externally disposing of the given parameters on
     * application shutdown, through {@link ConnectionProvider#dispose()} and
     * {@link LoopResources#dispose()}.
     *
     * @param provider the connection pool provider to use
     * @param resources the set of event loop threads to use
     * @param mapper a Function to customize the underlying HttpClient
     */
    public ReactorResources(ConnectionProvider provider, LoopResources resources,
                            Function<HttpClient, HttpClient> mapper) {
        this.httpClient = initializer.andThen(mapper).apply(initHttpClient(provider, resources));
    }

    /**
     * Create with a pre-configured {@link HttpClient} instance.
     *
     * @param httpClient the underlying HttpClient to use
     */
    public ReactorResources(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private HttpClient initHttpClient(ConnectionProvider provider, LoopResources resources) {
        return HttpClient.create(provider).tcpConfiguration(tcpClient -> tcpClient.runOn(resources));
    }

    /**
     * Get the {@link HttpClient} configured by this provider.
     *
     * @return a Reactor Netty HTTP client ready to perform requests
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
