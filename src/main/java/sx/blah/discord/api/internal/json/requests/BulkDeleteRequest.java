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

import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IMessage;

import java.util.List;

/**
 * This request is sent to discord to delete a set of messages.
 */
public class BulkDeleteRequest {

	/**
	 * The array of message ids to delete.
	 */
	public String[] messages;

	public BulkDeleteRequest(String[] messages) {
		this.messages = messages;
	}

	public BulkDeleteRequest(List<IMessage> messages) {
		this(messages.stream().map(IDiscordObject::getStringID).toArray(String[]::new));
	}
}
