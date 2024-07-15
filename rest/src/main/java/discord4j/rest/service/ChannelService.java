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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.rest.service;

import discord4j.discordjson.json.*;
import discord4j.rest.request.Router;
import discord4j.rest.route.Routes;
import discord4j.rest.util.MultipartRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Map;
import java.util.Objects;

public class ChannelService extends RestService {

    public ChannelService(Router router) {
        super(router);
    }

    public Mono<ChannelData> getChannel(long channelId) {
        return Routes.CHANNEL_GET.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(ChannelData.class);
    }

    public Mono<ChannelData> modifyChannel(long channelId, ChannelModifyRequest request, @Nullable String reason) {
        return Routes.CHANNEL_MODIFY_PARTIAL.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(ChannelData.class);
    }

    public Mono<ChannelData> modifyThread(long channelId, ThreadModifyRequest request, @Nullable String reason) {
        return Routes.CHANNEL_MODIFY_PARTIAL.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(ChannelData.class);
    }

    public Mono<ChannelData> deleteChannel(long channelId, @Nullable String reason) {
        return Routes.CHANNEL_DELETE.newRequest(channelId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(ChannelData.class);
    }

    public Flux<MessageData> getMessages(long channelId, Map<String, Object> queryParams) {
        return Routes.MESSAGES_GET.newRequest(channelId)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(MessageData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<MessageData> getMessage(long channelId, long messageId) {
        return Routes.MESSAGE_GET.newRequest(channelId, messageId)
                .exchange(getRouter())
                .bodyToMono(MessageData.class);
    }

    public Mono<MessageData> createMessage(long channelId, MultipartRequest<MessageCreateRequest> request) {
        return Routes.MESSAGE_CREATE.newRequest(channelId)
                .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
                .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getJsonPayload() : request))
                .exchange(getRouter())
                .bodyToMono(MessageData.class);
    }

    public Mono<Void> createReaction(long channelId, long messageId, String emoji) {
        return Routes.REACTION_CREATE.newRequest(channelId, messageId, emoji)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteOwnReaction(long channelId, long messageId, String emoji) {
        return Routes.REACTION_DELETE_OWN.newRequest(channelId, messageId, emoji)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteReaction(long channelId, long messageId, String emoji, long userId) {
        return Routes.REACTION_DELETE_USER.newRequest(channelId, messageId, emoji, userId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteReactions(long channelId, long messageId, String emoji) {
        return Routes.REACTION_DELETE.newRequest(channelId, messageId, emoji)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Flux<UserData> getReactions(long channelId, long messageId, String emoji,
                                       Map<String, Object> queryParams) {
        return Routes.REACTIONS_GET.newRequest(channelId, messageId, emoji)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(UserData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> deleteAllReactions(long channelId, long messageId) {
        return Routes.REACTIONS_DELETE_ALL.newRequest(channelId, messageId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<MessageData> editMessage(long channelId, long messageId, MultipartRequest<MessageEditRequest> request) {
        return Routes.MESSAGE_EDIT.newRequest(channelId, messageId)
                .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
                .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getJsonPayload() : request))
                .exchange(getRouter())
                .bodyToMono(MessageData.class);
    }

    public Mono<Void> deleteMessage(long channelId, long messageId, @Nullable String reason) {
        return Routes.MESSAGE_DELETE.newRequest(channelId, messageId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> bulkDeleteMessages(long channelId, BulkDeleteRequest request) {
        return Routes.MESSAGE_DELETE_BULK.newRequest(channelId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<MessageData> publishMessage(long channelId, long messageId) {
        return Routes.CROSSPOST_MESSAGE.newRequest(channelId, messageId)
                .exchange(getRouter())
                .bodyToMono(MessageData.class);
    }

    public Mono<Void> editChannelPermissions(long channelId, long overwriteId, PermissionsEditRequest request,
                                             @Nullable String reason) {
        return Routes.CHANNEL_PERMISSIONS_EDIT.newRequest(channelId, overwriteId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Flux<InviteData> getChannelInvites(long channelId) {
        return Routes.CHANNEL_INVITES_GET.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(InviteData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<InviteData> createChannelInvite(long channelId, InviteCreateRequest request, @Nullable String reason) {
        return Routes.CHANNEL_INVITE_CREATE.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(InviteData.class);
    }

    public Mono<Void> deleteChannelPermission(long channelId, long overwriteId, @Nullable String reason) {
        return Routes.CHANNEL_PERMISSION_DELETE.newRequest(channelId, overwriteId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<FollowedChannelData> followNewsChannel(long channelId, NewsChannelFollowRequest request) {
        return Routes.FOLLOW_NEWS_CHANNEL.newRequest(channelId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(FollowedChannelData.class);
    }

    public Mono<Void> triggerTypingIndicator(long channelId) {
        return Routes.TYPING_INDICATOR_TRIGGER.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Flux<MessageData> getPinnedMessages(long channelId) {
        return Routes.MESSAGES_PINNED_GET.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(MessageData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> addPinnedMessage(long channelId, long messageId) {
        return Routes.MESSAGES_PINNED_ADD.newRequest(channelId, messageId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> deletePinnedMessage(long channelId, long messageId) {
        return Routes.MESSAGES_PINNED_DELETE.newRequest(channelId, messageId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> addGroupDMRecipient(long channelId, long userId, GroupAddRecipientRequest request) {
        return Routes.GROUP_DM_RECIPIENT_ADD.newRequest(channelId, userId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteGroupDMRecipient(long channelId, long userId) {
        return Routes.GROUP_DM_RECIPIENT_DELETE.newRequest(channelId, userId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<ChannelData> startThreadWithMessage(long channelId, long messageId, StartThreadFromMessageRequest request) {
        return Routes.START_THREAD_WITH_MESSAGE.newRequest(channelId, messageId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(ChannelData.class);
    }

    public Mono<ChannelData> startThreadWithoutMessage(long channelId, StartThreadWithoutMessageRequest request) {
        return Routes.START_THREAD_WITHOUT_MESSAGE.newRequest(channelId)
                .body(request)
                .exchange(getRouter())
                .bodyToMono(ChannelData.class);
    }

    public Mono<ChannelData> startThreadInForumChannel(long channelId, StartThreadInForumChannelRequest request) {
        return Routes.START_THREAD_IN_FORUM_CHANNEL_MESSAGE.newRequest(channelId)
            .body(request)
            .exchange(getRouter())
            .bodyToMono(ChannelData.class);
    }

    public Mono<Void> joinThread(long channelId) {
        return Routes.JOIN_THREAD.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> addThreadMember(long channelId, long userId) {
        return Routes.ADD_THREAD_MEMBER.newRequest(channelId, userId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> leaveThread(long channelId) {
        return Routes.LEAVE_THREAD.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<Void> removeThreadMember(long channelId, long userId) {
        return Routes.REMOVE_THREAD_MEMBER.newRequest(channelId, userId)
                .exchange(getRouter())
                .bodyToMono(Void.class);
    }

    public Mono<ThreadMemberData> getThreadMember(long channelId, long userId) {
        return Routes.GET_THREAD_MEMBER.newRequest(channelId, userId)
                .exchange(getRouter())
                .bodyToMono(ThreadMemberData.class);
    }

    public Flux<ThreadMemberData> listThreadMembers(long channelId) {
        return Routes.LIST_THREAD_MEMBERS.newRequest(channelId)
                .exchange(getRouter())
                .bodyToMono(ThreadMemberData[].class)
                .flatMapMany(Flux::fromArray);
    }

    public Mono<ListThreadsData> listPublicArchivedThreads(long channelId, Map<String, Object> queryParams) {
        return Routes.LIST_PUBLIC_ARCHIVED_THREADS.newRequest(channelId)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(ListThreadsData.class);
    }

    public Mono<ListThreadsData> listPrivateArchivedThreads(long channelId, Map<String, Object> queryParams) {
        return Routes.LIST_PRIVATE_ARCHIVED_THREADS.newRequest(channelId)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(ListThreadsData.class);
    }

    public Mono<ListThreadsData> listJoinedPrivateArchivedThreads(long channelId, Map<String, Object> queryParams) {
        return Routes.LIST_JOINED_PRIVATE_ARCHIVED_THREADS.newRequest(channelId)
                .query(queryParams)
                .exchange(getRouter())
                .bodyToMono(ListThreadsData.class);
    }
}
