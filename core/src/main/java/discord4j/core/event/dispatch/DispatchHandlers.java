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

import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.*;
import discord4j.core.event.domain.channel.TypingStartEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.PartialUserData;
import discord4j.discordjson.json.PresenceData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.json.VoiceStateData;
import discord4j.discordjson.json.gateway.*;
import discord4j.gateway.retry.GatewayStateChange;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for {@link Dispatch} to {@link Event} mapping operations.
 */
public class DispatchHandlers implements DispatchEventMapper {

    private static final Map<Class<?>, DispatchHandler<?, ?, ?>> handlerMap = new HashMap<>();

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
        addHandler(MessageReactionRemoveEmoji.class, MessageDispatchHandlers::messageReactionRemoveEmoji);
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
        addHandler(InviteCreate.class, DispatchHandlers::inviteCreate);
        addHandler(InviteDelete.class, DispatchHandlers::inviteDelete);
        addHandler(InteractionCreate.class, DispatchHandlers::interactionCreate);
        addHandler(ApplicationCommandCreate.class, ApplicationCommandDispatchHandlers::applicationCommandCreate);
        addHandler(ApplicationCommandUpdate.class, ApplicationCommandDispatchHandlers::applicationCommandUpdate);
        addHandler(ApplicationCommandDelete.class, ApplicationCommandDispatchHandlers::applicationCommandDelete);

        addHandler(GatewayStateChange.class, LifecycleDispatchHandlers::gatewayStateChanged);

        addHandler(UnavailableGuildCreate.class, context -> Mono.empty());
    }

    private static <D, S, E extends Event> void addHandler(Class<D> dispatchType,
                                                           DispatchHandler<D, S, E> dispatchHandler) {
        handlerMap.put(dispatchType, dispatchHandler);
    }

    private static final Logger log = Loggers.getLogger(DispatchHandlers.class);

    /**
     * Process a {@link Dispatch} object wrapped with its context to potentially obtain an {@link Event}.
     *
     * @param context the DispatchContext used with this Dispatch object
     * @param <D> the Dispatch type
     * @param <S> the old state type, if applicable
     * @param <E> the resulting Event type
     * @return an Event mapped from the given Dispatch object, or null if no Event is produced.
     */
    @SuppressWarnings("unchecked")
    public <D, S, E extends Event> Mono<E> handle(DispatchContext<D, S> context) {
        DispatchHandler<D, S, E> handler = (DispatchHandler<D, S, E>) handlerMap.entrySet()
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

    private static Mono<PresenceUpdateEvent> presenceUpdate(DispatchContext<PresenceUpdate, PresenceAndUserData> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        PartialUserData userData = context.getDispatch().user();
        PresenceData presenceData = createPresence(context.getDispatch());
        Presence current = new Presence(presenceData);
        Presence oldPresence = context.getOldState()
                .flatMap(PresenceAndUserData::getPresenceData)
                .map(Presence::new)
                .orElse(null);
        User oldUser = context.getOldState()
                .flatMap(PresenceAndUserData::getUserData)
                .map(old -> new User(gateway, old))
                .orElse(null);

        return Mono.just(new PresenceUpdateEvent(gateway, context.getShardInfo(), guildId, oldUser, userData, current, oldPresence));
    }

    private static PresenceData createPresence(PresenceUpdate update) {
        return PresenceData.builder()
                .user(update.user())
                .status(update.status())
                .activities(update.activities())
                .clientStatus(update.clientStatus())
                .build();
    }

    private static Mono<TypingStartEvent> typingStart(DispatchContext<TypingStart, Void> context) {
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        Long guildId = context.getDispatch().guildId().toOptional().map(Snowflake::asLong).orElse(null);
        long userId = Snowflake.asLong(context.getDispatch().userId());
        Instant startTime = Instant.ofEpochSecond(context.getDispatch().timestamp());

        Member member = context.getDispatch().member().toOptional()
                .filter(__ -> guildId != null)
                .map(memberData -> new Member(context.getGateway(), memberData, guildId))
                .orElse(null);

        return Mono.just(new TypingStartEvent(context.getGateway(), context.getShardInfo(), channelId, guildId,
                userId, startTime, member));
    }

    private static Mono<UserUpdateEvent> userUpdate(DispatchContext<UserUpdate, UserData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        UserData userData = context.getDispatch().user();
        User current = new User(gateway, userData);

        return Mono.just(new UserUpdateEvent(gateway, context.getShardInfo(), current, context.getOldState()
                                .map(old -> new User(gateway, old)).orElse(null)));
    }

    private static Mono<Event> voiceServerUpdate(DispatchContext<VoiceServerUpdate, Void> context) {
        String token = context.getDispatch().token();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        String endpoint = context.getDispatch().endpoint();

        return Mono.just(new VoiceServerUpdateEvent(context.getGateway(), context.getShardInfo(), token, guildId,
                endpoint));
    }

    private static Mono<VoiceStateUpdateEvent> voiceStateUpdateDispatch(DispatchContext<VoiceStateUpdateDispatch, VoiceStateData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        VoiceStateData voiceStateData = context.getDispatch().voiceState();

        VoiceState current = new VoiceState(gateway, voiceStateData);

        return Mono.just(new VoiceStateUpdateEvent(gateway, context.getShardInfo(), current, context.getOldState()
                                .map(old -> new VoiceState(gateway, old)).orElse(null)));
    }

    private static Mono<Event> webhooksUpdate(DispatchContext<WebhooksUpdate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());

        return Mono.just(new WebhooksUpdateEvent(context.getGateway(), context.getShardInfo(), guildId, channelId));
    }

    private static Mono<InviteCreateEvent> inviteCreate(DispatchContext<InviteCreate, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        String code = context.getDispatch().code();
        Instant createdAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .parse(context.getDispatch().createdAt(), Instant::from);
        int uses = context.getDispatch().uses();
        int maxUses = context.getDispatch().maxUses();
        int maxAge = context.getDispatch().maxAge();
        boolean temporary = context.getDispatch().temporary();

        User current = context.getDispatch().inviter().toOptional()
                .map(userData -> new User(context.getGateway(), userData))
                .orElse(null);

        return Mono.just(new InviteCreateEvent(context.getGateway(), context.getShardInfo(), guildId, channelId, code,
                current, createdAt, uses, maxUses, maxAge, temporary));
    }

    private static Mono<InviteDeleteEvent> inviteDelete(DispatchContext<InviteDelete, Void> context) {
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long channelId = Snowflake.asLong(context.getDispatch().channelId());
        String code = context.getDispatch().code();

        return Mono.just(new InviteDeleteEvent(context.getGateway(), context.getShardInfo(), guildId, channelId, code));
    }

    private static Mono<InteractionCreateEvent> interactionCreate(DispatchContext<InteractionCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        Interaction interaction = new Interaction(gateway, context.getDispatch().interaction());
        return Mono.just(new InteractionCreateEvent(gateway, context.getShardInfo(), interaction));
    }
}
