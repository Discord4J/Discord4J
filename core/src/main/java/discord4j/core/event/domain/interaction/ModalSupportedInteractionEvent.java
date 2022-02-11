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
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.LayoutComponent;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.InteractionResponseType;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Dispatched when a user in a guild interacts with an application command or component. It is recommended you use a
 * subclass in your event listeners to access interaction-specific methods. Application command interactions like
 * {@link ChatInputInteractionEvent}, {@link UserInteractionEvent} and {@link MessageInteractionEvent} are created
 * through Discord REST API, while component interactions like {@link ButtonInteractionEvent} and
 * {@link SelectMenuInteractionEvent} are added to a message as user interface. See a diagram below for the current
 * event hierarchy for interactions.
 * <p>
 * You are required to respond to this interaction within a three-second window by using one of the following:
 * <ul>
 *     <li>{@link #reply()} to directly include a message</li>
 *     <li>{@link #presentModal(String, String, Collection)} ()} to pop a modal for the user to interact with</li>
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
 * </ul>
 * After the initial response is complete, you can work with the interaction using the following methods if
 * you did <i>not</i> respond with a modal:
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
public class ModalSupportedInteractionEvent extends DeferrableInteractionEvent {

    public ModalSupportedInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /**
     * Requests to respond to the interaction by presenting a modal for the user to fill out and submit.
     * Once the user submits the modal, it will be received as a new {@link ModalSubmitInteractionEvent}.
     *
     * @param title The title of the modal
     * @param customId A developer defined ID for the modal
     * @param components A collection of components the modal should contain
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> presentModal(String title, String customId, Collection<LayoutComponent> components) {
        InteractionApplicationCommandCallbackData data = InteractionApplicationCommandCallbackData.builder()
                .title(Objects.requireNonNull(title))
                .customId(Objects.requireNonNull(customId))
                .components(components.stream()
                        .map(LayoutComponent::getData)
                        .collect(Collectors.toList())
                ).build();

        return createInteractionResponse(InteractionResponseType.MODAL, data);
    }
}
