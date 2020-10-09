package discord4j.common.store;

import discord4j.common.store.layout.DataAccessor;
import discord4j.common.store.layout.GatewayDataUpdater;
import discord4j.common.store.layout.action.gateway.*;
import discord4j.common.store.layout.action.read.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActionMapper {

    private final Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Mono<?>>> mappings;

    private ActionMapper(Map<Class<? extends StoreAction<?>>, Function<StoreAction<?>, ? extends Mono<?>>> mappings) {
        this.mappings = mappings;
    }

    public static ActionMapper create() {
        return new ActionMapper(new HashMap<>());
    }

    static ActionMapper aggregate(ActionMapper... mappers) {
        return new ActionMapper(Arrays.stream(mappers)
                .flatMap(mapper -> mapper.mappings.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))); // throws ISE if duplicates
    }

    static ActionMapper fromDataAccessor(DataAccessor dataAccessor) {
        return create()
                .map(CountAction.class, dataAccessor::count)
                .map(GetChannelByIdAction.class, dataAccessor::getChannelById)
                .map(GetChannelVoiceStatesAction.class, dataAccessor::getChannelVoiceStates)
                .map(GetGuildByIdAction.class, dataAccessor::getGuildById)
                .map(GetGuildChannelsAction.class, dataAccessor::getGuildChannels)
                .map(GetGuildEmojiByIdAction.class, dataAccessor::getGuildEmojiById)
                .map(GetGuildEmojisAction.class, dataAccessor::getGuildEmojis)
                .map(GetGuildMembersAction.class, dataAccessor::getGuildMembers)
                .map(GetGuildPresencesAction.class, dataAccessor::getGuildPresences)
                .map(GetGuildRolesAction.class, dataAccessor::getGuildRoles)
                .map(GetGuildsAction.class, dataAccessor::getGuilds)
                .map(GetGuildVoiceStatesAction.class, dataAccessor::getGuildVoiceStates)
                .map(GetMemberByIdAction.class, dataAccessor::getMemberById)
                .map(GetMessageByIdAction.class, dataAccessor::getMessageById)
                .map(GetPresenceByIdAction.class, dataAccessor::getPresenceById)
                .map(GetRoleByIdAction.class, dataAccessor::getRoleById)
                .map(GetUserByIdAction.class, dataAccessor::getUserById)
                .map(GetUsersAction.class, dataAccessor::getUsers)
                .map(GetVoiceStateByIdAction.class, dataAccessor::getVoiceStateById);
    }

    static ActionMapper fromGatewayDataUpdater(GatewayDataUpdater gatewayDataUpdater) {
        return create()
                .map(ChannelCreateAction.class, gatewayDataUpdater::onChannelCreate)
                .map(ChannelDeleteAction.class, gatewayDataUpdater::onChannelDelete)
                .map(ChannelUpdateAction.class, gatewayDataUpdater::onChannelUpdate)
                .map(GuildCreateAction.class, gatewayDataUpdater::onGuildCreate)
                .map(GuildDeleteAction.class, gatewayDataUpdater::onGuildDelete)
                .map(GuildEmojisUpdateAction.class, gatewayDataUpdater::onGuildEmojisUpdate)
                .map(GuildMemberAddAction.class, gatewayDataUpdater::onGuildMemberAdd)
                .map(GuildMemberRemoveAction.class, gatewayDataUpdater::onGuildMemberRemove)
                .map(GuildMembersChunkAction.class, gatewayDataUpdater::onGuildMembersChunk)
                .map(GuildMemberUpdateAction.class, gatewayDataUpdater::onGuildMemberUpdate)
                .map(GuildRoleCreateAction.class, gatewayDataUpdater::onGuildRoleCreate)
                .map(GuildRoleDeleteAction.class, gatewayDataUpdater::onGuildRoleDelete)
                .map(GuildRoleUpdateAction.class, gatewayDataUpdater::onGuildRoleUpdate)
                .map(GuildUpdateAction.class, gatewayDataUpdater::onGuildUpdate)
                .map(InvalidateShardAction.class, gatewayDataUpdater::onInvalidateShard)
                .map(MessageCreateAction.class, gatewayDataUpdater::onMessageCreate)
                .map(MessageDeleteAction.class, gatewayDataUpdater::onMessageDelete)
                .map(MessageDeleteBulkAction.class, gatewayDataUpdater::onMessageDeleteBulk)
                .map(MessageReactionAddAction.class, gatewayDataUpdater::onMessageReactionAdd)
                .map(MessageReactionRemoveAction.class, gatewayDataUpdater::onMessageReactionRemove)
                .map(MessageReactionRemoveAllAction.class, gatewayDataUpdater::onMessageReactionRemoveAll)
                .map(MessageReactionRemoveEmojiAction.class, gatewayDataUpdater::onMessageReactionRemoveEmoji)
                .map(MessageUpdateAction.class, gatewayDataUpdater::onMessageUpdate)
                .map(PresenceUpdateAction.class, gatewayDataUpdater::onPresenceUpdate)
                .map(ReadyAction.class, gatewayDataUpdater::onReady)
                .map(UserUpdateAction.class, gatewayDataUpdater::onUserUpdate)
                .map(VoiceStateUpdateDispatchAction.class, gatewayDataUpdater::onVoiceStateUpdateDispatch);
    }

    /**
     * Maps a specific action type to a dataAccessor function to execute.
     *
     * @param actionType   the type of the action
     * @param dataAccessor the dataAccessor to execute when an action of the specified type is received
     * @param <R>          the return type of the action
     * @param <S>          the type of the action itself
     * @return this {@link ActionMapper} enriched with the added mapping
     */
    @SuppressWarnings("unchecked")
    public <R, S extends StoreAction<R>> ActionMapper map(Class<S> actionType,
                                                          Function<? super S, ? extends Mono<R>> dataAccessor) {
        mappings.put(actionType, action -> dataAccessor.apply((S) action));
        return this;
    }

    Function<StoreAction<?>, ? extends Mono<?>> get(Object obj) {
        return mappings.get(obj);
    }
}
