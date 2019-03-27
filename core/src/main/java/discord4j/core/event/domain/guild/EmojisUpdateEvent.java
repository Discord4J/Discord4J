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
package discord4j.core.event.domain.guild;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Dispatched when an emoji is added/deleted/or edited in a guild. The {@link #emojis} set includes ALL emojis of the
 * guild.
 * <p>
 * This event is dispatched by Discord
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-emojis-update">Guild Emojis Update</a>
 */
public class EmojisUpdateEvent extends GuildEvent {

    private final long guildId;
    private final Set<GuildEmoji> emojis;

    public EmojisUpdateEvent(DiscordClient client, long guildId, Set<GuildEmoji> emojis) {
        super(client);
        this.guildId = guildId;
        this.emojis = emojis;
    }

    /**
     * Gets the Snowflake ID of the guild involved in the event.
     *
     * @return The Snowflake ID of the guild.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the Guild whose emojis have been updated.
     *
     * @return A {@link Mono} where, upon successful completion, emits the Guild involved. If an error is received, it is emitted through the Mono.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets a list of ALL emojis of the Guild.
     *
     * @return A list of ALL emojis of the Guild.
     */
    public Set<GuildEmoji> getEmojis() {
        return emojis;
    }

    @Override
    public String toString() {
        return "EmojisUpdateEvent{" +
                "guildId=" + guildId +
                ", emojis=" + emojis +
                '}';
    }
}
