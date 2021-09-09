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
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.WebhookMultipartRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.function.Consumer;

/**
 * Dispatched when a user in a guild uses a Slash Command or clicks a Button.
 *
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#interaction-create">Interaction Create</a>
 *
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

    protected Mono<Void> createInteractionResponse(InteractionResponseType responseType, @Nullable InteractionApplicationCommandCallbackData data) {
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
     * to all participants in the invoking channel. For a "only you can see this" response, see
     * {@link #acknowledgeEphemeral()}, or to include a message, {@link #replyEphemeral(String)}
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction
     * and indicating a response will be edited later. The user sees a loading state. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> acknowledge() {
        return createInteractionResponse(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE, null);
    }

    /**
     * Acknowledges the interaction indicating a response will be edited later. Only the invoking user sees a loading
     * state.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing, acknowledging the interaction
     * and indicating a response will be edited later. If an error is received, it is emitted through the {@code Mono}.
     */
    // TODO: with new specs, this could be acknowledge().ephemeral() instead
    public Mono<Void> acknowledgeEphemeral() {
        InteractionApplicationCommandCallbackData data = InteractionApplicationCommandCallbackData.builder()
            .flags(Message.Flag.EPHEMERAL.getFlag())
            .build();

        return createInteractionResponse(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE, data);
    }

    /**
     * Requests to respond to the interaction with only
     * {@link InteractionApplicationCommandCallbackSpec#setContent(String) content}.
     *
     * @param content A string message to populate the message with.
     * @return A {@link Mono} where, upon successful completion, emits nothing, indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     * @see InteractionApplicationCommandCallbackSpec#setContent(String)
     */
    public Mono<Void> reply(final String content) {
        return reply(spec -> spec.setContent(content));
    }

    /**
     * Requests to respond to the interaction with only
     * {@link InteractionApplicationCommandCallbackSpec#setContent(String) content} and
     * {@link InteractionApplicationCommandCallbackSpec#setEphemeral(boolean) ephemeral set to true}. Only the invoking
     * user can see it.
     *
     * @param content A string message to populate the message with.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the ephemeral interaction
     * response has been sent. If an error is received, it is emitted through the {@code Mono}.
     * @see InteractionApplicationCommandCallbackSpec#setContent(String)
     * @see InteractionApplicationCommandCallbackSpec#setEphemeral(boolean)
     */
    // TODO: with new specs, this could be reply().ephemeral() instead
    public Mono<Void> replyEphemeral(final String content) {
        return reply(spec -> spec.setContent(content).setEphemeral(true));
    }

    /**
     * Requests to respond to the interaction with a message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link InteractionApplicationCommandCallbackSpec} to be
     *             operated on.
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the interaction response has
     * been sent. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> reply(final Consumer<? super InteractionApplicationCommandCallbackSpec> spec) {
        return Mono.defer(
            () -> {
                InteractionApplicationCommandCallbackSpec mutatedSpec =
                    new InteractionApplicationCommandCallbackSpec();

                getClient().getRestClient().getRestResources()
                    .getAllowedMentions()
                    .ifPresent(mutatedSpec::setAllowedMentions);

                spec.accept(mutatedSpec);

                return createInteractionResponse(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE, mutatedSpec.asRequest());
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
        public Mono<Void> deleteInitialResponse() {
            return restClient.getWebhookService()
                .deleteWebhookMessage(applicationId, interactionData.token(), "@original");
        }

        @Override
        public Mono<MessageData> createFollowupMessage(String content) {
            FollowupMessageRequest body = FollowupMessageRequest.builder().content(content).build();
            WebhookMultipartRequest request = new WebhookMultipartRequest(body);
            return restClient.getWebhookService()
                .executeWebhook(applicationId, interactionData.token(), true, request);
        }

        @Override
        public Mono<MessageData> createFollowupMessage(WebhookMultipartRequest request) {
            return restClient.getWebhookService()
                .executeWebhook(applicationId, interactionData.token(), true, request);
        }

        @Override
        public Mono<MessageData> createFollowupMessageEphemeral(String content) {
            FollowupMessageRequest body = FollowupMessageRequest.builder()
                .content(content)
                .flags(Message.Flag.EPHEMERAL.getFlag())
                .build();
            WebhookMultipartRequest request = new WebhookMultipartRequest(body);
            return restClient.getWebhookService()
                .executeWebhook(applicationId, interactionData.token(), true, request);
        }

        @Override
        public Mono<MessageData> createFollowupMessageEphemeral(WebhookMultipartRequest request) {
            FollowupMessageRequest newBody = FollowupMessageRequest.builder()
                .from(request.getExecuteRequest())
                .flags(Message.Flag.EPHEMERAL.getFlag())
                .build();
            WebhookMultipartRequest newRequest = new WebhookMultipartRequest(newBody, request.getFiles());

            return restClient.getWebhookService()
                .executeWebhook(applicationId, interactionData.token(), true, newRequest);
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
