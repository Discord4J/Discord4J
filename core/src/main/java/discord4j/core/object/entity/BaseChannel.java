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

import discord4j.core.Client;
import discord4j.core.object.Snowflake;
import discord4j.core.object.data.ChannelData;

import java.util.Arrays;
import java.util.Objects;

/** An internal implementation of {@link Channel} designed to streamline inheritance. */
class BaseChannel implements Channel {

	/** The Client associated to this object. */
	private final Client client;

	/** The raw data as represented by Discord. */
	protected final ChannelData data;

	/**
	 * Constructs an {@code BaseChannel} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param data The raw data as represented by Discord, must be non-null.
	 */
	BaseChannel(final Client client, final ChannelData data) {
		this.client = Objects.requireNonNull(client);
		this.data = Objects.requireNonNull(data);
	}

	@Override
	public final Client getClient() {
		return client;
	}

	@Override
	public final Snowflake getId() {
		return Snowflake.of(data.getId());
	}

	@Override
	public final Type getType() {
		return Arrays.stream(Type.values())
				.filter(type -> type.getValue() == data.getType())
				.findFirst() // If this throws Discord added something
				.orElseThrow(UnsupportedOperationException::new);
	}
}
