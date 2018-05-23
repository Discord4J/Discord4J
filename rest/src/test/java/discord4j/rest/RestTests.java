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

package discord4j.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

public abstract class RestTests {

    public static Router getRouter(String token, ObjectMapper mapper) {
        HttpHeaders defaultHeaders = new DefaultHttpHeaders();
        defaultHeaders.add("content-type", "application/json");
        defaultHeaders.add("authorization", "Bot " + token);
        defaultHeaders.add("user-agent", "DiscordBot(http://discord4j.com, test-suite)");
        HttpClient httpClient = HttpClient.create().baseUrl(Routes.BASE_URL).compress().wiretap();
        DiscordWebClient webClient = new DiscordWebClient(httpClient, defaultHeaders,
                ExchangeStrategies.withJacksonDefaults(mapper));
        return new Router(webClient, Schedulers.elastic());
    }
}
