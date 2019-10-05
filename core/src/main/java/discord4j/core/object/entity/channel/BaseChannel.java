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

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.data.stored.ChannelBean;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.EntityUtil;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;

/** An internal implementation of {@link Channel} designed to streamline inheritance. */
class BaseChannel implements Channel {

    /** The raw data as represented by Discord. */
    private final ChannelBean data;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /**
     * Constructs a {@code BaseChannel} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseChannel(final GatewayDiscordClient gateway, final ChannelBean data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public final DiscordClient getClient() {
        return gateway.rest();
    }

    @Override
    public GatewayDiscordClient getGateway() {
        return gateway;
    }

    @Override
    public final Snowflake getId() {
        return Snowflake.of(data.getId());
    }

    @Override
    public final Type getType() {
        return Type.of(data.getType());
    }

    @Override
    public final Mono<Void> delete(@Nullable final String reason) {
        return gateway.getRestClient().getChannelService()
                .deleteChannel(getId().asLong(), reason)
                .then();
    }

    /**
     * Gets the raw data as represented by Discord.
     *
     * @return The raw data as represented by Discord.
     */
    ChannelBean getData() {
        return data;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !BaseChannel.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        BaseChannel that = (BaseChannel) obj;
        return getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return EntityUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "BaseChannel{" +
                "data=" + data +
                '}';
    }
}
