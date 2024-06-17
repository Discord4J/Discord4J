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
import discord4j.core.object.entity.Message;
import discord4j.core.spec.*;
import discord4j.core.spec.legacy.LegacyInteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.gateway.ShardInfo;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

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
 *     <li>{@link #deferReply()} to acknowledge without a message, typically to perform a background task and give the
 *     user a loading state until it is edited</li>
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
public class DeferrableInteractionEvent extends InteractionCreateEvent {
    private final EventInteractionResponse response;

    public DeferrableInteractionEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo, interaction);
        this.response = new EventInteractionResponse(getClient().rest(), interaction.getData());
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. The user sees a loading state, visible
     * to all participants in the invoking channel. For an "only you can see this" response, see {@link
     * #acknowledgeEphemeral()}, or to include a message, {@link #reply(String) reply(String).withEphemeral(true)}
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction and
     * indicating a response will be edited later. The user sees a loading state. If an error is received, it is emitted
     * through the {@code Mono}.
     * @deprecated use {@link #deferReply()} instead
     */
    @Deprecated
    public Mono<Void> acknowledge() {
        return createInteractionResponse(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE, (InteractionApplicationCommandCallbackData) null);
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. The user sees a loading state, visible
     * to all participants in the invoking channel. For an "only you can see this" response, add
     * {@code withEphemeral(true)}, or to include a message, {@link #reply(String) reply(String).withEphemeral(true)}.
     * <p>
     * After calling {@code deferReply}, you are not allowed to call other acknowledging or reply method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @return A {@link InteractionCallbackSpecDeferReplyMono} where, upon successful completion, emits nothing;
     * acknowledging the interaction and indicating a response will be edited later. The user sees a loading state. If
     * an error is received, it is emitted through it.
     */
    public InteractionCallbackSpecDeferReplyMono deferReply() {
        return InteractionCallbackSpecDeferReplyMono.of(this);
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. The user sees a loading state, visible
     * to all participants in the invoking channel.
     * <p>
     * After calling {@code deferReply}, you are not allowed to call other acknowledging or reply method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @param spec an immutable object that specifies how to build the reply message to the interaction
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction and
     * indicating a response will be edited later. The user sees a loading state. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> deferReply(InteractionCallbackSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> createInteractionResponse(
                InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE, spec.asRequest()));
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. Only the invoking user sees a loading
     * state.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing, acknowledging the interaction
     * and indicating a response will be edited later. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated migrate to {@link #deferReply() deferReply().withEphemeral(true)}
     */
    @Deprecated
    public Mono<Void> acknowledgeEphemeral() {
        InteractionApplicationCommandCallbackData data = InteractionApplicationCommandCallbackData.builder()
                .flags(Message.Flag.EPHEMERAL.getFlag())
                .build();

        return createInteractionResponse(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE, data);
    }

    /**
     * Requests to respond to the interaction with a message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyInteractionApplicationCommandCallbackSpec} to
     *             be operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #reply(InteractionApplicationCommandCallbackSpec)}, {@link #reply(String)} or {@link
     * #reply()} which offer an immutable approach to build specs
     */
    @Deprecated
    public Mono<Void> reply(final Consumer<? super LegacyInteractionApplicationCommandCallbackSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyInteractionApplicationCommandCallbackSpec mutatedSpec =
                            new LegacyInteractionApplicationCommandCallbackSpec();

                    getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .ifPresent(mutatedSpec::setAllowedMentions);

                    spec.accept(mutatedSpec);

                    return createInteractionResponse(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE,
                            mutatedSpec.asRequest());
                });
    }

    /**
     * Requests to respond to the interaction with a message. Properties specifying how to build the reply message to
     * the interaction can be set via the {@code withXxx} methods of the returned {@link
     * InteractionApplicationCommandCallbackReplyMono}.
     * <p>
     * For component interactions, like buttons or select menus, this method will create a <strong>new</strong> message.
     * If you want to modify the message the component is on, see {@link ComponentInteractionEvent#edit()} or
     * {@link ComponentInteractionEvent#deferEdit()}.
     * <p>
     * After calling {@code reply}, you are not allowed to call other acknowledging or reply method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @return A {@link InteractionApplicationCommandCallbackReplyMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackReplyMono reply() {
        return InteractionApplicationCommandCallbackReplyMono.of(this);
    }

    /**
     * Requests to respond to the interaction with a notification instructing the user that this interaction requires a premium subscription.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has been sent. If an error is received, it is emitted through the {@code Mono}.
     * @deprecated in favor of using {@link discord4j.core.object.component.Button#premium(Snowflake)}. This will continue to function but may be eventually unsupported
     */
    @Experimental // This method could not be tested due to the lack of a Discord verified application
    @Deprecated
    public Mono<Void> replyWithPremiumRequired() {
        return Mono.defer(() -> createInteractionResponse(InteractionResponseType.PREMIUM_REQUIRED, MultipartRequest.ofRequest(InteractionApplicationCommandCallbackData.builder().build())));
    }

    /**
     * Requests to respond to the interaction with a message initialized with the specified content. Properties
     * specifying how to build the reply message to the interaction can be set via the {@code withXxx} methods of the
     * returned {@link InteractionApplicationCommandCallbackReplyMono}.
     * <p>
     * For component interactions, like buttons or select menus, this method will create a <strong>new</strong> message.
     * If you want to modify the message the component is on, see {@link ComponentInteractionEvent#edit()} or
     * {@link ComponentInteractionEvent#deferEdit()}.
     * <p>
     * After calling {@code reply}, you are not allowed to call other acknowledging or reply method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @param content a string to populate the message with
     * @return A {@link InteractionApplicationCommandCallbackReplyMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackReplyMono reply(final String content) {
        return reply().withContent(content);
    }

    /**
     * Requests to respond to the interaction with a message.
     * <p>
     * For component interactions, like buttons or select menus, this method will create a <strong>new</strong> message.
     * If you want to modify the message the component is on, see {@link ComponentInteractionEvent#edit()} or
     * {@link ComponentInteractionEvent#deferEdit()}.
     * <p>
     * After calling {@code reply}, you are not allowed to call other acknowledging or reply method and have to
     * either work with the initial reply using {@link #getReply()}, {@link #editReply()}, {@link #deleteReply()}, or
     * using followup messages with {@link #createFollowup()}, {@link #editFollowup(Snowflake)} or
     * {@link #deleteFollowup(Snowflake)}.
     *
     * @param spec an immutable object that specifies how to build the reply message to the interaction
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> reply(InteractionApplicationCommandCallbackSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> {
                    InteractionApplicationCommandCallbackSpec actualSpec = getClient().getRestClient()
                            .getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentions)
                            .orElse(spec);

                    return createInteractionResponse(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE, actualSpec.asRequest());
                });
    }

    /**
     * Requests to respond to the interaction by presenting a modal for the user to fill out and submit.
     * Once the user submits the modal, it will be received as a new {@link ModalSubmitInteractionEvent}. Properties
     * specifying how to build the modal can be set via the {@code withXxx} methods of the returned
     * {@link InteractionPresentModalMono}.
     *
     * @return A {@link InteractionPresentModalMono} where, upon successful completion, emits nothing; indicating the
     * interaction response has been sent. If an error is received, it is emitted through the
     * {@code InteractionPresentModalMono}.
     */
    public InteractionPresentModalMono presentModal() {
        return InteractionPresentModalMono.of(this);
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
        return presentModal().withTitle(title).withCustomId(customId).withComponents(components);
    }

    /**
     * Requests to respond to the interaction by presenting a modal for the user to fill out and submit with the given
     * spec contents. Once the user submits the modal, it will be received as a new {@link ModalSubmitInteractionEvent}.
     *
     * @param spec an immutable object that specifies how to present the modal window
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> presentModal(InteractionPresentModalSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> createInteractionResponse(InteractionResponseType.MODAL, spec.asRequest()));
    }

    /**
     * Edits the initial reply sent when accepting this interaction with the given message content.
     * <p>
     * For component interactions, like buttons or select menus, this method modifies the message depending on the
     * initial response method chosen: if {@link #deferReply()} or {@link #reply()} was used, the <strong>new</strong>
     * message created with the reply; if {@link ComponentInteractionEvent#edit()} or
     * {@link ComponentInteractionEvent#deferEdit()} was used, this method will modify the message the component is on.
     *
     * @param content a string to update the message with
     * @return a {@link InteractionReplyEditMono} where, upon successful completion, emits the updated message. If an
     * error is received, it is emitted through the {@code InteractionReplyEditMono}.
     */
    public InteractionReplyEditMono editReply(String content) {
        return editReply().withContentOrNull(content);
    }

    /**
     * Edits the initial reply sent when accepting this interaction. Properties specifying how to build the edit message
     * request can be set via the {@code withXxx} methods of the returned {@link InteractionReplyEditMono}.
     * <p>
     * For component interactions, like buttons or select menus, this method modifies the message depending on the
     * initial response method chosen: if {@link #deferReply()} or {@link #reply()} was used, the <strong>new</strong>
     * message created with the reply; if {@link ComponentInteractionEvent#edit()} or
     * {@link ComponentInteractionEvent#deferEdit()} was used, this method will modify the message the component is on.
     * <p>
     * By default, this method will append any file added through {@code withFiles}. To replace or remove individual
     * attachments, use {@code withAttachments} along with {@link discord4j.core.object.entity.Attachment} objects from
     * the original message you want to keep. It is not required to include the new files as {@code Attachment} objects.
     * <p>
     * For example, to replace all previous attachments, provide an empty {@code withAttachments} and your files:
     * <pre>{@code
     *  event.editReply()
     *     .withContentOrNull("Replaced all attachments")
     *     .withFiles(getFile())
     *     .withComponents(row)
     *     .withAttachments();
     * }</pre>
     * <p>
     * To replace a specific attachment, you need to pass the attachment details you want to keep. You could work from
     * the original {@link Message#getAttachments()} list and pass it to {@code withAttachments} and your files.
     * The following example removes only the first attachment:
     * <pre>{@code
     *  event.getReply()
     *     .flatMap(reply -> event.editReply()
     *             .withContentOrNull("Replaced the first attachment")
     *             .withFiles(getFile())
     *             .withComponents(row)
     *             .withAttachmentsOrNull(reply.getAttachments()
     *                     .stream()
     *                     .skip(1)
     *                     .collect(Collectors.toList())));
     * }</pre>
     * <p>
     * To clear all attachments, provide an empty {@code withAttachments}:
     * <pre>{@code
     *  event.editReply()
     *     .withContentOrNull("Removed all attachments")
     *     .withComponents(row)
     *     .withAttachments();
     * }</pre>
     *
     * @return a {@link InteractionReplyEditMono} where, upon successful completion, emits the updated message. If an
     * error is received, it is emitted through the {@code InteractionReplyEditMono}.
     */
    public InteractionReplyEditMono editReply() {
        return InteractionReplyEditMono.of(this);
    }

    /**
     * Edits the initial reply sent when accepting this interaction with the given spec contents.
     * <p>
     * For component interactions, like buttons or select menus, this method modifies the message depending on the
     * initial response method chosen: if {@link #deferReply()} or {@link #reply()} was used, the <strong>new</strong>
     * message created with the reply; if {@link ComponentInteractionEvent#edit()} or
     * {@link ComponentInteractionEvent#deferEdit()} was used, this method will modify the message the component is on.
     * <p>
     * By default, this method will append any file added through {@code withFiles}. To replace or remove individual
     * attachments, use {@code withAttachment} along with {@link discord4j.core.object.entity.Attachment} objects from
     * the original message you want to keep. It is not required to include the new files as {@code Attachment} objects.
     * <p>
     * See the docs for {@link #editReply()} for examples and adapt them to a standalone spec.
     *
     * @param spec an immutable object that specifies how to edit the initial reply
     * @return a {@link Mono} where, upon successful completion, emits the updated message. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    public Mono<Message> editReply(InteractionReplyEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> {
                    InteractionReplyEditSpec actualSpec = getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentionsOrNull)
                            .orElse(spec);
                    return getInteractionResponse().editInitialResponse(actualSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Returns the initial reply to this interaction.
     *
     * @return a {@link Mono} where, upon successful completion, emits the initial reply message. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> getReply() {
        return getInteractionResponse().getInitialResponse().map(data -> new Message(getClient(), data));
    }

    /**
     * Deletes the initial reply to this interaction.
     *
     * @return a {@link Mono} where, upon successful initial reply deletion, emits nothing indicating completion. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> deleteReply() {
        return getInteractionResponse().deleteInitialResponse();
    }

    /**
     * Creates a follow-up message to this interaction. Properties specifying how to build the follow-up message can be
     * set via the {@code withXxx} methods of the returned {@link InteractionFollowupCreateMono}.
     *
     * @return a {@link InteractionFollowupCreateMono} where, upon successful completion, emits the resulting follow-up
     * message. If an error is received, it is emitted through the {@code InteractionApplicationCommandCallbackMono}.
     */
    public InteractionFollowupCreateMono createFollowup() {
        return InteractionFollowupCreateMono.of(this);
    }

    /**
     * Creates a follow-up message to this interaction with the given message content.
     *
     * @param content a string to populate the followup message with
     * @return a {@link InteractionFollowupCreateMono} where, upon successful completion, emits the resulting follow-up
     * message. If an error is received, it is emitted through the {@code InteractionApplicationCommandCallbackMono}.
     */
    public InteractionFollowupCreateMono createFollowup(String content) {
        return createFollowup().withContent(content);
    }

    /**
     * Creates a follow-up message to this interaction.
     *
     * @param spec an immutable object that specifies how to build the follow-up message
     * @return a {@link Mono} where, upon successful completion, emits the resulting follow-up message. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> createFollowup(InteractionFollowupCreateSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> {
                    InteractionFollowupCreateSpec actualSpec = getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentions)
                            .orElse(spec);
                    return getInteractionResponse().createFollowupMessage(actualSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Edits a follow-up message to this interaction. Properties specifying how to edit the follow-up message can be
     * set via the {@code withXxx} methods of the returned {@link InteractionFollowupEditMono}.
     *
     * @param messageId the follow-up message ID to edit
     * @return a {@link InteractionFollowupEditMono} where, upon successful completion, emits the updated follow-up
     * message. If an error is received, it is emitted through the {@code InteractionFollowupEditMono}.
     */
    public InteractionFollowupEditMono editFollowup(Snowflake messageId) {
        return InteractionFollowupEditMono.of(messageId, this);
    }

    /**
     * Edits a follow-up message to this interaction.
     *
     * @param messageId the follow-up message ID to edit
     * @param spec an immutable object that specifies how to build the edited follow-up message
     * @return a {@link Mono} where, upon successful completion, emits the updated follow-up message. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> editFollowup(final Snowflake messageId, InteractionReplyEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(() -> {
                    InteractionReplyEditSpec actualSpec = getClient().getRestClient().getRestResources()
                            .getAllowedMentions()
                            .filter(allowedMentions -> !spec.isAllowedMentionsPresent())
                            .map(spec::withAllowedMentionsOrNull)
                            .orElse(spec);
                    return getInteractionResponse().editFollowupMessage(messageId.asLong(), actualSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Delete a followup message created under this interaction.
     *
     * @param messageId the message ID to be deleted
     * @return a {@link Mono} where, upon successful message deletion, returns a completion signal. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> deleteFollowup(final Snowflake messageId) {
        return getInteractionResponse().deleteFollowupMessage(messageId.asLong());
    }

    /**
     * Returns a REST-only handler for common operations related to an interaction response associated with this event.
     *
     * @return a handler aggregating a collection of REST API methods to work with an interaction response
     * @see #editReply()
     * @see #getReply()
     * @see #deleteReply()
     * @see #createFollowup()
     * @see #editFollowup(Snowflake)
     * @see #deleteFollowup(Snowflake)
     */
    public InteractionResponse getInteractionResponse() {
        return response;
    }
}
