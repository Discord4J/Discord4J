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
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.ThreadChannelEditMono;
import discord4j.core.spec.ThreadChannelEditSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ThreadMetadata;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Experimental
public final class ThreadChannel extends BaseChannel implements GuildMessageChannel {

    public ThreadChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    public Snowflake getStarterId() {
        return getData().ownerId().toOptional() // owner_id is repurposed for the starter
                .map(Snowflake::of)
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    // TODO: should this be Member? What if they're not in the guild anymore? Do we consider that anywhere else?
    public Mono<User> getStarter() {
        return getClient().getUserById(getStarterId());
    }

    public Mono<User> getStarter(EntityRetrievalStrategy retrievalStrategy) {
        return getClient().withRetrievalStrategy(retrievalStrategy).getUserById(getStarterId());
    }

    public Optional<Snowflake> getParentId() {
        return getData().parentId().toOptional()
                .flatMap(Function.identity())
                .map(Snowflake::of);
    }

    public Mono<TopLevelGuildMessageChannel> getParent() {
        return Mono.justOrEmpty(getParentId())
                .flatMap(getClient()::getChannelById)
                .cast(TopLevelGuildMessageChannel.class);
    }

    public Mono<TopLevelGuildMessageChannel> getParent(EntityRetrievalStrategy retrievalStrategy) {
        return Mono.justOrEmpty(getParentId())
                .flatMap(getClient().withRetrievalStrategy(retrievalStrategy)::getChannelById)
                .cast(TopLevelGuildMessageChannel.class);
    }

    public int getApproximateMessageCount() {
        return getData().messageCount().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    public int getApproximateMemberCount() {
        return getData().memberCount().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    public boolean isArchived() {
        return getMetadata().archived();
    }

    public Instant getArchiveTimestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(getMetadata().archiveTimestamp(), Instant::from);
    }

    public AutoArchiveDuration getAutoArchiveDuration() {
        return AutoArchiveDuration.of(getMetadata().autoArchiveDuration());
    }

    public boolean isLocked() {
        return getMetadata().locked().toOptional().orElse(false);
    }

    public boolean isPrivate() {
        return Type.of(getData().type()) == Type.GUILD_PRIVATE_THREAD;
    }

    private ThreadMetadata getMetadata() {
        return getData().threadMetadata().toOptional()
                .orElseThrow(IllegalStateException::new); // should always be present for threads
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Snowflake memberId) {
        return getParent().flatMap(parent -> parent.getEffectivePermissions(memberId));
    }

    @Override
    public Mono<PermissionSet> getEffectivePermissions(Member member) {
        return getParent().flatMap(parent -> parent.getEffectivePermissions(member));
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
                    .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
            .map(data -> EntityUtil.getChannel(getClient(), data))
            .cast(ThreadChannel.class);
    }

    public enum AutoArchiveDuration {

        // TODO naming
        UNKNOWN(-1),
        DURATION1(60),
        DURATION2(1440),
        DURATION3(4320),
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
        UNKNOWN(-1),
        GUILD_NEWS_THREAD(10),
        GUILD_PUBLIC_THREAD(11),
        GUILD_PRIVATE_THREAD(12);

        /** The underlying value as represented by Discord. */
        private final int value;

        /**
         * Constructs a {@code ThreadChannel.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final int value) {
            this.value = value;
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
