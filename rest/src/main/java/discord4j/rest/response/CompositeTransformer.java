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

package discord4j.rest.response;

import discord4j.common.annotations.Experimental;
import discord4j.rest.http.client.ClientResponse;
import discord4j.rest.request.DiscordWebRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * A {@link ResponseFunction} that can join multiple {@link ResponseFunction} instances.
 */
@Experimental
public class CompositeTransformer implements ResponseFunction {

    private final /*~~>*/List<ResponseFunction> responseFunctions;

    public CompositeTransformer(/*~~>*/List<ResponseFunction> responseFunctions) {
        /*~~>*/this.responseFunctions = responseFunctions;
    }

    @Override
    public Function<Mono<ClientResponse>, Mono<ClientResponse>> transform(DiscordWebRequest request) {
        return responseFunctions.stream()
                .map(rt -> rt.transform(request))
                .reduce(Function::andThen)
                .orElse(mono -> mono);
    }
}
