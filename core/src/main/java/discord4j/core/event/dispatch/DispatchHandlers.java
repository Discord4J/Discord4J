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

import com.fasterxml.jackson.databind.JsonNode;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.event.domain.*;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.data.stored.PresenceBean;
import discord4j.core.object.data.stored.UserBean;
import discord4j.core.object.data.stored.VoiceStateBean;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.gateway.json.dispatch.*;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.store.api.util.LongLongTuple2;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link discord4j.gateway.json.dispatch.Dispatch} to {@link discord4j.core.event.domain.Event}
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
     * Process a {@link discord4j.gateway.json.dispatch.Dispatch} object wrapped with its context to
     * potentially obtain an {@link discord4j.core.event.domain.Event}.
     *
     * @param context the DispatchContext used with this Dispatch object
     * @param <D> the Dispatch type
     * @param <E> the resulting Event type
     * @return an Event mapped from the given Dispatch object, or null if no Event is produced.
     */
    @SuppressWarnings("unchecked")
    public static <D extends Dispatch, E extends Event> Mono<E> handle(DispatchContext<D> context) {
        DispatchHandler<D, E> entry = (DispatchHandler<D, E>) handlerMap.get(context.getDispatch().getClass());
        if (entry == null) {
            return Mono.empty();
        }
        return entry.handle(context);
    }

    private static Mono<PresenceUpdateEvent> presenceUpdate(DispatchContext<PresenceUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();

        long guildId = context.getDispatch().getGuildId();
        JsonNode user = context.getDispatch().getUser();
        long userId = Long.parseUnsignedLong(user.get("id").asText());
        LongLongTuple2 key = LongLongTuple2.of(guildId, userId);
        PresenceBean bean = new PresenceBean(context.getDispatch());
        Presence current = new Presence(bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getPresenceStore().save(key, bean);

        Mono<Optional<User>> saveUser = serviceMediator.getStateHolder().getUserStore()
                .find(userId)
                .map(oldBean -> {
                    UserBean newBean = new UserBean(oldBean);
                    JsonNode username = user.get("username");
                    JsonNode discriminator = user.get("discriminator");
                    JsonNode avatar = user.get("avatar");

                    newBean.setUsername((username == null) ? oldBean.getUsername() : username.asText());
                    newBean.setDiscriminator((discriminator == null) ? oldBean.getDiscriminator() : discriminator.asText());
                    newBean.setAvatar((avatar == null) ? oldBean.getAvatar() : (avatar.isNull() ? null : avatar.asText()));

                    return Tuples.of(oldBean, newBean);
                })
                .flatMap(tuple -> serviceMediator.getStateHolder().getUserStore()
                        .save(userId, tuple.getT2())
                        .thenReturn(tuple.getT1()))
                .map(userBean -> new User(serviceMediator, userBean))
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());

        return saveUser.flatMap(oldUser ->
                serviceMediator.getStateHolder().getPresenceStore()
                        .find(key)
                        .flatMap(saveNew::thenReturn)
                        .map(old -> new PresenceUpdateEvent(client, guildId, oldUser.orElse(null), user, current, new Presence(old)))
                        .switchIfEmpty(saveNew.thenReturn(new PresenceUpdateEvent(client, guildId, oldUser.orElse(null), user, current, null))));
    }

    private static Mono<TypingStartEvent> typingStart(DispatchContext<TypingStart> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        long channelId = context.getDispatch().getChannelId();
        long userId = context.getDispatch().getUserId();
        Instant startTime = Instant.ofEpochMilli(context.getDispatch().getTimestamp());

        return Mono.just(new TypingStartEvent(client, channelId, userId, startTime));
    }

    private static Mono<UserUpdateEvent> userUpdate(DispatchContext<UserUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();

        UserBean bean = new UserBean(context.getDispatch().getUser());
        User current = new User(serviceMediator, bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getUserStore().save(bean.getId(), bean);

        return serviceMediator.getStateHolder().getUserStore()
                .find(context.getDispatch().getUser().getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new UserUpdateEvent(client, current, new User(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new UserUpdateEvent(client, current, null)));
    }

    private static Mono<Event> voiceServerUpdate(DispatchContext<VoiceServerUpdate> context) {
        DiscordClient client = context.getServiceMediator().getClient();
        String token = context.getDispatch().getToken();
        long guildId = context.getDispatch().getGuildId();
        String endpoint = context.getDispatch().getEndpoint();

        return Mono.just(new VoiceServerUpdateEvent(client, token, guildId, endpoint));
    }

    private static Mono<VoiceStateUpdateEvent> voiceStateUpdateDispatch(DispatchContext<VoiceStateUpdateDispatch>
                                                                                context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getClient();

        long guildId = context.getDispatch().getVoiceState().getGuildId();
        long userId = context.getDispatch().getVoiceState().getUserId();

        LongLongTuple2 key = LongLongTuple2.of(guildId, userId);
        VoiceStateBean bean = new VoiceStateBean(context.getDispatch().getVoiceState());
        VoiceState current = new VoiceState(serviceMediator, bean);

        Mono<Void> saveNew = serviceMediator.getStateHolder().getVoiceStateStore().save(key, bean);

        return serviceMediator.getStateHolder().getVoiceStateStore()
                .find(key)
                .flatMap(saveNew::thenReturn)
                .map(old -> new VoiceStateUpdateEvent(client, current, new VoiceState(serviceMediator, old)))
                .switchIfEmpty(saveNew.thenReturn(new VoiceStateUpdateEvent(client, current, null)));
    }

    private static Mono<Event> webhooksUpdate(DispatchContext<WebhooksUpdate> context) {
        long guildId = context.getDispatch().getGuildId();
        long channelId = context.getDispatch().getChannelId();

        return Mono.just(new WebhooksUpdateEvent(context.getServiceMediator().getClient(), guildId, channelId));
    }
}
