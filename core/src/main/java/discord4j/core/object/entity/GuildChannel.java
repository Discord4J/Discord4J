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
import discord4j.core.trait.Deletable;
import discord4j.core.trait.Positionable;
import discord4j.core.trait.Renameable;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

/** A Discord channel associated to a {@link Guild}. */
public interface GuildChannel<T extends GuildChannel<T>> extends Channel, Deletable, Positionable<T>, Renameable<T> {

	/**
	 * Gets the ID of the guild this channel is associated to.
	 *
	 * @return The ID of the guild this channel is associated to.
	 */
	Snowflake getGuildId();

	/**
	 * Requests to retrieve the guild this channel is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this channel is associated to.
	 * If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();

	/**
	 * Gets the permission overwrites for this channel.
	 *
	 * @return The permission overwrites for this channel.
	 */
	Set<PermissionOverwrite> getPermissionOverwrites();

	/**
	 * Gets the ID of the category for this channel, if present.
	 *
	 * @return The ID of the category for this channel, if present.
	 */
	Optional<Snowflake> getCategoryId();

	/**
	 * Requests to retrieve the category for this channel, if present.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Category} this channel, if present. If
	 * an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Category> getCategory();
}
