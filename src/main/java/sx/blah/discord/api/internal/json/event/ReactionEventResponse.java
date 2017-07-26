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

package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.ReactionEmojiObject;

/**
 * The object received on the gateway when reactions or added or removed on a message.
 */
public class ReactionEventResponse {
	/**
	 * The ID of the user who reacted.
	 */
	public String user_id;
	/**
	 * The ID of the message.
	 */
	public String message_id;
	/**
	 * The emoji involved.
	 */
	public ReactionEmojiObject emoji;
	/**
	 * The ID of the channel.
	 */
	public String channel_id;
}
