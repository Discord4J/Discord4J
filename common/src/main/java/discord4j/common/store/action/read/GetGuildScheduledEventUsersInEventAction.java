/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.common.store.action.read;

import discord4j.common.store.api.StoreAction;
import discord4j.discordjson.Id;

public class GetGuildScheduledEventUsersInEventAction implements StoreAction<Id> {

    private final long guildId;
    private final long eventId;

    public GetGuildScheduledEventUsersInEventAction(long guildId, long eventId) {
        this.guildId = guildId;
        this.eventId = eventId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getEventId() {
        return eventId;
    }
}
