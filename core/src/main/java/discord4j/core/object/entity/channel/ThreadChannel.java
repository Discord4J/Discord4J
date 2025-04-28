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
package discord4j.core.object.entity.channel;

import discord4j.common.annotations.Experimental;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.ThreadMember;
import discord4j.core.object.entity.User;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.ThreadChannelEditMono;
import discord4j.core.spec.ThreadChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ThreadMetadata;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ThreadChannel implements thread channel operations, for both thread channel that are associated to a text channel or forum channel.
 * You can fetch a ThreadChannel by id from the guild object using {@link discord4j.core.object.entity.Guild#getChannelById(Snowflake)} and by filtering it using {@link Mono#ofType(Class)} :
 * <pre>
 * Guild myGuild;
 * Mono&lt;ThreadChannel&gt; aThreadChannel = myGuild.getChannelById(Snowflake.of(1234567890123L))
 * .ofType(ThreadChannel.class);
 * </pre>
 */
@Experimental
public final class ThreadChannel extends BaseChannel implements GuildMessageChannel {

    /**
     * Builds a ThreadChannel object by associating it with a gateway client with the provided channel data.
     * To fetch a build object from the Discord gateway,
     * please see {@link GatewayDiscordClient#getChannelById(Snowflake)}.
     *
     * @param gateway The gateway associated with this client
     * @param data The parsed JSON data associated with this channel
     */
    public ThreadChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the user identifier that started this thread.
     *
     * @return An {@link Snowflake} representing the unique user identifier.
     */
    public Snowflake getStarterId() {
        return getData().ownerId().toOptional() // owner_id is repurposed for the starter
                .map(Snowflake::of)
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    /**
     * Gets the amount of seconds a user has to wait before sending another message (0-21600).
     * <p>
     * Bots, as well as users with the permission {@code manage_messages} or {@code manage_channel}, are unaffected.
     *
     * @return The amount of seconds a user has to wait before sending another message (0-21600).
     */
    public int getRateLimitPerUser() {
        return getData().rateLimitPerUser().toOptional().orElse(0);
    }

    // TODO: should this be Member? What if they're not in the guild anymore? Do we consider that anywhere else?
   /**
     * Gets the {@link User} that initiated this thread.
     * This method allows fetching a known user even if the user left the guild.
     * If you need to use a retrieval strategy, see {@link ThreadChannel#getStarter(EntityRetrievalStrategy)}.
     *
     * @return A {@link Mono} that, upon completion, emits the {@link User} that started the thread.
     * If any error occurs, it is emitted through the mono.
     */
    public Mono<User> getStarter() {
        return getClient().getUserById(getStarterId());
    }

   /**
     * Gets the {@link User} that initiated this thread.
     * This method allows fetching a known user even if the user left the guild.
     *
     * @return A {@link Mono} that, upon completion, emits the {@link User} that started the thread.
     * If any error occurs, it is emitted through the mono.
     */
    public Mono<User> getStarter(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getUserById(getStarterId());
    }

    /**
     * Gets the parent channel identifier, wrapped in an {@link Optional}.
     *
     * @return An {@link Optional}, containing the parent channel identifier as a {@link Snowflake}.
     */
    public Optional<Snowflake> getParentId() {
        return getData().parentId().toOptional()
                .flatMap(Function.identity())
                .map(Snowflake::of);
    }

    /**
     * Fetches the parent channel for this thread object.
     * When the parent channel is a message channel, an appropriate target class for casting is
     * {@link BaseTopLevelGuildChannel}.
     *
     * @return A {@link Mono} which, upon completion, emits a {@link BaseTopLevelGuildChannel}.
     * Any error is emitted through the mono.
     */
    public Mono<BaseTopLevelGuildChannel> getParent() {
        return Mono.justOrEmpty(getParentId())
                .flatMap(getClient()::getChannelById)
                .cast(BaseTopLevelGuildChannel.class);
    }

    /**
     * Fetches the parent channel for this thread object using the provided retrieval strategy.
     * When the parent channel is a message channel, an appropriate target class for casting is {@link BaseTopLevelGuildChannel}.
     *
     * @param retrievalStrategy The selected retrieval strategy for this request
     * @return A {@link Mono} which, upon completion, emits a {@link BaseTopLevelGuildChannel}.
     * Any error is emitted through the mono.
     */
    public Mono<BaseTopLevelGuildChannel> getParent(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getParentId())
                .flatMap(getClient().withRetrievalStrategy(retrievalStrategy)::getChannelById)
                .cast(BaseTopLevelGuildChannel.class);
    }

    /**
     * Requests to retrieve the member of this thread.
     *
     * @param userId The ID of the user.
     * @return A {@link Mono} where, upon successful completion, emits the {@link ThreadMember member} of this thread. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ThreadMember> getMember(Snowflake userId) {
        return getClient().getThreadMemberById(getId(), userId);
    }

    /**
     * Requests to retrieve the member of this thread, using the given retrieval strategy.
     *
     * @param userId The ID of the user.
     * @param retrievalStrategy the strategy to use to get the thread member
     * @return A {@link Mono} where, upon successful completion, emits the {@link ThreadMember member} of this thread. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<ThreadMember> getMember(Snowflake userId, EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy)
                .getThreadMemberById(getId(), userId);
    }

    /**
     * Returns all members of this thread.
     *
     * @return A {@link Flux} that continually emits all {@link ThreadMember members} of this thread.
     */
    public Flux<ThreadMember> getThreadMembers() {
        return getClient().getThreadMembers(getId());
    }

    /**
     * Returns all members of this thread, using the given retrieval strategy.
     *
     * @param retrievalStrategy the strategy to use to get the thread members
     * @return A {@link Flux} that continually emits all {@link ThreadMember members} of this thread.
     */
    public Flux<ThreadMember> getThreadMembers(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy)
                .getThreadMembers(getId());
    }

    /**
     * Get the approximate message count within this thread channel.
     * Messages that are deleted to not decrement the counter, hence the approximation.
     *
     * @return The approximate message count
     */
    public int getApproximateMessageCount() {
        return getData().messageCount().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    /**
     * Get the approximate member count that joined this thread.
     * Stops counting after 50 members.
     *
     * @return The approximate member count, from 1 to 50.
     */
    public int getApproximateMemberCount() {
        return getData().memberCount().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    /**
     * Tells if the thread is archived, manually or automatically.
     * Archived threads are inactive but can be un-archived by different people, depending on the parent channel configuration.
     *
     * @return A boolean indicating whether the thread is archived or not.
     */
    public boolean isArchived() {
        return getMetadata().archived();
    }

    /**
     * Gets the timestamp when the thread's archive status was last changed.
     *
     * @return An {@link Instant} representing the timestamp of the last change on the archival status of the thread.
     */
    public Instant getArchiveTimestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(getMetadata().archiveTimestamp(), Instant::from);
    }

    /**
     * Gets the default auto-archival duration configured for this thread channel.
     *
     * @return The {@link AutoArchiveDuration} configured for this channel.
     */
    public AutoArchiveDuration getAutoArchiveDuration() {
        return AutoArchiveDuration.of(getMetadata().autoArchiveDuration());
    }

    /**
     * Tells if the thread channel has been locked by a moderator.
     * This is a different feature from archiving the channel.
     *
     * @return Returns a boolean representing whether the channel is locked or not.
     */
    public boolean isLocked() {
        return getMetadata().locked().toOptional().orElse(false);
    }

    /**
     * Tells if the thread channel is private or not, based on its type.
     *
     * @return A boolean representing if the thread kind allows member join
     */
    // TODO Implement the "invitable" field that provide more information about that
    public boolean isPrivate() {
        return Type.of(getData().type()) == Type.GUILD_PRIVATE_THREAD;
    }

    /**
     * Returns the metadata associated with this thread.
     *
     * @return A {@link ThreadMetadata} object containing information about the thread
     */
    private ThreadMetadata getMetadata() {
        return getData().threadMetadata().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    /**
     * Computes permissions given to a specified member regarding this channel.
     *
     * @param memberId The ID of the member to get permissions for.
     * @return A {@link Mono} that, upon completion, emits a {@link PermissionSet} containing all allowed or denied
     * permissions to the given member.
     */
    @Override
    public Mono<PermissionSet> getEffectivePermissions(Snowflake memberId) {
        return getParent().flatMap(parent -> parent.getEffectivePermissions(memberId));
    }

    /**
     * Computes permissions given to a specified member regarding this channel.
     *
     * @param member The member to get permissions for.
     * @return A {@link Mono} that, upon completion, emits a {@link PermissionSet} containing all allowed or denied
     * permissions to the given member.
     */
    @Override
    public Mono<PermissionSet> getEffectivePermissions(Member member) {
        return getParent().flatMap(parent -> parent.getEffectivePermissions(member));
    }

    /**
     * Tells if this thread channel supports tagging using {@link ForumTag} objects.
     * The ability to tag a thread channel is currently related to the kind of parent channel.
     * <br>
     * When the channel does not support tagging, returned values from {@link ThreadChannel#getAppliedTagsIds()} and
     * {@link ThreadChannel#getAppliedTags()} return an empty list or {@link Flux}.
     *
     * @return A boolean indicating if the thread channel can be tagged.
     */
    public boolean canBeTagged() {
        return !getData().appliedTags().isAbsent();
    }

    /**
     * Returns the list of the applied forum tags on this thread. If the thread channel does not support tagging, the
     * list is empty. To know if the thread supports tagging, use {@link ThreadChannel#canBeTagged()}.
     *
     * @return An {@link Optional} item, wrapping a list of applied forum or media channel tags to this thread channel.
     * The optional item will be empty if the thread channel cannot contain tags, otherwise the list is empty.
     * @see ThreadChannel#canBeTagged()
     * @see ThreadChannel#getAppliedTags()
     */
    public List<Snowflake> getAppliedTagsIds() {
        return getData().appliedTags().toOptional()
            .orElse(new ArrayList<>())
            .stream().map(Snowflake::of).collect(Collectors.toList());
    }

    /**
     * Returns the list of applied tags to this thread channel directly in the form of {@link ForumTag} objects.
     * To only fetch the identifiers, use {@link ThreadChannel#getAppliedTagsIds()}.
     * If the thread channel does not support tags,
     *
     * @return A {@link Flux}, that emits all the applied tags on this thread channel. If an error occurs, it is emitted
     * through the flux. Only emits if the thread channel can have tags applied.
     * @see ThreadChannel#canBeTagged()
     * @see ThreadChannel#getAppliedTagsIds()
     */
    public Flux<ForumTag> getAppliedTags() {
        // We first the list inside a Mono, to directly emit an empty signal if the list is not available
        // The other approach would be to use a Flux on the list, but see below why we did not
        return Mono.justOrEmpty(getAppliedTagsIds())
            // This lets us work directly on the cast ForumChannel object and do so only once
            .flatMapMany(list -> getParent().ofType(ForumChannel.class)
                .flatMapIterable(ForumChannel::getAvailableTags)
                // Note the usage of the List#remove instead of the List#contains method to avoid iterating
                // over already found tags
                .filter(tag -> list.remove(tag.getId())));
    }

    /**
     * Adds the bot to this thread. Requires that the thread is not archived.
     *
     * @return A {@link Mono} where, upon successful completion, emits an empty {@code Mono}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> join() {
        return getClient().getRestClient().getChannelService().joinThread(getId().asLong());
    }

    /**
     * Removes the bot from this thread. Requires that the thread is not archived.
     *
     * @return A {@link Mono} where, upon successful completion, emits an empty {@code Mono}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> leave() {
        return getClient().getRestClient().getChannelService().leaveThread(getId().asLong());
    }

    /**
     * Adds a given {@code user} to this thread. Requires the ability to send messages in the thread. Also requires the
     * thread is not archived. Returns successfully if the user is already a member of this thread.
     *
     * @param user the member to add
     * @return A {@link Mono} where, upon successful completion, emits an empty {@code Mono}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addMember(User user) {
        return getClient().getRestClient().getChannelService().addThreadMember(getId().asLong(), user.getId().asLong());
    }

    /**
     * Removes a given {@code user} from this thread. Requires permission to manage threads, or the creator of the
     * thread if it is a guild private thread. Also requires the thread is not archived.
     *
     * @param user the member to remove
     * @return A {@link Mono} where, upon successful completion, emits an empty {@code Mono}.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> removeMember(User user) {
        return getClient().getRestClient().getChannelService()
                .removeThreadMember(getId().asLong(), user.getId().asLong());
    }

    /**
     * Requests to edit this thread channel. Properties specifying how to edit this thread channel can be set via the {@code
     * withXxx} methods of the returned {@link ThreadChannelEditMono}.
     *
     * @return A {@link ThreadChannelEditMono} where, upon successful completion, emits the edited {@link ThreadChannel}. If
     * an error is received, it is emitted through the {@code ThreadChannelEditMono}.
     */
    public ThreadChannelEditMono edit() {
        return ThreadChannelEditMono.of(this);
    }

    /**
     * Requests to edit this thread channel.
     *
     * @param spec an immutable object that specifies how to edit this thread channel
     * @return A {@link Mono} where, upon successful completion, emits the edited {@link ThreadChannel}. If an error is
     * received, it is emitted through the {@code Mono}.
     */
    public Mono<ThreadChannel> edit(ThreadChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                    .modifyThread(getId().asLong(), spec.asRequest(), spec.reason()))
            .map(data -> EntityUtil.getChannel(getClient(), data))
            .cast(ThreadChannel.class);
    }

    /**
     * Request to pin the current thread. Only available for threads created in a {@link ForumChannel}.
     *
     * @return A {@link Mono} where, upon successful completion, emits an empty {@code Mono}. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> pin() {
        return getParent()
            .filter(ForumChannel.class::isInstance)
            .map(__ -> this)
            .flatMap(threadChannel -> threadChannel.edit().withFlags(EnumSet.of(ForumChannel.Flag.PINNED)).then());
    }

    /**
     * Request to unpin the current thread. Only available for threads created in a {@link ForumChannel}.
     *
     * @return A {@link Mono} where, upon successful completion, emits an empty {@code Mono}. If an error is received, it
     * is emitted through the {@code Mono}.
     */
    public Mono<Void> unpin() {
        return getParent()
            .filter(ForumChannel.class::isInstance)
            .map(__ -> this)
            .flatMap(threadChannel -> threadChannel.edit().withFlags(EnumSet.noneOf(ForumChannel.Flag.class)).then());
    }

    /** Duration in minutes to automatically archive the thread after recent activity. */
    public enum AutoArchiveDuration {

        // TODO naming
        UNKNOWN(-1),
        /** 1 hour */
        DURATION1(60),
        /** 1 day */
        DURATION2(1440),
        /** 3 days */
        DURATION3(4320),
        /** 7 days */
        DURATION4(10080);

        private final int value;

        AutoArchiveDuration(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public Duration asDuration() {
            return Duration.ofMinutes(value);
        }

        public static AutoArchiveDuration of(int value) {
            switch (value) {
                case 60: return DURATION1;
                case 1440: return DURATION2;
                case 4320: return DURATION3;
                case 10080: return DURATION4;
                default: return UNKNOWN;
            }
        }
    }

    public enum Type {

        /** Unknown type. */
        UNKNOWN(-1, false),
        GUILD_NEWS_THREAD(10, true),
        GUILD_PUBLIC_THREAD(11, true),
        GUILD_PRIVATE_THREAD(12, false);

        /** The underlying value as represented by Discord. */
        private final int value;

        /** If the thread type is public */
        private final boolean isPublic;

        /**
         * Constructs a {@code ThreadChannel.Type}.
         *
         * @param value    The underlying value as represented by Discord.
         * @param isPublic If the thread type is public
         */
        Type(final int value, boolean isPublic) {
            this.value = value;
            this.isPublic = isPublic;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets whether the thread type is public.
         *
         * @return If the thread type is public
         */
        public boolean isPublic() {
            return this.isPublic;
        }

        /**
         * Gets the type of channel. It is guaranteed that invoking {@link #getValue()} from the returned enum will
         * equal ({@code ==}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of channel.
         */
        public static ThreadChannel.Type of(final int value) {
            switch (value) {
                case 10: return GUILD_NEWS_THREAD;
                case 11: return GUILD_PUBLIC_THREAD;
                case 12: return GUILD_PRIVATE_THREAD;
                default: return UNKNOWN;
            }
        }
    }

    @Override
    public String toString() {
        return "ThreadChannel{} " + super.toString();
    }
}
