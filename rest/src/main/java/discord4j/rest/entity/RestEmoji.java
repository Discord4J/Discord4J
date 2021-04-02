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

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.GuildEmojiModifyRequest;
import discord4j.rest.RestClient;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

/**
 * Represents a guild emoji entity in Discord.
 */
public class RestEmoji {

    private final RestClient restClient;
    private final long guildId;
    private final long id;

    private RestEmoji(RestClient restClient, long guildId, long id) {
        this.restClient = restClient;
        this.guildId = guildId;
        this.id = id;
    }

    /**
     * Create a {@link RestEmoji} with the given parameters. This method does not perform any API request.
     *
     * @param restClient REST API resources
     * @param guildId the ID of the guild this emoji belongs to
     * @param id the ID of this member
     * @return a {@code RestEmoji} represented by the given parameters.
     */
    public static RestEmoji create(RestClient restClient, Snowflake guildId, Snowflake id) {
        return new RestEmoji(restClient, guildId.asLong(), id.asLong());
    }

    static RestEmoji create(RestClient restClient, long guildId, long id) {
        return new RestEmoji(restClient, guildId, id);
    }

    /**
     * Returns the ID of the guild this emoji belongs to.
     *
     * @return The ID of the the guild this emoji belongs to.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Returns the ID of this emoji.
     *
     * @return The ID of this emoji
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Return this emoji's parent {@link RestGuild}. This method does not perform any API request.
     *
     * @return the parent {@code RestGuild} of this guild emoji.
     */
    public RestGuild guild() {
        return RestGuild.create(restClient, guildId);
    }

    /**
     * Retrieve this guild emoji's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link EmojiData} belonging to this entity.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<EmojiData> getData() {
        return restClient.getEmojiService().getGuildEmoji(guildId, id);
    }

    /**
     * Modify this guild emoji. Requires the {@link Permission#MANAGE_EMOJIS} permission. Returns the updated emoji
     * object on success.
     *
     * @param request the guild emoji modify request
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits the updated {@link EmojiData} on success. If an error
     * is received, it is emitted through the {@code Mono}.
     */
    public Mono<EmojiData> modify(GuildEmojiModifyRequest request, @Nullable String reason) {
        return restClient.getEmojiService().modifyGuildEmoji(guildId, id, request, reason);
    }

    /**
     * Delete this guild emoji. Requires the {@link Permission#MANAGE_EMOJIS} permission. Returns empty on success.
     *
     * @param reason an optional reason for the audit log
     * @return a {@link Mono} where, upon subscription, emits a complete signal on success. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getEmojiService().deleteGuildEmoji(guildId, id, reason);
    }
}
