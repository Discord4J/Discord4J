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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import discord4j.common.jackson.PossibleModule;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.http.client.DiscordWebClient;
import discord4j.rest.request.DefaultRouter;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.service.ChannelService;
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
        HttpClient httpClient = HttpClient.create().baseUrl(Routes.BASE_URL).compress(true);
        DiscordWebClient webClient = new DiscordWebClient(httpClient, defaultHeaders,
                ExchangeStrategies.withJacksonDefaults(mapper));
        return new DefaultRouter(webClient, Schedulers.elastic());
    }

    public static ObjectMapper getMapper(boolean ignoreUnknown) {
        return new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .addHandler(new UnknownPropertyHandler(ignoreUnknown))
                .registerModules(new PossibleModule(), new Jdk8Module());
    }

    public static ChannelService getChannelService() {
        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = getMapper(ignoreUnknown);
        Router router = getRouter(token, mapper);
        return new ChannelService(router);
    }
}
