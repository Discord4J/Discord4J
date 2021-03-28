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
package discord4j.rest.entity;

import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;

/**
 * Represents a guild template entity in Discord.
 */
public class RestGuildTemplate {

    private final RestClient restClient;
    private final String code;

    private RestGuildTemplate(RestClient restClient, String code) {
        this.restClient = restClient;
        this.code = code;
    }

    /**
     * Create a {@link RestGuildTemplate} with the given code. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param code the template's code
     * @return a {@code RestTemplate} represented by the given code.
     */
    public static RestGuildTemplate create(RestClient restClient, String code) {
        return new RestGuildTemplate(restClient, code);
    }

    /**
     * Retrieve this template's data upon subscription.
     *
     * @return a template object
     */
    public Mono<TemplateData> getData() {
        return restClient.getTemplateService().getTemplate(code);
    }

    /**
     * Create a new guild based on this template.
     * <p>
     * This endpoint can be used only by bots in less than 10 guilds.
     *
     * @return a guild object
     */
    public Mono<GuildData> createGuild(TemplateCreateGuildRequest request) {
        return restClient.getTemplateService().createGuild(code, request);
    }
}
