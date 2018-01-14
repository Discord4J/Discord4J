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

import discord4j.core.object.Snowflake;
import reactor.core.publisher.Flux;

import java.util.Set;

/** A Discord private channel (also known as a DM). */
public interface PrivateChannel extends MessageChannel {

	/**
	 * Gets the IDs of the recipients for this private channel.
	 *
	 * @return The IDs of the recipients for this private channel.
	 */
	Set<Snowflake> getRecipientIds();

	/**
	 * Requests to retrieve the recipients for this private channel.
	 *
	 * @return A {@link Flux} that continually emits the {@link User recipients} for this private channel. If an error
	 * is received, it is emitted through the {@code Flux}.
	 */
	Flux<User> getRecipients();
}
