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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class InteractionService extends RestService {

    public InteractionService(Router router) {
        super(router);
    }

    public Mono<JsonNode> createInteractionResponse(long interactionId, String interactionToken, InteractionResponseData response) {
        return Routes.INTERACTION_RESPONSE_CREATE.newRequest(interactionId, interactionToken)
            .body(response)
            .exchange(getRouter())
            .bodyToMono(JsonNode.class);
    }
}
