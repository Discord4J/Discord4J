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

import discord4j.common.annotations.Experimental;
import discord4j.common.json.MessageResponse;
import discord4j.common.json.UserResponse;
import discord4j.rest.json.request.*;
import discord4j.rest.json.response.ChannelResponse;
import discord4j.rest.json.response.InviteResponse;
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

    public Mono<ChannelResponse> getChannel(long channelId) {
        return Routes.CHANNEL_GET.newRequest(channelId)
                .exchange(getRouter());
    }

    public Mono<ChannelResponse> modifyChannel(long channelId, ChannelModifyRequest request, @Nullable String reason) {
        return Routes.CHANNEL_MODIFY_PARTIAL.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter());
    }

    public Mono<ChannelResponse> deleteChannel(long channelId, @Nullable String reason) {
        return Routes.CHANNEL_DELETE.newRequest(channelId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter());
    }

    public Flux<MessageResponse> getMessages(long channelId, Map<String, Object> queryParams) {
        return Routes.MESSAGES_GET.newRequest(channelId)
                .query(queryParams)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<MessageResponse> getMessage(long channelId, long messageId) {
        return Routes.MESSAGE_GET.newRequest(channelId, messageId)
                .exchange(getRouter());
    }

    public Mono<MessageResponse> createMessage(long channelId, MultipartRequest request) {
        return Routes.MESSAGE_CREATE.newRequest(channelId)
                .header("content-type", request.getFiles().isEmpty() ? "application/json" : "multipart/form-data")
                .body(Objects.requireNonNull(request.getFiles().isEmpty() ? request.getCreateRequest() : request))
                .exchange(getRouter());
    }

    public Mono<Void> createReaction(long channelId, long messageId, String emoji) {
        return Routes.REACTION_CREATE.newRequest(channelId, messageId, emoji)
                .exchange(getRouter());
    }

    public Mono<Void> deleteOwnReaction(long channelId, long messageId, String emoji) {
        return Routes.REACTION_DELETE_OWN.newRequest(channelId, messageId, emoji)
                .exchange(getRouter());
    }

    public Mono<Void> deleteReaction(long channelId, long messageId, String emoji, long userId) {
        return Routes.REACTION_DELETE.newRequest(channelId, messageId, emoji, userId)
                .exchange(getRouter());
    }

    public Flux<UserResponse> getReactions(long channelId, long messageId, String emoji,
                                           Map<String, Object> queryParams) {
        return Routes.REACTIONS_GET.newRequest(channelId, messageId, emoji)
                .query(queryParams)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> deleteAllReactions(long channelId, long messageId) {
        return Routes.REACTIONS_DELETE_ALL.newRequest(channelId, messageId)
                .exchange(getRouter());
    }

    public Mono<MessageResponse> editMessage(long channelId, long messageId, MessageEditRequest request) {
        return Routes.MESSAGE_EDIT.newRequest(channelId, messageId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteMessage(long channelId, long messageId, @Nullable String reason) {
        return Routes.MESSAGE_DELETE.newRequest(channelId, messageId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter());
    }

    public Mono<Void> bulkDeleteMessages(long channelId, BulkDeleteRequest request) {
        return Routes.MESSAGE_DELETE_BULK.newRequest(channelId)
                .body(request)
                .exchange(getRouter());
    }

    @Experimental
    public Mono<Void> suppressEmbeds(long channelId, long messageId, SuppressEmbedsRequest request) {
        return Routes.MESSAGE_SUPPRESS_EMBEDS.newRequest(channelId,messageId)
                .header("content-type", "application/json")
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> editChannelPermissions(long channelId, long overwriteId, PermissionsEditRequest request, @Nullable String reason) {
        return Routes.CHANNEL_PERMISSIONS_EDIT.newRequest(channelId, overwriteId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter());
    }

    public Flux<InviteResponse> getChannelInvites(long channelId) {
        return Routes.CHANNEL_INVITES_GET.newRequest(channelId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<InviteResponse> createChannelInvite(long channelId, InviteCreateRequest request, @Nullable String reason) {
        return Routes.CHANNEL_INVITE_CREATE.newRequest(channelId)
                .body(request)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter());
    }

    public Mono<Void> deleteChannelPermission(long channelId, long overwriteId, @Nullable String reason) {
        return Routes.CHANNEL_PERMISSION_DELETE.newRequest(channelId, overwriteId)
                .optionalHeader("X-Audit-Log-Reason", reason)
                .exchange(getRouter());
    }

    public Mono<Void> triggerTypingIndicator(long channelId) {
        return Routes.TYPING_INDICATOR_TRIGGER.newRequest(channelId)
                .exchange(getRouter());
    }

    public Flux<MessageResponse> getPinnedMessages(long channelId) {
        return Routes.MESSAGES_PINNED_GET.newRequest(channelId)
                .exchange(getRouter())
                .flatMapMany(Flux::fromArray);
    }

    public Mono<Void> addPinnedMessage(long channelId, long messageId) {
        return Routes.MESSAGES_PINNED_ADD.newRequest(channelId, messageId)
                .exchange(getRouter());
    }

    public Mono<Void> deletePinnedMessage(long channelId, long messageId) {
        return Routes.MESSAGES_PINNED_DELETE.newRequest(channelId, messageId)
                .exchange(getRouter());
    }

    public Mono<Void> addGroupDMRecipient(long channelId, long userId, GroupAddRecipientRequest request) {
        return Routes.GROUP_DM_RECIPIENT_ADD.newRequest(channelId, userId)
                .body(request)
                .exchange(getRouter());
    }

    public Mono<Void> deleteGroupDMRecipient(long channelId, long userId) {
        return Routes.GROUP_DM_RECIPIENT_DELETE.newRequest(channelId, userId)
                .exchange(getRouter());
    }
}
