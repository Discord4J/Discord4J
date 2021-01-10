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

import java.util.function.Function;

/**
 * An interaction handler that can have an additional asynchronous sequence for followup actions using
 * {@link #withFollowup(Function)}.
 */
@Experimental
public class FollowupInteractionHandler implements InteractionHandler {

    private final InteractionResponseData responseData;
    private final Function<InteractionResponse, Publisher<?>> followupHandler;

    FollowupInteractionHandler(InteractionResponseData responseData,
                               Function<InteractionResponse, Publisher<?>> followupHandler) {
        this.responseData = responseData;
        this.followupHandler = followupHandler;
    }

    @Override
    public InteractionResponseData response() {
        return responseData;
    }

    @Override
    public Publisher<?> onInteractionResponse(InteractionResponse response) {
        return followupHandler.apply(response);
    }

    /**
     * Create an interaction handler that can have additional behavior after an interaction is acknowledged. The
     * additional behavior is supplied a reactive sequence and will be subscribed for the duration of the interaction
     * token.
     *
     * @param followupHandler the function to retrieve a sequence from an interaction, for followup actions
     * @return this handler with the included followup actions
     */
    public InteractionHandler withFollowup(Function<InteractionResponse, Publisher<?>> followupHandler) {
        return new FollowupInteractionHandler(responseData, followupHandler);
    }
}
