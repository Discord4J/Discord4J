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

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.function.Function;

public class RSocketRouterOptions extends RouterOptions {

    private final Function<DiscordWebRequest, InetSocketAddress> requestTransportMapper;

    public RSocketRouterOptions(RouterOptions parent,
                                Function<DiscordWebRequest, InetSocketAddress> requestTransportMapper) {
        super(parent.getToken(),
                parent.getReactorResources(),
                parent.getExchangeStrategies(),
                parent.getResponseTransformers(),
                parent.getGlobalRateLimiter());

        this.requestTransportMapper = Objects.requireNonNull(requestTransportMapper, "requestTransportMapper");
    }

    public Function<DiscordWebRequest, InetSocketAddress> getRequestTransportMapper() {
        return requestTransportMapper;
    }
}
