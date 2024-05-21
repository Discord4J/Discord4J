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

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ThreadMember;
import discord4j.discordjson.json.gateway.ThreadMembersUpdate;
import discord4j.gateway.ShardInfo;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sent when anyone is added to or removed from a thread. If the current user does not have the
 * {@link discord4j.gateway.intent.Intent#GUILD_MEMBERS} Gateway Intent, then this event will only be sent if the
 * current user was added to or removed from the thread.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#thread-members-update">Discord Docs</a>
 */
public class ThreadMembersUpdateEvent extends ThreadEvent {

    private final ThreadMembersUpdate dispatch;
    private final List<ThreadMember> members;
    private final List<ThreadMember> old;

    public ThreadMembersUpdateEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ThreadMembersUpdate dispatch,
                                    List<ThreadMember> members, @Nullable List<ThreadMember> old) {
        super(gateway, shardInfo);
        this.dispatch = dispatch;
        this.members = members;
        this.old = old;
    }

    public Snowflake getThreadId() {
        return Snowflake.of(dispatch.id());
    }

    public Snowflake getGuildId() {
        return Snowflake.of(dispatch.guildId());
    }

    /**
     * Returns the approximate number of members in the thread, capped at 50.
     *
     * @return the approximate number of members in the thread
     */
    public int getMemberCount() {
        return dispatch.memberCount();
    }

    /**
     * Returns the list of members who were added to the thread.
     *
     * @return a list of {@link ThreadMember} added to the thread
     */
    public List<ThreadMember> getMembers() {
        return members;
    }

    /**
     * Returns the old list of members in the thread, if present.
     *
     * @return a list of {@link ThreadMember} in the thread before the event, if present
     */
    public Optional<List<ThreadMember>> getOld() {
        return Optional.ofNullable(old);
    }

    /**
     * Returns a list of user IDs who were removed from the thread.
     *
     * @return a list of {@link Snowflake} user IDs who were removed from the thread
     */
    public List<Snowflake> getRemovedMemberIds() {
        return dispatch.removedMemberIds().toOptional().orElse(Collections.emptyList()).stream()
                .map(Snowflake::of).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ThreadMembersUpdateEvent{" +
                "dispatch=" + dispatch +
                ", members=" + members +
                ", old=" + old +
                '}';
    }
}
