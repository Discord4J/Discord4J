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

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * Dispatched when the bot user disconnects from a voice channel.
 */
public class VoiceDisconnectedEvent extends VoiceChannelEvent {

	private final Reason reason;

	public VoiceDisconnectedEvent(IVoiceChannel channel, Reason reason) {
		super(channel);
		this.reason = reason;
	}

	public VoiceDisconnectedEvent(IGuild guild, Reason reason) {
		super(guild, guild.getConnectedVoiceChannel());
		this.reason = reason;
	}

	/**
	 * Gets the reason the client disconnected.
	 *
	 * @return The reason the client disconnected.
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * The possible reasons for disconnecting from the voice channel.
	 */
	public enum Reason {
		/**
		 * The user intentionally left the voice channel.
		 *
		 * @see IVoiceChannel#leave()
		 */
		LEFT_CHANNEL,
		/**
		 * The voice server was updated and is moving. (Most likely voice region change)
		 */
		SERVER_UPDATE,
		/**
		 * Something unknown caused the websocket to close. The connection will be abandoned.
		 */
		ABNORMAL_CLOSE
	}
}

