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
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A {@link DispatchStoreLayer} allows to intercept any {@link Dispatch} instance and execute the appropriate
 * {@link StoreAction} on a given {@link Store}. An instance of {@link DispatchStoreLayer} is specific to one shard,
 * in case of sharded connection to the gateway, a separate {@link DispatchStoreLayer} should be created for each of
 * them. It is completely independent to the gateway client itself, its use is completely optional.
 */
public class DispatchStoreLayer {

    private static final Logger log = Loggers.getLogger(DispatchStoreLayer.class);
    private static final Set<DispatchToAction> DISPATCH_TO_ACTION = new HashSet<>();

    static {
        add(ChannelCreate.class::isInstance, GatewayActions::channelCreate);
        add(ChannelDelete.class::isInstance, GatewayActions::channelDelete);
        add(ChannelUpdate.class::isInstance, GatewayActions::channelUpdate);
        add(GuildCreate.class::isInstance, GatewayActions::guildCreate);
        add(GuildDelete.class::isInstance, GatewayActions::guildDelete);
        add(GuildStickersUpdate.class::isInstance, GatewayActions::guildStickersUpdate);
        add(GuildEmojisUpdate.class::isInstance, GatewayActions::guildEmojisUpdate);
        add(GuildMemberAdd.class::isInstance, GatewayActions::guildMemberAdd);
        add(GuildMemberRemove.class::isInstance, GatewayActions::guildMemberRemove);
        add(GuildMembersChunk.class::isInstance, GatewayActions::guildMembersChunk);
        add(GuildMemberUpdate.class::isInstance, GatewayActions::guildMemberUpdate);
        add(GuildRoleCreate.class::isInstance, GatewayActions::guildRoleCreate);
        add(GuildRoleDelete.class::isInstance, GatewayActions::guildRoleDelete);
        add(GuildRoleUpdate.class::isInstance, GatewayActions::guildRoleUpdate);
        add(GuildScheduledEventCreate.class::isInstance, GatewayActions::guildScheduledEventCreate);
        add(GuildScheduledEventUpdate.class::isInstance, GatewayActions::guildScheduledEventUpdate);
        add(GuildScheduledEventDelete.class::isInstance, GatewayActions::guildScheduledEventDelete);
        add(GuildScheduledEventUserAdd.class::isInstance, GatewayActions::guildScheduledEventUserAdd);
        add(GuildScheduledEventUserRemove.class::isInstance, GatewayActions::guildScheduledEventUserRemove);
        add(GuildUpdate.class::isInstance, GatewayActions::guildUpdate);
        add(ThreadCreate.class::isInstance, GatewayActions::threadCreate);
        add(ThreadUpdate.class::isInstance, GatewayActions::threadUpdate);
        add(ThreadDelete.class::isInstance, GatewayActions::threadDelete);
        add(ThreadListSync.class::isInstance, GatewayActions::threadListSync);
        add(ThreadMemberUpdate.class::isInstance, GatewayActions::threadMemberUpdate);
        add(ThreadMembersUpdate.class::isInstance, GatewayActions::threadMembersUpdate);
        add(MessageCreate.class::isInstance, GatewayActions::messageCreate);
        add(MessageDelete.class::isInstance, GatewayActions::messageDelete);
        add(MessageDeleteBulk.class::isInstance, GatewayActions::messageDeleteBulk);
        add(MessageReactionAdd.class::isInstance, GatewayActions::messageReactionAdd);
        add(MessageReactionRemove.class::isInstance, GatewayActions::messageReactionRemove);
        add(MessageReactionRemoveAll.class::isInstance, GatewayActions::messageReactionRemoveAll);
        add(MessageReactionRemoveEmoji.class::isInstance, GatewayActions::messageReactionRemoveEmoji);
        add(MessageUpdate.class::isInstance, GatewayActions::messageUpdate);
        add(PresenceUpdate.class::isInstance, GatewayActions::presenceUpdate);
        add(Ready.class::isInstance, (Integer shard, Ready dispatch) -> GatewayActions.ready(dispatch));
        add(UserUpdate.class::isInstance, GatewayActions::userUpdate);
        add(VoiceStateUpdateDispatch.class::isInstance, GatewayActions::voiceStateUpdateDispatch);
        add(dispatch -> dispatch instanceof GatewayStateChange
                        && ((GatewayStateChange) dispatch).getState() == GatewayStateChange.State.DISCONNECTED,
                (shard, dispatch) -> GatewayActions.invalidateShard(shard, InvalidationCause.LOGOUT));
        add(dispatch -> dispatch instanceof GatewayStateChange
                        && ((GatewayStateChange) dispatch).getState() == GatewayStateChange.State.SESSION_INVALIDATED,
                (shard, dispatch) -> GatewayActions.invalidateShard(shard, InvalidationCause.HARD_RECONNECT));
    }

    private final Store store;
    private final ShardInfo shardInfo;

    private DispatchStoreLayer(Store store, ShardInfo shardInfo) {
        this.store = store;
        this.shardInfo = shardInfo;
    }

    /**
     * Creates a new {@link DispatchStoreLayer} operating on the given store and shard.
     *
     * @param store     the store to execute actions on
     * @param shardInfo the shard info where dispatches are received from
     * @return a new {@link DispatchStoreLayer}
     */
    public static DispatchStoreLayer create(Store store, ShardInfo shardInfo) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(shardInfo);
        return new DispatchStoreLayer(store, shardInfo);
    }

    @SuppressWarnings("unchecked")
    private static <D extends Dispatch> void add(Predicate<? super Dispatch> predicate,
                                                 BiFunction<Integer, D, StoreAction<?>> actionFactory) {
        DISPATCH_TO_ACTION.add(new DispatchToAction(predicate,
                (shard, dispatch) -> actionFactory.apply(shard, (D) dispatch)));
    }

    /**
     * Executes a store action depending on the type of the given dispatch. The dispatch given in argument is assumed
     * to come from the same shard as given when creating this {@link DispatchStoreLayer}. The shard info will be
     * overriden if an instance of {@link ShardAwareDispatch} is provided. The result of the store action, which
     * represents the old state of the data affected when applicable, is returned along with the dispatch itself in a
     * {@link StatefulDispatch} which can be processed downstream.
     *
     * @param dispatch the dispatch to produce the store action for
     * @return a {@link Mono} where, upon successful completion, emits the {@link StatefulDispatch} holding the
     * result of the store action execution, if any. If an error occurs during store execution, the error is dropped
     * and logged, and a {@link StatefulDispatch} with empty old state is returned.
     */
    public Mono<StatefulDispatch<?, ?>> store(Dispatch dispatch) {
        Objects.requireNonNull(dispatch);
        ShardInfo shardInfo;
        Dispatch actualDispatch;
        if (dispatch instanceof ShardAwareDispatch) {
            ShardAwareDispatch shardAwareDispatch = (ShardAwareDispatch) dispatch;
            shardInfo = ShardInfo.create(shardAwareDispatch.getShardIndex(), shardAwareDispatch.getShardCount());
            actualDispatch = shardAwareDispatch.getDispatch();
        } else {
            shardInfo = this.shardInfo;
            actualDispatch = dispatch;
        }
        return Flux.fromStream(DISPATCH_TO_ACTION.stream()
                .filter(entry -> entry.predicate.test(actualDispatch))
                .map(entry -> entry.actionFactory))
                .singleOrEmpty()
                .map(actionFactory -> actionFactory.apply(shardInfo.getIndex(), actualDispatch))
                .flatMap(action -> Mono.from(store.execute(action)))
                .<StatefulDispatch<?, ?>>map(oldState -> StatefulDispatch.of(shardInfo, actualDispatch, oldState))
                .onErrorResume(t -> Mono.fromRunnable(
                        () -> log.error("Error when executing store action on dispatch " + dispatch, t)))
                .defaultIfEmpty(StatefulDispatch.of(shardInfo, actualDispatch, null));
    }

    private static class DispatchToAction {

        private final Predicate<? super Dispatch> predicate;
        private final BiFunction<Integer, Dispatch, StoreAction<?>> actionFactory;

        private DispatchToAction(Predicate<? super Dispatch> predicate,
                                 BiFunction<Integer, Dispatch, StoreAction<?>> actionFactory) {
            this.predicate = predicate;
            this.actionFactory = actionFactory;
        }
    }
}
