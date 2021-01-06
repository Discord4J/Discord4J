package discord4j.rest.service;

import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.TemplateData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class TemplateService extends RestService {

    public TemplateService(Router router) {
        super(router);
    }

    public Mono<TemplateData> getTemplate(String templateCode) {
        return Routes.GUILD_TEMPLATE_GET.newRequest(templateCode)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<GuildUpdateData> createGuildFromTemplate(TemplateData request) {
        return Routes.TEMPLATE_GUILD_CREATE.newRequest()
            .body(request)
            .exchange(getRouter())
            .bodyToMono(GuildUpdateData.class);
    }
}
