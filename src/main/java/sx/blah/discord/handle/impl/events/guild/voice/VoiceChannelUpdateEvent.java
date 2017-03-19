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

package sx.blah.discord.handle.impl.events.guild.voice;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * This event is dispatched when a voice channel is updated.
 */
public class VoiceChannelUpdateEvent extends VoiceChannelEvent {

	private final IVoiceChannel oldVoiceChannel, newVoiceChannel;

	public VoiceChannelUpdateEvent(IVoiceChannel oldVoiceChannel, IVoiceChannel newVoiceChannel) {
		super(newVoiceChannel);
		this.oldVoiceChannel = oldVoiceChannel;
		this.newVoiceChannel = newVoiceChannel;
	}

	/**
	 * Gets the original voice channel.
	 *
	 * @return The un-updated instance of the voice channel.
	 */
	public IVoiceChannel getOldVoiceChannel() {
		return oldVoiceChannel;
	}

	/**
	 * Gets the new voice channel.
	 *
	 * @return The updated instance of the voice channel.
	 */
	public IVoiceChannel getNewVoiceChannel() {
		return newVoiceChannel;
	}
}
