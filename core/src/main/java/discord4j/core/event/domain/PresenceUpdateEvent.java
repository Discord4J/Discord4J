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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Dispatched when a user's presence changes.
 * <p>
 * The old presence may not be present if presences are not stored.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#presence-update">Presence Update</a>
 */
public class PresenceUpdateEvent extends Event {

    private final long guildId;
    private final long userId;
    private final Presence current;
    private final Presence old;

    public PresenceUpdateEvent(DiscordClient client, long guildId, long userId, Presence current,
                               @Nullable Presence old) {
        super(client);
        this.guildId = guildId;
        this.userId = userId;
        this.current = current;
        this.old = old;
    }

    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public Snowflake getUserId() {
        return Snowflake.of(userId);
    }

    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getUserId());
    }

    public Presence getCurrent() {
        return current;
    }

    public Optional<Presence> getOld() {
        return Optional.ofNullable(old);
    }
}
