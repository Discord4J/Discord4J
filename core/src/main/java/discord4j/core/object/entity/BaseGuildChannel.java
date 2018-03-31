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
package discord4j.core.object.entity;

import discord4j.core.ServiceMediator;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.Snowflake;
import discord4j.core.object.entity.bean.GuildChannelBean;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

/** An internal implementation of {@link GuildChannel} designed to streamline inheritance. */
class BaseGuildChannel extends BaseChannel implements GuildChannel {

    /**
     * Constructs an {@code BaseGuildChannel} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    BaseGuildChannel(final ServiceMediator serviceMediator, final GuildChannelBean data) {
        super(serviceMediator, data);
    }

    @Override
    public final Snowflake getGuildId() {
        return Snowflake.of(getData().getGuildId());
    }

    @Override
    public final Mono<Guild> getGuild() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    @Override
    public final Set<PermissionOverwrite> getPermissionOverwrites() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    @Override
    public final String getName() {
        return getData().getName();
    }

    @Override
    public final Optional<Snowflake> getCategoryId() {
        return Optional.ofNullable(getData().getParentId()).map(Snowflake::of);
    }

    @Override
    public final Mono<Category> getCategory() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }

    @Override
    protected GuildChannelBean getData() {
        return (GuildChannelBean) super.getData();
    }

    @Override
    public final Mono<Integer> getPosition() {
        throw new UnsupportedOperationException("Not yet implemented...");
    }
}
