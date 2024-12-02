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
import discord4j.core.object.entity.ThreadMember;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Optional;

/**
 * Sent when the thread member object for the current user is updated. This event is documented for completeness, but
 * unlikely to be used by most bots. For bots, this event largely is just a signal that you are a member of the thread.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#thread-member-update">Discord Docs</a>
 */
public class ThreadMemberUpdateEvent extends ThreadEvent {

    private final ThreadMember member;
    private final ThreadMember old;

    public ThreadMemberUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ThreadMember member,
                                   @Nullable ThreadMember old) {
        super(gateway, shardInfo);
        this.member = member;
        this.old = old;
    }

    public ThreadMember getMember() {
        return member;
    }

    public Optional<ThreadMember> getOld() {
        return Optional.ofNullable(old);
    }

    @Override
    public String toString() {
        return "ThreadMemberUpdateEvent{" +
                "member=" + member +
                ", old=" + old +
                '}';
    }
}
