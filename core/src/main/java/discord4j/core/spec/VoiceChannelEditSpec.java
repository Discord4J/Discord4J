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
package discord4j.core.spec;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import discord4j.rest.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spec used to modify a {@link VoiceChannel} entity.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class VoiceChannelEditSpec implements AuditSpec<ChannelModifyRequest> {

    private final ImmutableChannelModifyRequest.Builder requestBuilder = ChannelModifyRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the name for the modified {@link VoiceChannel}.
     *
     * @param name The name of the voice channel.
     * @return This spec.
     */
    public VoiceChannelEditSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the position for the modified {@link VoiceChannel}.
     *
     * @param position The raw position for the channel.
     * @return This spec.
     */
    public VoiceChannelEditSpec setPosition(int position) {
        requestBuilder.position(position);
        return this;
    }

    /**
     * Sets the permission overwrites for the modified {@link VoiceChannel}.
     *
     * @param permissionOverwrites The {@code Set<PermissionOverwrite>} which contains overwrites for the channel.
     * @return This spec.
     */
    public VoiceChannelEditSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        List<OverwriteData> raw = permissionOverwrites.stream()
                .map(o -> OverwriteData.builder()
                        .id(o.getTargetId().asString())
                        .type(o.getType().getValue())
                        .allow(o.getAllowed().getRawValue())
                        .deny(o.getDenied().getRawValue())
                        .build())
                .collect(Collectors.toList());

        requestBuilder.permissionOverwrites(raw);
        return this;
    }

    /**
     * Sets the parent ID for the modified {@link VoiceChannel}.
     * <p>
     * The parent ID is equivalent to a {@link Category} ID.
     *
     * @param parentId The {@code Snowflake} of the parent {@code Category}.
     * @return This spec.
     */
    public VoiceChannelEditSpec setParentId(@Nullable Snowflake parentId) {
        requestBuilder.parentId(parentId == null ? Possible.of(Optional.empty()) : Possible.of(Optional.of(parentId.asString())));
        return this;
    }

    /**
     * Sets the bitrate for the modified {@link VoiceChannel}.
     *
     * @param bitrate The maximum amount of bits to send per second in the voice channel, related to the quality of
     * audio. A valid bitrate is a number from 8 to 96.
     * @return This spec.
     */
    public VoiceChannelEditSpec setBitrate(int bitrate) {
        requestBuilder.bitrate(bitrate);
        return this;
    }

    /**
     * Sets the user limit for the modified {@link VoiceChannel}.
     * <p>
     * Users with {@link Permission#MOVE_MEMBERS} ignore this limit and can also move other users into the channel
     * past the limit.
     *
     * @param userLimit The maximum number of users that can join the voice channel at once.
     * @return This spec.
     */
    public VoiceChannelEditSpec setUserLimit(int userLimit) {
        requestBuilder.userLimit(userLimit);
        return this;
    }

    @Override
    public VoiceChannelEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public ChannelModifyRequest asRequest() {
        return requestBuilder.build();
    }
}
