package discord4j.core.object.guildmessagesearch;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.ThreadMember;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.json.GuildMessageSearchResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the result of a guild message search.
 *
 * @see Guild#searchMessages()
 */
public class GuildSearchResult {

    private final GuildMessageSearchResponse data;
    private final List<Message> messages;
    private final List<ThreadChannel> threads;
    private final List<ThreadMember> threadMembers;

    public GuildSearchResult(GatewayDiscordClient gateway, GuildMessageSearchResponse data) {
        this.data = data;

        this.messages = data.messages()
                .stream()
                .flatMap(Collection::stream)
                .map(messageData -> new Message(gateway, messageData))
                .collect(Collectors.toList());

        this.threads = data.threads()
                .toOptional()
                .map(channelData ->
                        channelData.stream()
                                .map(threadData -> new ThreadChannel(gateway, threadData))
                                .collect(Collectors.toList())
                ).orElse(Collections.emptyList());

        this.threadMembers = data.members()
                .toOptional()
                .map(memberData ->
                        memberData.stream()
                                .map(threadMemberData -> new ThreadMember(gateway, threadMemberData))
                                .collect(Collectors.toList())
                ).orElse(Collections.emptyList());
    }

    /**
     * Get the raw data of the search result.
     *
     * @return The raw data of the search result.
     */
    public GuildMessageSearchResponse getData() {
        return this.data;
    }

    /**
     * Get the total number of results returned by the search.
     *
     * @return The total number of results returned by the search.
     */
    public int getTotalResults() {
        return this.data.totalResults();
    }

    /**
     * Get the messages returned by the search.
     *
     * @return The messages returned by the search.
     */
    public List<Message> getMessages() {
        return this.messages;
    }

    /**
     * Get the threads returned by the search.
     *
     * @return The threads returned by the search.
     */
    public List<ThreadChannel> getThreads() {
        return this.threads;
    }

    /**
     * Get the thread members returned by the search.
     *
     * @return The thread members returned by the search.
     */
    public List<ThreadMember> getThreadMembers() {
        return this.threadMembers;
    }

    /**
     * Whether the search is doing a deep historical index.
     *
     * @return Whether the search is doing a deep historical index.
     */
    public boolean isDoingDeepHistoricalIndex() {
        return this.data.doingDeepHistoricalIndex();
    }

    /**
     * Get the total number of documents indexed by the search.
     *
     * @return The total number of documents indexed by the search.
     */
    public int getIndexedDocuments() {
        return this.data.documentsIndexed().toOptional().orElse(0);
    }
}
