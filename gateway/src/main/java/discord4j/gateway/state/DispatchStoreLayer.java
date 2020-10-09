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
package discord4j.gateway.state;

import discord4j.common.store.Store;
import discord4j.common.store.layout.StoreAction;
import discord4j.common.store.layout.InvalidationCause;
import discord4j.common.store.action.gateway.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class DispatchStoreLayer {

    private static final Map<Class<? extends Dispatch>, BiFunction<Integer, Dispatch, StoreAction<?>>> STORE_ACTION_MAP;

    static {
        STORE_ACTION_MAP = new HashMap<>();
        putAction(ChannelCreate.class, ChannelCreateAction::new);
        putAction(ChannelDelete.class, ChannelDeleteAction::new);
        putAction(ChannelUpdate.class, ChannelUpdateAction::new);
        putAction(GuildCreate.class, GuildCreateAction::new);
        putAction(GuildDelete.class, GuildDeleteAction::new);
        putAction(GuildEmojisUpdate.class, GuildEmojisUpdateAction::new);
        putAction(GuildMemberAdd.class, GuildMemberAddAction::new);
        putAction(GuildMemberRemove.class, GuildMemberRemoveAction::new);
        putAction(GuildMembersChunk.class, GuildMembersChunkAction::new);
        putAction(GuildMemberUpdate.class, GuildMemberUpdateAction::new);
        putAction(GuildRoleCreate.class, GuildRoleCreateAction::new);
        putAction(GuildRoleDelete.class, GuildRoleDeleteAction::new);
        putAction(GuildRoleUpdate.class, GuildRoleUpdateAction::new);
        putAction(GuildUpdate.class, GuildUpdateAction::new);
        putAction(MessageCreate.class, MessageCreateAction::new);
        putAction(MessageDelete.class, MessageDeleteAction::new);
        putAction(MessageDeleteBulk.class, MessageDeleteBulkAction::new);
        putAction(MessageReactionAdd.class, MessageReactionAddAction::new);
        putAction(MessageReactionRemove.class, MessageReactionRemoveAction::new);
        putAction(MessageReactionRemoveAll.class, MessageReactionRemoveAllAction::new);
        putAction(MessageReactionRemoveEmoji.class, MessageReactionRemoveEmojiAction::new);
        putAction(MessageUpdate.class, MessageUpdateAction::new);
        putAction(PresenceUpdate.class, PresenceUpdateAction::new);
        putAction(Ready.class, ReadyAction::new);
        putAction(UserUpdate.class, UserUpdateAction::new);
        putAction(VoiceStateUpdateDispatch.class, VoiceStateUpdateDispatchAction::new);
    }

    private final Store store;
    private final ShardInfo shardInfo;

    private DispatchStoreLayer(Store store, ShardInfo shardInfo) {
        this.store = store;
        this.shardInfo = shardInfo;
    }

    public static DispatchStoreLayer create(Store store, ShardInfo shardInfo) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(shardInfo);
        return new DispatchStoreLayer(store, shardInfo);
    }

    public Mono<StatefulDispatch<?, ?>> store(Dispatch dispatch) {
        Objects.requireNonNull(dispatch);
        return Mono.justOrEmpty(STORE_ACTION_MAP.get(dispatch.getClass()))
                .map(actionFactory -> {
                    // Special treatment for shard invalidation
                    if (dispatch instanceof GatewayStateChange) {
                        GatewayStateChange gatewayStateChange = (GatewayStateChange) dispatch;
                        switch (gatewayStateChange.getState()) {
                            case DISCONNECTED:
                                return new InvalidateShardAction(shardInfo.getIndex(), InvalidationCause.LOGOUT);
                            case SESSION_INVALIDATED:
                                return new InvalidateShardAction(shardInfo.getIndex(), InvalidationCause.HARD_RECONNECT);
                            default: break;
                        }
                    }
                    return actionFactory.apply(shardInfo.getIndex(), dispatch);
                })
                .flatMap(store::execute)
                .map(oldState -> StatefulDispatch.of(shardInfo, dispatch, oldState));
    }

    @SuppressWarnings("unchecked")
    private static <D extends Dispatch> void putAction(Class<D> clazz,
                                                       BiFunction<Integer, D, StoreAction<?>> actionFactory) {
        STORE_ACTION_MAP.put(clazz, (shard, dispatch) -> actionFactory.apply(shard, (D) dispatch));
    }
}
