package discord4j.core.object.entity.channel;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ThreadListPart;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.reaction.DefaultReaction;
import discord4j.core.spec.ForumChannelEditSpec;
import discord4j.core.spec.ForumThreadMessageCreateSpec;
import discord4j.core.spec.StartThreadInForumChannelMono;
import discord4j.core.spec.StartThreadInForumChannelSpec;
import discord4j.core.util.EntityUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ListThreadsData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A Discord guild channel that contains organized threads with labels.
 */
public final class ForumChannel extends BaseTopLevelGuildChannel implements CategorizableChannel {

    public ForumChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the topic for this guild forum.
     *
     * @return the user defined topic for this guild forum channel (0-4096 characters).
     */
    public Optional<String> getTopic() {
        return getData().topic().toOptional()
            .flatMap(opt -> opt); // Value is present on guild forum type but nullable
    }

    /**
     * Gets whether this channel is considered NSFW (Not Safe For Work).
     *
     * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
     */
    public boolean isNsfw() {
        return getData().nsfw().toOptional().orElse(false);
    }

    /**
     * Gets the channels {@link Flag} associated to this forum channel
     * Unknown flags are currently ignored.
     *
     * @return An {@link EnumSet} representing the <b>known flags</b> for this forum channel.
     */
    public EnumSet<Flag> getFlags() {
        return getData().flags().toOptional()
            .map(Flag::valueOf)
            .orElseThrow(IllegalStateException::new); // Mandatory for Forum Channels
    }

    /**
     * Gets the default auto archive duration for threads in this forum channel
     *
     * @return Default client auto archive duration in minutes wrapped in an {@link Optional} object
     */
    public Optional<Integer> getDefaultAutoArchiveDuration() {
        return getData().defaultAutoArchiveDuration().toOptional();
    }

    /**
     * Gets the available forum tags for this forum channel
     *
     * @return A list containing every available forum tag for this channel
     */
    public List<ForumTag> getAvailableTags() {
        return getData().availableTags().toOptional()
            .orElseThrow(IllegalStateException::new) // Mandatory for Forum channels
            .stream()
            .map(forumTagData -> new ForumTag(getClient(), forumTagData))
            .collect(Collectors.toList());
    }

    /**
     * Gets the default emoji to add in a reaction button on a thread for this forum channel
     *
     * @return An {@link Optional} that may contain the {@link DefaultReaction} emoji
     */
    public Optional<DefaultReaction> getDefaultReaction() {
        return getData().defaultReactionEmoji()
            .toOptional().orElseThrow(IllegalStateException::new) // Mandatory for Forum channels
            .map(defaultReactionData -> new DefaultReaction(getClient(), defaultReactionData));
    }

    /**
     * Gets the default thread rate limit duration per user.
     * This field is copied into a created thread and is not updated on the fly.
     *
     * @return The initial rate limit per user for newly created threads
     */
    public Optional<Integer> getDefaultThreadRateLimitPerUser() {
        return getData().defaultThreadRateLimitPerUser().toOptional();
    }


    /**
     * Gets the default associated {@link SortOrder} for this forum channel
     *
     * @return The {@link SortOrder}, wrapped in an {@link Optional}
     */
    public Optional<SortOrder> getSortOrder() {
        return getData().defaultSortOrder().toOptional()
            .orElseThrow(IllegalStateException::new) // Mandatory for Forum channels
            .map(SortOrder::valueOf);
    }

    /**
     * Gets the associated {@link LayoutType} for this Forum channel
     *
     * @return The associated {@link LayoutType}
     */
    public LayoutType getDefaultForumLayout() {
        return getData().defaultForumLayout().toOptional()
            .map(LayoutType::valueOf)
            .orElseThrow(IllegalStateException::new); // Mandatory for Forum channels
    }

    /**
     * Requests to edit the current forum channel object
     *
     * @param spec an immutable object that specifies the modifications requested
     * @return A {@link Mono} that, upon completion, emits the updated {@link ForumChannel} object
     */
    public Mono<ForumChannel> edit(ForumChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                () -> getClient().getRestClient().getChannelService()
                    .modifyChannel(getId().asLong(), spec.asRequest(), spec.reason()))
            .map(data -> EntityUtil.getChannel(getClient(), data))
            .cast(ForumChannel.class);
    }

    /**
     * Starts a new {@link ThreadChannel} in this forum channel
     *
     * @param request an immutable object that specifies how to create the thread
     * @return A {@link Mono} that, upon completion, emits a {@link ThreadChannel} object
     */
    public Mono<ThreadChannel> startThread(StartThreadInForumChannelSpec request) {
        return getClient().getRestClient().getChannelService().startThreadInForumChannel(getId().asLong(), request.asRequest())
            .map(channelData -> new ThreadChannel(getClient(), channelData));
    }

    /**
     * Starts a new {@link ThreadChannel} in this forum channel. Properties specifying how to create this channel
     * can be set via the {@code withXxx} methods of the returned {@link StartThreadInForumChannelMono}.
     *
     * @param name The name of the thread
     * @param messageCreateSpec The message to start the thread with
     * @return A {@link StartThreadInForumChannelMono} where, upon successful completion, emits the created
     * {@link ThreadChannel}. If an error is received, it is emitted through the {@code Mono}.
     */
    public StartThreadInForumChannelMono startThread(String name, ForumThreadMessageCreateSpec messageCreateSpec) {
        return StartThreadInForumChannelMono.of(name, messageCreateSpec, this);
    }

    /**
     * Request to retrieve all threads in this channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getAllThreads() {
        return Flux.merge(getActiveThreads(), getArchivedThreads());
    }

    /**
     * Request to retrieve all active threads in this forum channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getActiveThreads() {
        return getClient().getGuildById(getGuildId())
            .flatMapMany(Guild::getActiveThreads)
            .map(ThreadListPart::getThreads)
            .flatMap(Flux::fromIterable)
            .filter(thread -> thread.getParentId().map(id -> id.equals(getId())).orElse(false));
    }

    /**
     * Request to retrieve all archived threads in this forum channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getArchivedThreads() {
        return getRestChannel()
            .getPublicArchivedThreads()
            .map(ListThreadsData::threads)
            .flatMap(Flux::fromIterable)
            .map(channelData -> new ThreadChannel(getClient(), channelData));
    }

    /**
     * Layout types are used by Forum channels to organize their threads channel into a user view
     * <p>
     * Please see <a href="https://discord.com/developers/docs/resources/channel#channel-object-forum-layout-types">Discord documentation</a> for further details.
     */
    public enum LayoutType {
        /**
         * Internal value only, for unknown values
         */
        UNKNOWN(-1),

        /**
         * Default value for unset layout
         */
        NOT_SET(0),

        /**
         * Layout showing threads as a list
         */
        LIST_VIEW(1),

        /**
         * Layout showing threads as a gallery
         */
        GALLERY_VIEW(2);

        /**
         * Discord represented integer value
         */
        private final int value;

        LayoutType(int value) {
            this.value = value;
        }

        /**
         * Gets Discord represented value
         *
         * @return Discord integer represented value
         */
        public int getValue() {
            return value;
        }

        /**
         * Translates an integer value to a {@link LayoutType}
         *
         * @param value The integer value
         * @return The {@link LayoutType} if known, or the fallback UNKNOWN value
         */
        public static LayoutType valueOf(final int value) {
            // Same as SortOrder, we could exclude the UNKNOWN value
            for (LayoutType layoutType : LayoutType.values()) {
                if (layoutType.getValue() == value) {
                    return layoutType;
                }
            }
            // Fallback to UNKNOWN as default value
            return LayoutType.UNKNOWN;
        }
    }

    /**
     * Sort orders are proposed by Discord to choose the order or threads in a Forum channel
     * <br/>
     * Please see <a href="https://discord.com/developers/docs/resources/channel#channel-object-sort-order-types">Discord documentation</a> for further details
     */
    public enum SortOrder {

        /**
         * Internal value only, for unknown values
         */
        UNKNOWN(-1),
        LATEST_ACTIVITY(0),
        CREATION_DATE(1);

        /**
         * Discord represented integer value
         */
        private final int value;

        SortOrder(int value) {
            this.value = value;
        }

        /**
         * Gets Discord represented value
         *
         * @return Discord integer represented value
         */
        public int getValue() {
            return value;
        }

        /**
         * Translates an integer value to a {@link SortOrder}
         *
         * @param value The integer value
         * @return The SortOrder if known, or the fallback UNKNOWN value
         */
        public static SortOrder valueOf(final int value) {
            /* We could exclude UNKNOWN from the lookup array using
            Arrays.copyOfRange(SortOrder.values(), 1, SortOrder.values().length) but this would just waste CPU time
            because if -1 is one day used by Discord we would need anyway to update the code here */
            for (SortOrder sortOrder : SortOrder.values()) {
                if (sortOrder.getValue() == value) {
                    return sortOrder;
                }
            }
            // Fallback to UNKNOWN as default value
            return SortOrder.UNKNOWN;
        }
    }

}
