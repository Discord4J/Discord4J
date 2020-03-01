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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.*;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ImmutableUserData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.retry.GatewayStateChange;
import discord4j.store.api.util.LongLongTuple2;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link Dispatch} to {@link Event} mapping operations.
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

    private static <D, E extends Event> void addHandler(Class<D> dispatchType,
                                                        DispatchHandler<D, E> dispatchHandler) {
        handlerMap.put(dispatchType, dispatchHandler);
    }

    private static final Logger log = Loggers.getLogger(DispatchHandlers.class);

    /**
     * Process a {@link Dispatch} object wrapped with its context to potentially obtain an {@link Event}.
     *
     * @param context the DispatchContext used with this Dispatch object
     * @param <D> the Dispatch type
     * @param <E> the resulting Event type
     * @return an Event mapped from the given Dispatch object, or null if no Event is produced.
     */
    @SuppressWarnings("unchecked")
    public static <D, E extends Event> Mono<E> handle(DispatchContext<D> context) {
        DispatchHandler<D, E> handler = (DispatchHandler<D, E>) handlerMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isAssignableFrom(context.getDispatch().getClass()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (handler == null) {
            log.warn("Handler not found from: {}", context.getDispatch().getClass());
            return Mono.empty();
        }
        return Mono.defer(() -> handler.handle(context))
                .checkpoint("Dispatch handled for " + context.getDispatch().getClass());
    }

    private static Mono<PresenceUpdateEvent> presenceUpdate(DispatchContext<PresenceUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId().get());
        UserData userData = context.getDispatch().user();
        long userId = Long.parseUnsignedLong(userData.id());
        LongLongTuple2 key = LongLongTuple2.of(guildId, userId);
        Presence current = new Presence(context.getDispatch());

        Mono<Void> saveNew = context.getStateHolder().getPresenceStore().save(key, context.getDispatch());

        Mono<Optional<User>> saveUser = context.getStateHolder().getUserStore()
                .find(userId)
                .map(oldUserData -> {
                    UserData newUserData = ImmutableUserData.builder()
                            .from(oldUserData)
                            .username(newPossibleIfPresent(oldUserData.username(), userData.username()))
                            .discriminator(newPossibleIfPresent(oldUserData.discriminator(), userData.discriminator()))
                            .avatar(newPossibleIfPresent(oldUserData.avatar(), userData.avatar()))
                            .build();

                    return Tuples.of(oldUserData, newUserData);
                })
                .flatMap(tuple -> context.getStateHolder().getUserStore()
                        .save(userId, tuple.getT2())
                        .thenReturn(tuple.getT1()))
                .map(userBean -> new User(gateway, userBean))
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());

        return saveUser.flatMap(oldUser ->
                context.getStateHolder().getPresenceStore()
                        .find(key)
                        .flatMap(saveNew::thenReturn)
                        .map(old -> new PresenceUpdateEvent(gateway, context.getShardInfo(), guildId,
                                oldUser.orElse(null), userData, current, new Presence(old)))
                        .switchIfEmpty(saveNew.thenReturn(new PresenceUpdateEvent(gateway, context.getShardInfo(),
                                guildId, oldUser.orElse(null), userData, current, null))));
    }

    static <T> Possible<T> newPossibleIfPresent(Possible<T> oldPossible, Possible<T> newPossible) {
        return newPossible.isAbsent() ? oldPossible : newPossible;
    }

    private static Mono<TypingStartEvent> typingStart(DispatchContext<TypingStart> context) {
        long channelId = Long.parseUnsignedLong(context.getDispatch().channelId());
        Long guildId = context.getDispatch().guildId().toOptional().map(Long::parseUnsignedLong).orElse(null);
        long userId = Long.parseUnsignedLong(context.getDispatch().userId());
        Instant startTime = Instant.ofEpochSecond(context.getDispatch().timestamp());

        Member member = context.getDispatch().member().toOptional()
                .filter(__ -> guildId != null)
                .map(memberData -> new Member(context.getGateway(), memberData, guildId))
                .orElse(null);

        return Mono.just(new TypingStartEvent(context.getGateway(), context.getShardInfo(), channelId, guildId,
                userId, startTime, member));
    }

    private static Mono<UserUpdateEvent> userUpdate(DispatchContext<UserUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        UserData userData = context.getDispatch().user();
        long userId = Long.parseUnsignedLong(userData.id());
        User current = new User(gateway, userData);

        Mono<Void> saveNew = context.getStateHolder().getUserStore().save(userId, userData);

        return context.getStateHolder().getUserStore()
                .find(userId)
                .flatMap(saveNew::thenReturn)
                .map(old -> new UserUpdateEvent(gateway, context.getShardInfo(), current, new User(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new UserUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    private static Mono<Event> voiceServerUpdate(DispatchContext<VoiceServerUpdate> context) {
        String token = context.getDispatch().token();
        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        String endpoint = context.getDispatch().endpoint();

        return Mono.just(new VoiceServerUpdateEvent(context.getGateway(), context.getShardInfo(), token, guildId,
                endpoint));
    }

    private static Mono<VoiceStateUpdateEvent> voiceStateUpdateDispatch(
            DispatchContext<VoiceStateUpdateDispatch> context) {
        GatewayDiscordClient gateway = context.getGateway();
        VoiceStateData voiceStateData = context.getDispatch().voiceState();

        long guildId = Long.parseUnsignedLong(voiceStateData.guildId().get());
        long userId = Long.parseUnsignedLong(voiceStateData.userId());

        LongLongTuple2 key = LongLongTuple2.of(guildId, userId);
        VoiceState current = new VoiceState(gateway, voiceStateData);

        Mono<Void> saveNew = context.getStateHolder().getVoiceStateStore().save(key, voiceStateData);

        return context.getStateHolder().getVoiceStateStore()
                .find(key)
                .flatMap(saveNew::thenReturn)
                .map(old -> new VoiceStateUpdateEvent(gateway, context.getShardInfo(), current,
                        new VoiceState(gateway, old)))
                .switchIfEmpty(saveNew.thenReturn(new VoiceStateUpdateEvent(gateway, context.getShardInfo(), current,
                        null)));
    }

    private static Mono<Event> webhooksUpdate(DispatchContext<WebhooksUpdate> context) {
        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        long channelId = Long.parseUnsignedLong(context.getDispatch().channelId());

        return Mono.just(new WebhooksUpdateEvent(context.getGateway(), context.getShardInfo(), guildId, channelId));
    }
}
