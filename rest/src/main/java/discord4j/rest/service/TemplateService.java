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

    public Mono<GuildData> createGuild(String templateCode, TemplateCreateGuildRequest request, @Nullable String reason) {
        return Routes.TEMPLATE_GUILD_CREATE.newRequest(templateCode)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(GuildData.class);
    }
    public Flux<TemplateData> getTemplates(long guildId) {
        return Routes.GUILD_TEMPLATE_LIST_GET.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(TemplateData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Mono<TemplateData> createTemplate(long guildId, TemplateCreateRequest request, @Nullable String reason) {
        return Routes.GUILD_TEMPLATE_CREATE.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<TemplateData> syncTemplate(long guildId, String templateCode) {
        return Routes.GUILD_TEMPLATE_SYNC.newRequest(guildId, templateCode)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<TemplateData> modifyTemplate(long guildId, String templateCode, TemplateModifyRequest request, @Nullable String reason) {
        return Routes.GUILD_TEMPLATE_MODIFY.newRequest(guildId, templateCode)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }

    public Mono<TemplateData> deleteTemplate(long guildId, String templateCode) {
        return Routes.GUILD_TEMPLATE_DELETE.newRequest(guildId, templateCode)
            .exchange(getRouter())
            .bodyToMono(TemplateData.class);
    }
}
