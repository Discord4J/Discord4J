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
package discord4j.core.spec.legacy;

import discord4j.discordjson.json.GuildMemberModifyRequest;
import discord4j.discordjson.json.ImmutableGuildMemberModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import discord4j.common.util.Snowflake;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LegacySpec used to modify guild members.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#modify-guild-member">Modify Guild Member</a>
 */
public class LegacyGuildMemberEditSpec implements LegacyAuditSpec<GuildMemberModifyRequest> {

    private final ImmutableGuildMemberModifyRequest.Builder builder = GuildMemberModifyRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the new voice channel to move the targeted {@link Member}, if they are connected to voice. Requires the
     * {@link Permission#MOVE_MEMBERS} permission.
     *
     * @param channel The voice channel identifier or {@code null} to disconnect from voice channel.
     * @return This spec.
     */
    public LegacyGuildMemberEditSpec setNewVoiceChannel(@Nullable Snowflake channel) {
        builder.channelId(Possible.of(Optional.ofNullable(channel).map(Snowflake::asString)));
        return this;
    }

    /**
     * Sets whether the targeted {@link Member} is muted in voice channels, if they are connected to voice. Requires the
     * {@link Permission#MUTE_MEMBERS} permission.
     *
     * @param mute {@code true} if the {@link Member} should be muted, {@code false} otherwise.
     * @return This spec.
     */
    public LegacyGuildMemberEditSpec setMute(boolean mute) {
        builder.mute(mute);
        return this;
    }

    /**
     * Sets whether the targeted {@link Member} is deafened in voice channels, if they are connected to voice. Requires the
     * {@link Permission#DEAFEN_MEMBERS} permission.
     *
     * @param deaf {@code true} if the {@link Member} should be deafened, {@code false} otherwise.
     * @return This spec.
     */
    public LegacyGuildMemberEditSpec setDeafen(boolean deaf) {
        builder.deaf(deaf);
        return this;
    }

    /**
     * Sets a new nickname to the targeted {@link Member}. Requires the {@link Permission#MANAGE_NICKNAMES} permission.
     *
     * @param nickname The new nickname, can be {@code null} or an empty string to reset.
     * @return This spec.
     */
    public LegacyGuildMemberEditSpec setNickname(@Nullable String nickname) {
        builder.nickOrNull(nickname);
        return this;
    }

    /**
     * Sets the new role identifiers the targeted {@link Member} is assigned. Requires the
     * {@link Permission#MANAGE_ROLES} permission.
     *
     * @param roles The set of role identifiers.
     * @return This spec.
     */
    public LegacyGuildMemberEditSpec setRoles(Set<Snowflake> roles) {
        builder.roles(roles.stream().map(Snowflake::asString).collect(Collectors.toList()));
        return this;
    }

    @Override
    public LegacyGuildMemberEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public GuildMemberModifyRequest asRequest() {
        return builder.build();
    }
}
