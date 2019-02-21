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
package discord4j.core.event.domain.channel;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.GuildEvent;
import discord4j.core.object.entity.Category;
import discord4j.core.object.util.Snowflake;

/**
 * Dispatched when a {@link Category} is created in a guild.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#channel-create">Channel Create</a>
 */
public class CategoryCreateEvent extends AbstractChannelEvent implements GuildEvent {

    private final Category category;

    public CategoryCreateEvent(DiscordClient client, Category category) {
        super(client, category.getId().asLong());
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public Snowflake getGuildId() {
        return category.getGuildId();
    }

    @Override
    public String toString() {
        return "CategoryCreateEvent{" +
                "category=" + category +
                '}';
    }
}
