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
 * Sent to create a new channel.
 */
public class ChannelCreateRequest {

	/**
	 * The name of the channel. Must be 2-100 characters long.
	 */
	public String name;

	/**
	 * The type of channel.
	 */
	public int type;

	/**
	 * ID of the parent category for a channel.
	 */
	public Long parent_id;

	public ChannelCreateRequest(String name, int type, Long parentID) {
		this.name = name;
		this.type = type;
		this.parent_id = parentID;
	}
}
