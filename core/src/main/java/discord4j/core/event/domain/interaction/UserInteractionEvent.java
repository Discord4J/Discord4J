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

package discord4j.core.event.domain.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.User;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Dispatched when a user uses a user command (user context menu)
 * <p>
 * You are required to respond to this interaction within a three-second window by using one of the following:
 * <ul>
 *     <li>{@link #reply()} to directly include a message</li>
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
 *     <li>{@link #presentModal(String, String, Collection)} to pop a modal for the user to interact with</li>
 * </ul>
 * See {@link InteractionCreateEvent} for more details about valid operations.
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of
 * {@link ApplicationCommandInteractionEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class UserInteractionEvent extends ApplicationCommandInteractionEvent {

    public UserInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is a user command interaction.

    /**
     * Gets the resolved targeted User.
     *
     * @return The resolved targeted User.
     */
    public User getResolvedUser() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getResolved)
                .flatMap(it -> it.getUser(getTargetId()))
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the ID of the targeted User.
     *
     * @return The ID of the targeted User.
     */
    public Snowflake getTargetId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getTargetId)
                .orElseThrow(IllegalStateException::new); // should always be present for context menu commands
    }

    /**
     * Requests to retrieve the targeted User.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} targeted by the user.
     * If an error is received, it is emitted through the Mono.
     */
    public Mono<User> getTargetUser() {
        return getClient().getUserById(getTargetId());
    }

    /**
     * Requests to retrieve the targeted User.
     *
     * @param retrievalStrategy The strategy to use to get the target User
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} targeted by the user.
     * If an error is received, it is emitted through the Mono.
     */
    public Mono<User> getTargetUser(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy)
                .getUserById(getTargetId());
    }
}
