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

import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.GuildEmojiCreateRequest;
import discord4j.discordjson.json.GuildEmojiModifyRequest;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class EmojiService extends RestService {

    public EmojiService(Router router) {
        super(router);
    }

    public Flux<EmojiData> getGuildEmojis(long guildId) {
        return Routes.GUILD_EMOJIS_GET.newRequest(guildId)
                .exchange(getRouter())
                .bodyToMono(EmojiData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<EmojiData> getGuildEmoji(long guildId, long emojiId) {
        return Routes.GUILD_EMOJI_GET.newRequest(guildId, emojiId)
                .exchange(getRouter())
                .bodyToMono(EmojiData.class);
    }

    public Mono<EmojiData> createGuildEmoji(long guildId, GuildEmojiCreateRequest request, @Nullable String reason) {
        return Routes.GUILD_EMOJI_CREATE.newRequest(guildId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(EmojiData.class);
    }

    public Mono<EmojiData> modifyGuildEmoji(long guildId, long emojiId, GuildEmojiModifyRequest request, @Nullable String reason) {
        return Routes.GUILD_EMOJI_MODIFY.newRequest(guildId, emojiId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(EmojiData.class);
    }

    public Mono<Void> deleteGuildEmoji(long guildId, long emojiId, @Nullable String reason) {
        return Routes.GUILD_EMOJI_DELETE.newRequest(guildId, emojiId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }
}
