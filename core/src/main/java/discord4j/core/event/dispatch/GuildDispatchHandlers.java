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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.event.dispatch;

import discord4j.common.LogUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.guild.ScheduledEventCreateEvent;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.entity.*;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static discord4j.common.LogUtil.format;

class GuildDispatchHandlers {

    private static final Logger log = Loggers.getLogger(GuildDispatchHandlers.class);

    static Mono<BanEvent> guildBanAdd(DispatchContext<GuildBanAdd, Void> context) {
        User user = new User(context.getGateway(), context.getDispatch().user());
        long guildId = Snowflake.asLong(context.getDispatch().guildId());

        return Mono.just(new BanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<UnbanEvent> guildBanRemove(DispatchContext<GuildBanRemove, Void> context) {
        User user = new User(context.getGateway(), context.getDispatch().user());
        long guildId = Snowflake.asLong(context.getDispatch().guildId());

        return Mono.just(new UnbanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<GuildCreateEvent> guildCreate(DispatchContext<GuildCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        GuildCreateData createData;
        GuildData guild;
        if (context.getDispatch().guild().large()) {
            createData = GuildCreateData.builder()
                    .from(context.getDispatch().guild())
                    .members(Collections.emptyList())
                    .build();
            guild = GuildData.builder()
                    .from(createData)
                    .roles(createData.roles().stream().map(RoleData::id).collect(Collectors.toList()))
                    .emojis(createData.emojis().stream().map(EmojiData::id).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
                    .channels(createData.channels().stream().map(ChannelData::id).collect(Collectors.toList()))
                    .build();
        } else {
            createData = context.getDispatch().guild();
            guild = GuildData.builder()
                    .from(createData)
                    .roles(createData.roles().stream().map(RoleData::id).collect(Collectors.toList()))
                    .emojis(createData.emojis().stream().map(EmojiData::id).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
                    .members(createData.members().stream().map(data -> data.user().id()).distinct().collect(Collectors.toList()))
                    .channels(createData.channels().stream().map(ChannelData::id).collect(Collectors.toList()))
                    .build();
        }

        Mono<Void> asyncMemberChunk = Mono.create(sink -> sink.onRequest(__ -> {
            Context ctx = Context.of(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(gateway.hashCode()),
                    LogUtil.KEY_SHARD_ID, context.getShardInfo().getIndex());
            Disposable memberChunkTask = Mono.just(createData)
                    .filterWhen(context.getGateway().getGatewayResources().getMemberRequestFilter()::apply)
                    .flatMap(data -> {
                        log.debug(format(ctx, "Requesting members for guild {}"), createData.id());
                        return context.getGateway()
                                .requestMembers(Snowflake.of(data.id().asString()))
                                .then();
                    })
                    .subscribe(null, t -> log.warn(format(ctx, "Member request errored for {}"), createData.id(), t));
            sink.onCancel(memberChunkTask);
            sink.success();
        }));

        return asyncMemberChunk
                .thenReturn(new GuildCreateEvent(gateway, context.getShardInfo(), new Guild(gateway, guild)));
    }

    static Mono<GuildDeleteEvent> guildDelete(DispatchContext<GuildDelete, GuildData> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Snowflake.asLong(context.getDispatch().guild().id());
        boolean unavailable = context.getDispatch().guild().unavailable().toOptional()
                .orElse(false);
        Guild guild = context.getOldState().map(data -> new Guild(gateway, data)).orElse(null);

        return Mono.just(new GuildDeleteEvent(gateway, context.getShardInfo(), guildId, guild, unavailable));
    }

    static Mono<StickersUpdateEvent> guildStickersUpdate(DispatchContext<GuildStickersUpdate, Set<StickerData>> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());

        Set<GuildSticker> stickers = context.getDispatch().stickers()
            .stream()
            .map(sticker -> new GuildSticker(gateway, sticker, guildId))
            .collect(Collectors.toSet());

        Set<GuildSticker> oldStickers = context.getOldState()
            .map(oldState -> oldState.stream()
                .map(data -> new GuildSticker(gateway, data, guildId))
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());

        return Mono.just(new StickersUpdateEvent(gateway, context.getShardInfo(), guildId, stickers, oldStickers));
    }

    static Mono<EmojisUpdateEvent> guildEmojisUpdate(DispatchContext<GuildEmojisUpdate, Set<EmojiData>> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());

        Set<GuildEmoji> emojis = context.getDispatch().emojis()
                .stream()
                .map(emoji -> new GuildEmoji(gateway, emoji, guildId))
                .collect(Collectors.toSet());

        Set<GuildEmoji> oldEmojis = context.getOldState()
                .map(oldState -> oldState.stream()
                        .map(data -> new GuildEmoji(gateway, data, guildId))
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        return Mono.just(new EmojisUpdateEvent(gateway, context.getShardInfo(), guildId, emojis, oldEmojis));
    }

    static Mono<IntegrationsUpdateEvent> guildIntegrationsUpdate(DispatchContext<GuildIntegrationsUpdate, Void> context) {
        return Mono.just(new IntegrationsUpdateEvent(context.getGateway(), context.getShardInfo(),
                Snowflake.asLong(context.getDispatch().guildId())));
    }

    static Mono<MemberJoinEvent> guildMemberAdd(DispatchContext<GuildMemberAdd, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        MemberData member = context.getDispatch().member();

        return Mono.just(new MemberJoinEvent(gateway, context.getShardInfo(),
                        new Member(gateway, member, guildId), guildId));
    }

    static Mono<MemberLeaveEvent> guildMemberRemove(DispatchContext<GuildMemberRemove, MemberData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        UserData userData = context.getDispatch().user();

        User user = new User(gateway, userData);
        Member oldMember = context.getOldState().map(data -> new Member(gateway, data, guildId)).orElse(null);

        return Mono.just(new MemberLeaveEvent(gateway, context.getShardInfo(), user, guildId, oldMember));
    }

    static Mono<MemberChunkEvent> guildMembersChunk(DispatchContext<GuildMembersChunk, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        List<MemberData> members = context.getDispatch().members();
        int chunkIndex = context.getDispatch().chunkIndex();
        int chunkCount = context.getDispatch().chunkCount();
        List<Snowflake> notFound = context.getDispatch().notFound()
                .toOptional()
                .orElse(Collections.emptyList())
                .stream()
                .map(Snowflake::of)
                .collect(Collectors.toList());
        String nonce = context.getDispatch().nonce().toOptional().orElse(null);

        return Mono.just(new MemberChunkEvent(gateway, context.getShardInfo(), guildId,
                members.stream()
                    .map(member -> new Member(gateway, member, guildId))
                    .collect(Collectors.toSet()),
                chunkIndex, chunkCount, notFound, nonce));
    }

    static Mono<MemberUpdateEvent> guildMemberUpdate(DispatchContext<GuildMemberUpdate, MemberData> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long memberId = Snowflake.asLong(context.getDispatch().user().id());

        Set<Long> currentRoleIds = context.getDispatch().roles()
                .stream()
                .map(Snowflake::asLong)
                .collect(Collectors.toSet());
        String currentNick = Possible.flatOpt(context.getDispatch().nick()).orElse(null);
        String currentAvatar = context.getDispatch().avatar().orElse(null);
        String currentBanner = context.getDispatch().banner().orElse(null);
        String currentJoinedAt = context.getDispatch().joinedAt().orElse(null);
        String currentPremiumSince = Possible.flatOpt(context.getDispatch().premiumSince()).orElse(null);
        Boolean currentPending = context.getDispatch().pending().toOptional().orElse(null);
        String communicationDisabledUntil = Possible.flatOpt(context.getDispatch().communicationDisabledUntil()).orElse(null);
        AvatarDecoration avatarDecoration = Possible.flatOpt(context.getDispatch().avatarDecoration()).map(avatarDecorationData -> new AvatarDecoration(gateway, avatarDecorationData)).orElse(null);
        Member oldMember = context.getOldState()
                .map(data -> new Member(gateway, data, guildId))
                .orElse(null);

        return Mono.just(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, oldMember,
                currentRoleIds, currentNick, currentAvatar, currentBanner, currentJoinedAt,
                currentPremiumSince, currentPending, communicationDisabledUntil, avatarDecoration));
    }

    static Mono<RoleCreateEvent> guildRoleCreate(DispatchContext<GuildRoleCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        RoleData role = context.getDispatch().role();

        return Mono.just(new RoleCreateEvent(gateway, context.getShardInfo(), guildId,
                        new Role(gateway, role, guildId)));
    }

    static Mono<RoleDeleteEvent> guildRoleDelete(DispatchContext<GuildRoleDelete, RoleData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long roleId = Snowflake.asLong(context.getDispatch().roleId());

        Role oldRole = context.getOldState()
                .map(data -> new Role(gateway, data, guildId))
                .orElse(null);

        return Mono.just(new RoleDeleteEvent(gateway, context.getShardInfo(), guildId, roleId, oldRole));
    }

    static Mono<RoleUpdateEvent> guildRoleUpdate(DispatchContext<GuildRoleUpdate, RoleData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        RoleData role = context.getDispatch().role();
        Role current = new Role(gateway, role, guildId);

        Role oldRole = context.getOldState()
            .map(data -> new Role(gateway, data, guildId))
            .orElse(null);

        return Mono.just(new RoleUpdateEvent(gateway, context.getShardInfo(), current, oldRole));
    }

    static Mono<ScheduledEventCreateEvent> scheduledEventCreate(DispatchContext<GuildScheduledEventCreate, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        GuildScheduledEventData payload = context.getDispatch().scheduledEvent();
        ScheduledEvent scheduledEvent = new ScheduledEvent(gateway, payload);
        return Mono.just(new ScheduledEventCreateEvent(gateway, context.getShardInfo(), scheduledEvent));
    }

    static Mono<ScheduledEventUpdateEvent> scheduledEventUpdate(DispatchContext<GuildScheduledEventUpdate, GuildScheduledEventData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        GuildScheduledEventData payload = context.getDispatch().scheduledEvent();
        ScheduledEvent scheduledEvent = new ScheduledEvent(gateway, payload);
        ScheduledEvent oldScheduledEvent = context.getOldState()
                .map(data -> new ScheduledEvent(gateway, data))
                .orElse(null);
        return Mono.just(new ScheduledEventUpdateEvent(gateway, context.getShardInfo(), scheduledEvent, oldScheduledEvent));
    }

    static Mono<ScheduledEventDeleteEvent> scheduledEventDelete(DispatchContext<GuildScheduledEventDelete, GuildScheduledEventData> context) {
        GatewayDiscordClient gateway = context.getGateway();
        GuildScheduledEventData payload = context.getDispatch().scheduledEvent();
        ScheduledEvent scheduledEvent = new ScheduledEvent(gateway, payload);
        return Mono.just(new ScheduledEventDeleteEvent(gateway, context.getShardInfo(), scheduledEvent));
    }

    static Mono<ScheduledEventUserAddEvent> scheduledEventUserAdd(DispatchContext<GuildScheduledEventUserAdd, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        GuildScheduledEventUserAdd dispatch = context.getDispatch();
        return Mono.just(new ScheduledEventUserAddEvent(gateway, context.getShardInfo(),
                Snowflake.asLong(dispatch.guildId()),
                Snowflake.asLong(dispatch.scheduledEventId()),
                Snowflake.asLong(dispatch.userId())));
    }

    static Mono<ScheduledEventUserRemoveEvent> scheduledEventUserRemove(DispatchContext<GuildScheduledEventUserRemove, Void> context) {
        GatewayDiscordClient gateway = context.getGateway();
        GuildScheduledEventUserRemove dispatch = context.getDispatch();
        return Mono.just(new ScheduledEventUserRemoveEvent(gateway, context.getShardInfo(),
                Snowflake.asLong(dispatch.guildId()),
                Snowflake.asLong(dispatch.scheduledEventId()),
                Snowflake.asLong(dispatch.userId())));
    }

    static Mono<GuildUpdateEvent> guildUpdate(DispatchContext<GuildUpdate, GuildData> context) {
        GatewayDiscordClient gateway = context.getGateway();

        Guild oldGuild = context.getOldState()
                .map(data -> new Guild(gateway, data))
                .orElse(null);

        Guild newGuild = new Guild(gateway, GuildData.builder()
                .from(context.getOldState().orElseGet(() -> GuildData.builder()
                        // TODO fix this: signature requires Guild but we only have partial information
                        .joinedAt(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()))
                        .large(false)
                        .memberCount(context.getDispatch().guild().approximateMemberCount().toOptional().orElse(1))
                        .build()))
                .from(context.getDispatch().guild())
                .roles(context.getDispatch().guild().roles().stream()
                    .map(RoleData::id)
                    .collect(Collectors.toList()))
                .emojis(context.getDispatch().guild().emojis().stream()
                    .map(EmojiData::id)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()))
                .build());

        return Mono.just(new GuildUpdateEvent(gateway, context.getShardInfo(), newGuild, oldGuild));
    }

}
