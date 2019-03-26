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

import com.fasterxml.jackson.databind.JsonNode;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Dispatched when a user's presence changes.
 * <p>
 * The old presence may not be present if presences are not stored.
 * <p>
 * This event is dispatched by Discord
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#presence-update">Presence Update</a>
 */
public class PresenceUpdateEvent extends Event {

    private final long guildId;
    private final User oldUser;
    private final JsonNode user;
    private final Presence current;
    private final Presence old;

    public PresenceUpdateEvent(DiscordClient client, long guildId, @Nullable User oldUser, JsonNode user,
                               Presence current, @Nullable Presence old) {
        super(client);
        this.guildId = guildId;
        this.oldUser = oldUser;
        this.user = user;
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the Snowflake ID of the Guild containing the User whose presence has been updated.
     * @return The ID of the Guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the Guild containing the User whose presence has been updated.
     * @return The Guild involved.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the old version of the User that was updated. This may not be available if Users are not stored.
     * @return The old version of the user.
     */
    public Optional<User> getOldUser() {
        return Optional.ofNullable(oldUser);
    }

    /**
     * Gets the User's new username. This may not exist if the user's username has not been changed.
     * @return The user's new username.
     */
    public Optional<String> getNewUsername() {
        return Optional.ofNullable(user.get("username")).map(JsonNode::asText);
    }

    /**
     * Gets the User's new discriminator. This may not exist if the user's discriminator has not been changed.
     * @return The user's new discriminator.
     */
    public Optional<String> getNewDiscriminator() {
        return Optional.ofNullable(user.get("discriminator")).map(JsonNode::asText);
    }

    /**
     * Gets the User's new avatar. This may not exist if the user's discriminator has not been changed.
     * @return The user's new avatar.
     */
    public Optional<String> getNewAvatar() {
        return Optional.ofNullable(user.get("avatar"))
                .filter(node -> !node.isNull())
                .map(JsonNode::asText);
    }

    /**
     * Gets the Snowflake ID of the user whose presence has been changed in this event.
     * @return The ID of the user involved.
     */
    public Snowflake getUserId() {
        return Snowflake.of(user.get("id").asText());
    }

    /**
     * Gets the User whose presence has been changed in this event.
     * @return The user involved.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Gets the Member object of the User involved in this event.
     * @return The Member involved.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getUserId());
    }

    /**
     * Gets the current, new version of the presence.
     * @return The current, new version of the presence.
     */
    public Presence getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the presence that was changed. This may not be available if presence are not stored.
     * @return The old version of the presence.
     */
    public Optional<Presence> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "PresenceUpdateEvent{" +
                "guildId=" + guildId +
                ", oldUser=" + oldUser +
                ", user=" + user +
                ", current=" + current +
                ", old=" + old +
                '}';
    }
}
