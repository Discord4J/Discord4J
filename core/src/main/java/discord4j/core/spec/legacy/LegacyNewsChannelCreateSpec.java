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
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.common.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LegacySpec used to create guild {@link NewsChannel} entities.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-channel">Create Guild Channel</a>
 */
public class LegacyNewsChannelCreateSpec implements LegacyAuditSpec<ChannelCreateRequest> {

    private final ImmutableChannelCreateRequest.Builder requestBuilder = ChannelCreateRequest.builder()
            .type(Channel.Type.GUILD_NEWS.getValue());
    @Nullable
    private String reason;

    /**
     * Sets the name of the created {@link NewsChannel}.
     *
     * @param name The channel name.
     * @return This spec.
     */
    public LegacyNewsChannelCreateSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the created {@link NewsChannel} topic.
     *
     * @param topic The channel topic.
     * @return This spec.
     */
    public LegacyNewsChannelCreateSpec setTopic(String topic) {
        requestBuilder.topic(topic);
        return this;
    }

    /**
     * Sets the sorting position of the created {@link NewsChannel}.
     *
     * @param position The channel position.
     * @return This spec.
     */
    public LegacyNewsChannelCreateSpec setPosition(int position) {
        requestBuilder.position(position);
        return this;
    }

    /**
     * Sets the created {@link NewsChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This spec.
     */
    public LegacyNewsChannelCreateSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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
     * Sets the identifier of the parent category of the created {@link NewsChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This spec.
     */
    public LegacyNewsChannelCreateSpec setParentId(@Nullable Snowflake parentId) {
        requestBuilder.parentId(parentId == null ? Possible.absent() : Possible.of(parentId.asString()));
        return this;
    }

    /**
     * Sets whether the created {@link NewsChannel} is NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This spec.
     */
    public LegacyNewsChannelCreateSpec setNsfw(boolean nsfw) {
        requestBuilder.nsfw(nsfw);
        return this;
    }

    @Override
    public LegacyNewsChannelCreateSpec setReason(@Nullable final String reason) {
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
