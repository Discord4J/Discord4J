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
 * Dispatched when a voice heartbeat is received.
 */
public class VoicePingEvent extends VoiceChannelEvent {
	/**
	 * The ping for the voice channel the user is currently in.
	 */
	private final long ping;

	public VoicePingEvent(IVoiceChannel channel, long ping) {
		super(channel);
		this.ping = ping;
	}

	/**
	 * Gets the ping for the voice channel the user is currently in.
	 *
	 * @return The ping.
	 */
	public long getPing() {
		return ping;
	}
}
