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

import sx.blah.discord.api.internal.json.objects.GameObject;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

/**
 * Sent to update the bot's presence.
 */
public class PresenceUpdateRequest {

	/**
	 * The unix time of when the bot became idle or null if the bot is not idle.
	 */
	public Long since;
	/**
	 * The game the bot is playing.
	 */
	public GameObject game;
	/**
	 * The status of the bot.
	 */
	public String status;
	/**
	 * Whether the bot is afk.
	 */
	public boolean afk = false;

	public PresenceUpdateRequest(StatusType status, ActivityType type, String text, String streamUrl) {
		this.since = status == StatusType.IDLE ? System.currentTimeMillis() : null;
		this.game = type == ActivityType.STREAMING
				? new GameObject(text, streamUrl)
				: type == null ? null : new GameObject(text, type.ordinal());
		this.status = status.name().toLowerCase();
	}
}
