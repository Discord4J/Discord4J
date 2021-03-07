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

package discord4j.rest.interaction;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.WebhookMultipartRequest;
import reactor.core.publisher.Mono;

/**
 * A default implementation for REST-based interaction handling and response.
 */
@Experimental
public final class InteractionOperations implements RestInteraction, InteractionResponse,
        GuildInteraction, DirectInteraction {

    private final RestClient restClient;
    private final InteractionData interactionData;
    private final Mono<Long> applicationId;
    private final InteractionMemberOperations memberOperations;

    /**
     * Create an interaction operations object with the given parameters.
     *
     * @param restClient a REST client for interacting with Discord
     * @param interactionData the data representing the incoming interaction
     * @param applicationId a {@link Mono} supplier for the application ID
     */
    public InteractionOperations(RestClient restClient, InteractionData interactionData, Mono<Long> applicationId) {
        this.restClient = restClient;
        this.interactionData = interactionData;
        this.applicationId = applicationId;
        this.memberOperations = new InteractionMemberOperations(restClient, interactionData);
    }

    /////////////////////////////////////////////////////////////////
    // GuildInteraction

    @Override
    public Snowflake getGuildId() {
        return Snowflake.of(interactionData.guildId().get());
    }

    @Override
    public InteractionMember getInteractionMember() {
        return memberOperations;
    }

    /////////////////////////////////////////////////////////////////
    // DirectInteraction

    @Override
    public UserData getUserData() {
        return interactionData.user().get();
    }

    /////////////////////////////////////////////////////////////////
    // Interaction

    @Override
    public InteractionData getData() {
        return interactionData;
    }

    @Override
    public Snowflake getId() {
        return Snowflake.of(interactionData.id());
    }

    @Override
    public Snowflake getChannelId() {
        // TODO: will need a different handling if this starts being absent
        return Snowflake.of(interactionData.channelId().get());
    }

    @Override
    public ApplicationCommandInteractionData getCommandInteractionData() {
        return interactionData.data().get();
    }

    @Override
    public FollowupInteractionHandler acknowledge() {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(InteractionResponseType.ACKNOWLEDGE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build();
        return new FollowupInteractionHandler(responseData, __ -> Mono.empty());
    }

    @Override
    public FollowupInteractionHandler acknowledge(boolean withSource) {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(withSource ? InteractionResponseType.ACKNOWLEDGE_WITH_SOURCE.getValue() :
                        InteractionResponseType.ACKNOWLEDGE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build();
        return new FollowupInteractionHandler(responseData, __ -> Mono.empty());
    }

    @Override
    public FollowupInteractionHandler reply(String content, boolean withSource) {
        return reply(InteractionApplicationCommandCallbackData.builder().content(content).build(), withSource);
    }

    @Override
    public FollowupInteractionHandler reply(InteractionApplicationCommandCallbackData callbackData,
                                            boolean withSource) {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(withSource ? InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue() :
                        InteractionResponseType.CHANNEL_MESSAGE.getValue())
                .data(callbackData)
                .build();
        return new FollowupInteractionHandler(responseData, __ -> Mono.empty());
    }

    /////////////////////////////////////////////////////////////////
    // InteractionResponse

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
}
