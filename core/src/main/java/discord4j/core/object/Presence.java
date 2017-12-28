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
package discord4j.core.object;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.Optional;

/** An user's presence. */
public interface Presence {

	/**
	 * Gets the ID of the user this presence is for.
	 *
	 * @return The ID of the user this presence is for.
	 */
	Snowflake getUserId();

	/**
	 * Requests to retrieve the user this presence is for.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits a {@link Member} this presence is for. If an
	 * error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Member> getUser();

	/**
	 * Gets the ID of the guild this presence is for.
	 *
	 * @return The ID of the guild this presence is for.
	 */
	Snowflake getGuildId();

	/**
	 * Requests to retrieve the guild this presence is for.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits a {@link Guild} this presence is for. If an
	 * error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();

	/**
	 * Gets the text for this presence, if possible.
	 *
	 * @return The text for this presence, if possible.
	 */
	Optional<String> getText();

	/**
	 * Gets the streaming URL for this presence, if possible.
	 *
	 * @return The streaming URL for this presence, if possible.
	 */
	Optional<String> getStreamingUrl();

	/**
	 * Gets the activity for this presence, if possible.
	 *
	 * @return The activity for this presence, if possible.
	 */
	Optional<ActivityType> getActivityType();

	/**
	 * Gets the status for this presence.
	 *
	 * @return The status for this presence.
	 */
	StatusType getStatusType();
}
