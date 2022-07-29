/*
 *  This file is part of Discord4J.
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
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Dispatched when a user has pressed submit on a modal presented to them earlier
 * <p>
 * You are required to respond to this interaction within a three-second window by using one of the following:
 * <ul>
 *     <li>{@link #reply()} to directly include a message</li>
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
 *     <li>{@link #edit()} to modify the message that presented the modal</li>
 *     <li>{@link #deferEdit()} to acknowledge without a message, will not display a loading state and allows later
 *     modifications to the message that presented the modal</li>
 * </ul>
 * After the initial response is complete, you can work with the interaction using the following methods:
 * <ul>
 *     <li>{@link #editReply()} to edit the initial response</li>
 *     <li>{@link #getReply()} to fetch the initial response</li>
 *     <li>{@link #deleteReply()} to delete the initial response</li>
 * </ul>
 * You can also work with followup messages using:
 * <ul>
 *     <li>{@link #createFollowup()} to create a followup message</li>
 *     <li>{@link #editFollowup(Snowflake)} to update a followup message, given its ID</li>
 *     <li>{@link #deleteFollowup(Snowflake)} to delete a followup message, given its ID</li>
 * </ul>
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link InteractionCreateEvent}.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#interaction-create">Interaction Create</a>
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class ModalSubmitInteractionEvent extends ComponentInteractionEvent {

    public ModalSubmitInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // Convenience methods forwarding to ApplicationCommandInteraction methods.
    // We can assume these properties are present, because this is a modal submit interaction.

    /**
     * Gets the developer defined custom ID of this modal
     *
     * @return The custom ID of this modal
     */
    public String getCustomId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getCustomId)
                .orElseThrow(IllegalStateException::new); // should always be present for modal submits
    }

    /**
     * Gets the components from the modal
     *
     * @return The components from the modal
     */
    public /*~~>*/List<MessageComponent> getComponents() {
        return getInteraction().getCommandInteraction()
                .map(ApplicationCommandInteraction::getComponents)
                .orElse(Collections.emptyList()); // the list should never actually be empty, but just in case
    }

    /**
     * Gets the components from the modal that match the given component type.
     *
     * @param componentType the modal component type to return
     * @return The components from the modal
     */
    public <T extends MessageComponent> /*~~>*/List<T> getComponents(Class<T> componentType) {
        return getComponents()
                .stream()
                .flatMap(it -> {
                    if (it instanceof ActionRow) {
                        ActionRow row = (ActionRow) it;
                        return row.getChildren().stream();
                    }
                    return Stream.empty();
                })
                .flatMap(it -> {
                    if (componentType.isAssignableFrom(it.getClass())) {
                        return Stream.of(componentType.cast(it));
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Mono<Void> presentModal(InteractionPresentModalSpec spec) {
        return Mono.error(new UnsupportedOperationException("Modal submit interactions cannot present other modals"));
    }
}
