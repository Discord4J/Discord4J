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

package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.WebhookData;
import discord4j.discordjson.json.WebhookModifyRequest;
import discord4j.rest.RestClient;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Represents a webhook entity in Discord. Webhooks are a low-effort way to post messages to channels in Discord.
 */
public class RestWebhook {

    private final RestClient restClient;
    private final long id;

    private RestWebhook(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    /**
     * Create a {@link RestWebhook} for a given ID. This method does not perform any API request.
     *
     * @param restClient the client to make API requests
     * @param id the ID of this entity
     * @return a {@code RestWebhook} represented by this {@code id}.
     */
    public static RestWebhook create(RestClient restClient, Snowflake id) {
        return new RestWebhook(restClient, id.asLong());
    }

    static RestWebhook create(RestClient restClient, long id) {
        return new RestWebhook(restClient, id);
    }

    /**
     * Returns the ID of this webhook.
     *
     * @return The ID of this webhook
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Retrieve this webhook's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link WebhookData} belonging to this entity.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<WebhookData> getData() {
        return restClient.getWebhookService().getWebhook(id);
    }

    /**
     * Modify a webhook. Requires the {@link Permission#MANAGE_WEBHOOKS} permission. Returns the updated webhook
     * object on success.
     *
     * @param request a request to modify the webhook
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits the updated {@link WebhookData} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<WebhookData> modify(WebhookModifyRequest request, @Nullable String reason) {
        return restClient.getWebhookService().modifyWebhook(id, request, reason);
    }

    /**
     * Delete a webhook permanently. Requires the {@link Permission#MANAGE_WEBHOOKS} permission. Returns empty on
     * success.
     *
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits a complete signal on success. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getWebhookService().deleteWebhook(id, reason);
    }
}
