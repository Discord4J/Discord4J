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
package discord4j.core.event.dispatch;

import discord4j.common.json.payload.dispatch.*;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for {@link discord4j.common.json.payload.dispatch.Dispatch} to {@link discord4j.core.event.domain.Event}
 * mapping operations.
 */
public abstract class DispatchHandlers {

    private static final Map<Class<?>, DispatchHandler<?, ?>> handlerMap = new HashMap<>();

    static {
        addHandler(ChannelCreate.class, ChannelDispatchHandlers::channelCreate);
        addHandler(ChannelDelete.class, ChannelDispatchHandlers::channelDelete);
        addHandler(ChannelPinsUpdate.class, ChannelDispatchHandlers::channelPinsUpdate);
        addHandler(ChannelUpdate.class, ChannelDispatchHandlers::channelUpdate);
        addHandler(GuildBanAdd.class, GuildDispatchHandlers::guildBanAdd);
        addHandler(GuildBanRemove.class, GuildDispatchHandlers::guildBanRemove);
        addHandler(GuildCreate.class, GuildDispatchHandlers::guildCreate);
        addHandler(GuildDelete.class, GuildDispatchHandlers::guildDelete);
        addHandler(GuildEmojisUpdate.class, GuildDispatchHandlers::guildEmojisUpdate);
        addHandler(GuildIntegrationsUpdate.class, GuildDispatchHandlers::guildIntegrationsUpdate);
        addHandler(GuildMemberAdd.class, GuildDispatchHandlers::guildMemberAdd);
        addHandler(GuildMemberRemove.class, GuildDispatchHandlers::guildMemberRemove);
        addHandler(GuildMembersChunk.class, GuildDispatchHandlers::guildMembersChunk);
        addHandler(GuildMemberUpdate.class, GuildDispatchHandlers::guildMemberUpdate);
        addHandler(GuildRoleCreate.class, GuildDispatchHandlers::guildRoleCreate);
        addHandler(GuildRoleDelete.class, GuildDispatchHandlers::guildRoleDelete);
        addHandler(GuildRoleUpdate.class, GuildDispatchHandlers::guildRoleUpdate);
        addHandler(GuildUpdate.class, GuildDispatchHandlers::guildUpdate);
        addHandler(MessageCreate.class, MessageDispatchHandlers::messageCreate);
        addHandler(MessageDelete.class, MessageDispatchHandlers::messageDelete);
        addHandler(MessageDeleteBulk.class, MessageDispatchHandlers::messageDeleteBulk);
        addHandler(MessageReactionAdd.class, MessageDispatchHandlers::messageReactionAdd);
        addHandler(MessageReactionRemove.class, MessageDispatchHandlers::messageReactionRemove);
        addHandler(MessageReactionRemoveAll.class, MessageDispatchHandlers::messageReactionRemoveAll);
        addHandler(MessageUpdate.class, MessageDispatchHandlers::messageUpdate);
        addHandler(PresenceUpdate.class, DispatchHandlers::presenceUpdate);
        addHandler(Ready.class, LifecycleDispatchHandlers::ready);
        addHandler(Resumed.class, LifecycleDispatchHandlers::resumed);
        addHandler(TypingStart.class, DispatchHandlers::typingStart);
        addHandler(UserUpdate.class, DispatchHandlers::userUpdate);
        addHandler(VoiceServerUpdate.class, DispatchHandlers::voiceServerUpdate);
        addHandler(VoiceStateUpdateDispatch.class, DispatchHandlers::voiceStateUpdateDispatch);
        addHandler(WebhooksUpdate.class, DispatchHandlers::webhooksUpdate);

        addHandler(GatewayStateChange.class, LifecycleDispatchHandlers::gatewayStateChanged);
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

    private static Flux<Event> presenceUpdate(DispatchContext<PresenceUpdate> context) {
        // TODO
        return Flux.empty();
    }

    private static Flux<TypingStartEvent> typingStart(DispatchContext<TypingStart> context) {
        DiscordClient client = context.getServiceMediator().getDiscordClient();
        long channelId = context.getDispatch().getChannelId();
        long userId = context.getDispatch().getUserId();
        Instant startTime = Instant.ofEpochMilli(context.getDispatch().getTimestamp());

        return Flux.just(new TypingStartEvent(client, channelId, userId, startTime));
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
        return Flux.empty();
    }
}
