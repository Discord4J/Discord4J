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
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.bean.MessageBean;
import discord4j.core.object.entity.bean.UserBean;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registry for {@link discord4j.common.json.payload.dispatch.Dispatch} to {@link discord4j.core.event.domain.Event}
 * mapping operations.
 */
public abstract class DispatchHandlers {

    private static final Map<Class<?>, DispatchHandler<?, ?>> handlerMap = new HashMap<>();

    static {
        addHandler(ChannelCreate.class, DispatchHandlers::channelCreate);
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
    public static <D extends Dispatch, E extends Event> Flux<E> handle(DispatchContext<D> context) {
        DispatchHandler<D, E> entry = (DispatchHandler<D, E>) handlerMap.get(context.getDispatch().getClass());
        if (entry == null) {
            return Flux.empty();
        }
        return entry.handle(context);
    }

    private static Flux<Event> channelCreate(DispatchContext<ChannelCreate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> channelDelete(DispatchContext<ChannelDelete> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> channelPinsUpdate(DispatchContext<ChannelPinsUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> channelUpdate(DispatchContext<ChannelUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildBanAdd(DispatchContext<GuildBanAdd> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildBanRemove(DispatchContext<GuildBanRemove> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildCreate(DispatchContext<GuildCreate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildDelete(DispatchContext<GuildDelete> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildEmojisUpdate(DispatchContext<GuildEmojisUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildIntegrationsUpdate(DispatchContext<GuildIntegrationsUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildMemberAdd(DispatchContext<GuildMemberAdd> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildMemberRemove(DispatchContext<GuildMemberRemove> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildMembersChunk(DispatchContext<GuildMembersChunk> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildMemberUpdate(DispatchContext<GuildMemberUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildRoleCreate(DispatchContext<GuildRoleCreate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildRoleDelete(DispatchContext<GuildRoleDelete> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildRoleUpdate(DispatchContext<GuildRoleUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> guildUpdate(DispatchContext<GuildUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<MessageCreateEvent> messageCreate(DispatchContext<MessageCreate> context) {
        // TODO
        Message message = new Message(context.getClient(), new MessageBean(context.getDispatch().getMessage()));
        return Flux.just(new MessageCreateEvent(context.getClient(), message));
    }

    private static Flux<Event> messageDelete(DispatchContext<MessageDelete> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> messageDeleteBulk(DispatchContext<MessageDeleteBulk> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> messageReactionAdd(DispatchContext<MessageReactionAdd> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> messageReactionRemove(DispatchContext<MessageReactionRemove> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> messageReactionRemoveAll(DispatchContext<MessageReactionRemoveAll> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> messageUpdate(DispatchContext<MessageUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> presenceUpdate(DispatchContext<PresenceUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<ReadyEvent> ready(DispatchContext<Ready> context) {
        // TODO
        Ready dispatch = context.getDispatch();
        User self = new User(context.getClient(), new UserBean(dispatch.getUser()));
        Set<ReadyEvent.Guild> guilds = Arrays.stream(dispatch.getGuilds())
                .map(g -> new ReadyEvent.Guild(g.getId(), g.isUnavailable()))
                .collect(Collectors.toSet());

        return Flux.just(new ReadyEvent(context.getClient(), dispatch.getVersion(), self, guilds,
                dispatch.getSessionId(), dispatch.getTrace()));
    }

    private static Flux<Event> resumed(DispatchContext<Resumed> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> typingStart(DispatchContext<TypingStart> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> userUpdate(DispatchContext<UserUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> voiceServerUpdate(DispatchContext<VoiceServerUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> voiceStateUpdateDispatch(DispatchContext<VoiceStateUpdateDispatch> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> webhooksUpdate(DispatchContext<WebhooksUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<Event> gatewayStateChanged(DispatchContext<GatewayStateChange> context) {
        GatewayStateChange dispatch = context.getDispatch();
        switch (dispatch.getState()) {
            case CONNECTED:
                return Flux.just(new ConnectEvent(context.getClient()));
            case RETRY_STARTED:
                return Flux.just(new ReconnectStartEvent(context.getClient()));
            case RETRY_FAILED:
                return Flux.just(new ReconnectFailEvent(context.getClient(), dispatch.getCurrentAttempt()));
            case RETRY_SUCCEEDED:
                return Flux.just(new ReconnectEvent(context.getClient(), dispatch.getCurrentAttempt()));
            case DISCONNECTED:
                return Flux.just(new DisconnectEvent(context.getClient()));
        }
        return Flux.empty();
    }

}
