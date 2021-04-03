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

import discord4j.discordjson.json.RegionData;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import discord4j.discordjson.json.UpdateSelfVoiceStateRequest;
import discord4j.discordjson.json.UpdateOthersVoiceStateRequest;

public class VoiceService extends RestService {

    public VoiceService(Router router) {
        super(router);
    }

    public Flux<RegionData> getVoiceRegions() {
        return Routes.VOICE_REGION_LIST.newRequest()
                .exchange(getRouter())
                .bodyToMono(RegionData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> modifySelfVoiceState(long guildId, UpdateSelfVoiceStateRequest request) {
        return Routes.SELF_VOICE_STATE_MODIFY.newRequest(guildId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

    public Mono<Void> modifyOthersVoiceState(long guildId, long userId, UpdateOthersVoiceStateRequest request) {
        return Routes.OTHERS_VOICE_STATE_MODIFY.newRequest(guildId, userId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }
}
