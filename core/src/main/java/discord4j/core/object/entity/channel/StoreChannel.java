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
package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.StoreChannelEditMono;
import discord4j.core.spec.StoreChannelEditSpec;
import discord4j.core.spec.legacy.LegacyStoreChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/** A Discord store channel. */
public final class StoreChannel extends BaseTopLevelGuildChannel implements CategorizableChannel {

    /**
     * Constructs an {@code StoreChannel} with an associated {@link GatewayDiscordClient} and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public StoreChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Requests to edit this store channel.
     *
     * @param spec A {@link Consumer} that provides a "blank" {@link LegacyStoreChannelEditSpec} to be operated on.
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StoreChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @deprecated use {@link #edit(StoreChannelEditSpec)} or {@link #edit()} which offer an immutable approach to build
     * specs
     */
    @Deprecated
    public Mono<StoreChannel> edit(final Consumer<? super LegacyStoreChannelEditSpec> spec) {
        return Mono.defer(
                () -> {
                    LegacyStoreChannelEditSpec mutatedSpec = new LegacyStoreChannelEditSpec();
                    spec.accept(mutatedSpec);
                    return getClient().getRestClient().getChannelService()
                            .modifyChannel(getId().asLong(), mutatedSpec.asRequest(), mutatedSpec.getReason());
                })
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(StoreChannel.class);
    }

    /**
     * Requests to edit this store channel. Properties specifying how to edit this store channel can be set via the
     * {@code withXxx} methods of the returned {@link StoreChannelEditMono}.
     *
     * @return A {@link StoreChannelEditMono} where, upon successful completion, emits the edited {@link StoreChannel}.
     * If an error is received, it is emitted through the {@code StoreChannelEditMono}.
     */
    public StoreChannelEditMono edit() {
        return StoreChannelEditMono.of(this);
    }

    /**
     * Requests to edit this store channel.
     *
     * @param spec an immutable object that specifies how to edit this store channel
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link StoreChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<StoreChannel> edit(StoreChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                        .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(getClient(), data))
                .cast(StoreChannel.class);
    }

    @Override
    public String toString() {
        return "StoreChannel{} " + super.toString();
    }
}
