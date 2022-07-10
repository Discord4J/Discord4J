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

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LegacySpec used to create guild {@link TextChannel} entities.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-channel">Create Guild Channel</a>
 */
public class LegacyTextChannelCreateSpec implements LegacyAuditSpec<ChannelCreateRequest> {

    private final ImmutableChannelCreateRequest.Builder requestBuilder = ChannelCreateRequest.builder()
            .type(Channel.Type.GUILD_TEXT.getValue());
    @Nullable
    private String reason;

    /**
     * Sets the name of the created {@link TextChannel}.
     *
     * @param name The channel name.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the created {@link TextChannel} topic.
     *
     * @param topic The channel topic.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setTopic(@Nullable String topic) {
        requestBuilder.topic(topic == null ? Possible.absent() : Possible.of(topic));
        return this;
    }

    /**
     * Sets the amount of seconds a user has to wait before sending another message to the created
     * {@link TextChannel}, from 0 to 120. Does not affect bots or users with {@link Permission#MANAGE_MESSAGES} or
     * {@link Permission#MANAGE_CHANNELS} permissions.
     *
     * @param rateLimitPerUser The channel user rate limit, in seconds.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setRateLimitPerUser(int rateLimitPerUser) {
        requestBuilder.rateLimitPerUser(rateLimitPerUser);
        return this;
    }

    /**
     * Sets the sorting position of the created {@link TextChannel}.
     *
     * @param position The channel position.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setPosition(int position) {
        requestBuilder.position(position);
        return this;
    }

    /**
     * Sets the created {@link TextChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        List<OverwriteData> raw = permissionOverwrites.stream()
                .map(PermissionOverwrite::getData)
                .collect(Collectors.toList());

        requestBuilder.permissionOverwrites(raw);
        return this;
    }

    /**
     * Sets the identifier of the parent category of the created {@link TextChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setParentId(@Nullable Snowflake parentId) {
        requestBuilder.parentId(parentId == null ? Possible.absent() : Possible.of(parentId.asString()));
        return this;
    }

    /**
     * Sets whether the created {@link TextChannel} is NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This spec.
     */
    public LegacyTextChannelCreateSpec setNsfw(boolean nsfw) {
        requestBuilder.nsfw(nsfw);
        return this;
    }

    @Override
    public LegacyTextChannelCreateSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public ChannelCreateRequest asRequest() {
        return requestBuilder.build();
    }
}
