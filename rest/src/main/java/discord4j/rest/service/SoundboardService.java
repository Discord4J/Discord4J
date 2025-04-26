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

import discord4j.discordjson.json.SendSoundboardSoundRequest;
import discord4j.discordjson.json.SoundboardSoundCreateRequest;
import discord4j.discordjson.json.SoundboardSoundData;
import discord4j.discordjson.json.SoundboardSoundDataList;
import discord4j.discordjson.json.SoundboardSoundModifyRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class SoundboardService extends RestService {

    public SoundboardService(Router router) {
        super(router);
    }

    public Mono<Void> sendSoundboardSound(long channelId, SendSoundboardSoundRequest request) {
        return Routes.SEND_SOUNDBOARD_SOUND.newRequest(channelId)
            .body(request)
            .exchange(getRouter())
            .skipBody();
    }

    public Flux<SoundboardSoundData> getDefaultSoundboardSounds() {
        return Routes.LIST_DEFAULT_SOUNDBOARD_SOUNDS.newRequest()
            .exchange(getRouter())
            .bodyToMono(SoundboardSoundData[].class)
            .flatMapMany(Flux::fromArray);
    }

    public Flux<SoundboardSoundData> getGuildSoundboardSounds(long guildId) {
        return Routes.LIST_GUILD_SOUNDBOARD_SOUNDS.newRequest(guildId)
            .exchange(getRouter())
            .bodyToMono(SoundboardSoundDataList.class)
            .map(SoundboardSoundDataList::items)
            .flatMapMany(Flux::fromIterable);
    }

    public Mono<SoundboardSoundData> getGuildSoundboardSound(long guildId, long soundBoardId) {
        return Routes.GET_GUILD_SOUNDBOARD_SOUND.newRequest(guildId, soundBoardId)
            .exchange(getRouter())
            .bodyToMono(SoundboardSoundData.class);
    }

    public Mono<SoundboardSoundData> createGuildSoundboardSound(long guildId, SoundboardSoundCreateRequest request, @Nullable String reason) {
        return Routes.CREATE_GUILD_SOUNDBOARD_SOUND.newRequest(guildId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(SoundboardSoundData.class);
    }

    public Mono<SoundboardSoundData> modifyGuildSoundboardSound(long guildId, long soundBoardId, SoundboardSoundModifyRequest request, @Nullable String reason) {
        return Routes.MODIFY_GUILD_SOUNDBOARD_SOUND.newRequest(guildId, soundBoardId)
            .body(request)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(SoundboardSoundData.class);
    }

    public Mono<Void> deleteGuildSoundboardSound(long guildId, long soundBoardId, @Nullable String reason) {
        return Routes.DELETE_GUILD_SOUNDBOARD_SOUND.newRequest(guildId, soundBoardId)
            .optionalHeader("X-Audit-Log-Reason", reason)
            .exchange(getRouter())
            .bodyToMono(Void.class);
    }

}
