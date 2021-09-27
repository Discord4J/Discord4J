package discord4j.rest.service;

import discord4j.discordjson.json.StageInstanceCreateRequest;
import discord4j.discordjson.json.StageInstanceData;
import discord4j.discordjson.json.StageInstanceModifyRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class StageInstanceService extends RestService {

    public StageInstanceService(Router router) {
        super(router);
    }

    public Mono<StageInstanceData> createStageInstance(StageInstanceCreateRequest request, @Nullable String reason) {
        return Routes.CREATE_STAGE_INSTANCE.newRequest()
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(StageInstanceData.class);
    }

    public Mono<StageInstanceData> getStageInstance(long channelId) {
        return Routes.GET_STAGE_INSTANCE.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(StageInstanceData.class);
    }

    public Mono<StageInstanceData> modifyStageInstance(long channelId, StageInstanceModifyRequest request, @Nullable String reason) {
        return Routes.MODIFY_STAGE_INSTANCE.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(StageInstanceData.class);
    }

    public Mono<StageInstanceData> deleteStageInstance(long channelId, @Nullable String reason) {
        return Routes.DELETE_STAGE_INSTANCE.newRequest(channelId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(StageInstanceData.class);
    }

}
