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

package sx.blah.discord.handle.impl.events.guild;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Dispatched when the owner of a guild changes.
 */
public class GuildTransferOwnershipEvent extends GuildEvent {

	private final IUser oldOwner, newOwner;

	public GuildTransferOwnershipEvent(IUser oldOwner, IUser newOwner, IGuild guild) {
		super(guild);
		this.oldOwner = oldOwner;
		this.newOwner = newOwner;
	}

	/**
	 * Gets the original owner of the guild.
	 *
	 * @return The original owner of the guild.
	 */
	public IUser getOldOwner() {
		return oldOwner;
	}

	/**
	 * Gets the new owner of the guild.
	 *
	 * @return The new owner of the guild.
	 */
	public IUser getNewOwner() {
		return newOwner;
	}
}
