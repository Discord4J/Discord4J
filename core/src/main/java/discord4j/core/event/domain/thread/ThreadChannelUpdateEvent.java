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
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Sent when a thread is updated. This is not sent when the channel field last_message_id is altered.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#thread-member-update">Discord Docs</a>
 */
public class ThreadChannelUpdateEvent extends ThreadEvent {

    private final ThreadChannel channel;
    private final ThreadChannel old;

    public ThreadChannelUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ThreadChannel channel,
                                    @Nullable ThreadChannel old) {
        super(gateway, shardInfo);
        this.channel = channel;
        this.old = old;
    }

    public ThreadChannel getChannel() {
        return channel;
    }

    public Optional<ThreadChannel> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "ThreadChannelUpdateEvent{" +
                "channel=" + channel +
                ", old=" + old +
                '}';
    }
}
