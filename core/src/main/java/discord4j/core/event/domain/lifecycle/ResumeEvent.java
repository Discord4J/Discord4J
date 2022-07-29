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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.event.domain.lifecycle;

import discord4j.core.GatewayDiscordClient;
import discord4j.gateway.ShardInfo;

import java.util.List;

/**
 * Dispatched when the gateway connection is successfully resumed.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway#resumed">Resumed</a>
 */
public class ResumeEvent extends GatewayLifecycleEvent {

    private final /*~~>*/List<String> trace;

    public ResumeEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, /*~~>*/List<String> trace) {
        super(gateway, shardInfo);
        this.trace = trace;
    }

    /**
     * Gets the trace of the event. Used for debugging - the guilds the user is in.
     *
     * @return The trace provided by Discord, containing the guild the user is in.
     */
    public /*~~>*/List<String> getTrace() {
        return trace;
    }

    @Override
    public String toString() {
        return "ResumeEvent{" +
                "trace=" + trace +
                "} " + super.toString();
    }
}
