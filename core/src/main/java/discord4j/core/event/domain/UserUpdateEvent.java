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

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.User;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Dispatched when a user is updated.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#user-update">User Update</a>
 */
public class UserUpdateEvent extends Event {

    private final User current;
    private final User old;

    public UserUpdateEvent(DiscordClient client, User current, @Nullable User old) {
        super(client);
        this.current = current;
        this.old = old;
    }

    public User getCurrent() {
        return current;
    }

    public Optional<User> getOld() {
        return Optional.ofNullable(old);
    }
}
