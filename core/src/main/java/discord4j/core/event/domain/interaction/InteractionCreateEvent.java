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
import discord4j.core.event.domain.Event;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.*;
import discord4j.core.spec.legacy.LegacyInteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Dispatched when a user in a guild interacts with an application command or component.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#interaction-create">Interaction Create</a>
 * <p>
 * <img src="doc-files/InteractionCreateEvent.png">
 */
@Experimental
public class InteractionCreateEvent extends Event {

    private final Interaction interaction;
    private final EventInteractionResponse response;

    public InteractionCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo);
        this.interaction = interaction;
        this.response = new EventInteractionResponse(getClient().rest(), interaction.getData());
    }

    /**
     * Gets the {@link discord4j.core.object.command.Interaction} associated with the event.
     *
     * @return The {@link discord4j.core.object.command.Interaction} associated with the event.
     */
    public Interaction getInteraction() {
        return interaction;
    }

    protected Mono<Void> createInteractionResponse(InteractionResponseType responseType,
                                                   @Nullable InteractionApplicationCommandCallbackData data) {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(responseType.getValue())
                .data(data == null ? Possible.absent() : Possible.of(data))
                .build();

        long id = interaction.getId().asLong();
        String token = interaction.getToken();

        return getClient().rest().getInteractionService()
                .createInteractionResponse(id, token, responseData);
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
        return createInteractionResponse(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE, null);
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. The user sees a loading state, visible
     * to all participants in the invoking channel. For an "only you can see this" response, add
     * {@code withEphemeral(true)}, or to include a message, {@link #reply(String) reply(String).withEphemeral(true)}
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
     *
     * @return A {@link InteractionApplicationCommandCallbackReplyMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackReplyMono reply() {
        return InteractionApplicationCommandCallbackReplyMono.of(this);
    }

    /**
     * Requests to respond to the interaction with a message initialized with the specified content. Properties
     * specifying how to build the reply message to the interaction can be set via the {@code withXxx} methods of the
     * returned {@link InteractionApplicationCommandCallbackReplyMono}.
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
                            .map(spec::withAllowedMentions)
                            .orElse(spec);

                    return createInteractionResponse(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE, actualSpec.asRequest());
                });
    }

    public Mono<Message> editReply(String content) {
        return editReply().withContentOrNull(content);
    }

    /**
     * Edits the initial reply sent when accepting this interaction. Properties specifying how to build the edit message
     * request can be set via the {@code withXxx} methods of the returned {@link InteractionReplyEditMono}.
     *
     * @return a {@link InteractionReplyEditMono} where, upon successful completion, emits the updated message. If an
     * error is received, it is emitted through the {@code InteractionReplyEditMono}.
     */
    public InteractionReplyEditMono editReply() {
        return InteractionReplyEditMono.of(this);
    }

    /**
     * Edits the initial reply sent when accepting this interaction with the given spec contents.
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
                            .map(spec::withAllowedMentionsOrNull)
                            .orElse(spec);
                    return getInteractionResponse().editFollowupMessage(messageId.asLong(), actualSpec.asRequest());
                })
                .map(data -> new Message(getClient(), data));
    }

    /**
     * Returns a REST-only handler for common operations related to an interaction response associated with this event.
     *
     * @return a handler aggregating a collection of REST API methods to work with an interaction response
     * @see InteractionCreateEvent#editReply()
     * @see InteractionCreateEvent#getReply()
     * @see InteractionCreateEvent#deleteReply()
     * @see InteractionCreateEvent#createFollowup()
     * @see InteractionCreateEvent#editFollowup(Snowflake)
     */
    public InteractionResponse getInteractionResponse() {
        return response;
    }

    static class EventInteractionResponse implements InteractionResponse {

        private final RestClient restClient;
        private final InteractionData interactionData;
        private final long applicationId;

        EventInteractionResponse(RestClient restClient, InteractionData interactionData) {
            this.restClient = restClient;
            this.interactionData = interactionData;
            this.applicationId = Snowflake.asLong(interactionData.applicationId());
        }

        @Override
        public Mono<MessageData> getInitialResponse() {
            return restClient.getWebhookService()
                    .getWebhookMessage(applicationId, interactionData.token(), "@original");
        }

        @Override
        public Mono<MessageData> editInitialResponse(WebhookMessageEditRequest request) {
            return restClient.getWebhookService()
                    .modifyWebhookMessage(applicationId, interactionData.token(), "@original", request);
        }

        @Override
        public Mono<MessageData> editInitialResponse(MultipartRequest<WebhookMessageEditRequest> request) {
            return restClient.getWebhookService()
                    .modifyWebhookMessage(applicationId, interactionData.token(), "@original", request);
        }

        @Override
        public Mono<Void> deleteInitialResponse() {
            return restClient.getWebhookService()
                    .deleteWebhookMessage(applicationId, interactionData.token(), "@original");
        }

        @Override
        public Mono<MessageData> createFollowupMessage(String content) {
            FollowupMessageRequest body = FollowupMessageRequest.builder().content(content).build();
            return restClient.getWebhookService()
                    .executeWebhook(applicationId, interactionData.token(), true, MultipartRequest.ofRequest(body));
        }

        @Override
        public Mono<MessageData> createFollowupMessage(MultipartRequest<? extends WebhookExecuteRequest> request) {
            return restClient.getWebhookService()
                    .executeWebhook(applicationId, interactionData.token(), true, request);
        }

        @Override
        public Mono<MessageData> createFollowupMessageEphemeral(String content) {
            FollowupMessageRequest body = FollowupMessageRequest.builder()
                    .content(content)
                    .flags(Message.Flag.EPHEMERAL.getFlag())
                    .build();
            return restClient.getWebhookService()
                    .executeWebhook(applicationId, interactionData.token(), true, MultipartRequest.ofRequest(body));
        }

        @Override
        public Mono<MessageData> createFollowupMessageEphemeral(MultipartRequest<WebhookExecuteRequest> request) {
            FollowupMessageRequest newBody = FollowupMessageRequest.builder()
                    .from(request.getJsonPayload())
                    .flags(Message.Flag.EPHEMERAL.getFlag())
                    .build();
            return restClient.getWebhookService()
                    .executeWebhook(applicationId, interactionData.token(), true, MultipartRequest.ofRequest(newBody));
        }

        @Override
        public Mono<MessageData> editFollowupMessage(long messageId, WebhookMessageEditRequest request, boolean wait) {
            return restClient.getWebhookService()
                    .modifyWebhookMessage(applicationId, interactionData.token(), String.valueOf(messageId), request);
        }

        @Override
        public Mono<MessageData> editFollowupMessage(long messageId,
                                                     MultipartRequest<WebhookMessageEditRequest> request) {
            return restClient.getWebhookService()
                    .modifyWebhookMessage(applicationId, interactionData.token(), String.valueOf(messageId), request);
        }

        @Override
        public Mono<Void> deleteFollowupMessage(long messageId) {
            return restClient.getWebhookService()
                    .deleteWebhookMessage(applicationId, interactionData.token(), String.valueOf(messageId));
        }
    }
}
