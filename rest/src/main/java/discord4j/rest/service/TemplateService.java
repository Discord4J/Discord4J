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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class TemplateService extends RestService {

    public TemplateService(Router router) {
        super(router);
    }

    public Mono<TemplateData> getTemplate(String templateCode) {
        return Routes.GUILD_TEMPLATE_GET.newRequest(templateCode)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<GuildData> createGuild(String templateCode, TemplateCreateGuildRequest request) {
        return Routes.TEMPLATE_GUILD_CREATE.newRequest(templateCode)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(GuildData.class);
    }
    public Flux<TemplateData> getTemplates(long guildId) {
        return Routes.GUILD_TEMPLATE_LIST_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(TemplateData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<TemplateData> createTemplate(long guildId, TemplateCreateRequest request) {
        return Routes.GUILD_TEMPLATE_CREATE.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<TemplateData> syncTemplate(long guildId, String templateCode) {
        return Routes.GUILD_TEMPLATE_SYNC.newRequest(guildId, templateCode)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<TemplateData> modifyTemplate(long guildId, String templateCode, TemplateModifyRequest request) {
        return Routes.GUILD_TEMPLATE_MODIFY.newRequest(guildId, templateCode)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<TemplateData> deleteTemplate(long guildId, String templateCode) {
        return Routes.GUILD_TEMPLATE_DELETE.newRequest(guildId, templateCode)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }
}
