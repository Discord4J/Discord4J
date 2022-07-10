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
import discord4j.core.object.entity.Message;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Dispatched when a user uses a message command (message context menu)
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
public class MessageInteractionEvent extends ApplicationCommandInteractionEvent {

    public MessageInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is a message command interaction.

    /**
     * Gets the resolved targeted Message.
     *
     * @return The resolved targeted Message.
     */
    public Message getResolvedMessage() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getResolved)
                .flatMap(it -> it.getMessage(getTargetId()))
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the ID of the targeted Message.
     *
     * @return The ID of the targeted Message.
     */
    public Snowflake getTargetId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getTargetId)
                .orElseThrow(IllegalStateException::new); // should always be present for context menu commands
    }

    /**
     * Requests to retrieve the targeted Message.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} targeted by the user.
     * If an error is received, it is emitted through the Mono.
     */
    public Mono<Message> getTargetMessage() {
        return getClient().getMessageById(getInteraction().getChannelId(), getTargetId());
    }

    /**
     * Requests to retrieve the targeted Message.
     *
     * @param retrievalStrategy The strategy to use to get the target Message
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} targeted by the user.
     * If an error is received, it is emitted through the Mono.
     */
    public Mono<Message> getTargetMessage(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy)
                .getMessageById(getInteraction().getChannelId(), getTargetId());
    }
}
