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

import discord4j.common.json.request.GuildEmojiCreateRequest;
import discord4j.common.json.request.GuildEmojiModifyRequest;
import discord4j.common.json.response.EmojiResponse;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import reactor.core.publisher.Mono;

public class EmojiService extends RestService {

    public EmojiService(Router router) {
        super(router);
    }

    public Mono<EmojiResponse[]> getGuildEmojis(long guildId) {
        return Routes.GUILD_EMOJIS_GET.newRequest(guildId)
                .exchange(getRouter());
    }

    public Mono<EmojiResponse> getGuildEmoji(long guildId, long emojiID) {
        return Routes.GUILD_EMOJI_GET.newRequest(guildId, emojiID)
                .exchange(getRouter());
    }

    public Mono<EmojiResponse> createGuildEmoji(long guildId, GuildEmojiCreateRequest request) {
        return Routes.GUILD_EMOJI_CREATE.newRequest(guildId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<EmojiResponse> modifyGuildEmoji(long guildId, long emojiId, GuildEmojiModifyRequest request) {
        return Routes.GUILD_EMOJI_MODIFY.newRequest(guildId, emojiId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteGuildEmoji(long guildId, long emojiId) {
        return Routes.GUILD_EMOJI_DELETE.newRequest(guildId, emojiId)
                .exchange(getRouter());
    }
}
