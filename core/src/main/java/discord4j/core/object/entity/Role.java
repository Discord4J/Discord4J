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

import discord4j.core.object.PermissionSet;
import discord4j.core.object.Snowflake;
import discord4j.core.trait.Positionable;
import reactor.core.publisher.Mono;

import java.awt.*;

/**
 * A Discord role.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/permissions#role-object">Role Object</a>
 */
public interface Role extends Entity, Positionable {

	/**
	 * Gets the role name.
	 *
	 * @return The role name.
	 */
	String getName();

	/**
	 * Gets the color assigned to this role.
	 *
	 * @return The color assigned to this role.
	 */
	Color getColor();

	/**
	 * Gets whether if this role is pinned in the user listing.
	 *
	 * @return {@code true} if this role is pinned in the user listing, {@code false} otherwise.
	 */
	boolean isHoisted();

	/**
	 * Gets the permissions assigned to this role.
	 *
	 * @return The permissions assigned to this role.
	 */
	PermissionSet getPermissions();

	/**
	 * Gets whether this role is managed by an integration.
	 *
	 * @return {@code true} if this role is managed by an integration, {@code false} otherwise.
	 */
	boolean isManaged();

	/**
	 * Gets whether this role is mentionable.
	 *
	 * @return {@code true} if this role is mentionable, {@code false} otherwise.
	 */
	boolean isMentionable();

	/**
	 * Gets the ID of the guild this role is associated to.
	 *
	 * @return The ID of the guild this role is associated to.
	 */
	Snowflake getGuildId();

	/**
	 * Requests to retireve the guild this role is associated to.
	 *
	 * @return A {@link Mono} where, upon successful completion, emits the {@link Guild guild} this role is associated
	 * to. If an error is received, it is emitted through the {@code Mono}.
	 */
	Mono<Guild> getGuild();

	/**
	 * Gets the <i>raw</i> mention. This is the format utilized to directly mention another role (assuming the role
	 * exists in context of the mention).
	 *
	 * @return The <i>raw</i> mention.
	 */
	default String getMention() {
		return "<@&" + getId().asString() + ">";
	}
}
