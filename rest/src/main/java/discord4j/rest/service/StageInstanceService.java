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
