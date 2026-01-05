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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Category;
import discord4j.gateway.ShardInfo;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Dispatched when a {@link Category} is updated in a guild.
 * <p>
 * The old category may not be present if categories are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#channel-update">Channel Update</a>
 */
public class CategoryUpdateEvent extends ChannelEvent {

    private final Category current;
    private final Category old;

    public CategoryUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, Category current, @Nullable Category old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Get the current, new, version of the {@link Category} that has been updated in this event.
     *
     * @return The current version of the updated {@link Category}.
     */
    public Category getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link Category} that has been updated in this event, if present.
     * This may not be available if {@code Categories} are not stored.
     *
     * @return The old version of the updated {@link Category}, if present.
     */
    public Optional<Category> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "CategoryUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
