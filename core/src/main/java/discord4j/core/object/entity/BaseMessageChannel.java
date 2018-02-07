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

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

/** An internal implementation of {@link MessageChannel} designed to streamline inheritance. */
class BaseMessageChannel extends BaseChannel implements MessageChannel {

	/**
	 * Constructs an {@code BaseMessageChannel} with an associated client and Discord data.
	 *
	 * @param client The Client associated to this object, must be non-null.
	 * @param channel The raw data as represented by Discord, must be non-null.
	 */
	BaseMessageChannel(final Client client, final ChannelResponse channel) {
		super(client, channel);
	}

	@Override
	public Optional<Snowflake> getLastMessageId() {
		return Optional.ofNullable(getChannel().getLastMessageId()).map(Snowflake::of);
	}

	@Override
	public Mono<Message> getLastMessage() {
		throw new UnsupportedOperationException("Not yet implemented...");
	}

	@Override
	public Optional<Instant> getLastPinTimestamp() {
		return Optional.ofNullable(getChannel().getLastPinTimestamp()).map(Instant::parse);
	}
}
