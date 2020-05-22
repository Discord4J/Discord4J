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

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.WebhookModifyRequest;
import discord4j.rest.RestTests;
import discord4j.rest.request.Router;
import org.junit.Test;

public class WebhookServiceTest {

    private static final long permanentChannel = Snowflake.asLong(System.getenv("permanentChannel"));
    private static final long guild = Snowflake.asLong(System.getenv("guild"));
    private static final long permanentWebhook = Snowflake.asLong(System.getenv("permanentWebhook"));

    private WebhookService webhookService = null;

    private WebhookService getWebhookService() {

        if (webhookService != null) {
            return webhookService;
        }

        String token = System.getenv("token");
        boolean ignoreUnknown = !Boolean.parseBoolean(System.getenv("failUnknown"));
        ObjectMapper mapper = RestTests.getMapper(ignoreUnknown);
        Router router = RestTests.getRouter(token, mapper);

        return webhookService = new WebhookService(router);
    }

    @Test
    public void testCreateWebhook() {
        // TODO
    }

    @Test
    public void testGetChannelWebhooks() {
        getWebhookService().getChannelWebhooks(permanentChannel).then().block();
    }

    @Test
    public void testGetGuildWebhooks() {
        getWebhookService().getGuildWebhooks(guild).then().block();
    }

    @Test
    public void testGetWebhook() {
        getWebhookService().getWebhook(permanentWebhook).block();
    }

    @Test
    public void testModifyWebhook() {
        WebhookModifyRequest req = WebhookModifyRequest.builder()
            .name("Permanent Webhook")
            .build();
        getWebhookService().modifyWebhook(permanentWebhook, req, null).block();
    }

    @Test
    public void testDeleteWebhook() {
        // TODO
    }
}
