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
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.presence.Status;
import discord4j.core.state.StateHolder;
import discord4j.core.util.ListUtil;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.LongObjTuple2;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static discord4j.common.LogUtil.format;

class GuildDispatchHandlers {

    private static final Logger log = Loggers.getLogger(GuildDispatchHandlers.class);

    static Mono<BanEvent> guildBanAdd(DispatchContext<GuildBanAdd> context) {
        User user = new User(context.getGateway(), context.getDispatch().user());
        long guildId = Snowflake.asLong(context.getDispatch().guildId());

        return Mono.just(new BanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<UnbanEvent> guildBanRemove(DispatchContext<GuildBanRemove> context) {
        User user = new User(context.getGateway(), context.getDispatch().user());
        long guildId = Snowflake.asLong(context.getDispatch().guildId());

        return Mono.just(new UnbanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<GuildCreateEvent> guildCreate(DispatchContext<GuildCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        Context c = buildContext(gateway, context.getShardInfo());
        GuildCreateData createData;
        GuildData guild;
        if (context.getDispatch().guild().large()) {
            // Solves https://github.com/Discord4J/Discord4J/issues/429
            // Member store cannot have duplicates because keys cannot
            // be duplicated, but array addition in GuildBean can
            //guildBean.setMembers(new long[0]);
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

        long guildId = Snowflake.asLong(guild.id());

        Mono<Void> saveGuild = context.getStateHolder().getGuildStore().save(guildId, guild)
                .doOnSubscribe(s -> log.trace(format(c, "GuildCreate doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildCreate doFinally {}: {}"), guildId, s));

        Mono<Void> saveChannels = context.getStateHolder().getChannelStore()
                .save(Flux.fromIterable(createData.channels())
                        .map(channel -> Tuples.of(Snowflake.asLong(channel.id()),
                                ChannelData.builder().from(channel).guildId(guild.id()).build())));

        Mono<Void> saveRoles = context.getStateHolder().getRoleStore()
                .save(Flux.fromIterable(createData.roles())
                        .map(role -> Tuples.of(Snowflake.asLong(role.id()), role)));

        Mono<Void> saveEmojis = context.getStateHolder().getGuildEmojiStore()
                .save(Flux.fromIterable(createData.emojis())
                        .map(emoji -> Tuples.of(Snowflake.asLong(emoji.id()
                                .orElseThrow(NoSuchElementException::new)), emoji)));

        Mono<Void> saveMembers = context.getStateHolder().getMemberStore()
                .save(Flux.fromIterable(createData.members())
                        .map(member -> Tuples.of(LongLongTuple2.of(guildId,
                                Snowflake.asLong(member.user().id())), member)));

        Mono<Void> saveUsers = context.getStateHolder().getUserStore()
                .save(Flux.fromIterable(createData.members())
                        .map(MemberData::user)
                        .map(user -> Tuples.of(Snowflake.asLong(user.id()), user)));

        Mono<Void> saveVoiceStates = context.getStateHolder().getVoiceStateStore()
                .save(Flux.fromIterable(createData.voiceStates())
                        .map(voiceState -> Tuples.of(LongLongTuple2.of(guildId,
                                Snowflake.asLong(voiceState.userId())), wrapVoiceState(voiceState, guild.id()))));

        Mono<Void> savePresences = context.getStateHolder().getPresenceStore()
                .save(Flux.fromIterable(createData.presences())
                        .map(presence -> Tuples.of(LongLongTuple2.of(guildId,
                                Snowflake.asLong(presence.user().id())), presence)));

        Mono<Void> saveOfflinePresences = Flux.fromIterable(createData.members())
                .filterWhen(member -> context.getStateHolder().getPresenceStore()
                        .find(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())))
                        .hasElement()
                        .map(id -> !id))
                .flatMap(member -> context.getStateHolder().getPresenceStore()
                        .save(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())),
                                createPresence(member)))
                .then();

        Mono<Void> asyncMemberChunk = Mono.create(sink -> sink.onRequest(__ -> {
            Context ctx = Context.of(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(gateway.hashCode()),
                    LogUtil.KEY_SHARD_ID, context.getShardInfo().getIndex());
            Disposable memberChunkTask = Mono.just(createData)
                    .filterWhen(context.getGateway().getGatewayResources().getMemberRequestFilter()::apply)
                    .flatMap(data -> {
                        log.debug(format(ctx, "Requesting members for guild {}"), createData.id());
                        int shardId = context.getShardInfo().getIndex();
                        return context.getGateway().getGatewayClientGroup().unicast(
                                ShardGatewayPayload.requestGuildMembers(
                                        RequestGuildMembers.builder()
                                                .guildId(data.id().asString())
                                                .query(Possible.of(""))
                                                .limit(0)
                                                .build(), shardId));
                    })
                    .subscribe(null, t -> log.warn(format(ctx, "Member request errored for {}"), createData.id(), t));
            sink.onCancel(memberChunkTask);
            sink.success();
        }));

        return saveGuild
                .and(saveChannels)
                .and(saveRoles)
                .and(saveEmojis)
                .and(saveMembers)
                .and(saveUsers)
                .and(saveVoiceStates)
                .and(savePresences)
                .and(saveOfflinePresences)
                .then(asyncMemberChunk)
                .thenReturn(new GuildCreateEvent(gateway, context.getShardInfo(), new Guild(gateway, guild)));
    }

    private static VoiceStateData wrapVoiceState(VoiceStateData voiceState, Id guildId) {
        return VoiceStateData.builder()
                .from(voiceState)
                .guildId(guildId)
                .build();
    }

    private static PresenceData createPresence(MemberData member) {
        return PresenceData.builder()
                .user(PartialUserData.builder()
                        .id(member.user().id())
                        .username(member.user().username())
                        .discriminator(member.user().discriminator())
                        .avatar(Possible.of(member.user().avatar()))
                        .bot(member.user().bot())
                        .system(member.user().system())
                        .mfaEnabled(member.user().mfaEnabled())
                        .locale(member.user().locale())
                        .verified(member.user().verified())
                        .email(member.user().email().isAbsent() ? Possible.absent() :
                                member.user().email().get().map(Possible::of).orElse(Possible.absent()))
                        .flags(member.user().flags())
                        .premiumType(member.user().premiumType())
                        .build())
                .status(Status.OFFLINE.getValue())
                .clientStatus(ClientStatusData.builder()
                        .desktop(Possible.absent())
                        .mobile(Possible.absent())
                        .web(Possible.absent())
                        .build())
                .build();
    }

    static Mono<GuildDeleteEvent> guildDelete(DispatchContext<GuildDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();

        long guildId = Snowflake.asLong(context.getDispatch().guild().id());
        boolean unavailable = context.getDispatch().guild().unavailable().toOptional()
                .orElse(false);

        Mono<Void> deleteGuild = stateHolder.getGuildStore().delete(guildId);

        return stateHolder.getGuildStore().find(guildId)
                .flatMap(guild -> {
                    Flux<Long> channels = Flux.fromIterable(guild.channels()).map(Snowflake::asLong);
                    Flux<Long> roles = Flux.fromIterable(guild.roles()).map(Snowflake::asLong);
                    Flux<Long> emojis = Flux.fromIterable(guild.emojis()).map(Snowflake::asLong);

                    Mono<Void> deleteChannels = stateHolder.getChannelStore().delete(channels);
                    Mono<Void> deleteRoles = stateHolder.getRoleStore().delete(roles);
                    Mono<Void> deleteEmojis = stateHolder.getGuildEmojiStore().delete(emojis);
                    Mono<Void> deleteMembers = stateHolder.getMemberStore()
                            .deleteInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, -1));
                    // TODO delete messages
                    // TODO delete no longer visible users
                    Mono<Void> deleteVoiceStates = stateHolder.getVoiceStateStore()
                            .deleteInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, -1));
                    Mono<Void> deletePresences = stateHolder.getPresenceStore()
                            .deleteInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, -1));

                    return deleteChannels
                            .and(deleteRoles)
                            .and(deleteEmojis)
                            .and(deleteMembers)
                            .and(deleteVoiceStates)
                            .and(deletePresences)
                            .thenReturn(guild);
                })
                .flatMap(deleteGuild::thenReturn)
                .map(data -> {
                    Guild guild = new Guild(context.getGateway(), data);
                    return new GuildDeleteEvent(gateway, context.getShardInfo(), guildId, guild, unavailable);
                })
                .switchIfEmpty(deleteGuild.thenReturn(new GuildDeleteEvent(gateway, context.getShardInfo(), guildId,
                        null, unavailable)));
    }

    static Mono<EmojisUpdateEvent> guildEmojisUpdate(DispatchContext<GuildEmojisUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        Context c = buildContext(gateway, context.getShardInfo());

        Mono<Void> updateGuildBean = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> GuildData.builder()
                        .from(guild)
                        .emojis(context.getDispatch().emojis().stream()
                                .map(EmojiData::id)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild))
                .doOnSubscribe(s -> log.trace(format(c, "GuildEmojisUpdate doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildEmojisUpdate doFinally {}: {}"), guildId, s));

        Mono<Void> saveEmojis = context.getStateHolder().getGuildEmojiStore()
                .saveWithLong(Flux.fromIterable(context.getDispatch().emojis())
                        .map(emoji -> LongObjTuple2.of(emoji.id()
                                .map(Snowflake::asLong)
                                .orElseThrow(NoSuchElementException::new), emoji)));

        Set<GuildEmoji> emojis = context.getDispatch().emojis()
                .stream()
                .map(emoji -> new GuildEmoji(gateway, emoji, guildId))
                .collect(Collectors.toSet());

        return context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMap(guild -> Flux.fromIterable(guild.emojis()).map(Snowflake::asLong)
                        .flatMap(id -> context.getStateHolder().getGuildEmojiStore().find(id))
                        .map(emoji -> new GuildEmoji(gateway, emoji, guildId))
                        .collect(Collectors.toSet())
                ).flatMap(old -> updateGuildBean
                        .and(saveEmojis)
                        .thenReturn(new EmojisUpdateEvent(gateway, context.getShardInfo(), guildId, emojis, old)))
                .defaultIfEmpty(new EmojisUpdateEvent(gateway, context.getShardInfo(), guildId, emojis, null));
    }

    static Mono<IntegrationsUpdateEvent> guildIntegrationsUpdate(DispatchContext<GuildIntegrationsUpdate> context) {
        return Mono.just(new IntegrationsUpdateEvent(context.getGateway(), context.getShardInfo(),
                Snowflake.asLong(context.getDispatch().guildId())));
    }

    static Mono<MemberJoinEvent> guildMemberAdd(DispatchContext<GuildMemberAdd> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        MemberData member = context.getDispatch().member();
        UserData user = member.user();
        long userId = Snowflake.asLong(user.id());
        Context c = buildContext(gateway, context.getShardInfo());

        Mono<Void> addMemberId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> GuildData.builder()
                        .from(guild)
                        .members(ListUtil.add(guild.members(), member.user().id()))
                        .memberCount(guild.memberCount() + 1)
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild))
                .doOnSubscribe(s -> log.trace(format(c, "GuildMemberAdd doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildMemberAdd doFinally {}: {}"), guildId, s));

        Mono<Void> saveMember = context.getStateHolder().getMemberStore()
                .save(LongLongTuple2.of(guildId, userId), member);

        Mono<Void> saveUser = context.getStateHolder().getUserStore()
                .save(userId, user);

        return addMemberId
                .and(saveMember)
                .and(saveUser)
                .thenReturn(new MemberJoinEvent(gateway, context.getShardInfo(),
                        new Member(gateway, member, guildId), guildId));
    }

    static Mono<MemberLeaveEvent> guildMemberRemove(DispatchContext<GuildMemberRemove> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        UserData userData = context.getDispatch().user();
        long userId = Snowflake.asLong(userData.id());
        Context c = buildContext(gateway, context.getShardInfo());

        Mono<Void> removeMemberId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> GuildData.builder()
                        .from(guild)
                        .members(ListUtil.remove(guild.members(), member -> member.equals(userData.id())))
                        .memberCount(guild.memberCount() - 1)
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild))
                .doOnSubscribe(s -> log.trace(format(c, "GuildMemberRemove doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildMemberRemove doFinally {}: {}"), guildId, s));

        Mono<Member> member = context.getStateHolder().getMemberStore()
                .find(LongLongTuple2.of(guildId, userId))
                .map(data -> new Member(gateway, data, guildId));

        Mono<Void> deleteMember = context.getStateHolder().getMemberStore()
                .delete(LongLongTuple2.of(guildId, userId));

        Mono<Void> deletePresence = context.getStateHolder().getPresenceStore()
                .delete(LongLongTuple2.of(guildId, userId));

        Mono<Void> deleteOrphanUser = context.getStateHolder().getMemberStore()
                .keys().filter(key -> key.getT1() != guildId && key.getT2() == userId)
                .hasElements()
                .flatMap(hasMutualServers -> Mono.just(userId)
                        .filter(__ -> !hasMutualServers)
                        .flatMap(context.getStateHolder().getUserStore()::delete));

        User user = new User(gateway, userData);

        return member.map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(value -> Mono.when(removeMemberId, deleteMember, deletePresence, deleteOrphanUser).thenReturn(value))
                .map(m -> new MemberLeaveEvent(gateway, context.getShardInfo(), user, guildId, m.orElse(null)));
    }

    static Mono<MemberChunkEvent> guildMembersChunk(DispatchContext<GuildMembersChunk> context) {
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
        Context c = buildContext(gateway, context.getShardInfo());

        Flux<Tuple2<LongLongTuple2, MemberData>> memberPairs = Flux.fromIterable(members)
                .map(data -> Tuples.of(LongLongTuple2.of(guildId, Snowflake.asLong(data.user().id())),
                        data));

        Flux<Tuple2<Long, UserData>> userPairs = Flux.fromIterable(members)
                .map(data -> Tuples.of(Snowflake.asLong(data.user().id()), data.user()));

        Mono<Void> addMemberIds = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> GuildData.builder()
                        .from(guild)
                        .members(ListUtil.addAllDistinct(guild.members(), members.stream()
                                .map(data -> data.user().id())
                                .collect(Collectors.toList())))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild))
                .doOnSubscribe(s -> log.trace(format(c, "GuildMembersChunk doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildMembersChunk doFinally {}: {}"), guildId, s));

        Mono<Void> saveMembers = context.getStateHolder().getMemberStore().save(memberPairs);

        Mono<Void> saveUsers = context.getStateHolder().getUserStore().save(userPairs);

        Mono<Void> saveOfflinePresences = Flux.fromIterable(members)
                .filterWhen(member -> context.getStateHolder().getPresenceStore()
                        .find(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())))
                        .hasElement()
                        .map(identity -> !identity))
                .flatMap(member -> context.getStateHolder().getPresenceStore()
                        .save(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())),
                                createPresence(member)))
                .then();

        return addMemberIds
                .and(saveMembers)
                .and(saveUsers)
                .and(saveOfflinePresences)
                .thenReturn(new MemberChunkEvent(gateway, context.getShardInfo(), guildId,
                        members.stream()
                                .map(member -> new Member(gateway, member, guildId))
                                .collect(Collectors.toSet()),
                        chunkIndex, chunkCount, notFound, nonce));
    }

    static Mono<MemberUpdateEvent> guildMemberUpdate(DispatchContext<GuildMemberUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long memberId = Snowflake.asLong(context.getDispatch().user().id());

        List<Long> currentRoles = context.getDispatch().roles()
                .stream()
                .map(Snowflake::asLong)
                .collect(Collectors.toList());
        String currentNick = Possible.flatOpt(context.getDispatch().nick()).orElse(null);
        String currentJoinedAt = context.getDispatch().joinedAt();
        String currentPremiumSince = Possible.flatOpt(context.getDispatch().premiumSince()).orElse(null);
        Boolean currentPending = context.getDispatch().pending().toOptional().orElse(null);

        LongLongTuple2 key = LongLongTuple2.of(guildId, memberId);

        Mono<MemberUpdateEvent> update = context.getStateHolder().getMemberStore()
                .find(key)
                .flatMap(oldMember -> {
                    Member old = new Member(gateway, oldMember, guildId);

                    GuildMemberUpdate dispatch = context.getDispatch();
                    MemberData newMember = MemberData.builder()
                            .from(oldMember)
                            .roles(dispatch.roles().stream().map(Id::of).collect(Collectors.toList()))
                            .user(dispatch.user())
                            .nick(dispatch.nick())
                            .joinedAt(dispatch.joinedAt())
                            .premiumSince(dispatch.premiumSince())
                            .pending(dispatch.pending())
                            .build();

                    return context.getStateHolder().getMemberStore()
                            .save(key, newMember)
                            .thenReturn(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, old,
                                    currentRoles, currentNick, currentJoinedAt, currentPremiumSince, currentPending));
                });

        return update.defaultIfEmpty(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, null,
                currentRoles, currentNick, currentJoinedAt, currentPremiumSince, currentPending));
    }

    static Mono<RoleCreateEvent> guildRoleCreate(DispatchContext<GuildRoleCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        RoleData role = context.getDispatch().role();
        Context c = buildContext(gateway, context.getShardInfo());

        Mono<Void> addRoleId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> GuildData.builder()
                        .from(guild)
                        .addRole(role.id())
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild))
                .doOnSubscribe(s -> log.trace(format(c, "GuildRoleCreate doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildRoleCreate doFinally {}: {}"), guildId, s));

        Mono<Void> saveRole = context.getStateHolder().getRoleStore()
                .save(Snowflake.asLong(role.id()), role);

        return addRoleId
                .and(saveRole)
                .thenReturn(new RoleCreateEvent(gateway, context.getShardInfo(), guildId,
                        new Role(gateway, role, guildId)));
    }

    static Mono<RoleDeleteEvent> guildRoleDelete(DispatchContext<GuildRoleDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        long roleId = Snowflake.asLong(context.getDispatch().roleId());
        Context c = buildContext(gateway, context.getShardInfo());

        @SuppressWarnings("ReactiveStreamsUnusedPublisher")
        Mono<Void> removeRoleId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> GuildData.builder()
                        .from(guild)
                        .roles(ListUtil.remove(guild.roles(), role -> role.equals(context.getDispatch().roleId())))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild))
                .doOnSubscribe(s -> log.trace(format(c, "GuildRoleDelete doOnSubscribe {}"), guildId))
                .doFinally(s -> log.trace(format(c, "GuildRoleDelete doFinally {}: {}"), guildId, s));

        Mono<Void> deleteRole = context.getStateHolder().getRoleStore().delete(roleId);

        @SuppressWarnings("ReactiveStreamsUnusedPublisher")
        Mono<Void> removeRoleFromMembers = context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMapMany(guild -> Flux.fromIterable(guild.members())
                        .map(Snowflake::asLong))
                .flatMap(memberId -> context.getStateHolder().getMemberStore()
                        .find(LongLongTuple2.of(guildId, memberId)))
                .filter(member -> member.roles().contains(context.getDispatch().roleId()))
                .map(member -> MemberData.builder()
                        .from(member)
                        .roles(ListUtil.remove(member.roles(),
                                role -> role.equals(context.getDispatch().roleId())))
                        .build())
                .flatMap(member -> context.getStateHolder().getMemberStore()
                        .save(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())), member))
                .then();

        return context.getStateHolder().getRoleStore()
                .find(roleId)
                .flatMap(removeRoleId::thenReturn)
                .flatMap(deleteRole::thenReturn)
                .flatMap(removeRoleFromMembers::thenReturn)
                .map(role -> new RoleDeleteEvent(gateway, context.getShardInfo(), guildId, roleId,
                        new Role(gateway, role, guildId)))
                .defaultIfEmpty(new RoleDeleteEvent(gateway, context.getShardInfo(), guildId, roleId, null));
    }

    static Mono<RoleUpdateEvent> guildRoleUpdate(DispatchContext<GuildRoleUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guildId());
        RoleData role = context.getDispatch().role();
        long roleId = Snowflake.asLong(role.id());
        Role current = new Role(gateway, role, guildId);

        Mono<Void> saveNew = context.getStateHolder().getRoleStore().save(roleId, role);

        return context.getStateHolder().getRoleStore()
                .find(roleId)
                .flatMap(saveNew::thenReturn)
                .map(old -> new RoleUpdateEvent(gateway, context.getShardInfo(), current,
                        new Role(gateway, old, guildId)))
                .switchIfEmpty(saveNew.thenReturn(new RoleUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    static Mono<GuildUpdateEvent> guildUpdate(DispatchContext<GuildUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Snowflake.asLong(context.getDispatch().guild().id());
        Context c = buildContext(gateway, context.getShardInfo());

        return context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMap(oldGuildData -> {
                    GuildData newGuildData = GuildData.builder()
                            .from(oldGuildData)
                            .from(context.getDispatch().guild())
                            .roles(context.getDispatch().guild().roles().stream()
                                    .map(RoleData::id)
                                    .collect(Collectors.toList()))
                            .emojis(context.getDispatch().guild().emojis().stream()
                                    .map(EmojiData::id)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                            .build();

                    Guild old = new Guild(gateway, oldGuildData);
                    Guild current = new Guild(gateway, newGuildData);

                    return context.getStateHolder().getGuildStore()
                            .save(guildId, newGuildData)
                            .doOnSubscribe(s -> log.trace(format(c, "GuildUpdate doOnSubscribe {}"), guildId))
                            .doFinally(s -> log.trace(format(c, "GuildUpdate doFinally {}: {}"), guildId, s))
                            .thenReturn(new GuildUpdateEvent(gateway, context.getShardInfo(), current, old));
                })
                .switchIfEmpty(Mono.fromCallable(() -> new GuildUpdateEvent(gateway, context.getShardInfo(),
                        new Guild(gateway, GuildData.builder()
                                .from(context.getDispatch().guild())
                                .roles(context.getDispatch().guild().roles().stream()
                                        .map(RoleData::id)
                                        .collect(Collectors.toList()))
                                .emojis(context.getDispatch().guild().emojis().stream()
                                        .map(EmojiData::id)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList()))
                                // TODO fix this: signature requires Guild but we only have partial information
                                .joinedAt(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()))
                                .large(false)
                                .memberCount(context.getDispatch().guild().approximateMemberCount().toOptional().orElse(1))
                                .build()), null)));
    }

    private static Context buildContext(GatewayDiscordClient gateway, ShardInfo shard) {
        return Context.of(LogUtil.KEY_GATEWAY_ID, Integer.toHexString(gateway.hashCode()))
                .put(LogUtil.KEY_SHARD_ID, shard.getIndex());
    }

}
