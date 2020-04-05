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
import discord4j.common.JacksonResources;
import discord4j.common.ReactorResources;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.rest.http.ExchangeStrategies;
import discord4j.rest.request.*;
import discord4j.rest.service.ChannelService;

import java.util.Collections;

public abstract class RestTests {

    public static Router getRouter(String token, ObjectMapper mapper) {
        return new DefaultRouter(new RouterOptions(token, ReactorResources.create(), ExchangeStrategies.jackson(mapper),
                Collections.emptyList(), BucketGlobalRateLimiter.create(), RequestQueueFactory.buffering()));
    }

    public static ObjectMapper getMapper(boolean ignoreUnknown) {
        return new JacksonResources(mapper -> mapper.addHandler(new UnknownPropertyHandler(ignoreUnknown)))
                .getObjectMapper();
    }

    public static ChannelService getChannelService() {
        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = getMapper(ignoreUnknown);
        Router router = getRouter(token, mapper);
        return new ChannelService(router);
    }
}
