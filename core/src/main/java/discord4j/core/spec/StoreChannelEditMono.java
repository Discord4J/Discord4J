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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.channel.StoreChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ImmutableChannelModifyRequest;
import discord4j.discordjson.json.OverwriteData;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Mono used to modify a guild {@link StoreChannel} settings.
 *
 * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
 */
public class StoreChannelEditMono extends AuditableRequest<StoreChannel, ImmutableChannelModifyRequest.Builder, StoreChannelEditMono> {

    private final GatewayDiscordClient gateway;
    private final long channelId;

    public StoreChannelEditMono(Supplier<ImmutableChannelModifyRequest.Builder> requestBuilder, @Nullable String reason,
                                GatewayDiscordClient gateway, long channelId) {
        super(requestBuilder, reason);
        this.gateway = gateway;
        this.channelId = channelId;
    }

    public StoreChannelEditMono(GatewayDiscordClient gateway, long channelId) {
        this(ImmutableChannelModifyRequest::builder, null, gateway, channelId);
    }

    @Override
    public StoreChannelEditMono withReason(String reason) {
        return new StoreChannelEditMono(requestBuilder, reason, gateway, channelId);
    }

    @Override
    StoreChannelEditMono withBuilder(UnaryOperator<ImmutableChannelModifyRequest.Builder> f) {
        return new StoreChannelEditMono(apply(f), reason, gateway, channelId);
    }

    /**
     * Sets the name for the {@link StoreChannel}.
     *
     * @param name The new name of the category.
     * @return This mono.
     */
    public StoreChannelEditMono withName(String name) {
        return withBuilder(it -> it.name(name));
    }

    /**
     * Sets the position for the {@link StoreChannel}.
     *
     * @param position The raw position for the category.
     * @return This mono.
     */
    public StoreChannelEditMono withPosition(int position) {
        return withBuilder(it -> it.position(position));
    }

    /**
     * Sets the permission overwrites for the {@link StoreChannel}.
     *
     * @param permissionOverwrites The {@code Set<PermissionOverwrite>} which contains overwrites for the category.
     * @return This mono.
     */
    public StoreChannelEditMono withPermissionOverwrites(Set<? extends PermissionOverwrite> permissionOverwrites) {
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

    @Override
    Mono<StoreChannel> getRequest() {
        return Mono.defer(() -> gateway.getRestClient().getChannelService()
            .modifyChannel(channelId, requestBuilder.get().build(), reason))
            .map(data -> EntityUtil.getChannel(gateway, data))
            .cast(StoreChannel.class);
    }
}
