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

package discord4j.rest.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.discordjson.json.InteractionResponseData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * An interaction handler is responsible for providing an initial response and followup for incoming interactions.
 */
@Experimental
public interface InteractionHandler {

    /**
     * Return the response to be sent to Discord on interaction create.
     *
     * @return the raw data to build an initial interaction response
     */
    InteractionResponseData response();

    /**
     * Return a reactive sequence to work with an interaction token after an initial response has been sent.
     *
     * @param response a handler with all common actions to derive the asynchronous followup sequence
     * @return a publisher, like a {@link Mono} or {@link Flux} to be subscribed for the duration of the interaction
     * token, after which will be cancelled through {@link Flux#take(Duration)} semantics.
     */
    Publisher<?> onInteractionResponse(InteractionResponse response);
}
