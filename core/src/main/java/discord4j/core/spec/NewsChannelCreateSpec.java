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

import discord4j.common.json.OverwriteEntity;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.rest.json.request.ChannelCreateRequest;
import reactor.util.annotation.Nullable;

import java.util.Set;

/**
 * Spec used to create guild {@link NewsChannel} entities.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/guild#create-guild-channel">Create Guild Channel</a>
 */
public class NewsChannelCreateSpec implements AuditSpec<ChannelCreateRequest> {

    private final ChannelCreateRequest.Builder requestBuilder = ChannelCreateRequest.builder()
            .type(Channel.Type.GUILD_NEWS.getValue());
    @Nullable
    private String reason;

    /**
     * Sets the name of the created {@link NewsChannel}.
     *
     * @param name The channel name.
     * @return This spec.
     */
    public NewsChannelCreateSpec setName(String name) {
        requestBuilder.name(name);
        return this;
    }

    /**
     * Sets the created {@link NewsChannel} topic.
     *
     * @param topic The channel topic.
     * @return This spec.
     */
    public NewsChannelCreateSpec setTopic(@Nullable String topic) {
        requestBuilder.topic(topic);
        return this;
    }

    /**
     * Sets the sorting position of the created {@link NewsChannel}.
     *
     * @param position The channel position.
     * @return This spec.
     */
    public NewsChannelCreateSpec setPosition(int position) {
        requestBuilder.setPosition(position);
        return this;
    }

    /**
     * Sets the created {@link NewsChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This spec.
     */
    public NewsChannelCreateSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        OverwriteEntity[] raw = permissionOverwrites.stream()
                .map(o -> new OverwriteEntity(o.getTargetId().asLong(), o.getType().getValue(),
                        o.getAllowed().getRawValue(), o.getDenied().getRawValue()))
                .toArray(OverwriteEntity[]::new);

        requestBuilder.permissionOverwrites(raw);
        return this;
    }

    /**
     * Sets the identifier of the parent category of the created {@link NewsChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This spec.
     */
    public NewsChannelCreateSpec setParentId(@Nullable Snowflake parentId) {
        requestBuilder.parentId(parentId == null ? null : parentId.asLong());
        return this;
    }

    /**
     * Sets whether the created {@link NewsChannel} is NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This spec.
     */
    public NewsChannelCreateSpec setNsfw(boolean nsfw) {
        requestBuilder.nsfw(nsfw);
        return this;
    }

    @Override
    public NewsChannelCreateSpec setReason(@Nullable final String reason) {
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
