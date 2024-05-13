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

package discord4j.core.event.domain.thread;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.gateway.ShardInfo;

/**
 * Sent when a thread is created, relevant to the current user, or when the current user is added to a thread.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#thread-create">Discord Docs</a>
 */
public class ThreadChannelCreateEvent extends ThreadEvent {

    private final ThreadChannel channel;
    private final boolean threadNewlyCreated;

    public ThreadChannelCreateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ThreadChannel channel, boolean threadNewlyCreated) {
        super(gateway, shardInfo);
        this.channel = channel;
        this.threadNewlyCreated = threadNewlyCreated;
    }

    public ThreadChannel getChannel() {
        return channel;
    }

    /**
     * Gets if the thread related to this event was newly created.
     *
     * @return {@code true} if was newly created, {@code false} otherwise.
     */
    public boolean isNewlyCreated() {
        return threadNewlyCreated;
    }

    @Override
    public String toString() {
        return "ThreadChannelCreateEvent{" +
                "channel=" + channel +
                ",newlyCreated=" + threadNewlyCreated +
                '}';
    }
}
