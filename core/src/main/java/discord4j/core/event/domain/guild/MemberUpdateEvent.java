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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.common.util.Snowflake;
import discord4j.gateway.ShardInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dispatched when a user's nickname or roles change in a guild.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#guild-member-update">Guild Member Update</a>
 */
public class MemberUpdateEvent extends GuildEvent {

    private final long guildId;
    private final long memberId;

    @Nullable
    private final Member old;

    private final List<Long> currentRoles;
    @Nullable
    private final String currentNickname;
    private final String currentJoinedAt;
    @Nullable
    private final String currentPremiumSince;
    @Nullable
    private final Boolean currentPending;

    public MemberUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, long memberId,
                             @Nullable Member old, List<Long> currentRoles, @Nullable String currentNickname,
                             String currentJoinedAt, @Nullable String currentPremiumSince,
                             @Nullable Boolean currentPending) {
        super(gateway, shardInfo);

        this.guildId = guildId;
        this.memberId = memberId;
        this.old = old;
        this.currentRoles = currentRoles;
        this.currentNickname = currentNickname;
        this.currentJoinedAt = currentJoinedAt;
        this.currentPremiumSince = currentPremiumSince;
        this.currentPending = currentPending;
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Guild} involved in the event.
     *
     * @return The ID of the {@link Guild} involved.
     */
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    /**
     * Requests to retrieve the {@link Guild} involved in the event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} involved.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    /**
     * Gets the {@link Snowflake} ID of the {@link Member} involved in the event.
     *
     * @return The ID of the {@link Member} involved.
     */
    public Snowflake getMemberId() {
        return Snowflake.of(memberId);
    }

    /**
     * Requests to retrieve the {@link Member} involved in the event.
     *
     * @return A {@link Mono} where, upon successful completion, emits the {@link Member} that has been updated.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Member> getMember() {
        return getClient().getMemberById(getGuildId(), getMemberId());
    }

    /**
     * Gets the old version of the {@link Member} involved in the event, if present.
     * This may not be available if {@code Members} are not stored.
     *
     * @return the old version of the {@link Member} involved, if present.
     */
    public Optional<Member> getOld() {
        return Optional.ofNullable(old);
    }

    /**
     * Gets a list of {@link Snowflake} IDs of roles the {@link Member} is currently assigned.
     *
     * @return The IDs of the roles the {@link Member} is assigned.
     */
    public Set<Snowflake> getCurrentRoles() {
        return currentRoles.stream()
                .map(Snowflake::of)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the current nickname of the {@link Member} involved in this event, if present.
     *
     * @return The current nickname, if any, of the {@link Member} involved, if present.
     */
    public Optional<String> getCurrentNickname() {
        return Optional.ofNullable(currentNickname);
    }

    /**
     * Gets the current join time of the {@link Member} involved in this event.
     *
     * @return The current join time of the {@link Member} involved in this event.
     */
    public Instant getJoinTime() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(currentJoinedAt, Instant::from);
    }

    /**
     * Gets when the user started boosting the guild, if present.
     *
     * @return When the user started boosting the guild, if present.
     */
    public Optional<Instant> getCurrentPremiumSince() {
        return Optional.ofNullable(currentPremiumSince)
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets whether the user has currently not yet passed the guild's Membership Screening requirements.
     *
     * @return Whether the user has currently not yet passed the guild's Membership Screening requirements.
     */
    public boolean isCurrentPending() {
        return Optional.ofNullable(currentPending).orElse(false);
    }

    @Override
    public String toString() {
        return "MemberUpdateEvent{" +
                "guildId=" + guildId +
                ", memberId=" + memberId +
                ", old=" + old +
                ", currentRoles=" + currentRoles +
                ", currentNickname='" + currentNickname + '\'' +
                ", currentJoinedAt='" + currentJoinedAt + '\'' +
                ", currentPremiumSince='" + currentPremiumSince + '\'' +
                '}';
    }
}
