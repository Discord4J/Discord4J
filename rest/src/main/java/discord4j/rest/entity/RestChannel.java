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
import discord4j.discordjson.json.*;
import discord4j.rest.RestClient;
import discord4j.rest.util.MultipartRequest;
import discord4j.rest.util.PaginationUtil;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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
        return restClient.getChannelService().createMessage(id, new MultipartRequest(request));
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
    public Mono<MessageData> createMessage(MultipartRequest request) {
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

        return Flux.create(sink -> {
            final Disposable disposable = Flux.from(messageIds)
                .distinct(Snowflake::asLong) // REST requires unique IDs
                .filter(id -> { // REST requires IDs that are younger than 2 weeks
                    final boolean ignored = timeLimit.isAfter(id.getTimestamp());
                    if (ignored) {
                        sink.next(id);
                    }

                    return !ignored;
                }).map(Snowflake::asString)
                .buffer(100) // REST requires N <= 100 IDs
                .filter(chunk -> { // REST requires N >= 2 IDs
                    final boolean ignored = chunk.size() == 1;
                    if (ignored) {
                        sink.next(Snowflake.of(chunk.get(0)));
                    }

                    return !ignored;
                }).flatMap(chunk -> restClient.getChannelService()
                    .bulkDeleteMessages(id, BulkDeleteRequest.builder().messages(chunk).build()))
                .subscribe(null, sink::error, sink::complete, sink.currentContext());

            sink.onDispose(disposable);
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
}
