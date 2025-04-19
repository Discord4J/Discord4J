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
import discord4j.core.object.entity.AvatarDecoration;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Role;
import discord4j.core.util.ImageUtil;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
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

    /** The path for member avatar image URLs. */
    private static final String AVATAR_IMAGE_PATH = "guilds/%s/users/%s/avatars/%s";
    /** The path for member banner image URLs. */
    private static final String BANNER_IMAGE_PATH = "banners/%s/%s";

    private final long guildId;
    private final long memberId;

    @Nullable
    private final Member old;

    private final Set<Long> currentRoleIds;
    @Nullable
    private final String currentNickname;
    @Nullable
    private final String currentAvatar;
    @Nullable
    private final String currentBanner;
    @Nullable
    private final String currentJoinedAt;
    @Nullable
    private final String currentPremiumSince;
    @Nullable
    private final Boolean currentPending;
    @Nullable
    private final String communicationDisabledUntil;
    @Nullable
    private final AvatarDecoration avatarDecoration;

    public MemberUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, long guildId, long memberId,
                             @Nullable Member old, Set<Long> currentRoleIds, @Nullable String currentNickname,
                             @Nullable String currentAvatar, @Nullable String currentBanner, @Nullable String currentJoinedAt,
                             @Nullable String currentPremiumSince,
                             @Nullable Boolean currentPending, @Nullable String communicationDisabledUntil,
                             @Nullable AvatarDecoration avatarDecoration) {
        super(gateway, shardInfo);

        this.guildId = guildId;
        this.memberId = memberId;
        this.old = old;
        this.currentRoleIds = currentRoleIds;
        this.currentNickname = currentNickname;
        this.currentAvatar = currentAvatar;
        this.currentBanner = currentBanner;
        this.currentJoinedAt = currentJoinedAt;
        this.currentPremiumSince = currentPremiumSince;
        this.currentPending = currentPending;
        this.communicationDisabledUntil = communicationDisabledUntil;
        this.avatarDecoration = avatarDecoration;
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
    public Set<Snowflake> getCurrentRoleIds() {
        return currentRoleIds.stream()
            .map(Snowflake::of)
            .collect(Collectors.toSet());
    }

    /**
     * Requests to receive the list of {@link Role} roles that the {@link Member} is currently assigned.
     *
     * @return A {@link Flux} emitting the {@link Role} roles that the {@link Member} is assigned.
     */
    public Flux<Role> getCurrentRoles() {
        return getClient().getGuildRoles(getGuildId())
            .filter(role -> currentRoleIds.contains(role.getId().asLong()));
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
     * Gets the current member's guild avatar URL, if present.
     *
     * @param format the format for the URL.
     * @return The current member's guild avatar URL, if present.
     */
    public Optional<String> getCurrentGuildAvatarUrl(Image.Format format) {
        return Optional.ofNullable(currentAvatar)
            .map(avatar -> ImageUtil.getUrl(String.format(AVATAR_IMAGE_PATH,
                guildId, Snowflake.asString(memberId), avatar), format));
    }


    /**
     * Gets the current member's guild avatar. This is the avatar at the url given by
     * {@link #getCurrentGuildAvatarUrl(Image.Format)}.
     *
     * @param format The format for the avatar.
     * @return a {@link Mono} where, upon successful completion, emits the current {@link Image guild avatar} of the
     * member.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getCurrentGuildAvatar(Image.Format format) {
        return Mono.justOrEmpty(getCurrentGuildAvatarUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the current member's guild banner URL, if present.
     *
     * @param format the format for the URL.
     * @return The current member's guild banner URL, if present.
     */
    public Optional<String> getCurrentGuildBannerUrl(Image.Format format) {
        return Optional.ofNullable(currentBanner)
            .map(avatar -> ImageUtil.getUrl(String.format(BANNER_IMAGE_PATH,
                guildId, Snowflake.asString(memberId), avatar), format));
    }


    /**
     * Gets the current member's guild banner. This is the banner at the url given by
     * {@link #getCurrentGuildBannerUrl(Image.Format)}.
     *
     * @param format The format for the banner.
     * @return a {@link Mono} where, upon successful completion, emits the current {@link Image guild banner} of the
     * member.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Image> getCurrentGuildBanner(Image.Format format) {
        return Mono.justOrEmpty(getCurrentGuildBannerUrl(format)).flatMap(Image::ofUrl);
    }

    /**
     * Gets the current join time of the {@link Member} involved in this event, if present. It is typically absent if
     * this event is caused by a lurking stage channel member.
     *
     * @return The current join time of the {@link Member} involved in this event, if present.
     */
    public Optional<Instant> getJoinTime() {
        return Optional.ofNullable(currentJoinedAt)
            .map(it -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(it, Instant::from));
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

    /**
     * Gets when the user ends their timeout, if present.
     *
     * @return When the user ends their timeout in the server, if present.
     */
    public Optional<Instant> getCommunicationDisabledUntil() {
        return Optional.ofNullable(communicationDisabledUntil)
            .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets the user avatar decoration, if present.
     *
     * @return The user avatar decoration, if present.
     */
    public Optional<AvatarDecoration> getAvatarDecoration() {
        return Optional.ofNullable(avatarDecoration);
    }

    @Override
    public String toString() {
        return "MemberUpdateEvent{" +
            "guildId=" + guildId +
            ", memberId=" + memberId +
            ", old=" + old +
            ", currentRoleIds=" + currentRoleIds +
            ", currentNickname='" + currentNickname + '\'' +
            ", currentAvatar='" + currentAvatar + '\'' +
            ", currentJoinedAt='" + currentJoinedAt + '\'' +
            ", currentPremiumSince='" + currentPremiumSince + '\'' +
            ", currentPending=" + currentPending +
            ", communicationDisabledUntil='" + communicationDisabledUntil + '\'' +
            ", avatarDecoration='" + avatarDecoration + '\'' +
            '}';
    }
}
