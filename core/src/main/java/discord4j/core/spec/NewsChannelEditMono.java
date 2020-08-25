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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Mono used to modify a guild {@link NewsChannel} settings.
 * G
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class NewsChannelEditMono extends AuditableRequest<NewsChannel, ImmutableChannelModifyRequest.Builder, NewsChannelEditMono> {

    private final GatewayDiscordClient gateway;
    private final long channelId;

    public NewsChannelEditMono(Supplier<ImmutableChannelModifyRequest.Builder> requestBuilder, @Nullable String reason,
                               GatewayDiscordClient gateway, long channelId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.channelId = channelId;
    }

    public NewsChannelEditMono(GatewayDiscordClient gateway, long channelId) {
        this(ImmutableChannelModifyRequest::builder, null, gateway, channelId);
    }

    @Override
    public NewsChannelEditMono withReason(String reason) {
        return new NewsChannelEditMono(requestBuilder, reason, gateway, channelId);
    }

    @Override
    NewsChannelEditMono withBuilder(UnaryOperator<ImmutableChannelModifyRequest.Builder> f) {
        return new NewsChannelEditMono(apply(f), reason, gateway, channelId);
    }

    /**
     * Sets the name of the modified {@link NewsChannel}.
     *
     * @param name The channel name.
     * @return This mono.
     */
    public NewsChannelEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the position of the modified {@link NewsChannel}.
     *
     * @param position The channel position.
     * @return This mono.
     */
    public NewsChannelEditMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
    }

    /**
     * Sets the topic of the modified {@link NewsChannel}.
     *
     * @param topic The channel topic.
     * @return This mono.
     */
    public NewsChannelEditMono withTopic(String topic) {
        return withBuilder(it -> it.topic(topic));
    }

    /**
     * Sets whether the modified {@link NewsChannel} should be NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This mono.
     */
    public NewsChannelEditMono withNsfw(boolean nsfw) {
        return withBuilder(it -> it.nsfw(nsfw));
    }

    /**
     * Sets the modified {@link NewsChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This mono.
     */
    public NewsChannelEditMono withPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
        return withBuilder(it -> it.permissionOverwrites(
            permissionOverwrites.stream()
                .map(o -> OverwriteData.builder()
                    .id(o.getTargetId().asString())
                    .type(o.getType().getValue())
                    .allow(o.getAllowed().getRawValue())
                    .deny(o.getDenied().getRawValue())
                    .build())
                .collect(Collectors.toList())
        ));
    }

    /**
     * Sets the identifier of the parent category of the modified {@link NewsChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This mono.
     */
    public NewsChannelEditMono withParentId(@Nullable Snowflake parentId) {
        return withBuilder(it -> it.parentId(Possible.of(Optional.ofNullable(parentId).map(Snowflake::asString))));
    }

    @Override
    Mono<NewsChannel> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getChannelService()
            .modifyChannel(channelId, requestBuilder.get().build(), reason)).map(data -> EntityUtil.getChannel(gateway, data))
            .cast(NewsChannel.class);
    }
}
