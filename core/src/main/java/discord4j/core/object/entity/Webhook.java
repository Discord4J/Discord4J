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
import discord4j.core.trait.Renameable;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * A Discord webhook.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/webhook">Webhook Resource</a>
 */
public interface Webhook extends Deletable, Entity, Renameable<Webhook> {

	/**
	 * Gets the ID of the guild this webhook is associated to, if present.
	 *
	 * @return The ID of the guild this webhook is associated to, if present.
	 */
	Optional<Snowflake> getGuildId();

	/**
	 * Requests to retrieve the guild this webhook is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild} this webhook is associated to,
	 * if present. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();

	/**
	 * Gets the ID of the channel this webhook is associated to.
	 *
	 * @return The ID of the channel this webhook is associated to.
	 */
	Snowflake getChannelId();

	/**
	 * Requests to retrieve the channel this webhook is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel} this webhook is
	 * associated to. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<TextChannel> getChannel();

	/**
	 * Gets the user this webhook was created by, if present.
	 *
	 * @return The user this webhook was created by, if present.
	 */
	Optional<User> getCreator();

	/**
	 * Gets the avatar of this webhook, if present.
	 *
	 * @return The avatar of this webhook, if present.
	 */
	Optional<String> getAvatar();

	/**
	 * Gets the secure token of this webhook.
	 *
	 * @return The secure token of this webhook.
	 */
	String getToken();
}
