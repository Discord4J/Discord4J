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

package discord4j.core.event;

import discord4j.common.json.payload.dispatch.*;
import discord4j.core.event.domain.*;
import discord4j.gateway.retry.GatewayStateChange;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for {@link discord4j.common.json.payload.dispatch.Dispatch} to {@link discord4j.core.event.domain.Event}
 * mapping operations.
 */
public abstract class DispatchHandlers {

    private static final Map<Class<?>, DispatchHandler<?, ?>> handlerMap = new HashMap<>();

    static {
        addHandler(ChannelDelete.class, DispatchHandlers::channelDelete);
        addHandler(ChannelPinsUpdate.class, DispatchHandlers::channelPinsUpdate);
        addHandler(ChannelUpdate.class, DispatchHandlers::channelUpdate);
        addHandler(GuildBanAdd.class, DispatchHandlers::guildBanAdd);
        addHandler(GuildBanRemove.class, DispatchHandlers::guildBanRemove);
        addHandler(GuildCreate.class, DispatchHandlers::guildCreate);
        addHandler(GuildDelete.class, DispatchHandlers::guildDelete);
        addHandler(GuildEmojisUpdate.class, DispatchHandlers::guildEmojisUpdate);
        addHandler(GuildIntegrationsUpdate.class, DispatchHandlers::guildIntegrationsUpdate);
        addHandler(GuildMemberAdd.class, DispatchHandlers::guildMemberAdd);
        addHandler(GuildMemberRemove.class, DispatchHandlers::guildMemberRemove);
        addHandler(GuildMembersChunk.class, DispatchHandlers::guildMembersChunk);
        addHandler(GuildMemberUpdate.class, DispatchHandlers::guildMemberUpdate);
        addHandler(GuildRoleCreate.class, DispatchHandlers::guildRoleCreate);
        addHandler(GuildRoleDelete.class, DispatchHandlers::guildRoleDelete);
        addHandler(GuildRoleUpdate.class, DispatchHandlers::guildRoleUpdate);
        addHandler(GuildUpdate.class, DispatchHandlers::guildUpdate);
        addHandler(MessageCreate.class, DispatchHandlers::messageCreate);
        addHandler(MessageDelete.class, DispatchHandlers::messageDelete);
        addHandler(MessageDeleteBulk.class, DispatchHandlers::messageDeleteBulk);
        addHandler(MessageReactionAdd.class, DispatchHandlers::messageReactionAdd);
        addHandler(MessageReactionRemove.class, DispatchHandlers::messageReactionRemove);
        addHandler(MessageReactionRemoveAll.class, DispatchHandlers::messageReactionRemoveAll);
        addHandler(MessageUpdate.class, DispatchHandlers::messageUpdate);
        addHandler(PresenceUpdate.class, DispatchHandlers::presenceUpdate);
        addHandler(Ready.class, DispatchHandlers::ready);
        addHandler(Resumed.class, DispatchHandlers::resumed);
        addHandler(TypingStart.class, DispatchHandlers::typingStart);
        addHandler(UserUpdate.class, DispatchHandlers::userUpdate);
        addHandler(VoiceServerUpdate.class, DispatchHandlers::voiceServerUpdate);
        addHandler(VoiceStateUpdateDispatch.class, DispatchHandlers::voiceStateUpdateDispatch);
        addHandler(WebhooksUpdate.class, DispatchHandlers::webhooksUpdate);

        addHandler(GatewayStateChange.class, DispatchHandlers::gatewayStateChanged);
    }

    private static <D extends Dispatch, E extends Event> void addHandler(Class<D> dispatchType,
                                                                         DispatchHandler<D, E> dispatchHandler) {
        handlerMap.put(dispatchType, dispatchHandler);
    }

    /**
     * Process a {@link discord4j.common.json.payload.dispatch.Dispatch} object wrapped with its context to
     * potentially obtain an {@link discord4j.core.event.domain.Event}.
     *
     * @param context the DispatchContext used with this Dispatch object
     * @param <D> the Dispatch type
     * @param <E> the resulting Event type
     * @return an Event mapped from the given Dispatch object, or null if no Event is produced.
     */
    @SuppressWarnings("unchecked")
    public static <D extends Dispatch, E extends Event> E handle(DispatchContext<D> context) {
        DispatchHandler<D, E> entry = (DispatchHandler<D, E>) handlerMap.get(context.getDispatch().getClass());
        if (entry == null) {
            return null;
        }
        return entry.handle(context);
    }

    private static Event channelDelete(DispatchContext<ChannelDelete> context) {
        // TODO
        return null;
    }

    private static Event channelPinsUpdate(DispatchContext<ChannelPinsUpdate> context) {
        // TODO
        return null;
    }

    private static Event channelUpdate(DispatchContext<ChannelUpdate> context) {
        // TODO
        return null;
    }

    private static Event guildBanAdd(DispatchContext<GuildBanAdd> context) {
        // TODO
        return null;
    }

    private static Event guildBanRemove(DispatchContext<GuildBanRemove> context) {
        // TODO
        return null;
    }

    private static Event guildCreate(DispatchContext<GuildCreate> context) {
        // TODO
        return null;
    }

    private static Event guildDelete(DispatchContext<GuildDelete> context) {
        // TODO
        return null;
    }

    private static Event guildEmojisUpdate(DispatchContext<GuildEmojisUpdate> context) {
        // TODO
        return null;
    }

    private static Event guildIntegrationsUpdate(DispatchContext<GuildIntegrationsUpdate> context) {
        // TODO
        return null;
    }

    private static Event guildMemberAdd(DispatchContext<GuildMemberAdd> context) {
        // TODO
        return null;
    }

    private static Event guildMemberRemove(DispatchContext<GuildMemberRemove> context) {
        // TODO
        return null;
    }

    private static Event guildMembersChunk(DispatchContext<GuildMembersChunk> context) {
        // TODO
        return null;
    }

    private static Event guildMemberUpdate(DispatchContext<GuildMemberUpdate> context) {
        // TODO
        return null;
    }

    private static Event guildRoleCreate(DispatchContext<GuildRoleCreate> context) {
        // TODO
        return null;
    }

    private static Event guildRoleDelete(DispatchContext<GuildRoleDelete> context) {
        // TODO
        return null;
    }

    private static Event guildRoleUpdate(DispatchContext<GuildRoleUpdate> context) {
        // TODO
        return null;
    }

    private static Event guildUpdate(DispatchContext<GuildUpdate> context) {
        // TODO
        return null;
    }

    private static MessageCreatedEvent messageCreate(DispatchContext<MessageCreate> context) {
        // TODO
        return new MessageCreatedEvent(context.getDispatch());
    }

    private static Event messageDelete(DispatchContext<MessageDelete> context) {
        // TODO
        return null;
    }

    private static Event messageDeleteBulk(DispatchContext<MessageDeleteBulk> context) {
        // TODO
        return null;
    }

    private static Event messageReactionAdd(DispatchContext<MessageReactionAdd> context) {
        // TODO
        return null;
    }

    private static Event messageReactionRemove(DispatchContext<MessageReactionRemove> context) {
        // TODO
        return null;
    }

    private static Event messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll> context) {
        // TODO
        return null;
    }

    private static Event messageUpdate(DispatchContext<MessageUpdate> context) {
        // TODO
        return null;
    }

    private static Event presenceUpdate(DispatchContext<PresenceUpdate> context) {
        // TODO
        return null;
    }

    private static ReadyEvent ready(DispatchContext<Ready> context) {
        // TODO
        return new ReadyEvent(context.getDispatch());
    }

    private static Event resumed(DispatchContext<Resumed> context) {
        // TODO
        return null;
    }

    private static Event typingStart(DispatchContext<TypingStart> context) {
        // TODO
        return null;
    }

    private static Event userUpdate(DispatchContext<UserUpdate> context) {
        // TODO
        return null;
    }

    private static Event voiceServerUpdate(DispatchContext<VoiceServerUpdate> context) {
        // TODO
        return null;
    }

    private static Event voiceStateUpdateDispatch(DispatchContext<VoiceStateUpdateDispatch> context) {
        // TODO
        return null;
    }

    private static Event webhooksUpdate(DispatchContext<WebhooksUpdate> context) {
        // TODO
        return null;
    }

    private static Event gatewayStateChanged(DispatchContext<GatewayStateChange> context) {
        GatewayStateChange dispatch = context.getDispatch();
        switch (dispatch.getState()) {
            case CONNECTED:
                return new ConnectedEvent();
            case RETRY_STARTED:
                return new ReconnectStartedEvent();
            case RETRY_FAILED:
                return new ReconnectFailedEvent();
            case RETRY_SUCCEEDED:
                return new ReconnectedEvent();
            case DISCONNECTED:
                return new DisconnectedEvent();
        }
        return null;
    }

}
