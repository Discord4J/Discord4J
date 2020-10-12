package discord4j.common.store;

import discord4j.common.store.action.gateway.*;
import discord4j.common.store.action.read.*;
import discord4j.common.store.layout.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Objects;

public final class Store {

    private final ActionMapper actionMapper;

    private Store(ActionMapper actionMapper) {
        this.actionMapper = actionMapper;
    }

    public static Store noOp() {
        return new Store(ActionMapper.create());
    }

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
        return ActionMapper.create()
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
                            throw new AssertionError();
                    }
                })
                .map(CountInGuildAction.class, action -> {
                    switch (action.getEntity()) {
                        case CHANNELS:
                            return dataAccessor.countChannelsInGuild(action.getGuildId());
                        case EMOJIS:
                            return dataAccessor.countEmojisInGuild(action.getGuildId());
                        case MEMBERS:
                            return dataAccessor.countMembersInGuild(action.getGuildId());
                        case PRESENCES:
                            return dataAccessor.countPresencesInGuild(action.getGuildId());
                        case ROLES:
                            return dataAccessor.countRolesInGuild(action.getGuildId());
                        case VOICE_STATES:
                            return dataAccessor.countVoiceStatesInGuild(action.getGuildId());
                        default:
                            throw new AssertionError();
                    }
                })
                .map(CountInChannelAction.class, action -> {
                    switch (action.getEntity()) {
                        case MESSAGES:
                            return dataAccessor.countMessagesInChannel(action.getChannelId());
                        case VOICE_STATES:
                            return dataAccessor.countVoiceStatesInChannel(action.getChannelId());
                        default:
                            throw new AssertionError();
                    }
                })
                .map(GetChannelByIdAction.class, action -> dataAccessor.getChannelById(action.getChannelId()))
                .map(GetChannelVoiceStatesAction.class, action -> dataAccessor.getChannelVoiceStates(action.getChannelId()))
                .map(GetGuildByIdAction.class, action -> dataAccessor.getGuildById(action.getGuildId()))
                .map(GetGuildChannelsAction.class, action -> dataAccessor
                        .getGuildChannels(action.getGuildId(), action.requireComplete()))
                .map(GetGuildEmojiByIdAction.class, action -> dataAccessor
                        .getGuildEmojiById(action.getGuildId(), action.getEmojiId()))
                .map(GetGuildEmojisAction.class, action -> dataAccessor
                        .getGuildEmojis(action.getGuildId(), action.requireComplete()))
                .map(GetGuildMembersAction.class, action -> dataAccessor
                        .getGuildMembers(action.getGuildId(), action.requireComplete()))
                .map(GetGuildPresencesAction.class, action -> dataAccessor
                        .getGuildPresences(action.getGuildId(), action.requireComplete()))
                .map(GetGuildRolesAction.class, action -> dataAccessor
                        .getGuildRoles(action.getGuildId(), action.requireComplete()))
                .map(GetGuildsAction.class, action -> dataAccessor.getGuilds(action.requireComplete()))
                .map(GetGuildVoiceStatesAction.class, action -> dataAccessor.getGuildVoiceStates(action.getGuildId()))
                .map(GetMemberByIdAction.class, action -> dataAccessor
                        .getMemberById(action.getGuildId(), action.getUserId()))
                .map(GetMessageByIdAction.class, action -> dataAccessor
                        .getMessageById(action.getChannelId(), action.getMessageId()))
                .map(GetPresenceByIdAction.class, action -> dataAccessor
                        .getPresenceById(action.getGuildId(), action.getUserId()))
                .map(GetRoleByIdAction.class, action -> dataAccessor
                        .getRoleById(action.getGuildId(), action.getRoleId()))
                .map(GetUserByIdAction.class, action -> dataAccessor.getUserById(action.getUserId()))
                .map(GetUsersAction.class, action -> dataAccessor.getUsers())
                .map(GetVoiceStateByIdAction.class, action -> dataAccessor
                        .getVoiceStateById(action.getGuildId(), action.getUserId()));
    }

    private static ActionMapper gatewayDataUpdaterToMapper(GatewayDataUpdater gatewayDataUpdater) {
        Objects.requireNonNull(gatewayDataUpdater);
        return ActionMapper.create()
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
                        .onInvalidateShard(action.getShardIndex(), action.getCause()))
                .map(MessageCreateAction.class, action -> gatewayDataUpdater
                        .onMessageCreate(action.getShardIndex(), action.getMessageCreate()))
                .map(MessageDeleteAction.class, action -> gatewayDataUpdater
                        .onMessageDelete(action.getShardIndex(), action.getMessageDelete()))
                .map(MessageDeleteBulkAction.class, action -> gatewayDataUpdater
                        .onMessageDeleteBulk(action.getShardIndex(), action.getMessageDeleteBulk()))
                .map(MessageReactionAddAction.class,action -> gatewayDataUpdater
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
                .map(ReadyAction.class, action -> gatewayDataUpdater
                        .onReady(action.getShardIndex(), action.getReady()))
                .map(UserUpdateAction.class, action -> gatewayDataUpdater
                        .onUserUpdate(action.getShardIndex(), action.getUserUpdate()))
                .map(VoiceStateUpdateDispatchAction.class, action -> gatewayDataUpdater
                        .onVoiceStateUpdateDispatch(action.getShardIndex(), action.getVoiceStateUpdateDispatch()));
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
