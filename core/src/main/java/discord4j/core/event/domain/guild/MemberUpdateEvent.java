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
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dispatched when a user's nickname or roles change in a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-member-update">Guild Member Update</a>
 */
public class MemberUpdateEvent extends GuildEvent {

    private final long guildId;
    private final long memberId;

    @Nullable
    private final Member old;

    private final long[] currentRoles;
    @Nullable
    private final String currentNickname;

    public MemberUpdateEvent(DiscordClient client, long guildId, long memberId, @Nullable Member old,
                             long[] currentRoles, @Nullable String currentNickname) {
        super(client);

        this.guildId = guildId;
        this.memberId = memberId;
        this.old = old;
        this.currentRoles = currentRoles;
        this.currentNickname = currentNickname;
    }

    /**
     * Gets the Snowflake ID of the Guild involved in the event.
     * @return The ID of the Guild involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Gets the Guild involved in the event.
     * @return The guild involved.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the Snowflake ID of the Member involved in the event.
     * @return The ID of the Member involved.
     */
    public Snowflake getMemberId() {
        return Snowflake.of(memberId);
    }

    /**
     * Gets the Member involved in the event.
     * @return The Member involved.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getMemberId());
    }

    /**
     * Gets the old version of the Member involved in the event. This may not be available if Members are not stored.
     * @return the old version of the Member involved.
     */
    public Optional<Member> getOld() {
        return Optional.ofNullable(old);
    }

    /**
     * Gets a list of Snowflake IDs of roles the Member is currently assigned.
     * @return The IDs of the roles the Member is assigned.
     */
    public Set<Snowflake> getCurrentRoles() {
        return Arrays.stream(currentRoles)
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the current nickname of the Member involved in this event.
     * @return The current nickname, if any, of the Member involved.
     */
    public Optional<String> getCurrentNickname() {
        return Optional.ofNullable(currentNickname);
    }

    @Override
    public String toString() {
        return "MemberUpdateEvent{" +
                "guildId=" + guildId +
                ", memberId=" + memberId +
                ", old=" + old +
                ", currentRoles=" + Arrays.toString(currentRoles) +
                ", currentNickname='" + currentNickname + '\'' +
                '}';
    }
}
