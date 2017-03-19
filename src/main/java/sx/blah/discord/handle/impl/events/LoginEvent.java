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

package sx.blah.discord.handle.impl.events;

import sx.blah.discord.api.IShard;

/**
 * This event is fired when a shard has established an initial connection to the Discord gateway.
 * At this point, the bot has <b>not</b> received all of the necessary information to interact with all aspects of the api.
 * Wait for {@link ReadyEvent} to do so.
 * @deprecated Use {@link sx.blah.discord.handle.impl.events.shard.LoginEvent} instead.
 */
@Deprecated
public class LoginEvent extends sx.blah.discord.handle.impl.events.shard.LoginEvent {
	
	public LoginEvent(IShard shard) {
		super(shard);
	}
}
