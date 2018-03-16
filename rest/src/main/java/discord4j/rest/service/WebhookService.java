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

import discord4j.common.json.request.WebhookCreateRequest;
import discord4j.common.json.request.WebhookModifyRequest;
import discord4j.common.json.response.WebhookResponse;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class WebhookService extends RestService {

    public WebhookService(Router router) {
        super(router);
    }

    public Mono<WebhookResponse> createWebhook(long channelId, WebhookCreateRequest request) {
        return Routes.CHANNEL_WEBHOOK_CREATE.newRequest(channelId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<WebhookResponse[]> getChannelWebhooks(long channelId) {
        return Routes.CHANNEL_WEBHOOKS_GET.newRequest(channelId)
                .exchange(getRouter());
    }

    public Mono<WebhookResponse[]> getGuildWebhooks(long guildId) {
        return Routes.GUILD_WEBHOOKS_GET.newRequest(guildId)
                .exchange(getRouter());
    }

    public Mono<WebhookResponse> getWebhook(long webhookId) {
        return Routes.WEBHOOK_GET.newRequest(webhookId)
                .exchange(getRouter());
    }

    public Mono<WebhookResponse> modifyWebhook(long webhookId, WebhookModifyRequest request) {
        return Routes.WEBHOOK_MODIFY.newRequest(webhookId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteWebhook(long webhookId) {
        return Routes.WEBHOOK_DELETE.newRequest(webhookId)
                .exchange(getRouter());
    }
}
