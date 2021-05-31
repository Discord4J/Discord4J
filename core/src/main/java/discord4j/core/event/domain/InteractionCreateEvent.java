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

package discord4j.core.event.domain;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionApplicationCommandCallbackMono;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.legacy.LegacyInteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.*;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Dispatched when a user in a guild uses a Slash Command.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#interaction-create">Interaction Create</a>
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

    private ApplicationCommandInteractionData getCommandInteractionData() {
        return interaction.getData().data().get();
    }

    /**
     * Gets the id of the invoked command.
     *
     * @return The id of the invoked command.
     */
    public Snowflake getCommandId() {
        return Snowflake.of(getCommandInteractionData().id());
    }

    /**
     * Gets the name of the invoked command.
     *
     * @return The name of the invoked command.
     */
    public String getCommandName() {
        return getCommandInteractionData().name();
    }

    private Mono<Void> createInteractionResponse(InteractionResponseData responseData) {
        long id = interaction.getId().asLong();
        String token = interaction.getToken();

        return getClient().rest().getInteractionService()
                .createInteractionResponse(id, token, responseData);
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. The user sees a loading state, visible
     * to all participants in the invoking channel. For a "only you can see this" response, see {@link
     * #acknowledgeEphemeral()}, or to include a message, {@link #reply(String) reply(String).withEphemeral(true)}
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction and
     * indicating a response will be edited later. The user sees a loading state. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    public Mono<Void> acknowledge() {
        return createInteractionResponse(InteractionResponseData.builder()
                .type(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build());
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. Only the invoking user sees a loading
     * state.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing, acknowledging the interaction
     * and indicating a response will be edited later. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> acknowledgeEphemeral() {
        return createInteractionResponse(InteractionResponseData.builder()
                .type(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder()
                        .flags(Message.Flag.EPHEMERAL.getFlag())
                        .build())
                .build());
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
                    return createInteractionResponse(InteractionResponseData.builder()
                            .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                            .data(mutatedSpec.asRequest())
                            .build());
                });
    }

    /**
     * Requests to respond to the interaction with a message. Properties specifying how to build the reply message to
     * the interaction can be set via the {@code withXxx} methods of the returned {@link
     * InteractionApplicationCommandCallbackMono}.
     *
     * @return A {@link InteractionApplicationCommandCallbackMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackMono reply() {
        return InteractionApplicationCommandCallbackMono.of(this);
    }

    /**
     * Requests to respond to the interaction with a message initialized with the specified content. Properties
     * specifying how to build the reply message to the interaction can be set via the {@code withXxx} methods of the
     * returned {@link InteractionApplicationCommandCallbackMono}.
     *
     * @param content a string to populate the message with
     * @return A {@link InteractionApplicationCommandCallbackMono} where, upon successful completion, emits nothing;
     * indicating the interaction response has been sent. If an error is received, it is emitted through the {@code
     * InteractionApplicationCommandCallbackMono}.
     */
    public InteractionApplicationCommandCallbackMono reply(final String content) {
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
                    return createInteractionResponse(InteractionResponseData.builder()
                            .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                            .data(actualSpec.asRequest())
                            .build());
                });
    }

    /**
     * Gets a handler for common operations related to an interaction followup response associated with this event.
     *
     * @return A handler for common operations related to an interaction followup response associated with this event.
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
        public Mono<MessageData> editInitialResponse(WebhookMessageEditRequest request) {
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
            WebhookExecuteRequest body = WebhookExecuteRequest.builder().content(content).build();
            return restClient.getWebhookService()
                    .executeWebhook(applicationId, interactionData.token(), true, MultipartRequest.ofRequest(body));
        }

        @Override
        public Mono<MessageData> createFollowupMessage(MultipartRequest<WebhookExecuteRequest> request) {
            return restClient.getWebhookService()
                    .executeWebhook(applicationId, interactionData.token(), true, request);
        }

        @Override
        public Mono<MessageData> editFollowupMessage(long messageId, WebhookMessageEditRequest request, boolean wait) {
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
