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

package discord4j.rest.entity;

import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.GuildEmojiModifyRequest;
import discord4j.rest.RestClient;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class RestEmoji {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    private RestEmoji(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    public static RestEmoji create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestEmoji(restClient, guildId.asLong(), id.asLong());
    }

    public static RestEmoji create(RestClient restClient, long guildId, long id) {
        return new RestEmoji(restClient, guildId, id);
    }

    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    public Mono<EmojiData> getData() {
        return restClient.getEmojiService().getGuildEmoji(guildId, id);
    }

    public Mono<EmojiData> modify(GuildEmojiModifyRequest request, @Nullable String reason) {
        return restClient.getEmojiService().modifyGuildEmoji(guildId, id, request, reason);
    }

    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getEmojiService().deleteGuildEmoji(guildId, id, reason);
    }
}
