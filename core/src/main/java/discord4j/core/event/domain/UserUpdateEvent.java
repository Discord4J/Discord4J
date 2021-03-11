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
package discord4j.core.event.domain;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when the bot's user is updated. {@link PresenceUpdateEvent} is dispatched for users the bot is receiving.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#user-update">User Update</a>
 */
public class UserUpdateEvent extends Event {

    private final User current;
    private final User old;

    public UserUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, User current, @Nullable User old) {
        super(gateway, shardInfo);
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the current, new version of the {@link User} that has been updated in this event.
     *
     * @return The current version of the {@link User} updated in this event.
     */
    public User getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link User} that has been updated in this event, if present.
     * This may not be available if {@code Users} are not stored.
     *
     * @return The old version of the {@link User} that has been updated in this event, if present.
     */
    public Optional<User> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "UserUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
