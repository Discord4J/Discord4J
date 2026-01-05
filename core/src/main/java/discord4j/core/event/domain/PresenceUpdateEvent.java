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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.PartialUserData;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.ShardInfo;
import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Dispatched when a user's presence changes. This includes username, discriminator, and avatar changes.
 * <p>
 * The old presence may not be present if presences are not stored.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#presence-update">Presence Update</a>
 */
public class PresenceUpdateEvent extends Event {

    private final long guildId;
    private final User oldUser;
    private final PartialUserData user;
    private final Presence current;
    private final Presence old;

    public PresenceUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, @Nullable User oldUser,
                               PartialUserData user, Presence current, @Nullable Presence old) {
        super(gateway, shardInfo);
        this.guildId = guildId;
        this.oldUser = oldUser;
        this.user = user;
        this.current = current;
        this.old = old;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} containing the {@link User} whose presence has been updated.
     *
     * @return The ID of the {@link Guild} involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the {@link Guild} containing the {@link User} whose presence has been updated.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved in the event.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the old version of the {@link User} that was updated, if present.
     * This may not be available if {@code Users} are not stored.
     *
     * @return The old version of the {@link User}, if present.
     */
    public Optional<User> getOldUser() {
        return Optional.ofNullable(oldUser);
    }

    /**
     * Gets the {@link User}'s new global name, if present. This may not exist if the {@code user}'s global
     * name has not been changed.
     *
     * @return The {@link User}'s new global name, if present.
     */
    public Optional<String> getNewGlobalName() {
        return Possible.flatOpt(user.globalName());
    }

    /**
     * Gets the {@link User}'s new username, if present. This may not exist if the {@code user}'s username has not
     * been changed.
     *
     * @return The {@link User}'s new username, if present.
     */
    public Optional<String> getNewUsername() {
        return user.username().toOptional();
    }

    /**
     * Gets the {@link User}'s new discriminator, if present.
     * This may not exist if the {@code User}'s discriminator has not been changed.
     *
     * @return The {@link User}'s new discriminator, if present.
     * @deprecated This method will be removed once the system change is complete.
     */
    @Deprecated
    public Optional<String> getNewDiscriminator() {
        return user.discriminator().toOptional();
    }

    /**
     * Gets the {@link User}'s new avatar, if present. This may not exist if the {@code User}'s discriminator has not
     * been changed.
     *
     * @return The user's new avatar, if present.
     */
    public Optional<String> getNewAvatar() {
        return Possible.flatOpt(user.avatar());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link User} whose presence has been updated in this event.
     *
     * @return The ID of the {@link User} whose presence has been updated.
     */
    public Snowflake getUserId() {
        return Snowflake.of(user.id());
    }

    /**
     * Requests to retrieve the {@link User} whose presence has been changed in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link User} involved in this event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<User> getUser() {
        return getClient().getUserById(getUserId());
    }

    /**
     * Requests to retrieve the {@link Member} object of the {@link User} involved in this event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} involved in this event.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getUserId());
    }

    /**
     * Gets the current, new version of the {@link Presence}.
     *
     * @return The current, new version of the {@link Presence}.
     */
    public Presence getCurrent() {
        return current;
    }

    /**
     * Gets the old version of the {@link Presence} that was changed, if present.
     * This may not be available if {@code Presence} are not stored.
     *
     * @return The old version of the {@link Presence}, if present.
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
