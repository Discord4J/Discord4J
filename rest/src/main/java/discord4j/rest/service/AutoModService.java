package discord4j.rest.service;

import discord4j.discordjson.json.*;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class AutoModService extends RestService {

    public AutoModService(Router router) {
        super(router);
    }

    public Flux<AutoModRuleData> getAutoModRules(long guildId) {
        return Routes.AUTO_MOD_RULES_GET.newRequest(guildId)
                .exchange(getRouter())
                .bodyToMono(AutoModRuleData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<AutoModRuleData> getAutoModRule(long guildId, long ruleId) {
        return Routes.AUTO_MOD_RULE_GET.newRequest(guildId, ruleId)
                .exchange(getRouter())
                .bodyToMono(AutoModRuleData.class);
    }

    public Mono<AutoModRuleData> createAutoModRule(long guildId, AutoModRuleCreateRequest request, @Nullable String reason) {
        return Routes.AUTO_MOD_RULE_MODIFY.newRequest(guildId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(AutoModRuleData.class);
    }

    public Mono<AutoModRuleData> modifyAutoModRule(long guildId, long ruleId, AutoModRuleModifyRequest request, @Nullable String reason) {
        return Routes.AUTO_MOD_RULE_MODIFY.newRequest(guildId, ruleId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(AutoModRuleData.class);
    }

    public Mono<Void> deleteAutoModRule(long guildId, long ruleId, @Nullable String reason) {
        return Routes.AUTO_MOD_RULE_DELETE.newRequest(guildId, ruleId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }
}
