package discord4j.rest.service;

import discord4j.discordjson.json.GuildUpdateData;
import discord4j.discordjson.json.TemplateData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
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

    public Mono<TemplateData> deleteTemplate(String templateCode, @Nullable String reason) {
        return Routes.TEMPLATE_DELETE.newRequest(reason)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }
}
