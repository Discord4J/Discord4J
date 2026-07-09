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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.ThreadListPart;
import discord4j.core.object.entity.ForumTag;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.reaction.DefaultReaction;
import discord4j.core.spec.ForumChannelEditSpec;
import discord4j.core.spec.ForumThreadMessageCreateSpec;
import discord4j.core.spec.StartThreadInForumChannelMono;
import discord4j.core.spec.StartThreadInForumChannelSpec;
import discord4j.core.spec.StartThreadInMediaChannelMono;
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
 * A Discord guild channel that contains organized threads with labels where only can use images.
 */
public final class MediaChannel extends BaseTopLevelGuildChannel implements CategorizableChannel {

    public MediaChannel(GatewayDiscordClient gateway, ChannelData data) {
        super(gateway, data);
    }

    /**
     * Gets the topic for this guild media.
     *
     * @return the user defined topic for this guild media channel (0-4096 characters).
     */
    public Optional<String> getTopic() {
        return this.getData().topic().toOptional()
                .flatMap(opt -> opt); // Value is present on the guild media type but nullable
    }

    /**
     * Gets whether this channel is considered NSFW (Not Safe For Work).
     *
     * @return {@code true} if this channel is considered NSFW (Not Safe For Work), {@code false} otherwise.
     */
    public boolean isNsfw() {
        return this.getData().nsfw().toOptional().orElse(false);
    }

    /**
     * Gets the channels {@link discord4j.core.object.entity.Message.Flag} associated to this media channel
     * Unknown flags are currently ignored.
     *
     * @return An {@link EnumSet} representing the <b>known flags</b> for this media channel.
     */
    public EnumSet<Flag> getFlags() {
        return this.getData().flags().toOptional()
                .map(Flag::valueOf)
                .orElseThrow(IllegalStateException::new); // Mandatory for media channels
    }

    /**
     * Gets the default auto archive duration for threads in this media channel
     *
     * @return Default client auto archive duration in minutes wrapped in an {@link Optional} object
     */
    public Optional<Integer> getDefaultAutoArchiveDuration() {
        return this.getData().defaultAutoArchiveDuration().toOptional();
    }

    /**
     * Gets the available forum tags for this media channel
     *
     * @return A list containing every available forum tag for this channel
     */
    public List<ForumTag> getAvailableTags() {
        return this.getData().availableTags().toOptional()
                .orElseThrow(IllegalStateException::new) // Mandatory for media channels
                .stream()
                .map(forumTagData -> new ForumTag(getClient(), forumTagData))
                .collect(Collectors.toList());
    }

    /**
     * Gets the default emoji to add in a reaction button on a thread for this media channel
     *
     * @return An {@link Optional} that may contain the {@link DefaultReaction} emoji
     */
    public Optional<DefaultReaction> getDefaultReaction() {
        return this.getData().defaultReactionEmoji()
                .toOptional().orElseThrow(IllegalStateException::new) // Mandatory for media channels
                .map(defaultReactionData -> new DefaultReaction(getClient(), defaultReactionData));
    }

    /**
     * Gets the default thread rate limit duration per user.
     * This field is copied into a created thread and is not updated on the fly.
     *
     * @return The initial rate limit per user for newly created threads
     */
    public Optional<Integer> getDefaultThreadRateLimitPerUser() {
        return this.getData().defaultThreadRateLimitPerUser().toOptional();
    }


    /**
     * Gets the default associated {@link ForumChannel.SortOrder} for this media channel
     *
     * @return The {@link ForumChannel.SortOrder}, wrapped in an {@link Optional}
     */
    public Optional<ForumChannel.SortOrder> getSortOrder() {
        return this.getData().defaultSortOrder().toOptional()
                .orElseThrow(IllegalStateException::new) // Mandatory for media channels
                .map(ForumChannel.SortOrder::valueOf);
    }

    /**
     * Gets the associated {@link ForumChannel.LayoutType} for this media channel
     *
     * @return The associated {@link ForumChannel.LayoutType}
     */
    public ForumChannel.LayoutType getDefaultForumLayout() {
        return this.getData().defaultForumLayout().toOptional()
                .map(ForumChannel.LayoutType::valueOf)
                .orElseThrow(IllegalStateException::new); // Mandatory for media channels
    }

    /**
     * Requests to edit the current media channel object
     *
     * @param spec an immutable object that specifies the modifications requested
     * @return A {@link Mono} that, upon completion, emits the updated {@link ForumChannel} object
     */
    public Mono<ForumChannel> edit(ForumChannelEditSpec spec) {
        Objects.requireNonNull(spec);
        return Mono.defer(
                        () -> this.getClient().getRestClient().getChannelService()
                                .modifyChannel(this.getId().asLong(), spec.asRequest(), spec.reason()))
                .map(data -> EntityUtil.getChannel(this.getClient(), data))
                .cast(ForumChannel.class);
    }

    /**
     * Starts a new {@link ThreadChannel} in this media channel
     *
     * @param request an immutable object that specifies how to create the thread
     * @return A {@link Mono} that, upon completion, emits a {@link ThreadChannel} object
     */
    public Mono<ThreadChannel> startThread(StartThreadInForumChannelSpec request) {
        return this.getClient().getRestClient().getChannelService().startThreadInForumChannel(getId().asLong(), request.asRequest())
                .map(channelData -> new ThreadChannel(this.getClient(), channelData));
    }

    /**
     * Starts a new {@link ThreadChannel} in this media channel. Properties specifying how to create this channel
     * can be set via the {@code withXxx} methods of the returned {@link StartThreadInForumChannelMono}.
     *
     * @param name The name of the thread
     * @param messageCreateSpec The message to start the thread with
     * @return A {@link StartThreadInForumChannelMono} where, upon successful completion, emits the created
     * {@link ThreadChannel}. If an error is received, it is emitted through the {@code Mono}.
     */
    public StartThreadInMediaChannelMono startThread(String name, ForumThreadMessageCreateSpec messageCreateSpec) {
        return StartThreadInMediaChannelMono.of(name, messageCreateSpec, this);
    }

    /**
     * Request to retrieve all threads in this channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getAllThreads() {
        return Flux.merge(this.getActiveThreads(), this.getArchivedThreads());
    }

    /**
     * Request to retrieve all active threads in this media channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getActiveThreads() {
        return this.getClient().getGuildById(getGuildId())
                .flatMapMany(Guild::getActiveThreads)
                .map(ThreadListPart::getThreads)
                .flatMap(Flux::fromIterable)
                .filter(thread -> thread.getParentId().map(id -> id.equals(getId())).orElse(false));
    }

    /**
     * Request to retrieve all archived threads in this media channel.
     *
     * @return A {@link Flux} that continually emits the {@link ThreadChannel threads} of the channel. If an error is
     * received, it is emitted through the {@code Flux}.
     */
    public Flux<ThreadChannel> getArchivedThreads() {
        return this.getRestChannel()
                .getPublicArchivedThreads()
                .map(ListThreadsData::threads)
                .flatMap(Flux::fromIterable)
                .map(channelData -> new ThreadChannel(getClient(), channelData));
    }
}
