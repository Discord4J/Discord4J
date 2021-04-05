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

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import discord4j.rest.util.InteractionResponseType;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Mono;

import java.util.Collections;

class InteractionOperations implements RestInteraction, InteractionResponse, GuildInteraction, DirectInteraction {

    private final RestClient restClient;
    private final InteractionData interactionData;
    private final long applicationId;
    private final InteractionMemberOperations memberOperations;

    InteractionOperations(RestClient restClient, InteractionData interactionData) {
        this.restClient = restClient;
        this.interactionData = interactionData;
        this.applicationId = Snowflake.asLong(interactionData.applicationId());
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
                .type(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder().build())
                .build();
        return new FollowupInteractionHandler(responseData, __ -> Mono.empty());
    }

    @Override
    public FollowupInteractionHandler acknowledgeEphemeral() {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(InteractionApplicationCommandCallbackData.builder()
                        .flags(1 << 6)
                        .build())
                .build();
        return new FollowupInteractionHandler(responseData, __ -> Mono.empty());
    }

    @Override
    public FollowupInteractionHandler reply(String content) {
        return reply(InteractionApplicationCommandCallbackData.builder()
                .content(content)
                .build());
    }

    @Override
    public FollowupInteractionHandler replyEphemeral(String content) {
        return reply(InteractionApplicationCommandCallbackData.builder()
                .content(content)
                .flags(6)
                .build());
    }

    @Override
    public FollowupInteractionHandler reply(InteractionApplicationCommandCallbackData callbackData) {
        InteractionResponseData responseData = InteractionResponseData.builder()
                .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getValue())
                .data(callbackData)
                .build();
        return new FollowupInteractionHandler(responseData, __ -> Mono.empty());
    }

    /////////////////////////////////////////////////////////////////
    // InteractionResponse

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
                .executeWebhook(applicationId, interactionData.token(), true,
                        MultipartRequest.ofRequestAndFiles(body, Collections.emptyList()));
    }

    @Override
    public Mono<MessageData> createFollowupMessage(MultipartRequest<WebhookExecuteRequest> request, boolean wait) {
        return restClient.getWebhookService()
                .executeWebhook(applicationId, interactionData.token(), wait, request);
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
