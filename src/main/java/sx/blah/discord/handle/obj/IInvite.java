/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.obj;

import sx.blah.discord.api.IDiscordClient;

/**
 * An invite to a guild channel.
 */
public interface IInvite {

	/**
	 * Gets the invite code for the invite.
	 *
	 * @return The invite code for the invite.
	 */
	String getCode();

	/**
	 * Gets the guild the invite is for.
	 *
	 * @return The guild the invite is for.
	 */
	IGuild getGuild();

	/**
	 * Gets the channel the invite is for.
	 *
	 * @return The channel the invite is for.
	 */
	IChannel getChannel();

	/**
	 * Gets the user who created the invite.
	 *
	 * <p>This is null for vanity url invites and widget invites.
	 *
	 * @return The user who created the invite.
	 */
	IUser getInviter();

	/**
	 * Gets the client the invite belongs to.
	 *
	 * @return The client the invite belongs to.
	 */
	IDiscordClient getClient();

	/**
	 * Deletes the invite.
	 */
	void delete();
}
