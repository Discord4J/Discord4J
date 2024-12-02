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
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.json.gateway.ThreadListSync;
import discord4j.gateway.ShardInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sent when the current user gains access to a channel.
 *
 * @see <a href="https://discord.com/developers/docs/topics/gateway-events#thread-list-sync">Discord Docs</a>
 */
public class ThreadListSyncEvent extends ThreadEvent {

    private final ThreadListSync dispatch;
    private final List<ThreadChannel> syncedThreads;

    public ThreadListSyncEvent(GatewayDiscordClient gateway, ShardInfo shardInfo, ThreadListSync dispatch) {
        super(gateway, shardInfo);
        this.dispatch = dispatch;
        this.syncedThreads = dispatch.threads().stream()
                .map(data -> new ThreadChannel(gateway, data))
                .collect(Collectors.toList());
    }

    /**
     * Returns the ID of the guild for this event.
     *
     * @return a {@link Snowflake} guild ID for this event
     */
    public Snowflake getGuildId() {
        return Snowflake.of(dispatch.guildId());
    }

    /**
     * Returns the parent channel IDs whose threads are being synced. If absent, then threads were synced for the entire
     * guild. This array may contain channel IDs that have no active threads as well.
     *
     * @return if present, a list of {@link Snowflake} channel IDs that are having their threads synced, otherwise if
     * absent, means this sync event is for the entire guild.
     */
    public Optional<List<Snowflake>> getSyncedChannelIds() {
        return dispatch.channelIds().toOptional().map(list -> list.stream().map(Snowflake::of).collect(Collectors.toList()));
    }

    /**
     * Returns all active threads in the given channels that the current user can access.
     *
     * @return a list of {@link ThreadChannel} with all active threads for the current user
     */
    public List<ThreadChannel> getSyncedThreads() {
        return syncedThreads;
    }

    /**
     * Returns all thread member objects from the synced threads for the current user, indicating which threads the
     * current user has been added to
     */
    public List<ThreadMember> getThreadMembers() {
        return dispatch.members().stream().map(data -> new ThreadMember(getClient(), data)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ThreadListSyncEvent{" +
                "dispatch=" + dispatch +
                ", syncedThreads=" + syncedThreads +
                '}';
    }
}
