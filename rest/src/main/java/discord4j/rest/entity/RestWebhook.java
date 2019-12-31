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

import discord4j.rest.RestClient;
import discord4j.rest.entity.data.WebhookData;
import reactor.core.publisher.Mono;

public class RestWebhook {

    private final RestClient restClient;
    private final long id;

    public RestWebhook(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    public Mono<WebhookData> getData() {
        return restClient.getWebhookService()
                .getWebhook(id)
                .map(WebhookData::new);
    }
}
