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
import reactor.core.publisher.Mono;

/**
 * A Discord user.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/user">Users Resource</a>
 */
public interface User extends Entity {

	/**
	 * Gets the user's username, not unique across the platform.
	 *
	 * @return The user's username, not unique across the platform.
	 */
	String getUsername();

	/**
	 * Gets the user's 4-digit discord-tag.
	 *
	 * @return The user's 4-digit discord-tag.
	 */
	String getDiscriminator();

	/**
	 * Gets the user's avatar hash.
	 *
	 * @return The user's avatar hash.
	 */
	String getAvatarHash();

	/**
	 * Gets whether the user belongs to an OAuth2 application.
	 *
	 * @return {@code true} if the user belongs to an OAuth2 application, {@code false} otherwise.
	 */
	boolean isBot();

	/**
	 * Gets the <i>raw</i> mention. This is the format utilized to directly mention another user (assuming the user
	 * exists in context of the mention).
	 *
	 * @return The <i>raw</i> mention.
	 */
	default String getMention() {
		return "<@" + getId().asString() + ">";
	}

	/**
	 * Requests to retrieve this user as a {@link Member}.
	 *
	 * @param guildId The ID of the guild to associate this user as a {@link Member}.
	 * @return A {@link Mono} where, upon successful completion, emits this user as a {@link Member member}. If an error
	 * is received, it is emitted through the {@code Mono}.
	 */
	Mono<Member> asMember(Snowflake guildId);

	/**
	 * Requests to retrieve this user as a {@link Member}.
	 *
	 * @param guild The guild to associate this user as a {@link Member}.
	 * @return A {@link Mono} where, upon successful completion, emits this user as a {@link Member member}. If an error
	 * is received, it is emitted through the {@code Mono}.
	 */
	default Mono<Member> asMember(Guild guild) {
		return asMember(guild.getId());
	}

	/**
	 * Requests to retrieve the private channel (DM) to this user.
	 * 
	 * @return A {@link Mono} where, upon successful completion, emits the {@link PrivateChannel private channel} to
	 * this user. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<PrivateChannel> getPrivateChannel();
}
