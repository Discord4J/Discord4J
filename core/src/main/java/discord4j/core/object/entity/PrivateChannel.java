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
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** A Discord private channel (also known as a DM). */
public final class PrivateChannel extends BaseMessageChannel {

	/**
	 * Constructs an {@code PrivateChannel} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param data The raw data as represented by Discord, must be non-null.
	 */
	public PrivateChannel(final Client client, final ChannelData data) {
		super(client, data);
	}

	/**
	 * Gets the IDs of the recipients for this private channel.
	 *
	 * @return The IDs of the recipients for this private channel.
	 */
	public Set<Snowflake> getRecipientIds() {
		if (data.getRecipients() == null) throw new IllegalStateException();

		return Arrays.stream(data.getRecipients())
				.mapToObj(Snowflake::of)
				.collect(Collectors.toSet());
	}

	/**
	 * Requests to retrieve the recipients for this private channel.
	 *
	 * @return A {@link Flux} that continually emits the {@link User recipients} for this private channel. If an error
	 * is received, it is emitted through the {@code Flux}.
	 */
	public Flux<User> getRecipients() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}
}
