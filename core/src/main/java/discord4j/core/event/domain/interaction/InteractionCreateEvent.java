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
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Dispatched when a user in a guild interacts with an application command, component, or other interaction based UI
 * element. It is recommended you use a subclass in your event listeners to access interaction-specific methods.
 * See a diagram below for the current event hierarchy for interactions.
 * <p>
 * You should use one of the following interaction-specific events to access interaction-specific methods:
 * <ul>
 *     <li>{@link ChatInputInteractionEvent} dispatched when a user types a chat input (slash) command</li>
 *     <li>{@link UserInteractionEvent} dispatched when a user uses a context menu command on a user</li>
 *     <li>{@link MessageInteractionEvent} dispatched when a user uses a context menu command on a message</li>
 *     <li>{@link ButtonInteractionEvent} dispatched when a user clicks a button component</li>
 *     <li>{@link SelectMenuInteractionEvent} dispatched when a user confirms a selection in a select menu component</li>
 *     <li>{@link ChatInputAutoCompleteEvent} dispatched when a user starts chat command auto complete</li>
 *     <li>{@link ModalSubmitInteractionEvent} dispatched when a user submits a previously presented modal</li>
 * </ul>
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

    public InteractionCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Interaction interaction) {
        super(gateway, shardInfo);
        this.interaction = interaction;
    }

    /**
     * Gets the {@link discord4j.core.object.command.Interaction} associated with the event.
     *
     * @return The {@link discord4j.core.object.command.Interaction} associated with the event.
     */
    public Interaction getInteraction() {
        return interaction;
    }

    /**
     * Gets the {@link discord4j.core.object.entity.User} associated with the event.
     * The User is retrieved from the {@link #getInteraction} method.
     * 
     * @return The {@link discord4j.core.object.entity.User} associated with the event.
     */
    public User getUser() {
        return interaction.getUser();
    }

    @Deprecated
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

    protected Mono<Void> createInteractionResponse(InteractionResponseType responseType,
                                                   MultipartRequest<InteractionApplicationCommandCallbackData> data) {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(responseType.getValue())
                .data(Possible.of(data.getJsonPayload()))
                .build();

        long id = interaction.getId().asLong();
        String token = interaction.getToken();

        return getClient().rest().getInteractionService()
                .createInteractionResponse(id, token, data.withRequest(responseData));
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
