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

package sx.blah.discord.api.internal.json.requests.voice;

/**
 * Sent when the bot wants to join, move, or disconnect from a voice channel.
 */
public class VoiceStateUpdateRequest {
	/**
	 * The ID of the guild the channel is in.
	 */
	public String guild_id;
	/**
	 * The ID of the channel.
	 */
	public String channel_id;
	/**
	 * Whether the bot is self muted.
	 */
	public boolean self_mute;
	/**
	 * Whether the bot is self deafened.
	 */
	public boolean self_deaf;

	public VoiceStateUpdateRequest(String guild_id, String channel_id, boolean self_mute, boolean self_deaf) {
		this.guild_id = guild_id;
		this.channel_id = channel_id;
		this.self_mute = self_mute;
		this.self_deaf = self_deaf;
	}
}
