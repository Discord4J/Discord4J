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

package sx.blah.discord.handle.impl.events.guild.voice.user;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * Dispatched when a user moves from one voice channel to another.
 */
public class UserVoiceChannelMoveEvent extends UserVoiceChannelEvent {

	private final IVoiceChannel oldChannel;
	private final IVoiceChannel newChannel;

	public UserVoiceChannelMoveEvent(IUser user, IVoiceChannel oldChannel, IVoiceChannel newChannel) {
		super(newChannel, user);
		this.oldChannel = oldChannel;
		this.newChannel = newChannel;
	}

	/**
	 * Gets the voice channel the user left.
	 *
	 * @return The voice channel the user left.
	 */
	public IVoiceChannel getOldChannel() {
		return oldChannel;
	}

	/**
	 * Gets the voice channel the user joined.
	 *
	 * @return The voice channel the user joined.
	 */
	public IVoiceChannel getNewChannel() {
		return newChannel;
	}
}
