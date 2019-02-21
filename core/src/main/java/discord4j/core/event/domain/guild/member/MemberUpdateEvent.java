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
import discord4j.core.event.domain.user.UserEvent;
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
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#guild-member-update">Guild Member Update</a>
 */
public class MemberUpdateEvent extends AbstractGuildEvent implements UserEvent {

    private final long memberId;

    @Nullable
    private final Member old;

    private final long[] currentRoles;
    @Nullable
    private final String currentNickname;

    public MemberUpdateEvent(DiscordClient client, long guildId, long memberId, @Nullable Member old,
                             long[] currentRoles, @Nullable String currentNickname) {
        super(client, guildId);
        this.memberId = memberId;
        this.old = old;
        this.currentRoles = currentRoles;
        this.currentNickname = currentNickname;
    }

    @Override
    public Snowflake getUserId() {
        return Snowflake.of(memberId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public Snowflake getMemberId() {
        return Snowflake.of(memberId);
    }

    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getMemberId());
    }

    public Optional<Member> getOld() {
        return Optional.ofNullable(old);
    }

    public Set<Snowflake> getCurrentRoles() {
        return Arrays.stream(currentRoles)
                .mapToObj(Snowflake::of)
                .collect(Collectors.toSet());
    }

    public Optional<String> getCurrentNickname() {
        return Optional.ofNullable(currentNickname);
    }

    @Override
    public String toString() {
        return "MemberUpdateEvent{" +
                "memberId=" + memberId +
                ", old=" + old +
                ", currentRoles=" + Arrays.toString(currentRoles) +
                ", currentNickname='" + currentNickname + '\'' +
                '}';
    }
}
