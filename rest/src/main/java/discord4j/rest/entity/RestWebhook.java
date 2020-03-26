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

import discord4j.discordjson.json.WebhookData;
import discord4j.discordjson.json.WebhookModifyRequest;
import discord4j.rest.RestClient;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class RestWebhook {

    private final RestClient restClient;
    private final long id;

    private RestWebhook(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    public static RestWebhook create(RestClient restClient, Snowflake id) {
        return new RestWebhook(restClient, id.asLong());
    }

    public static RestWebhook create(RestClient restClient, long id) {
        return new RestWebhook(restClient, id);
    }

    public Mono<WebhookData> getData() {
        return restClient.getWebhookService().getWebhook(id);
    }

    public Mono<WebhookData> modify(WebhookModifyRequest request, @Nullable String reason) {
        return restClient.getWebhookService().modifyWebhook(id, request, reason);
    }

    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getWebhookService().deleteWebhook(id, reason);
    }
}
