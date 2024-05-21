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

package discord4j.common.store.api.layout;

import discord4j.common.store.api.object.InvalidationCause;
import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * Defines methods to handle update operations in response to events received from the Discord gateway.
 */
public interface GatewayDataUpdater {

    /**
     * Updates the internal state of the store according to the given {@link ChannelCreate} gateway dispatch. This
     * will typically perform an insert operation on the related {@link ChannelData}, and add the ID to the list
     * returned by {@link GuildData#channels()} if applicable.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ChannelDelete} gateway dispatch. This
     * will typically perform a delete operation on a related {@link ChannelData} that is already present in the
     * store, and remove the ID from the list returned by {@link GuildData#channels()} if applicable.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link ChannelData} before the deletion
     */
    Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ChannelUpdate} gateway dispatch. This
     * will typically perform an update operation on a related {@link ChannelData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link ChannelData} before the update
     */
    Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildCreate} gateway dispatch. This
     * will typically perform an insert operation on the related {@link GuildData}, as well as all associated
     * entities received in the payload, such as channels, roles, emojis, members, voice states and presences.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildDelete} gateway dispatch. This
     * will typically perform a delete operation on a related {@link GuildData} that is already present in the store,
     * and clean up all entities that are associated to that guild, such as the channels, the roles, the emojis, the
     * members, the voice states and the messages.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link GuildData} before the deletion
     */
    Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildStickersUpdate} gateway dispatch.
     * This will typically perform an update operation on a related collection of {@link StickerData} that is already
     * present in the store, and update the list returned by {@link GuildData#stickers()}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * set of {@link StickerData} before the update
     */
    Mono<Set<StickerData>> onGuildStickersUpdate(int shardIndex, GuildStickersUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildEmojisUpdate} gateway dispatch.
     * This will typically perform an update operation on a related collection of {@link EmojiData} that is already
     * present in the store, and update the list returned by {@link GuildData#emojis()}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * set of {@link EmojiData} before the update
     */
    Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildMemberAdd} gateway dispatch. This
     * will typically perform an insert operation on the related {@link MemberData}, add the ID of the member
     * into the list returned by {@link GuildData#members()}, and increment the count returned by
     * {@link GuildData#memberCount()}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildMemberRemove} gateway dispatch.
     * This will typically perform a delete operation on the related {@link MemberData}, remove the ID of the
     * member from the list returned by {@link GuildData#members()}, and decrement the count returned by
     * {@link GuildData#memberCount()}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link MemberData} before the deletion
     */
    Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildMembersChunk} gateway dispatch.
     * This will typically perform the same kind of operations than {@link #onGuildMemberAdd(int, GuildMemberAdd)},
     * but adapted for whole chunks of members.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildMemberUpdate} gateway dispatch.
     * This will typically perform an update operation on a related {@link MemberData} that is already present in
     * the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link MemberData} before the update
     */
    Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildRoleCreate} gateway dispatch. This
     * will typically perform an insert operation on the related {@link RoleData}, and add the role ID to the list
     * returned by {@link GuildData#roles()}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildRoleDelete} gateway dispatch. This
     * will typically perform a delete operation on a related {@link RoleData} that is already present in the store,
     * and remove the role ID from the list returned by {@link GuildData#roles()} and {@link MemberData#roles()}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link RoleData} before the deletion
     */
    Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildRoleUpdate} gateway dispatch. This
     * will typically perform an update operation on a related {@link RoleData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link RoleData} before the update
     */
    Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch);

    /**
     * Updates the internal state of the store according to the {@link GuildScheduledEventCreate} gateway dispatch. This
     * will typically perform an insert operation on a new {@link GuildScheduledEventData} in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildScheduledEventCreate(int shardIndex, GuildScheduledEventCreate dispatch);

    /**
     * Updates the internal state of the store according to the {@link GuildScheduledEventUpdate} gateway dispatch. This
     * will typically perform an update operation on a related {@link GuildScheduledEventData} already present in the
     * store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning {@link GuildScheduledEventData}
     * in a state before the update
     */
    Mono<GuildScheduledEventData> onGuildScheduledEventUpdate(int shardIndex, GuildScheduledEventUpdate dispatch);

    /**
     * Updates the internal state of the store according to the {@link GuildScheduledEventDelete} gateway dispatch. This
     * will typically perform a delete operation on a related {@link GuildScheduledEventData} in the store,
     * if present.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the
     * {@link GuildScheduledEventData} in a state before the deletion.
     */
    Mono<GuildScheduledEventData> onGuildScheduledEventDelete(int shardIndex, GuildScheduledEventDelete dispatch);

    /**
     * Updates the internal state of the store according to the {@link GuildScheduledEventUserAdd} gateway dispatch.
     * This will typically perform an insert operation on a related {@link java.util.List} handling a relationship
     * between a {@link GuildScheduledEventData} and the provided {@link GuildScheduledEventUserData}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildScheduledEventUserAdd(int shardIndex, GuildScheduledEventUserAdd dispatch);

    /**
     * Updates the internal state of the store according to the {@link GuildScheduledEventUserRemove} gateway dispatch.
     * This will typically perform a delete operation on a related {@link java.util.List} handling a relationship
     * between a {@link GuildScheduledEventData} and the provided {@link GuildScheduledEventUserData}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildScheduledEventUserRemove(int shardIndex, GuildScheduledEventUserRemove dispatch);

    /**
     * Updates the internal state of the store according to the given {@link GuildUpdate} gateway dispatch. This will
     * typically perform an update operation on a related {@link GuildData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link GuildData} before the update
     */
    Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch);

    /**
     * Handles the invalidation of a specific shard. When a shard is invalidated, all cached data related to it should
     * be considered stale and the implementation may perform some cleanup work.
     *
     * @param shardIndex the index of the shard to invalidate
     * @param cause      the cause of the invalidation
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause);

    /**
     * Updates the internal state of the store according to the given {@link MessageCreate} gateway dispatch. This
     * will typically perform an insert operation on the related {@link MessageData}, and update the
     * {@code last_message_id} field of the channel where the message was sent in.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageDelete} gateway dispatch. This
     * will typically perform a delete operation on a related {@link MessageData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link MessageData} before the deletion
     */
    Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageDeleteBulk} gateway dispatch. This
     * will typically perform a delete operation on a related collection of {@link MessageData} that is already present
     * in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * set of {@link MessageData} before the deletion
     */
    Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageReactionAdd} gateway dispatch.
     * This will typically perform an update operation on a related {@link MessageData} that is already present in
     * the store in order to add the reaction.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageReactionRemove} gateway dispatch.
     * This will typically perform an update operation on a related {@link MessageData} that is already present in
     * the store in order to remove the reaction.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageReactionRemoveAll} gateway
     * dispatch. This will typically perform an update operation on a related {@link MessageData} that is already
     * present in the store in order to remove all reactions.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageReactionRemoveEmoji} gateway
     * dispatch. This will typically perform an update operation on a related {@link MessageData} that is already
     * present in the store in order to remove all reactions for a specific emoji.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch);

    /**
     * Updates the internal state of the store according to the given {@link MessageUpdate} gateway dispatch. This
     * will typically perform an update operation on a related {@link MessageData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link MessageData} before the update
     */
    Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link PresenceUpdate} gateway dispatch. This
     * will typically perform an insert or an update operation on the related {@link PresenceData}, and update the
     * related {@link UserData} to reflect the new presence.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of both the
     * {@link PresenceData} and the {@link UserData} before the update
     */
    Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link Ready} gateway dispatch. This will
     * typically perform an insert operation on the {@link UserData} that represents the self-user, and allocate the
     * resources needed to receive further events happening on this shard index.
     *
     * @param dispatch the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onReady(Ready dispatch);

    /**
     * Updates the internal state of the store according to the given {@link StageInstanceCreate} gateway dispatch.
     * This will typically perform an insert operation on the related {@link StageInstanceData}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onStageInstanceCreate(int shardIndex, StageInstanceCreate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link StageInstanceCreate} gateway dispatch.
     * This will typically perform an insert operation on the related {@link StageInstanceData} that is already present
     * in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, returning the old state of the
     * {@link StageInstanceData} before the update
     */
    Mono<StageInstanceData> onStageInstanceUpdate(int shardIndex, StageInstanceUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link StageInstanceCreate} gateway dispatch.
     * This will typically perform an delete operation on the related {@link StageInstanceData} that is already present
     * in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, returning the old state of the
     * {@link StageInstanceData} before the delete
     */
    Mono<StageInstanceData> onStageInstanceDelete(int shardIndex, StageInstanceDelete dispatch);

    /**
     * Updates the internal state of the store according to the given {@link UserUpdate} gateway dispatch. This will
     * typically perform an update operation on a related {@link UserData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link UserData} before the update
     */
    Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link VoiceStateUpdateDispatch} gateway
     * dispatch. This will typically perform an insert, update or delete operation on the related
     * {@link VoiceStateData}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link VoiceStateData} before the update
     */
    Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch);

    /**
     * Invoked to indicate that all members for the specified guild ID were received. It serves as a hint to indicate
     * to the store that information on the full member list for this specific guild may now be accurately returned, so
     * that further calls to {@link DataAccessor#countExactMembersInGuild(long)} and
     * {@link DataAccessor#getExactMembersInGuild(long)} with the same guild ID no longer fail.
     *
     * @param guildId the guild ID
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onGuildMembersCompletion(long guildId);

    /**
     * Updates the internal state of the store according to the given {@link ThreadCreate} gateway dispatch. This
     * will typically perform an insert operation on the related {@link ChannelData}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onThreadCreate(int shardIndex, ThreadCreate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ThreadUpdate} gateway dispatch. This
     * will typically perform an update operation on a related {@link ChannelData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link ChannelData} before the update
     */
    Mono<ChannelData> onThreadUpdate(int shardIndex, ThreadUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ThreadDelete} gateway dispatch. This
     * will typically perform a delete operation on a related {@link ChannelData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onThreadDelete(int shardIndex, ThreadDelete dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ThreadListSync} gateway dispatch. This
     * will typically perform an update and delete operation on the related {@link ChannelData} and {@link ThreadMemberData}.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done
     */
    Mono<Void> onThreadListSync(int shardIndex, ThreadListSync dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ThreadMemberUpdate} gateway dispatch. This
     * will typically perform a delete operation on a related {@link ThreadMemberData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * {@link ThreadMemberUpdate} before the deletion
     */
    Mono<ThreadMemberData> onThreadMemberUpdate(int shardIndex, ThreadMemberUpdate dispatch);

    /**
     * Updates the internal state of the store according to the given {@link ThreadMembersUpdate} gateway dispatch. This
     * will typically perform a delete operation on a related {@link ThreadMemberData} that is already present in the store.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a {@link Mono} completing when the operation is done, optionally returning the old state of the
     * list of {@link ThreadMemberData} before the deletion
     */
    Mono<List<ThreadMemberData>> onThreadMembersUpdate(int shardIndex, ThreadMembersUpdate dispatch);
}
