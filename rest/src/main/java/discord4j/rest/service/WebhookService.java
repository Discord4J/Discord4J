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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.service;

import discord4j.discordjson.json.*;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

public class WebhookService extends RestService {

    public WebhookService(Router router) {
        super(router);
    }

    public Mono<WebhookData> createWebhook(long channelId, WebhookCreateRequest request, @Nullable String reason) {
        return Routes.CHANNEL_WEBHOOK_CREATE.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(WebhookData.class);
    }

    public Flux<WebhookData> getChannelWebhooks(long channelId) {
        return Routes.CHANNEL_WEBHOOKS_GET.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(WebhookData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Flux<WebhookData> getGuildWebhooks(long guildId) {
        return Routes.GUILD_WEBHOOKS_GET.newRequest(guildId)
                .exchange(getRouter())
                .bodyToMono(WebhookData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<WebhookData> getWebhook(long webhookId) {
        return Routes.WEBHOOK_GET.newRequest(webhookId)
                .exchange(getRouter())
                .bodyToMono(WebhookData.class);
    }

    public Mono<WebhookData> getWebhookWithToken(long webhookId, String token) {
        return Routes.WEBHOOK_TOKEN_GET.newRequest(webhookId, token)
                .exchange(getRouter())
                .bodyToMono(WebhookData.class);
    }

    public Mono<WebhookData> modifyWebhook(long webhookId, WebhookModifyRequest request, @Nullable String reason) {
        return Routes.WEBHOOK_MODIFY.newRequest(webhookId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(WebhookData.class);
    }

    public Mono<WebhookData> modifyWebhookWithToken(long webhookId, String token,
                                                    WebhookModifyWithTokenRequest request) {
        // The reason is ignored when updating a webhook using the token.
        return Routes.WEBHOOK_TOKEN_MODIFY.newRequest(webhookId, token)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(WebhookData.class);
    }

    public Mono<Void> deleteWebhook(long webhookId, @Nullable String reason) {
        return Routes.WEBHOOK_DELETE.newRequest(webhookId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteWebhookWithToken(long webhookId, String token) {
        // The reason is ignored when deleting a webhook using the token.
        return Routes.WEBHOOK_TOKEN_DELETE.newRequest(webhookId, token)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    /**
     * Executes the specified webhook.
     *
     * @param wait true if you want to return message data and errors for the webhook.
     * @return If wait is true, a mono that contains the message information of the execution or an
     * error if the webhook is unsuccessful. If wait is false, the mono completes as soon as the request
     * is finished sending, and DOES NOT result in an error if the message is not saved.
     */
    public Mono<MessageData> executeWebhook(long webhookId, String token, boolean wait,
                                            MultipartRequest<? extends WebhookExecuteRequest> request) {
        return Routes.WEBHOOK_EXECUTE
                .newRequest(webhookId, token)
                .query("wait", wait)
                .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
                .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getJsonPayload() : request))
                .exchange(getRouter())
                .bodyToMono(MessageData.class);
    }

    /**
     * Executes the specified webhook.
     *
     * @param wait true if you want to return message data and errors for the webhook.
     * @param threadId specify the thread id within a webhook's channel.
     * @return If wait is true, a mono that contains the message information of the execution or an
     * error if the webhook is unsuccessful. If wait is false, the mono completes as soon as the request
     * is finished sending, and DOES NOT result in an error if the message is not saved.
     */
    public Mono<MessageData> executeWebhook(long webhookId, String token, boolean wait, long threadId,
                                            MultipartRequest<? extends WebhookExecuteRequest> request) {
        return Routes.WEBHOOK_EXECUTE
            .newRequest(webhookId, token)
            .query("wait", wait)
            .query("thread_id", threadId)
            .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
            .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getJsonPayload() : request))
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> getWebhookMessage(long webhookId, String webhookToken, String messageId) {
        return Routes.WEBHOOK_MESSAGE_GET.newRequest(webhookId, webhookToken, messageId)
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> getWebhookMessage(long webhookId, String webhookToken, String messageId, long threadId) {
        return Routes.WEBHOOK_MESSAGE_GET.newRequest(webhookId, webhookToken, messageId)
            .query("thread_id", threadId)
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> modifyWebhookMessage(long webhookId, String webhookToken, String messageId,
                                                  WebhookMessageEditRequest request) {
        return Routes.WEBHOOK_MESSAGE_EDIT.newRequest(webhookId, webhookToken, messageId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> modifyWebhookMessage(long webhookId, String webhookToken, String messageId, long threadId,
                                                  WebhookMessageEditRequest request) {
        return Routes.WEBHOOK_MESSAGE_EDIT.newRequest(webhookId, webhookToken, messageId)
            .query("thread_id", threadId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> modifyWebhookMessage(long webhookId, String webhookToken, String messageId,
                                                  MultipartRequest<WebhookMessageEditRequest> request) {
        return Routes.WEBHOOK_MESSAGE_EDIT.newRequest(webhookId, webhookToken, messageId)
                .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
                .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getJsonPayload() : request))
                .exchange(getRouter())
                .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> modifyWebhookMessage(long webhookId, String webhookToken, String messageId, long threadId,
                                                  MultipartRequest<WebhookMessageEditRequest> request) {
        return Routes.WEBHOOK_MESSAGE_EDIT.newRequest(webhookId, webhookToken, messageId)
            .query("thread_id", threadId)
            .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
            .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getJsonPayload() : request))
            .exchange(getRouter())
            .bodyToMono(MessageData.class);
    }

    public Mono<Void> deleteWebhookMessage(long webhookId, String webhookToken, String messageId) {
        return Routes.WEBHOOK_MESSAGE_DELETE.newRequest(webhookId, webhookToken, messageId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> deleteWebhookMessage(long webhookId, String webhookToken, String messageId, long threadId) {
        return Routes.WEBHOOK_MESSAGE_DELETE.newRequest(webhookId, webhookToken, messageId)
            .query("thread_id", threadId)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }
}
