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
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Mono used to create guild {@link NewsChannel} entities.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#create-guild-channel">Create Guild Channel</a>
 */
public class NewsChannelCreateMono extends AuditableRequest<NewsChannel, ImmutableChannelCreateRequest.Builder, NewsChannelCreateMono> {

    private final GatewayDiscordClient gateway;
    private final long guildId;

    public NewsChannelCreateMono(Supplier<ImmutableChannelCreateRequest.Builder> requestBuilder, @Nullable String reason,
                                 GatewayDiscordClient gateway, long guildId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.guildId = guildId;
    }

    public NewsChannelCreateMono(GatewayDiscordClient gateway, long guildId) {
        this(() -> ImmutableChannelCreateRequest.builder().type(Channel.Type.GUILD_NEWS.getValue()), null, gateway, guildId);
    }

    @Override
    public NewsChannelCreateMono withReason(String reason) {
        return new NewsChannelCreateMono(requestBuilder, reason, gateway, guildId);
    }

    @Override
    NewsChannelCreateMono withBuilder(UnaryOperator<ImmutableChannelCreateRequest.Builder> f) {
        return new NewsChannelCreateMono(apply(f), reason, gateway, guildId);
    }

    /**
     * Sets the name of the created {@link NewsChannel}.
     *
     * @param name The channel name.
     * @return This mono.
     */
    public NewsChannelCreateMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the created {@link NewsChannel} topic.
     *
     * @param topic The channel topic.
     * @return This mono.
     */
    public NewsChannelCreateMono withTopic(String topic) {
        return withBuilder(it -> it.topic(topic));
    }

    /**
     * Sets the sorting position of the created {@link NewsChannel}.
     *
     * @param position The channel position.
     * @return This mono.
     */
    public NewsChannelCreateMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
    }

    /**
     * Sets the created {@link NewsChannel} permission overwrites.
     *
     * @param permissionOverwrites The set of {@link PermissionOverwrite} objects.
     * @return This mono.
     */
    public NewsChannelCreateMono withPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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
     * Sets the identifier of the parent category of the created {@link NewsChannel}.
     *
     * @param parentId The parent category identifier.
     * @return This mono.
     */
    public NewsChannelCreateMono withParentId(@Nullable Snowflake parentId) {
        return withBuilder(it -> it.parentId(parentId == null ? Possible.absent() : Possible.of(parentId.asString())));
    }

    /**
     * Sets whether the created {@link NewsChannel} is NSFW (not safe for work).
     *
     * @param nsfw The channel nsfw property.
     * @return This mono.
     */
    public NewsChannelCreateMono withNsfw(boolean nsfw) {
        return withBuilder(it -> it.nsfw(nsfw));
    }

    @Override
    Mono<NewsChannel> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getGuildService()
            .createGuildChannel(guildId, requestBuilder.get().build(), reason))
            .map(data -> EntityUtil.getChannel(gateway, data))
            .cast(NewsChannel.class);
    }
}
