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
import discord4j.common.store.action.gateway.GatewayActions;
import discord4j.common.store.api.StoreAction;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.json.ShardAwareDispatch;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class DispatchStoreLayer {

    private static final Map<Class<? extends Dispatch>, BiFunction<Integer, Dispatch, StoreAction<?>>> STORE_ACTION_MAP;

    static {
        STORE_ACTION_MAP = new HashMap<>();
        putAction(ChannelCreate.class, GatewayActions::channelCreate);
        putAction(ChannelDelete.class, GatewayActions::channelDelete);
        putAction(ChannelUpdate.class, GatewayActions::channelUpdate);
        putAction(GuildCreate.class, GatewayActions::guildCreate);
        putAction(GuildDelete.class, GatewayActions::guildDelete);
        putAction(GuildEmojisUpdate.class, GatewayActions::guildEmojisUpdate);
        putAction(GuildMemberAdd.class, GatewayActions::guildMemberAdd);
        putAction(GuildMemberRemove.class, GatewayActions::guildMemberRemove);
        putAction(GuildMembersChunk.class, GatewayActions::guildMembersChunk);
        putAction(GuildMemberUpdate.class, GatewayActions::guildMemberUpdate);
        putAction(GuildRoleCreate.class, GatewayActions::guildRoleCreate);
        putAction(GuildRoleDelete.class, GatewayActions::guildRoleDelete);
        putAction(GuildRoleUpdate.class, GatewayActions::guildRoleUpdate);
        putAction(GuildUpdate.class, GatewayActions::guildUpdate);
        putAction(MessageCreate.class, GatewayActions::messageCreate);
        putAction(MessageDelete.class, GatewayActions::messageDelete);
        putAction(MessageDeleteBulk.class, GatewayActions::messageDeleteBulk);
        putAction(MessageReactionAdd.class, GatewayActions::messageReactionAdd);
        putAction(MessageReactionRemove.class, GatewayActions::messageReactionRemove);
        putAction(MessageReactionRemoveAll.class, GatewayActions::messageReactionRemoveAll);
        putAction(MessageReactionRemoveEmoji.class, GatewayActions::messageReactionRemoveEmoji);
        putAction(MessageUpdate.class, GatewayActions::messageUpdate);
        putAction(PresenceUpdate.class, GatewayActions::presenceUpdate);
        putAction(Ready.class, (shard, dispatch) -> GatewayActions.ready(dispatch));
        putAction(UserUpdate.class, GatewayActions::userUpdate);
        putAction(VoiceStateUpdateDispatch.class, GatewayActions::voiceStateUpdateDispatch);
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

    @SuppressWarnings("unchecked")
    private static <D extends Dispatch> void putAction(Class<D> clazz,
                                                       BiFunction<Integer, D, StoreAction<?>> actionFactory) {
        STORE_ACTION_MAP.put(clazz, (shard, dispatch) -> actionFactory.apply(shard, (D) dispatch));
    }

    public Mono<StatefulDispatch<?, ?>> store(Dispatch dispatch) {
        Objects.requireNonNull(dispatch);
        ShardInfo shardInfo;
        if (dispatch instanceof ShardAwareDispatch) {
            ShardAwareDispatch shardAwareDispatch = (ShardAwareDispatch) dispatch;
            shardInfo = ShardInfo.create(shardAwareDispatch.getShardIndex(), shardAwareDispatch.getShardCount());
        } else {
            shardInfo = this.shardInfo;
        }
        return Flux.fromStream(STORE_ACTION_MAP.entrySet().stream()
                        .filter(entry -> entry.getKey().isInstance(dispatch))
                        .map(Map.Entry::getValue))
                .singleOrEmpty()
                .onErrorMap(IndexOutOfBoundsException.class, AssertionError::new)
                .map(actionFactory -> {
                    // Special treatment for shard invalidation
                    if (dispatch instanceof GatewayStateChange) {
                        GatewayStateChange gatewayStateChange = (GatewayStateChange) dispatch;
                        switch (gatewayStateChange.getState()) {
                            case DISCONNECTED:
                                return GatewayActions.invalidateShard(shardInfo.getIndex(),
                                        InvalidationCause.LOGOUT);
                            case SESSION_INVALIDATED:
                                return GatewayActions.invalidateShard(shardInfo.getIndex(),
                                        InvalidationCause.HARD_RECONNECT);
                            default:
                                break;
                        }
                    }
                    return actionFactory.apply(shardInfo.getIndex(), dispatch);
                })
                .flatMap(action -> Mono.from(store.execute(action)))
                .<StatefulDispatch<?, ?>>map(oldState -> StatefulDispatch.of(shardInfo, dispatch, oldState))
                .defaultIfEmpty(StatefulDispatch.of(shardInfo, dispatch, null));
    }
}
