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

package discord4j.common.store.action.gateway;

import discord4j.common.store.api.StoreAction;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.discordjson.json.gateway.*;

/**
 * Provides static factories to obtain {@link StoreAction} instances that enable updating data in a store in response
 * to an event received from the Discord gateway.
 */
public class GatewayActions {

    private GatewayActions() {
        throw new AssertionError("No discord4j.common.store.action.gateway.GatewayActions instances for you!");
    }

    /**
     * Creates an action to execute when a {@link ChannelCreate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ChannelCreateAction}
     */
    public static ChannelCreateAction channelCreate(int shardIndex, ChannelCreate dispatch) {
        return new ChannelCreateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ChannelDelete} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ChannelDeleteAction}
     */
    public static ChannelDeleteAction channelDelete(int shardIndex, ChannelDelete dispatch) {
        return new ChannelDeleteAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ChannelUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ChannelUpdateAction}
     */
    public static ChannelUpdateAction channelUpdate(int shardIndex, ChannelUpdate dispatch) {
        return new ChannelUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildCreate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildCreateAction}
     */
    public static GuildCreateAction guildCreate(int shardIndex, GuildCreate dispatch) {
        return new GuildCreateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildDelete} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildDeleteAction}
     */
    public static GuildDeleteAction guildDelete(int shardIndex, GuildDelete dispatch) {
        return new GuildDeleteAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildStickersUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildStickersUpdateAction}
     */
    public static GuildStickersUpdateAction guildStickersUpdate(int shardIndex, GuildStickersUpdate dispatch) {
        return new GuildStickersUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildEmojisUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildEmojisUpdateAction}
     */
    public static GuildEmojisUpdateAction guildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        return new GuildEmojisUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildMemberAdd} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildMemberAddAction}
     */
    public static GuildMemberAddAction guildMemberAdd(int shardIndex, GuildMemberAdd dispatch) {
        return new GuildMemberAddAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildMemberRemove} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildMemberRemoveAction}
     */
    public static GuildMemberRemoveAction guildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
        return new GuildMemberRemoveAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildMembersChunk} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildMembersChunkAction}
     */
    public static GuildMembersChunkAction guildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
        return new GuildMembersChunkAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildMemberUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildMemberUpdateAction}
     */
    public static GuildMemberUpdateAction guildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
        return new GuildMemberUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildRoleCreate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildRoleCreateAction}
     */
    public static GuildRoleCreateAction guildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        return new GuildRoleCreateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildRoleDelete} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildRoleDeleteAction}
     */
    public static GuildRoleDeleteAction guildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        return new GuildRoleDeleteAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildRoleUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildRoleUpdateAction}
     */
    public static GuildRoleUpdateAction guildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        return new GuildRoleUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildScheduledEventCreate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildScheduledEventCreate}
     */
    public static GuildScheduledEventCreateAction guildScheduledEventCreate(int shardIndex, GuildScheduledEventCreate dispatch) {
        return new GuildScheduledEventCreateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildScheduledEventUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildScheduledEventUpdate}
     */
    public static GuildScheduledEventUpdateAction guildScheduledEventUpdate(int shardIndex, GuildScheduledEventUpdate dispatch) {
        return new GuildScheduledEventUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildScheduledEventDelete} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildScheduledEventDelete}
     */
    public static GuildScheduledEventDeleteAction guildScheduledEventDelete(int shardIndex, GuildScheduledEventDelete dispatch) {
        return new GuildScheduledEventDeleteAction(shardIndex, dispatch);
    }


    /**
     * Creates an action to execute when a {@link GuildScheduledEventUserAdd} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildScheduledEventUserAddAction}
     */
    public static GuildScheduledEventUserAddAction guildScheduledEventUserAdd(int shardIndex,
                                                                              GuildScheduledEventUserAdd dispatch) {
        return new GuildScheduledEventUserAddAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildScheduledEventUserRemove} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildScheduledEventUserRemoveAction}
     */
    public static GuildScheduledEventUserRemoveAction guildScheduledEventUserRemove(int shardIndex,
                                                                                    GuildScheduledEventUserRemove dispatch) {
        return new GuildScheduledEventUserRemoveAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link GuildUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link GuildUpdateAction}
     */
    public static GuildUpdateAction guildUpdate(int shardIndex, GuildUpdate dispatch) {
        return new GuildUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a shard should be invalidated.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param cause      the cause of the invalidation
     * @return a new {@link InvalidateShardAction}
     */
    public static InvalidateShardAction invalidateShard(int shardIndex, InvalidationCause cause) {
        return new InvalidateShardAction(shardIndex, cause);
    }

    /**
     * Creates an action to execute when a {@link MessageCreate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageCreateAction}
     */
    public static MessageCreateAction messageCreate(int shardIndex, MessageCreate dispatch) {
        return new MessageCreateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageDelete} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageDeleteAction}
     */
    public static MessageDeleteAction messageDelete(int shardIndex, MessageDelete dispatch) {
        return new MessageDeleteAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageDeleteBulk} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageDeleteBulkAction}
     */
    public static MessageDeleteBulkAction messageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
        return new MessageDeleteBulkAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageReactionAdd} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageReactionAddAction}
     */
    public static MessageReactionAddAction messageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
        return new MessageReactionAddAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageReactionRemove} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageReactionRemoveAction}
     */
    public static MessageReactionRemoveAction messageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        return new MessageReactionRemoveAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageReactionRemoveAll} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageReactionRemoveAllAction}
     */
    public static MessageReactionRemoveAllAction messageReactionRemoveAll(int shardIndex,
                                                                          MessageReactionRemoveAll dispatch) {
        return new MessageReactionRemoveAllAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageReactionRemoveEmoji} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageReactionRemoveEmojiAction}
     */
    public static MessageReactionRemoveEmojiAction messageReactionRemoveEmoji(int shardIndex,
                                                                              MessageReactionRemoveEmoji dispatch) {
        return new MessageReactionRemoveEmojiAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link MessageUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link MessageUpdateAction}
     */
    public static MessageUpdateAction messageUpdate(int shardIndex, MessageUpdate dispatch) {
        return new MessageUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link PresenceUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link PresenceUpdateAction}
     */
    public static PresenceUpdateAction presenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        return new PresenceUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link Ready} is received from the gateway.
     *
     * @param dispatch the dispatch data coming from Discord gateway
     * @return a new {@link ReadyAction}
     */
    public static ReadyAction ready(Ready dispatch) {
        return new ReadyAction(dispatch);
    }

    /**
     * Creates an action to execute when a {@link UserUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link UserUpdateAction}
     */
    public static UserUpdateAction userUpdate(int shardIndex, UserUpdate dispatch) {
        return new UserUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link VoiceStateUpdateDispatch} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link VoiceStateUpdateDispatchAction}
     */
    public static VoiceStateUpdateDispatchAction voiceStateUpdateDispatch(int shardIndex,
                                                                          VoiceStateUpdateDispatch dispatch) {
        return new VoiceStateUpdateDispatchAction(shardIndex, dispatch);
    }

    /**
     * Creates an action that allows to signal that the full member list for the specified guild has been received.
     *
     * @param guildId the guild ID
     * @return a new {@link CompleteGuildMembersAction}
     */
    public static CompleteGuildMembersAction completeGuildMembers(long guildId) {
        return new CompleteGuildMembersAction(guildId);
    }

    /**
     * Creates an action to execute when a {@link ThreadCreate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ThreadCreateAction}
     */
    public static ThreadCreateAction threadCreate(int shardIndex, ThreadCreate dispatch) {
        return new ThreadCreateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ThreadUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ThreadUpdateAction}
     */
    public static ThreadUpdateAction threadUpdate(int shardIndex, ThreadUpdate dispatch) {
        return new ThreadUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ThreadDelete} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ThreadDeleteAction}
     */
    public static ThreadDeleteAction threadDelete(int shardIndex, ThreadDelete dispatch) {
        return new ThreadDeleteAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ThreadListSync} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ThreadListSyncAction}
     */
    public static ThreadListSyncAction threadListSync(int shardIndex, ThreadListSync dispatch) {
        return new ThreadListSyncAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ThreadMemberUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ThreadMemberUpdateAction}
     */
    public static ThreadMemberUpdateAction threadMemberUpdate(int shardIndex, ThreadMemberUpdate dispatch) {
        return new ThreadMemberUpdateAction(shardIndex, dispatch);
    }

    /**
     * Creates an action to execute when a {@link ThreadMembersUpdate} is received from the gateway.
     *
     * @param shardIndex the index of the shard where the dispatch comes from
     * @param dispatch   the dispatch data coming from Discord gateway
     * @return a new {@link ThreadMembersUpdateAction}
     */
    public static ThreadMembersUpdateAction threadMembersUpdate(int shardIndex, ThreadMembersUpdate dispatch) {
        return new ThreadMembersUpdateAction(shardIndex, dispatch);
    }
}
