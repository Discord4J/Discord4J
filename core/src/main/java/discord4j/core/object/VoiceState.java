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

import discord4j.core.object.entity.AudioChannel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Optional;

/** Used to represent a user's voice connection status. */
public interface VoiceState {

	/**
	 * Gets the guild ID this voice state is for, if present.
	 *
	 * @return The guild ID this voice state is for, if present.
	 */
	Optional<Snowflake> getGuildId();

	/**
	 * Requests to retrieve the guild this voice state is for, if possible.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this voice state is for, if
	 * possible. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();

	/**
	 * Gets the channel ID this user is connected to.
	 *
	 * @return The channel ID this user is connected to.
	 */
	Snowflake getChannelId();

	/**
	 * Requests to retrieve the channel this user is connected to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link AudioChannel} this user is connected
	 * to. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<AudioChannel> getChannel();

	/**
	 * Gets the user ID this voice state is for.
	 *
	 * @return The user ID this voice state is for.
	 */
	Snowflake getUserId();

	/**
	 * Requests to retrieve the user this voice state is for.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User} this voice state is for. If an
	 * error is received, it is emitted through the {@code Mono}.
	 */
	Mono<User> getUser();

	/**
	 * Gets the session ID for this voice state.
	 *
	 * @return The session ID for this voice state.
	 */
	String getSessionId();

	/**
	 * Gets whether this user is deafened by the server.
	 *
	 * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
	 */
	boolean isDeaf();

	/**
	 * Gets whether this user is muted by the server.
	 *
	 * @return {@code true} if the user is deafened by the server, {@code false} otherwise.
	 */
	boolean isMuted();

	/**
	 * Gets whether this user is locally deafened.
	 *
	 * @return {@code true} if this user is locally deafened, {@code false} otherwise.
	 */
	boolean isSelfDeaf();

	/**
	 * Gets whether this user is locally muted.
	 *
	 * @return {@code true} if this user is locally muted, {@code false} otherwise.
	 */
	boolean isSelfMuted();

	/**
	 * Gets whether this user is muted by the current user.
	 *
	 * @return {@code true} if this user is muted by the current user, {@code false} otherwise.
	 */
	boolean isSuppressed();
}
