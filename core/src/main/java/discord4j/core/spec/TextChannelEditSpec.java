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
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spec used to modify a guild {@link TextChannel} settings.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class TextChannelEditSpec implements Spec<ChannelModifyRequest> {

    private final ImmutableChannelModifyRequest.Builder requestBuilder = ChannelModifyRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the name of the modified {@link TextChannel}.
     *
     * @param name The channel name.
     * @return This spec.
     */
    public TextChannelEditSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the position of the modified {@link TextChannel}.
     *
     * @param position The channel position.
     * @return This spec.
     */
    public TextChannelEditSpec setPosition(int position) {
        requestBuilder.position(position);
        return this;
    }

    /**
     * Sets the topic of the modified {@link TextChannel}.
     *
     * @param topic The channel topic.
     * @return This spec.
     */
    public TextChannelEditSpec setTopic(String topic) {
        requestBuilder.topic(topic);
        return this;
    }

    /**
     * Sets whether the modified {@link TextChannel} should be NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This spec.
     */
    public TextChannelEditSpec setNsfw(boolean nsfw) {
        requestBuilder.nsfw(nsfw);
        return this;
    }

    /**
     * Sets the modified {@link TextChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This spec.
     */
    public TextChannelEditSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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
     * Sets the identifier of the parent category of the modified {@link TextChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This spec.
     */
    public TextChannelEditSpec setParentId(@Nullable Snowflake parentId) {
        requestBuilder.parentId(parentId == null ? Possible.of(Optional.empty()) : Possible.of(Optional.of(parentId.asString())));
        return this;
    }

    /**
     * Sets the amount of seconds a user has to wait before sending another message to the modified
     * {@link TextChannel}, from 0 to 21600 seconds. Does not affect bots or users with {@link Permission#MANAGE_MESSAGES} or
     * {@link Permission#MANAGE_CHANNELS} permissions.
     *
     * @param rateLimitPerUser The channel user rate limit, in seconds.
     * @return This spec.
     */
    public TextChannelEditSpec setRateLimitPerUser(int rateLimitPerUser) {
        requestBuilder.rateLimitPerUser(rateLimitPerUser);
        return this;
    }

    public TextChannelEditSpec setReason(@Nullable final String reason) {
        this.reason = reason;
        return this;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public ChannelModifyRequest asRequest() {
        return requestBuilder.build();
    }
}
