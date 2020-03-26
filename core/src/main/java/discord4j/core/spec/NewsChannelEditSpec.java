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

import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.ImmutableOverwriteData;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.rest.util.Snowflake;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spec used to modify a guild {@link NewsChannel} settings.
 *G
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class NewsChannelEditSpec implements AuditSpec<ChannelModifyRequest> {

    private final ImmutableChannelModifyRequest.Builder requestBuilder = ImmutableChannelModifyRequest.builder();
    @Nullable
    private String reason;

    /**
     * Sets the name of the modified {@link NewsChannel}.
     *
     * @param name The channel name.
     * @return This spec.
     */
    public NewsChannelEditSpec setName(String name) {
        requestBuilder.name(Possible.of(name));
        return this;
    }

    /**
     * Sets the position of the modified {@link NewsChannel}.
     *
     * @param position The channel position.
     * @return This spec.
     */
    public NewsChannelEditSpec setPosition(int position) {
        requestBuilder.position(Possible.of(position));
        return this;
    }

    /**
     * Sets the topic of the modified {@link NewsChannel}.
     *
     * @param topic The channel topic.
     * @return This spec.
     */
    public NewsChannelEditSpec setTopic(String topic) {
        requestBuilder.topic(Possible.of(topic));
        return this;
    }

    /**
     * Sets whether the modified {@link NewsChannel} should be NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This spec.
     */
    public NewsChannelEditSpec setNsfw(boolean nsfw) {
        requestBuilder.nsfw(Possible.of(nsfw));
        return this;
    }

    /**
     * Sets the modified {@link NewsChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This spec.
     */
    public NewsChannelEditSpec setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        List<OverwriteData> raw = permissionOverwrites.stream()
                .map(o -> ImmutableOverwriteData.of(o.getTargetId().asString(), o.getType().getValue(),
                        o.getAllowed().getRawValue(), o.getDenied().getRawValue()))
                .collect(Collectors.toList());

        requestBuilder.permissionOverwrites(Possible.of(raw));
        return this;
    }

    /**
     * Sets the identifier of the parent category of the modified {@link NewsChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This spec.
     */
    public NewsChannelEditSpec setParentId(@Nullable Snowflake parentId) {
        requestBuilder.parentId(Possible.of(Optional.ofNullable(parentId).map(Snowflake::asString)));
        return this;
    }

    @Override
    public NewsChannelEditSpec setReason(@Nullable final String reason) {
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
