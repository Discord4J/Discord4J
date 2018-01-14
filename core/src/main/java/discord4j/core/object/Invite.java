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
import discord4j.core.object.entity.TextChannel;
import reactor.core.publisher.Mono;

/**
 * A Discord invite.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/invite">Invite Resource</a>
 */
public interface Invite {

	/**
	 * Gets the invite code (unique ID).
	 *
	 * @return The invite code (unique ID).
	 */
	String getCode();

	/**
	 * Gets the ID of the guild this invite is associated to.
	 *
	 * @return The ID of the guild this invite is associated to.
	 */
	Snowflake getGuildId();

	/**
	 * Requests to retrieve the guild this invite is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this invite is associated
	 * to. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();

	/**
	 * Gets the ID of the channel this invite is associated to.
	 *
	 * @return The ID of the channel this invite is associated to.
	 */
	Snowflake getChannelId();

	/**
	 * Requests to retrieve the channel this invite is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link TextChannel channel} this invite is
	 * associated to. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<TextChannel> getChannel();
}
