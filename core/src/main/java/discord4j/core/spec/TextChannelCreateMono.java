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
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Mono used to create guild {@link TextChannel} entities.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-channel">Create Guild Channel</a>
 */
public class TextChannelCreateMono extends AuditableRequest<TextChannel, ImmutableChannelCreateRequest.Builder, TextChannelCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;

    public TextChannelCreateMono(Supplier<ImmutableChannelCreateRequest.Builder> requestBuilder, @Nullable String reason,
                                 GatewayDiscordClient gateway, long guildId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public TextChannelCreateMono(GatewayDiscordClient gateway, long guildId) {
        this(ImmutableChannelCreateRequest::builder, null, gateway, guildId);
    }

    @Override
    public TextChannelCreateMono withReason(String reason) {
        return new TextChannelCreateMono(requestBuilder, reason, gateway, guildId);
    }

    @Override
    TextChannelCreateMono withBuilder(UnaryOperator<ImmutableChannelCreateRequest.Builder> f) {
        return new TextChannelCreateMono(apply(f), reason, gateway, guildId);
    }

    /**
     * Sets the name of the created {@link TextChannel}.
     *
     * @param name The channel name.
     * @return This mono.
     */
    public TextChannelCreateMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the created {@link TextChannel} topic.
     *
     * @param topic The channel topic.
     * @return This mono.
     */
    public TextChannelCreateMono withTopic(@Nullable String topic) {
        return withBuilder(it -> it.topic(topic == null ? Possible.absent() : Possible.of(topic)));
    }

    /**
     * Sets the amount of seconds a user has to wait before sending another message to the created
     * {@link TextChannel}, from 0 to 120. Does not affect bots or users with {@link Permission#MANAGE_MESSAGES} or
     * {@link Permission#MANAGE_CHANNELS} permissions.
     *
     * @param rateLimitPerUser The channel user rate limit, in seconds.
     * @return This mono.
     */
    public TextChannelCreateMono withRateLimitPerUser(int rateLimitPerUser) {
        return withBuilder(it -> it.rateLimitPerUser(rateLimitPerUser));
    }

    /**
     * Sets the sorting position of the created {@link TextChannel}.
     *
     * @param position The channel position.
     * @return This mono.
     */
    public TextChannelCreateMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
    }

    /**
     * Sets the created {@link TextChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This mono.
     */
    public TextChannelCreateMono setPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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
     * Sets the identifier of the parent category of the created {@link TextChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This mono.
     */
    public TextChannelCreateMono withParentId(@Nullable Snowflake parentId) {
        return withBuilder(it -> it.parentId(parentId == null ? Possible.absent() : Possible.of(parentId.asString())));
    }

    /**
     * Sets whether the created {@link TextChannel} is NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This mono.
     */
    public TextChannelCreateMono withNsfw(boolean nsfw) {
        return withBuilder(it -> it.nsfw(nsfw));
    }

    @Override
    Mono<TextChannel> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getGuildService()
            .createGuildChannel(guildId, requestBuilder.get().build(), reason))
            .map(data -> EntityUtil.getChannel(gateway, data))
            .cast(TextChannel.class);
    }
}
