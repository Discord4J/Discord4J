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

package discord4j.common.store;

import discord4j.common.store.action.gateway.*;
import discord4j.common.store.action.read.*;
import discord4j.common.store.api.*;
import discord4j.common.store.api.layout.DataAccessor;
import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.layout.StoreLayout;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * A {@link Store} represents a container that holds, retrieves, and updates data received from Discord throughout the
 * life of a bot application. Any operation performed on a {@link Store} (whether it's to read data, update data upon
 * receiving a gateway event, etc) is represented by a {@link StoreAction} object, that can be passed to the
 * {@link Store#execute(StoreAction)} method. Actions are in charge of encoding all information needed for the store
 * to operate on the data accordingly.
 *
 * <p>
 * A {@link Store} is constructed by passing a {@link StoreLayout}, which defines handlers for the different types of
 * actions, whether they are Discord4J-specific or user-defined. The layout interface allows to enforce support for a
 * minimal set of actions in order to fulfill the caching expectations of the Discord client.
 *
 * @see Store#fromLayout(StoreLayout)
 */
public final class Store {

    private static final Store NO_OP = new Store(ActionMapper.empty());

    private final ActionMapper actionMapper;

    private Store(ActionMapper actionMapper) {
        this.actionMapper = actionMapper;
    }

    /**
     * Returns a {@link Store} that will ignore all actions.
     *
     * @return a no-op {@link Store}
     */
    public static Store noOp() {
        return NO_OP;
    }

    /**
     * Creates a new {@link Store} that will handle actions according to the given layout.
     *
     * @param layout a {@link StoreLayout}
     * @return a new {@link Store}
     */
    public static Store fromLayout(StoreLayout layout) {
        return new Store(layoutToMapper(layout));
    }

    private static ActionMapper layoutToMapper(StoreLayout layout) {
        ActionMapper dataAccessorMapper = dataAccessorToMapper(layout.getDataAccessor());
        ActionMapper gatewayDataUpdaterMapper = gatewayDataUpdaterToMapper(layout.getGatewayDataUpdater());
        ActionMapper customMapper = layout.getCustomActionMapper();
        return ActionMapper.aggregate(dataAccessorMapper, gatewayDataUpdaterMapper, customMapper);
    }

    private static ActionMapper dataAccessorToMapper(DataAccessor dataAccessor) {
        Objects.requireNonNull(dataAccessor);
        return ActionMapper.builder()
                .map(CountMessagesInChannelAction.class, action -> dataAccessor
                        .countMessagesInChannel(action.getChannelId()))
                .map(CountVoiceStatesInChannelAction.class, action -> dataAccessor
                        .countVoiceStatesInChannel(action.getGuildId(), action.getChannelId()))
                .map(CountInGuildAction.class, action -> {
                    switch (action.getEntity()) {
                        case CHANNELS:
                            return dataAccessor.countChannelsInGuild(action.getGuildId());
                        case EMOJIS:
                            return dataAccessor.countEmojisInGuild(action.getGuildId());
                        case MEMBERS:
                            return dataAccessor.countMembersInGuild(action.getGuildId());
                        case MEMBERS_EXACT:
                            return dataAccessor.countExactMembersInGuild(action.getGuildId());
                        case PRESENCES:
                            return dataAccessor.countPresencesInGuild(action.getGuildId());
                        case ROLES:
                            return dataAccessor.countRolesInGuild(action.getGuildId());
                        case VOICE_STATES:
                            return dataAccessor.countVoiceStatesInGuild(action.getGuildId());
                        default:
                            throw new IllegalArgumentException("Unhandled entity " + action.getEntity());
                    }
                })
                .map(CountTotalAction.class, action -> {
                    switch (action.getEntity()) {
                        case CHANNELS:
                            return dataAccessor.countChannels();
                        case EMOJIS:
                            return dataAccessor.countEmojis();
                        case GUILDS:
                            return dataAccessor.countGuilds();
                        case MEMBERS:
                            return dataAccessor.countMembers();
                        case MESSAGES:
                            return dataAccessor.countMessages();
                        case PRESENCES:
                            return dataAccessor.countPresences();
                        case ROLES:
                            return dataAccessor.countRoles();
                        case USERS:
                            return dataAccessor.countUsers();
                        case VOICE_STATES:
                            return dataAccessor.countVoiceStates();
                        default:
                            throw new IllegalArgumentException("Unhandled entity " + action.getEntity());
                    }
                })
                .map(GetChannelsAction.class, action -> dataAccessor.getChannels())
                .map(GetChannelsInGuildAction.class, action -> dataAccessor.getChannelsInGuild(action.getGuildId()))
                .map(GetChannelByIdAction.class, action -> dataAccessor.getChannelById(action.getChannelId()))
                .map(GetEmojisAction.class, action -> dataAccessor.getEmojis())
                .map(GetEmojisInGuildAction.class, action -> dataAccessor.getEmojisInGuild(action.getGuildId()))
                .map(GetEmojiByIdAction.class, action -> dataAccessor
                        .getEmojiById(action.getGuildId(), action.getEmojiId()))
                .map(GetGuildsAction.class, action -> dataAccessor.getGuilds())
                .map(GetGuildByIdAction.class, action -> dataAccessor.getGuildById(action.getGuildId()))
                .map(GetMembersAction.class, action -> dataAccessor.getMembers())
                .map(GetMembersInGuildAction.class, action -> dataAccessor.getMembersInGuild(action.getGuildId()))
                .map(GetExactMembersInGuildAction.class, action -> dataAccessor
                        .getExactMembersInGuild(action.getGuildId()))
                .map(GetMemberByIdAction.class, action -> dataAccessor
                        .getMemberById(action.getGuildId(), action.getUserId()))
                .map(GetMessagesAction.class, action -> dataAccessor.getMessages())
                .map(GetMessagesInChannelAction.class, action -> dataAccessor
                        .getMessagesInChannel(action.getChannelId()))
                .map(GetMessageByIdAction.class, action -> dataAccessor
                        .getMessageById(action.getChannelId(), action.getMessageId()))
                .map(GetPresencesAction.class, action -> dataAccessor.getPresences())
                .map(GetPresencesInGuildAction.class, action -> dataAccessor.getPresencesInGuild(action.getGuildId()))
                .map(GetPresenceByIdAction.class, action -> dataAccessor
                        .getPresenceById(action.getGuildId(), action.getUserId()))
                .map(GetRolesAction.class, action -> dataAccessor.getRoles())
                .map(GetRolesInGuildAction.class, action -> dataAccessor.getRolesInGuild(action.getGuildId()))
                .map(GetRoleByIdAction.class, action -> dataAccessor
                        .getRoleById(action.getGuildId(), action.getRoleId()))
                .map(GetUsersAction.class, action -> dataAccessor.getUsers())
                .map(GetUserByIdAction.class, action -> dataAccessor.getUserById(action.getUserId()))
                .map(GetVoiceStatesAction.class, action -> dataAccessor.getVoiceStates())
                .map(GetVoiceStatesInChannelAction.class, action -> dataAccessor
                        .getVoiceStatesInChannel(action.getGuildId(), action.getChannelId()))
                .map(GetVoiceStatesInGuildAction.class, action -> dataAccessor
                        .getVoiceStatesInGuild(action.getGuildId()))
                .map(GetVoiceStateByIdAction.class, action -> dataAccessor
                        .getVoiceStateById(action.getGuildId(), action.getUserId()))
                .map(GetStageInstanceByChannelIdAction.class, action -> dataAccessor
                        .getStageInstanceByChannelId(action.getChannelId()))
                .build();
    }

    private static ActionMapper gatewayDataUpdaterToMapper(GatewayDataUpdater gatewayDataUpdater) {
        Objects.requireNonNull(gatewayDataUpdater);
        return ActionMapper.builder()
                .map(ChannelCreateAction.class, action -> gatewayDataUpdater
                        .onChannelCreate(action.getShardIndex(), action.getChannelCreate()))
                .map(ChannelDeleteAction.class, action -> gatewayDataUpdater
                        .onChannelDelete(action.getShardIndex(), action.getChannelDelete()))
                .map(ChannelUpdateAction.class, action -> gatewayDataUpdater
                        .onChannelUpdate(action.getShardIndex(), action.getChannelUpdate()))
                .map(GuildCreateAction.class, action -> gatewayDataUpdater
                        .onGuildCreate(action.getShardIndex(), action.getGuildCreate()))
                .map(GuildDeleteAction.class, action -> gatewayDataUpdater
                        .onGuildDelete(action.getShardIndex(), action.getGuildDelete()))
                .map(GuildEmojisUpdateAction.class, action -> gatewayDataUpdater
                        .onGuildEmojisUpdate(action.getShardIndex(), action.getGuildEmojisUpdate()))
                .map(GuildMemberAddAction.class, action -> gatewayDataUpdater
                        .onGuildMemberAdd(action.getShardIndex(), action.getGuildMemberAdd()))
                .map(GuildMemberRemoveAction.class, action -> gatewayDataUpdater
                        .onGuildMemberRemove(action.getShardIndex(), action.getGuildMemberRemove()))
                .map(GuildMembersChunkAction.class, action -> gatewayDataUpdater
                        .onGuildMembersChunk(action.getShardIndex(), action.getGuildMembersChunk()))
                .map(GuildMemberUpdateAction.class, action -> gatewayDataUpdater
                        .onGuildMemberUpdate(action.getShardIndex(), action.getGuildMemberUpdate()))
                .map(GuildRoleCreateAction.class, action -> gatewayDataUpdater
                        .onGuildRoleCreate(action.getShardIndex(), action.getGuildRoleCreate()))
                .map(GuildRoleDeleteAction.class, action -> gatewayDataUpdater
                        .onGuildRoleDelete(action.getShardIndex(), action.getGuildRoleDelete()))
                .map(GuildRoleUpdateAction.class, action -> gatewayDataUpdater
                        .onGuildRoleUpdate(action.getShardIndex(), action.getGuildRoleUpdate()))
                .map(GuildUpdateAction.class, action -> gatewayDataUpdater
                        .onGuildUpdate(action.getShardIndex(), action.getGuildUpdate()))
                .map(InvalidateShardAction.class, action -> gatewayDataUpdater
                        .onShardInvalidation(action.getShardIndex(), action.getCause()))
                .map(MessageCreateAction.class, action -> gatewayDataUpdater
                        .onMessageCreate(action.getShardIndex(), action.getMessageCreate()))
                .map(MessageDeleteAction.class, action -> gatewayDataUpdater
                        .onMessageDelete(action.getShardIndex(), action.getMessageDelete()))
                .map(MessageDeleteBulkAction.class, action -> gatewayDataUpdater
                        .onMessageDeleteBulk(action.getShardIndex(), action.getMessageDeleteBulk()))
                .map(MessageReactionAddAction.class, action -> gatewayDataUpdater
                        .onMessageReactionAdd(action.getShardIndex(), action.getMessageReactionAdd()))
                .map(MessageReactionRemoveAction.class, action -> gatewayDataUpdater
                        .onMessageReactionRemove(action.getShardIndex(), action.getMessageReactionRemove()))
                .map(MessageReactionRemoveAllAction.class, action -> gatewayDataUpdater
                        .onMessageReactionRemoveAll(action.getShardIndex(), action.getMessageReactionRemoveAll()))
                .map(MessageReactionRemoveEmojiAction.class, action -> gatewayDataUpdater
                        .onMessageReactionRemoveEmoji(action.getShardIndex(), action.getMessageReactionRemoveEmoji()))
                .map(MessageUpdateAction.class, action -> gatewayDataUpdater
                        .onMessageUpdate(action.getShardIndex(), action.getMessageUpdate()))
                .map(PresenceUpdateAction.class, action -> gatewayDataUpdater
                        .onPresenceUpdate(action.getShardIndex(), action.getPresenceUpdate()))
                .map(ReadyAction.class, action -> gatewayDataUpdater.onReady(action.getReady()))
                .map(StageInstanceCreateAction.class, action -> gatewayDataUpdater
                        .onStageInstanceCreate(action.getShardIndex(), action.getStageInstanceCreate()))
                .map(StageInstanceUpdateAction.class, action -> gatewayDataUpdater
                        .onStageInstanceUpdate(action.getShardIndex(), action.getStageInstanceUpdate()))
                .map(StageInstanceDeleteAction.class, action -> gatewayDataUpdater
                        .onStageInstanceDelete(action.getShardIndex(), action.getStageInstanceDelete()))
                .map(UserUpdateAction.class, action -> gatewayDataUpdater
                        .onUserUpdate(action.getShardIndex(), action.getUserUpdate()))
                .map(VoiceStateUpdateDispatchAction.class, action -> gatewayDataUpdater
                        .onVoiceStateUpdateDispatch(action.getShardIndex(), action.getVoiceStateUpdateDispatch()))
                .map(CompleteGuildMembersAction.class, action -> gatewayDataUpdater
                        .onGuildMembersCompletion(action.getGuildId()))
                .build();
    }

    /**
     * Executes the given action. The action will be routed based on the concrete type of the action, and handled
     * according to the layout given when creating this {@link Store}. If the concrete type of the action is unknown
     * and no custom mapping was defined for it, it will return empty.
     *
     * @param action the action to execute
     * @param <R>    the type of data returned by the action
     * @return a {@link Publisher} where, upon successful completion, emits the result(s) produced by the execution of
     * the action, if any. If an error is received, it is emitted through the {@link Publisher}.
     */
    public <R> Publisher<R> execute(StoreAction<R> action) {
        return actionMapper.findHandlerForAction(action)
                .<Publisher<R>>map(h -> h.apply(action))
                .orElse(Flux.empty());
    }
}
