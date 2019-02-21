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
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a guild is updated.
 * <p>
 * The old guild may not be present if guilds are not stored.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-update">Guild Update</a>
 */
public class GuildUpdateEvent extends AbstractGuildEvent {

    private final Guild current;
    private final Guild old;

    public GuildUpdateEvent(DiscordClient client, Guild current, @Nullable Guild old) {
        super(client, current.getId().asLong());
        this.current = current;
        this.old = old;
    }

    public Guild getCurrent() {
        return current;
    }

    public Optional<Guild> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "GuildUpdateEvent{" +
                "current=" + current +
                ", old=" + old +
                '}';
    }
}
