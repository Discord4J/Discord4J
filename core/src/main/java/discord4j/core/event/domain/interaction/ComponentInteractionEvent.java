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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionApplicationCommandCallbackEditMono;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionCallbackSpec;
import discord4j.core.spec.InteractionCallbackSpecDeferEditMono;
import discord4j.core.spec.legacy.LegacyInteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.InteractionResponseType;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dispatched when a user interacts with a {@link MessageComponent} the bot has sent.
 * <p>
 * You are required to respond to this interaction within a three-second window by using one of the following:
 * <ul>
 *     <li>{@link #reply()} to directly include a message</li>
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
 *     <li>{@link #edit()} to modify the message the component is on</li>
 *     <li>{@link #deferEdit()} to acknowledge without a message, will not display a loading state and allows later
 *     modifications to the message the component is on</li>
 *     <li>{@link #presentModal(String, String, Collection)} to pop a modal for the user to interact with</li>
 * </ul>
 * See {@link InteractionCreateEvent} for more details about valid operations.
 * <p>
 * This is not directly dispatched by Discord, but is a utility specialization of {@link InteractionCreateEvent}.
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
public class ComponentInteractionEvent extends DeferrableInteractionEvent {

    public ComponentInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
    }

    /**
     * Gets the developer-defined custom id associated with the component.
     *
     * @return The component's custom id.
     * @see Button#getCustomId()
     */
    public String getCustomId() {
        return getInteraction().getCommandInteraction()
                .flatMap(ApplicationCommandInteraction::getCustomId)
                // note: custom_id is not guaranteed to present on components in general (e.g., link buttons),
                // but it is guaranteed to be present here, because we received an interaction_create for it
                // (which doesn't happen for components without custom_id)
                .orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the message the component is on.
     * <p>
     * For ephemeral messages, only the ID is present. Use {@link #getMessageId()}
     *
     * @return The message the component is on.
     */
    public Optional<Message> getMessage() {
        return getInteraction().getMessage();
    }

    /**
     * Gets the ID of the message the component is on.
     *
     * @return The ID of the message the component is on.
     */
    public Snowflake getMessageId() {
        return getInteraction().getMessageId()
                .orElseThrow(IllegalStateException::new); // at least the ID is always present for component interactions
    }

    /**
     * Acknowledge the interaction by indicating a message will be edited later. For component interactions, the user
     * <strong>does not</strong> see a loading state. For an "only you can see this" response, add
     * {@code withEphemeral(true)}, or to directly edit it, {@link #edit() edit().withEphemeral(true)}.
     * <p>
     * After calling {@code deferEdit}, you are not allowed to call other acknowledge, reply or edit method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @return a {@link InteractionCallbackSpecDeferEditMono} where, upon successful completion, emits nothing;
     * acknowledging the interaction and indicating a response will be edited later. If an error is received, it is
     * emitted through it.
     */
    public InteractionCallbackSpecDeferEditMono deferEdit() {
        return InteractionCallbackSpecDeferEditMono.of(this);
    }

    /**
     * Acknowledge the interaction by indicating a message will be edited later. For component interactions, the user
     * <strong>does not</strong> see a loading state.
     * <p>
     * After calling {@code deferEdit}, you are not allowed to call other acknowledge, reply or edit method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @param spec an immutable object that specifies how to build the reply message to the interaction
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction and
     * indicating a response will be edited later. The user sees a loading state. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> deferEdit(InteractionCallbackSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> createInteractionResponse(
                InteractionResponseType.DEFERRED_UPDATE_MESSAGE, spec.asRequest()));
    }

    /**
     * Requests to respond to the interaction by editing the message that originated this component interaction.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link InteractionApplicationCommandCallbackSpec} to be
     * operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit()}, {@link #edit(InteractionApplicationCommandCallbackSpec)}  which offer an
     * immutable approach to build specs
     */
    @Deprecated
    public Mono<Void> edit(Consumer<? super LegacyInteractionApplicationCommandCallbackSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyInteractionApplicationCommandCallbackSpec mutatedSpec =
                            new LegacyInteractionApplicationCommandCallbackSpec();

                    getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .ifPresent(mutatedSpec::setAllowedMentions);

                    spec.accept(mutatedSpec);

                    return createInteractionResponse(InteractionResponseType.UPDATE_MESSAGE, mutatedSpec.asRequest());
                });
    }

    /**
     * Requests to respond to the interaction by editing the message that originated this component interaction.
     * Properties specifying how to edit the message can be set via the {@code withXxx} methods of the returned
     * {@link InteractionApplicationCommandCallbackEditMono}.
     * <p>
     * After calling {@code edit}, you are not allowed to call other acknowledging, reply or edit method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @return A {@link InteractionApplicationCommandCallbackEditMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackEditMono edit() {
        return InteractionApplicationCommandCallbackEditMono.of(this);
    }

    /**
     * Requests to respond to the interaction by editing the message that originated this component interaction.
     * Properties specifying how to edit the message can be set via the {@code withXxx} methods of the returned
     * {@link InteractionApplicationCommandCallbackEditMono}.
     * <p>
     * After calling {@code edit}, you are not allowed to call other acknowledging, reply or edit method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @return A {@link InteractionApplicationCommandCallbackEditMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackEditMono edit(String content) {
        return edit().withContent(content);
    }

    /**
     * Requests to respond to the interaction by editing the message that originated this component interaction.
     * <p>
     * After calling {@code edit}, you are not allowed to call other acknowledging, reply or edit method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @param spec an immutable object that specifies how to edit the message the button is on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> edit(InteractionApplicationCommandCallbackSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> {
                    InteractionApplicationCommandCallbackSpec actualSpec = getClient().getRestClient()
                            .getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentions)
                            .orElse(spec);

                    return createInteractionResponse(InteractionResponseType.UPDATE_MESSAGE, actualSpec.asRequest());
                });
    }

    /**
     * Acknowledge the interaction by indicating a message will be edited later. For component interactions, the user
     * <strong>does not</strong> see a loading state.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction and
     * indicating a response will be edited later. The user sees a loading state. If an error is received, it is emitted
     * through the {@code Mono}.
     * @deprecated for components, migrate to {@link #deferEdit()}
     */
    @Override
    public Mono<Void> acknowledge() {
        return createInteractionResponse(InteractionResponseType.DEFERRED_UPDATE_MESSAGE, null);
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. For component interactions, the user
     * <strong>does not</strong> see a loading state.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing, acknowledging the interaction
     * and indicating a response will be edited later. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated for components, migrate to {@link #deferEdit() deferEdit().withEphemeral(true)}
     */
    @Override
    public Mono<Void> acknowledgeEphemeral() {
        InteractionApplicationCommandCallbackData data = InteractionApplicationCommandCallbackData.builder()
                .flags(Message.Flag.EPHEMERAL.getFlag())
                .build();

        return createInteractionResponse(InteractionResponseType.DEFERRED_UPDATE_MESSAGE, data);
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
