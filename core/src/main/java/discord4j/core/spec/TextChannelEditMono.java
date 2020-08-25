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
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Mono used to modify a guild {@link TextChannel} settings.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class TextChannelEditMono extends AuditableRequest<TextChannel, ImmutableChannelModifyRequest.Builder, TextChannelEditMono> {

    private final GatewayDiscordClient gateway;
    private final long channelId;

    public TextChannelEditMono(Supplier<ImmutableChannelModifyRequest.Builder> requestBuilder, @Nullable String reason,
                               GatewayDiscordClient gateway, long channelId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.channelId = channelId;
    }

    public TextChannelEditMono(GatewayDiscordClient gateway, long channelId) {
        this(ImmutableChannelModifyRequest::builder, null, gateway, channelId);
    }

    @Override
    public TextChannelEditMono withReason(String reason) {
        return new TextChannelEditMono(requestBuilder, reason, gateway, channelId);
    }

    @Override
    TextChannelEditMono withBuilder(UnaryOperator<ImmutableChannelModifyRequest.Builder> f) {
        return new TextChannelEditMono(apply(f), reason, gateway, channelId);
    }

    /**
     * Sets the name of the modified {@link TextChannel}.
     *
     * @param name The channel name.
     * @return This mono.
     */
    public TextChannelEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the position of the modified {@link TextChannel}.
     *
     * @param position The channel position.
     * @return This mono.
     */
    public TextChannelEditMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
    }

    /**
     * Sets the topic of the modified {@link TextChannel}.
     *
     * @param topic The channel topic.
     * @return This mono.
     */
    public TextChannelEditMono withTopic(String topic) {
        return withBuilder(it -> it.topic(topic));
    }

    /**
     * Sets whether the modified {@link TextChannel} should be NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This mono.
     */
    public TextChannelEditMono withNsfw(boolean nsfw) {
        return withBuilder(it -> it.nsfw(nsfw));
    }

    /**
     * Sets the modified {@link TextChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This mono.
     */
    public TextChannelEditMono withPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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
     * Sets the identifier of the parent category of the modified {@link TextChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This mono.
     */
    public TextChannelEditMono withParentId(@Nullable Snowflake parentId) {
        return withBuilder(it -> it.parentId(parentId == null ? Possible.absent() : Possible.of(Optional.of(parentId.asString()))));
    }

    /**
     * Sets the amount of seconds a user has to wait before sending another message to the modified
     * {@link TextChannel}, from 0 to 21600 seconds. Does not affect bots or users with {@link Permission#MANAGE_MESSAGES} or
     * {@link Permission#MANAGE_CHANNELS} permissions.
     *
     * @param rateLimitPerUser The channel user rate limit, in seconds.
     * @return This mono.
     */
    public TextChannelEditMono withRateLimitPerUser(int rateLimitPerUser) {
        return withBuilder(it -> it.rateLimitPerUser(rateLimitPerUser));
    }

    @Override
    Mono<TextChannel> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getChannelService()
            .modifyChannel(channelId, requestBuilder.get().build(), reason))
            .map(bean -> EntityUtil.getChannel(gateway, bean))
            .cast(TextChannel.class);
    }
}
