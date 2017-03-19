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
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.StatusType;

public class PresenceUpdateRequest {
	/**
	 * The time (in epoch milliseconds) since the user became idle or null if not idle
	 */
	public Long idle_since; // This must be the boxed Long because it can be null

	/**
	 * The game the user is playing, or null if no game
	 */
	public GameObject game;

	public PresenceUpdateRequest(Long idle_since, GameObject obj) {
		this.idle_since = idle_since;
		this.game = obj;
	}

	public PresenceUpdateRequest(Long idle_since, IPresence presence) {
		this(idle_since, new GameObject(presence.getPlayingText().orElse(null),
				presence.getStreamingUrl().orElse(null),
				presence.getStatus() == StatusType.STREAMING
						? GameObject.STREAMING
						: (presence.getPlayingText().isPresent() ? GameObject.GAME : GameObject.NONE)));
	}
}
