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
import reactor.core.publisher.Mono;

import java.util.Set;

/** A customized emoji created by a {@link Guild}. */
public interface GuildEmoji extends Entity {

	/**
	 * Gets the emoji name.
	 *
	 * @return The emoji name.
	 */
	String getName();

	/**
	 * Gets the IDs of the roles this emoji is whitelisted to.
	 *
	 * @return The IDs of the roles this emoji is whitelisted to.
	 */
	Set<Snowflake> getRoleIds();

	/**
	 * Requests to retrieve the roles this emoji is whitelisted to.
	 *
	 * @return A {@link Flux} that continually emits the {@link Role roles} this emoji is whitelisted for. if an error
	 * is received, it is emitted through the {@code Flux}.
	 */
	Flux<Role> getRoles();

	/**
	 * Gets the ID of the user that created this emoji.
	 *
	 * @return The ID of the user that created this emoji.
	 */
	Snowflake getUserId();

	/**
	 * Requests to retrieve the user that created this emoji.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link User user} that created this emoji. If
	 * an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<User> getUser();

	/**
	 * Gets whether this emoji must be wrapped in colons.
	 *
	 * @return {@code true} if this emoji must be wrapped in colons, {@code false} otherwise.
	 */
	boolean requireColons();

	/**
	 * Gets whether this emoji is managed.
	 *
	 * @return {@code true} if this emoji is managed, {@code false} otherwise.
	 */
	boolean isManaged();

	/**
	 * Gets whether this emoji is animated.
	 *
	 * @return {@code true} if this emoji is animated, {@code false} otherwise.
	 */
	boolean isAnimated();

	/**
	 * Gets the ID of the guild this emoji is associated to.
	 *
	 * @return The ID of the guild this emoji is associated to.
	 */
	Snowflake getGuildId();

	/**
	 * Requests to retrieve the guild this emoji is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this emoji is associated
	 * to. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();
}
