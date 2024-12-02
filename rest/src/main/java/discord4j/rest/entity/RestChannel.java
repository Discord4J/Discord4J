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

package discord4j.rest.entity;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.BulkDeleteRequest;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.FollowedChannelData;
import discord4j.discordjson.json.GroupAddRecipientRequest;
import discord4j.discordjson.json.InviteCreateRequest;
import discord4j.discordjson.json.InviteData;
import discord4j.discordjson.json.ListThreadsData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.NewsChannelFollowRequest;
import discord4j.discordjson.json.PermissionsEditRequest;
import discord4j.discordjson.json.StartThreadFromMessageRequest;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import discord4j.discordjson.json.ThreadMemberData;
import discord4j.discordjson.json.ThreadMetadata;
import discord4j.discordjson.json.WebhookData;
import discord4j.rest.RestClient;
import discord4j.rest.util.MultipartRequest;
import discord4j.rest.util.PaginationUtil;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a guild or DM channel within Discord.
 */
public class RestChannel {

    private final RestClient restClient;
    private final long id;

    private RestChannel(RestClient restClient, long id) {
        this.restClient = restClient;
        this.id = id;
    }

    /**
     * Create a {@link RestChannel} with the given parameters.
     *
     * @param restClient REST API resources
     * @param id the ID of this channel
     */
    public static RestChannel create(RestClient restClient, Snowflake id) {
        return new RestChannel(restClient, id.asLong());
    }

    static RestChannel create(RestClient restClient, long id) {
        return new RestChannel(restClient, id);
    }

    /**
     * Returns the ID of this channel.
     *
     * @return The ID of this channel
     */
    public Snowflake getId() {
        return Snowflake.of(id);
    }

    /**
     * Retrieve this channel's data upon subscription.
     *
     * @return a {@link Mono} where, upon successful completion, emits the {@link ChannelData} belonging to this
     * channel. If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel">Get Channel</a>
     */
    public Mono<ChannelData> getData() {
        return restClient.getChannelService().getChannel(id);
    }

    /**
     * Return a {@link RestMessage} belonging to this channel. This method does not perform any API request.
     *
     * @param messageId the message ID under this channel
     * @return a {@code RestMessage} represented by the given parameters.
     */
    public RestMessage message(Snowflake messageId) {
        return RestMessage.create(restClient, id, messageId.asLong());
    }

    /**
     * Request to edit this text channel using a given {@link ChannelModifyRequest} as body and optionally, a reason.
     *
     * @param request request body used to create a new message
     * @param reason a reason for this action, can be {@code null}
     * @return a {@link Mono} where, upon successful completion, emits the edited {@link ChannelData}. If an error is
     * received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#modify-channel">Modify Channel</a>
     */
    public Mono<ChannelData> modify(ChannelModifyRequest request, @Nullable String reason) {
        return restClient.getChannelService().modifyChannel(id, request, reason);
    }

    /**
     * Request to delete this channel while optionally specifying a reason.
     *
     * @param reason a reason for this action, can be {@code null}
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the channel has been deleted.
     * If an error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#deleteclose-channel">Delete/Close Channel</a>
     */
    public Mono<Void> delete(@Nullable String reason) {
        return restClient.getChannelService().deleteChannel(id, reason).then();
    }

    /**
     * Request to retrieve <i>all</i> messages <i>before</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in <i>reverse-</i>chronological order (newest to oldest). It is
     * recommended to limit the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs
     * within a specified range) or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> messages from {@code messageId} to {@code myOtherMessageId}:
     * {@code getMessagesBefore(messageId).takeWhile(message -> message.getId().compareTo(myOtherMessageId) >= 0)}
     *
     * @param messageId The ID of the <i>newest</i> message to retrieve.
     * @return A {@link Flux} that continually emits <i>all</i> {@link MessageData messages} <i>before</i> the
     * specified ID. If an error is received, it is emitted through the {@code Flux}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#get-channel-messages">Get Channel Messages</a>
     */
    public Flux<MessageData> getMessagesBefore(Snowflake messageId) {
        Function<Map<String, Object>, Flux<MessageData>> doRequest =
                params -> restClient.getChannelService().getMessages(id, params);
        return PaginationUtil.paginateBefore(doRequest, data -> Snowflake.asLong(data.id()), messageId.asLong(), 100);
    }

    /**
     * Request to retrieve <i>all</i> messages <i>after</i> the specified ID.
     * <p>
     * The returned {@code Flux} will emit items in chronological order (oldest to newest). It is recommended to limit
     * the emitted items by invoking either {@link Flux#takeWhile(Predicate)} (to retrieve IDs within a specified range)
     * or {@link Flux#take(long)} (to retrieve a specific amount of IDs).
     * <p>
     * The following example will get <i>all</i> messages from {@code messageId} to {@code myOtherMessageId}:
     * {@code getMessagesAfter(messageId).takeWhile(message -> message.getId().compareTo(myOtherMessageId) <= 0)}
     *
     * @param messageId the ID of the <i>oldest</i> message to retrieve.
     * @return a {@link Flux} that continually emits <i>all</i> {@link MessageData messages} <i>after</i> the
     * specified ID. If an error is received, it is emitted through the {@code Flux}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#get-channel-messages">Get Channel Messages</a>
     */
    public Flux<MessageData> getMessagesAfter(Snowflake messageId) {
        Function<Map<String, Object>, Flux<MessageData>> doRequest = params ->
                restClient.getChannelService().getMessages(id, params);
        return PaginationUtil.paginateAfter(doRequest, data -> Snowflake.asLong(data.id()), messageId.asLong(), 100);
    }

    /**
     * Create a {@link RestMessage} entity for a given ID under this channel. This method does not perform any
     * network request.
     *
     * @param messageId the message's ID
     * @return a {@link RestMessage} facade for the given message under this channel to perform actions on it
     */
    public RestMessage getRestMessage(Snowflake messageId) {
        return RestMessage.create(restClient, id, messageId.asLong());
    }

    /**
     * Request to create a message using a given {@link MessageCreateRequest} as body. If you want to include
     * attachments to your message, see {@link #createMessage(MultipartRequest)}.
     *
     * @param request request body used to create a new message
     * @return a {@link Mono} where, upon successful completion, emits the created {@link MessageData}. If an
     * error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#create-message">Create Message</a>
     */
    public Mono<MessageData> createMessage(MessageCreateRequest request) {
        return restClient.getChannelService().createMessage(id, MultipartRequest.ofRequest(request));
    }

    /**
     * Request to create a message using a given {@link MultipartRequest} as body. A {@link MultipartRequest} is a
     * custom object allowing you to add attachments to a message.
     *
     * @param request request body used to create a new message
     * @return a {@link Mono} where, upon successful completion, emits the created {@link MessageData}. If an
     * error is received, it is emitted through the {@code Mono}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#create-message">Create Message</a>
     */
    public Mono<MessageData> createMessage(MultipartRequest<MessageCreateRequest> request) {
        // TODO: improve API to create MultipartRequest objects
        return restClient.getChannelService().createMessage(id, request);
    }

    /**
     * Wrapper for {@link RestChannel#createMessage(MessageCreateRequest)} taking only message content.
     *
     * @param content The content of the message
     * @return a {@link Mono} where, upon successful completion, emits the created {@link MessageData}. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageData> createMessage(String content) {
        return createMessage(MessageCreateRequest.builder().content(content).build());
    }

    /**
     * Wrapper for {@link RestChannel#createMessage(MessageCreateRequest)} taking an embed only.
     *
     * @param embed The embed of the message
     * @return a {@link Mono} where, upon successful completion, emits the created {@link MessageData}. If an
     * error is received, it is emitted through the {@code Mono}.
     */
    public Mono<MessageData> createMessage(EmbedData embed) {
        return createMessage(MessageCreateRequest.builder().embed(embed).build());
    }

    /*
     * This is the tale of a woman who spent way too much time implementing this and, in the end, ~~it didn't even
     * matter~~ it's only somewhat okay. This comment will go into lengthy detail on how the current implementation was
     * derived and hopefully what we can expect out of Reactor in the future to make it better or even perfect.
     *
     * Bulk deleting for Discord is tricky. Discord itself has 3 limitations we need to work around; messages cannot be
     * older than 2 weeks, you cannot bulk delete more than 100 messages, and you must supply 2 or more messages. In
     * addition to these limitations, there are limitations the implementation needs to impose on itself. For one,
     * messageIds cannot be cached as the source can be effectively infinite for large channels and cause a OOME. The
     * second limitation is messageIds should not be subscribed multiple times (per subscription of bulkDelete) for the
     * same reason and will likely hit rate limits due to the sheer amount of message fetching if the source is coming
     * from a getMessagesBefore or getMessagesAfter call (the most common use case for this method).
     *
     * With these limitations in mind our options become rather limited. Let's also add that our goal is to be
     * "efficient" which we will define as doing the least amount of bulk delete requests as possible and to be
     * "accurate" which means every eligible message for bulk delete is bulk deleted. Let's consider the options
     * I have thought of and explain why they won't work.
     *
     * The original implementation of this method was the most "efficient" and "accurate". What it did was iterate
     * through messageIds and put all ineligible ones into some Collection then buffer the eligible ones up to 100 then
     * bulk deleted them. After iterating through all messageIds the outstanding buffer is bulk deleted (if there was a
     * collection of 1 left over it was put into ineligible) then it would send out all the ineligible IDs at once. We
     * are "efficient" by only doing requests once we've filled our buffers and we are "accurate" in that no eligible
     * message was counted as ineligible.
     *
     * Splendid!...so why was it changed? Well, the big problem comes from that external Collection. For one, because of
     * how it was setup the insertion wasn't (and couldn't be) done in a thread-safe manner. For List implementations
     * there is no real efficient way to guarantee memory-safety except for copying (CopyOnWriteArrayList, way too much
     * memory and costly performance wise) or synchronizing (which is introducing blocking locks in a reactive
     * environment). The second problem is a lot more damning, for large channels it would OOME. Since for any long-term
     * channel most messages will be older than 2 weeks, so the vast majority of IDs will be put into that Collection.
     * If the source is from an unbounded getMessagesBefore, for example, then for any sufficiently large channel the
     * bot would certainly OOME with no real obvious reason. With these two limitations, this was scraped.
     *
     * The second implementation is almost like the first, but with a small tweak. Instead of a Collection that would
     * hold ineligible IDs and dump them in the end, it was a sink so as ineligible IDs are found they are just
     * immediately sent downstream. Cool! We just solved both of the previous problems at once! But then a new problem
     * arrived, early termination of the chain and thus dropping buffered eligible IDs. Take the following:
     *
     * channel.getMessagesBefore(Snowflake.of(Instant.now()))
     *     .map(Message::getId)
     *     .transform(channel::bulkDelete)
     *     .take(100)
     *
     * The snippet says "keep bulk deleting messages until 100 ineligible messages arrive". Let us assume the channel
     * has 23 eligible messages but has many thousands of ineligible ones. In the implementation I described what would
     * happen is we buffer up the 23 eligible messages, but then we start sending ineligible ones downstream. Once we
     * sent 100, the chain is rightfully cancelled and the buffer just...goes away. We never even bulk deleted! Because
     * once the chain is cancelled you cannot follow it up with another reactive action (the bulk delete) as that
     * doesn't make sense. Now let's say you have 123 eligible messages, you get 100 bulk deleted correctly, but then
     * drop the remaining 23...one can see how this would be incredibly annoying for the end-user. They wouldn't even
     * come back through the Flux! They are just lost...forever.
     *
     * Which leads us to this implementation. Instead of buffering up only eligible IDs instead let's buffer *all* IDs.
     * Once you have a buffer, separate out the eligible ones from the ineligible, do a bulk delete, then send out the
     * ineligible ones. This implementation...works. But it's not the most "accurate" nor "efficient" theoretical way to
     * do this. Let's go into detail why.
     *
     * For the earlier snippet this implementation is perfectly "efficient" and "accurate". Since the messages are
     * ordered (reverse chronologically) all eligible IDs are grouped at once and thus bulk deleted as expected. The
     * rest are sent downstream to be terminated early with little memory penalty. Perfect, and for most users, the use
     * case of using this with a getMessagesBefore is the most common one.
     *
     * For getMessagesAfter it is "efficient", but not really "accurate", but it's only "inaccurate" for a very fringe
     * case. Consider having 152 messages and the first 99 are ineligible, and the rest are eligible. What will happen
     * is the 100th message in the buffer, seeing as it's only 1 message, will be counted as ineligible, when it could
     * have, optimally, been put with the other 51 messages in the next chunk and be bulk deleted. However, the
     * implementation is still "efficient" as bulk deleted messages will be grouped together (just at the end rather
     * than the start).
     *
     * For random cases, yeah, the "accuracy" and "efficiency" will be all over the place. Assuming even distribution of
     * 50 ineligible messages and 50 eligible messages per buffer then our "efficiency" is twice as worse than it could
     * be (doing bulk deletes in sets of 50s instead of 100s). And because eligible messages are not grouped, there can
     * be many cases of lone eligible messages in buffers that will then be counted as ineligible. That said, the chance
     * of the source of messageIds being completely random is a rather fringe case, and the API *does* work as it is
     * stated in the documentation. So I think despite these potential problems, this is acceptable behavior given our
     * goals and many limitations.
     *
     * So how could this all be improved? If simply there was an operation in Reactor where for some buffer operation,
     * if a cancellation happened, whatever is in the buffer is sent out in some capacity and can be worked on in some
     * reactive manner. We could then go back to the second implementation of this method and be perfectly "efficient"
     * and "accurate" with no consequences.
     *
     * And that's it. Wow that was long. Hope you all enjoyed that needlessly large explanation of my rationale that
     * very few will likely read. Oh well. Hope you enjoy fixing this mess. -Dannie
     */

    /**
     * Request to bulk delete the supplied message IDs.
     *
     * @param messageIds a {@link Publisher} to supply the message IDs to bulk delete.
     * @return a {@link Flux} that continually emits {@link Long message IDs} that were <b>not</b> bulk deleted
     * (typically if the ID was older than 2 weeks). If an error is received, it is emitted through the {@code Flux}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#bulk-delete-messages">Bulk Delete Messages</a>
     */
    public Flux<Snowflake> bulkDelete(final Publisher<Snowflake> messageIds) {
        final Instant timeLimit = Instant.now().minus(Duration.ofDays(14L));

        return Flux.from(messageIds)
            .distinct()
            .buffer(100)
            .flatMap(ids -> {
                final List<String> eligibleIds = new ArrayList<>(0);
                final Collection<Snowflake> ineligibleIds = new ArrayList<>(0);

                for (final Snowflake id : ids) {
                    if (id.getTimestamp().isBefore(timeLimit)) {
                        ineligibleIds.add(id);

                    } else {
                        eligibleIds.add(id.asString());
                    }
                }

                if (eligibleIds.size() == 1) {
                    ineligibleIds.add(Snowflake.of(eligibleIds.get(0)));
                    eligibleIds.clear();
                }

                return Mono.just(eligibleIds)
                    .filter(chunk -> !chunk.isEmpty())
                    .flatMap(chunk -> restClient.getChannelService()
                        .bulkDeleteMessages(id, BulkDeleteRequest.builder().messages(chunk).build()))
                    .thenMany(Flux.fromIterable(ineligibleIds));
            });
    }

    /**
     * Request to edit channel permission overwrites for the given member or role while optionally specifying a reason.
     *
     * @param targetId the ID of the member or role to add the overwrite for
     * @param request the overwrite request to edit
     * @param reason the reason, if present
     * @return a {@link Mono} where, upon successful completion, emits nothing; If an error is received, it is emitted
     * through the {@code Mono}
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#edit-channel-permissions">Edit Channel Permissions</a>
     */
    public Mono<Void> editChannelPermissions(Snowflake targetId, PermissionsEditRequest request,
                                             @Nullable String reason) {
        return restClient.getChannelService().editChannelPermissions(id, targetId.asLong(), request, reason);
    }

    /**
     * Request to retrieve this channel's invites.
     *
     * @return a {@link Flux} that continually emits this channel's {@link InviteData invites}. If an error is
     * received, it is emitted through the {@code Flux}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-channel-invites">Get Channel Invites</a>
     */
    public Flux<InviteData> getInvites() {
        return restClient.getChannelService().getChannelInvites(id);
    }

    /**
     * Request to create an invite.
     *
     * @param request request body used to create a new invite
     * @param reason the reason, if present
     * @return a {@link Mono} where, upon successful completion, emits the created {@link InviteData}. If an error
     * is received, it is emitted through the {@code Mono}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#create-channel-invite">Create Channel Invite</a>
     */
    public Mono<InviteData> createInvite(InviteCreateRequest request, @Nullable String reason) {
        return restClient.getChannelService().createChannelInvite(id, request, reason);
    }

    /**
     * Request to delete this permission overwrite while optionally specifying a reason.
     *
     * @param reason the reason, if present.
     * @return a {@link Mono} where, upon successful completion, emits nothing; indicating the permission overwrite has
     * been deleted. If an error is received, it is emitted through the {@code Mono}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#delete-channel-permission">Delete Channel Permission</a>
     */
    public Mono<Void> deleteChannelPermission(Snowflake targetId, @Nullable final String reason) {
        return restClient.getChannelService().deleteChannelPermission(id, targetId.asLong(), reason);
    }

    /**
     * Requests to follow this channel. Only works if this channel represents a news channel. Following this channel
     * will create a webhook in a chosen target channel where 'MANAGE_WEBHOOKS' permission is granted.
     *
     * @param request the request to follow this channel
     * @return A {@link Mono} where, upon successful completion, emits the data indicating that the channel has been
     * followed. If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<FollowedChannelData> follow(NewsChannelFollowRequest request) {
        return restClient.getChannelService().followNewsChannel(id, request);
    }

    /**
     * Request to trigger the typing indicator in this channel. A single invocation of this method will trigger the
     * indicator for 10 seconds or until the bot sends a message in this channel.
     *
     * @return a {@link Mono} which completes upon successful triggering of the typing indicator in this channel. If
     * an error is received, it is emitted through the {@code Mono}.
     * @see
     * <a href="https://discord.com/developers/docs/resources/channel#trigger-typing-indicator">Trigger Typing Indicator</a>
     */
    public Mono<Void> type() {
        return restClient.getChannelService().triggerTypingIndicator(id);
    }

    /**
     * Request to retrieve all the pinned messages for this channel.
     *
     * @return a {@link Flux} that continually emits all the pinned messages for this channel. If an error is received,
     * it is emitted through the {@code Flux}.
     * @see <a href="https://discord.com/developers/docs/resources/channel#get-pinned-messages">Get Pinned Messages</a>
     */
    public Flux<MessageData> getPinnedMessages() {
        return restClient.getChannelService().getPinnedMessages(id);
    }

    /**
     * Request to pin a message in this channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the messaged was pinned. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> addPinnedMessage(Snowflake messageId) {
        return restClient.getChannelService().addPinnedMessage(id, messageId.asLong());
    }

    /**
     * Request to unpin a message in this channel.
     *
     * @return A {@link Mono} where, upon successful completion, emits nothing; indicating the message was unpinned. If
     * an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> deletePinnedMessage(Snowflake messageId) {
        return restClient.getChannelService().deletePinnedMessage(id, messageId.asLong());
    }

    public Mono<Void> addGroupDMRecipient(Snowflake userId, GroupAddRecipientRequest request) {
        return restClient.getChannelService().addGroupDMRecipient(id, userId.asLong(), request);
    }

    public Mono<Void> deleteGroupDMRecipient(Snowflake userId) {
        return restClient.getChannelService().deleteGroupDMRecipient(id, userId.asLong());
    }

    public Flux<WebhookData> getWebhooks() {
        return restClient.getWebhookService().getChannelWebhooks(id);
    }

    public Mono<ThreadMemberData> getThreadMember(Snowflake userId) {
        return restClient.getChannelService().getThreadMember(id, userId.asLong());
    }

    public Flux<ListThreadsData> listThreads(Function<Map<String, Object>, Mono<ListThreadsData>> doRequest) {
        Function<ListThreadsData, String> getLastThreadId = response -> {
            List<ChannelData> threads = response.threads();

            if (!response.hasMore().toOptional().orElse(false)) {
                return null;
            }

            if (threads.isEmpty()) {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
            }

            return threads.get(threads.size() - 1)
                .threadMetadata()
                .toOptional()
                .map(ThreadMetadata::archiveTimestamp)
                .orElse(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()));
        };

        return PaginationUtil.paginateBefore(doRequest.andThen(Mono::flux), getLastThreadId, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()), 100);
    }

    public Flux<ListThreadsData> getPublicArchivedThreads() {
        return listThreads(params -> restClient.getChannelService().listPublicArchivedThreads(id, params));
    }

    public Flux<ListThreadsData> getPrivateArchivedThreads() {
        return listThreads(params -> restClient.getChannelService().listPrivateArchivedThreads(id, params));
    }

    public Flux<ListThreadsData> getJoinedPrivateArchivedThreads() {
        return listThreads(params -> restClient.getChannelService().listJoinedPrivateArchivedThreads(id, params));
    }

    public Mono<ChannelData> startThreadWithoutMessage(StartThreadWithoutMessageRequest request) {
        return restClient.getChannelService().startThreadWithoutMessage(id, request);
    }

    public Mono<ChannelData> startThreadFromMessage(long messageId, StartThreadFromMessageRequest request) {
        return restClient.getChannelService().startThreadWithMessage(id, messageId, request);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RestChannel that = (RestChannel) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
