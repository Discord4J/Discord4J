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
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.*;
import discord4j.gateway.ShardInfo;
import discord4j.rest.RestClient;
import discord4j.rest.interaction.InteractionResponse;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.WebhookMultipartRequest;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
     * to all participants in the invoking channel. For a "only you can see this" response, see
     * {@link #acknowledgeEphemeral()}, or to include a message, {@link #replyEphemeral(String)}
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; acknowledging the interaction
     * and indicating a response will be edited later. The user sees a loading state. If an error is received, it
     * is emitted through the {@code Mono}.
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
    public Mono<Void> replyEphemeral(final String content) {
        return reply(spec -> spec.setContent(content).setEphemeral(true));
    }

    /**
     * Requests to respond to the interaction with a message.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link InteractionApplicationCommandCallbackSpec} to be
     * operated on.
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
                    return createInteractionResponse(InteractionResponseData.builder()
                            .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                            .data(mutatedSpec.asRequest())
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
        private final Mono<Long> applicationId;

        EventInteractionResponse(RestClient restClient, InteractionData interactionData) {
            this.restClient = restClient;
            this.interactionData = interactionData;
            this.applicationId = restClient.getApplicationId()
                    .cache(__ -> Duration.ofMillis(Long.MAX_VALUE), e -> Duration.ZERO, () -> Duration.ZERO);
        }

        @Override
        public Mono<MessageData> editInitialResponse(WebhookMessageEditRequest request) {
            return applicationId.flatMap(id -> restClient.getWebhookService()
                    .modifyWebhookMessage(id, interactionData.token(), "@original", request));
        }

        @Override
        public Mono<Void> deleteInitialResponse() {
            return applicationId.flatMap(id -> restClient.getWebhookService()
                    .deleteWebhookMessage(id, interactionData.token(), "@original"));
        }

        @Override
        public Mono<MessageData> createFollowupMessage(String content) {
            WebhookExecuteRequest body = WebhookExecuteRequest.builder().content(content).build();
            WebhookMultipartRequest request = new WebhookMultipartRequest(body);
            return applicationId.flatMap(id -> restClient.getWebhookService()
                    .executeWebhook(id, interactionData.token(), true, request));
        }

        @Override
        public Mono<MessageData> createFollowupMessage(WebhookMultipartRequest request, boolean wait) {
            return applicationId.flatMap(id -> restClient.getWebhookService()
                    .executeWebhook(id, interactionData.token(), wait, request));
        }

        @Override
        public Mono<MessageData> editFollowupMessage(long messageId, WebhookMessageEditRequest request, boolean wait) {
            return applicationId.flatMap(id -> restClient.getWebhookService()
                    .modifyWebhookMessage(id, interactionData.token(), String.valueOf(messageId), request));
        }

        @Override
        public Mono<Void> deleteFollowupMessage(long messageId) {
            return applicationId.flatMap(id -> restClient.getWebhookService()
                    .deleteWebhookMessage(id, interactionData.token(), String.valueOf(messageId)));
        }
    }
}
