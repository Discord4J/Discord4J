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

import discord4j.common.json.response.ChannelResponse;
import discord4j.core.Client;
import discord4j.core.object.Snowflake;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/** An internal implementation of {@link GuildChannel} designed to streamline inheritance. */
class BaseGuildChannel extends BaseChannel implements GuildChannel {

	/**
	 * Constructs an {@code BaseGuildChannel} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param channel The raw data as represented by Discord, must be non-null.
	 */
	BaseGuildChannel(final Client client, final ChannelResponse channel) {
		super(client, channel);
	}

	@Override
	public final Snowflake getGuildId() {
		if (getChannel().getGuildId() == null) throw new IllegalStateException();
		return Snowflake.of(getChannel().getGuildId());
	}

	@Override
	public final Mono<Guild> getGuild() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	@Override
	public final Set<PermissionOverwrite> getPermissionOverwrites() {
		return Optional.ofNullable(getChannel().getPermissionOverwrites())
				.map(Arrays::stream)
				.map(overwrites -> overwrites.map(overwrite -> new PermissionOverwrite(getClient(), overwrite)))
				.map(overwrites -> overwrites.collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
	}

	@Override
	public final String getName() {
		return getChannel().getName();
	}

	@Override
	public final Optional<Snowflake> getCategoryId() {
		return Optional.ofNullable(getChannel().getParentId()).map(Snowflake::of);
	}

	@Override
	public final Mono<Category> getCategory() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	@Override
	public final Mono<Integer> getPosition() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}
}
