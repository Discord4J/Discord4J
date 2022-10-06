package discord4j.core.object;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ThreadMember;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ListThreadsData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** A part of thread channels list which contains channels and thread members. */
public class ThreadListPart {
    private final List<ThreadChannel> threads;

    private final List<ThreadMember> members;

    public ThreadListPart(GatewayDiscordClient client, ListThreadsData data) {
        this.threads = data.threads().stream()
                .map(channelData -> (ThreadChannel) EntityUtil.getChannel(client, channelData))
                .collect(Collectors.toList());

        this.members = data.members().stream()
                .map(threadMemberData -> new ThreadMember(client, threadMemberData))
                .collect(Collectors.toList());
    }

    private ThreadListPart(List<ThreadChannel> threads, List<ThreadMember> members) {
        this.threads = threads;
        this.members = members;
    }

    /**
     * Gets the thread channels in this portion of the threads list.
     *
     * @return The thread channels in this portion of the threads list.
     */
    public List<ThreadChannel> getThreads() {
        return threads;
    }

    /**
     * Gets the thread members in this portion of the threads list.
     *
     * @return The thread members in this portion of the threads list.
     */
    public List<ThreadMember> getMembers() {
        return members;
    }

    /**
     * Combines this portion of the threads list with another portion.
     *
     * @param other The other portion to combine with.
     * @return A new {@link ThreadListPart} with both parts.
     */
    public ThreadListPart combine(ThreadListPart other) {
        List<ThreadChannel> combineThreads = new ArrayList<>(threads.size() + other.threads.size());
        combineThreads.addAll(threads);
        combineThreads.addAll(other.threads);

        List<ThreadMember> combineMembers = new ArrayList<>(members.size() + other.members.size());
        combineMembers.addAll(members);
        combineMembers.addAll(other.members);

        return new ThreadListPart(combineThreads, combineMembers);
    }
}
