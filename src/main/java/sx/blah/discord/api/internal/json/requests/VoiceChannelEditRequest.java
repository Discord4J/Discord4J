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

package sx.blah.discord.api.internal.json.requests;

/**
 * Sent to edit a voice channel's properties.
 */
public class VoiceChannelEditRequest {

	/**
	 * The new name (2-100 characters long) of the channel.
	 */
	public String name;

	/**
	 * The new position of the channel.
	 */
	public int position;

	/**
	 * The new bitrate of the channel.
	 */
	public int bitrate;

	/**
	 * The new user limit of the channel.
	 */
	public int user_limit;

	public VoiceChannelEditRequest(String name, int position, int bitrate, int user_limit) {
		this.name = name;
		this.position = position;
		this.bitrate = bitrate;
		this.user_limit = user_limit;
	}
}
