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

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.presence.Status;
import discord4j.core.state.StateHolder;
import discord4j.core.util.ListUtil;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.json.ShardGatewayPayload;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.LongObjTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

class GuildDispatchHandlers {

    static Mono<BanEvent> guildBanAdd(DispatchContext<GuildBanAdd> context) {
        User user = new User(context.getGateway(), context.getDispatch().user());
        long guildId = Long.parseUnsignedLong(context.getDispatch().guild());

        return Mono.just(new BanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<UnbanEvent> guildBanRemove(DispatchContext<GuildBanRemove> context) {
        User user = new User(context.getGateway(), context.getDispatch().user());
        long guildId = Long.parseUnsignedLong(context.getDispatch().guild());

        return Mono.just(new UnbanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<GuildCreateEvent> guildCreate(DispatchContext<GuildCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        GuildData guild;
        if (context.getDispatch().guild().large().get()) {
            // Solves https://github.com/Discord4J/Discord4J/issues/429
            // Member store cannot have duplicates because keys cannot
            // be duplicated, but array addition in GuildBean can
            //guildBean.setMembers(new long[0]);
            guild = ImmutableGuildData.builder()
                    .from(context.getDispatch().guild())
                    .members(Possible.of(Collections.emptyList()))
                    .build();
        } else {
            guild = context.getDispatch().guild();
        }

        long guildId = Long.parseUnsignedLong(guild.id());

        Mono<Void> saveGuild = context.getStateHolder().getGuildStore().save(guildId, guild);
        // TODO optimize to separate into three Publisher<Channel> and saveAll to limit store hits
        Mono<Void> saveChannels = Flux.fromIterable(guild.channels().get())
                .flatMap(channel -> context.getStateHolder().getChannelStore()
                        .save(Long.parseUnsignedLong(channel.id()), ImmutableChannelData.builder()
                                .from(channel)
                                .guildId(Possible.of(guild.id()))
                                .build()))
                .then();

        Mono<Void> saveRoles = context.getStateHolder().getRoleStore()
                .save(Flux.fromIterable(guild.roles())
                        .map(role -> Tuples.of(Long.parseUnsignedLong(role.id()), role)));

        Mono<Void> saveEmojis = context.getStateHolder().getGuildEmojiStore()
                .save(Flux.fromIterable(guild.emojis())
                        .map(emoji -> Tuples.of(Long.parseUnsignedLong(emoji.id().orElseThrow(NoSuchElementException::new)), emoji)));

        Mono<Void> saveMembers = context.getStateHolder().getMemberStore()
                .save(Flux.fromIterable(guild.members().get())
                        .map(member -> Tuples.of(LongLongTuple2.of(guildId,
                                Long.parseUnsignedLong(member.user().id())), member)));

        Mono<Void> saveUsers = context.getStateHolder().getUserStore()
                .save(Flux.fromIterable(guild.members().get())
                        .map(MemberData::user)
                        .map(user -> Tuples.of(Long.parseUnsignedLong(user.id()), user)));

        Mono<Void> saveVoiceStates = context.getStateHolder().getVoiceStateStore()
                .save(Flux.fromIterable(guild.voiceStates().get())
                        .map(voiceState -> Tuples.of(LongLongTuple2.of(guildId,
                                Long.parseUnsignedLong(voiceState.userId())), voiceState)));

        Mono<Void> savePresences = context.getStateHolder().getPresenceStore()
                .save(Flux.fromIterable(guild.presences().get())
                        .map(presence -> Tuples.of(LongLongTuple2.of(guildId,
                                Long.parseUnsignedLong(presence.user().id())), presence)));

        Mono<Void> startMemberChunk = context.getGateway().getGatewayResources().isMemberRequest() ?
                Mono.just(guild)
                        .filter(data -> data.large().get())
                        .flatMap(data -> {
                            int shardId = context.getShardInfo().getIndex();
                            return context.getGateway().getGatewayClientGroup().unicast(
                                    ShardGatewayPayload.requestGuildMembers(
                                            ImmutableRequestGuildMembers.builder()
                                                    .guildId(data.id())
                                                    .query(Possible.of(""))
                                                    .limit(0)
                                                    .build(), shardId));
                        })
                        .then() : Mono.empty();

        Mono<Void> saveOfflinePresences = Flux.fromIterable(guild.members().get())
                .filterWhen(member -> context.getStateHolder().getPresenceStore()
                        .find(LongLongTuple2.of(guildId, Long.parseUnsignedLong(member.user().id())))
                        .hasElement()
                        .map(id -> !id))
                .flatMap(member -> context.getStateHolder().getPresenceStore()
                        .save(LongLongTuple2.of(guildId, Long.parseUnsignedLong(member.user().id())),
                                createPresence(member)))
                .then();

        return saveGuild
                .and(saveChannels)
                .and(saveRoles)
                .and(saveEmojis)
                .and(saveMembers)
                .and(saveUsers)
                .and(saveVoiceStates)
                .and(savePresences)
                .and(saveOfflinePresences)
                .and(startMemberChunk)
                .thenReturn(new GuildCreateEvent(gateway, context.getShardInfo(), new Guild(gateway, guild)));
    }

    private static PresenceData createPresence(MemberData member) {
        return ImmutablePresenceData.builder()
                .user(ImmutablePartialUserData.builder()
                        .id(member.user().id())
                        .username(Possible.of(member.user().username()))
                        .discriminator(Possible.of(member.user().discriminator()))
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
                .clientStatus(ImmutableClientStatusData.builder()
                        .desktop(Possible.absent())
                        .mobile(Possible.absent())
                        .web(Possible.absent())
                        .build())
                .build();
    }

    static Mono<GuildDeleteEvent> guildDelete(DispatchContext<GuildDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();

        long guildId = Long.parseUnsignedLong(context.getDispatch().guild().id());
        boolean unavailable = context.getDispatch().guild().unavailable().get();

        Mono<Void> deleteGuild = stateHolder.getGuildStore().delete(guildId);

        return stateHolder.getGuildStore().find(guildId)
                .flatMap(guild -> {
                    Flux<Long> channels = Flux.fromIterable(guild.channels().get())
                            .map(it -> Long.parseUnsignedLong(it.id()));
                    Flux<Long> roles = Flux.fromIterable(guild.roles())
                            .map(it -> Long.parseUnsignedLong(it.id()));
                    Flux<Long> emojis = Flux.fromIterable(guild.emojis())
                            .map(it -> Long.parseUnsignedLong(it.id().orElseThrow(NoSuchElementException::new)));

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
        long guildId = Long.parseUnsignedLong(context.getDispatch().guild());

        Mono<Void> updateGuildBean = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> ImmutableGuildData.builder()
                        .from(guild)
                        .emojis(context.getDispatch().emojis())
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> saveEmojis = context.getStateHolder().getGuildEmojiStore()
                .saveWithLong(Flux.fromIterable(context.getDispatch().emojis())
                        .map(emoji -> LongObjTuple2.of(emoji.id()
                                .map(Long::parseUnsignedLong)
                                .orElseThrow(NoSuchElementException::new), emoji)));

        Set<GuildEmoji> emojis = context.getDispatch().emojis()
                .stream()
                .map(emoji -> new GuildEmoji(gateway, emoji, guildId))
                .collect(Collectors.toSet());

        return updateGuildBean
                .and(saveEmojis)
                .thenReturn(new EmojisUpdateEvent(gateway, context.getShardInfo(), guildId, emojis));
    }

    static Mono<IntegrationsUpdateEvent> guildIntegrationsUpdate(DispatchContext<GuildIntegrationsUpdate> context) {
        return Mono.just(new IntegrationsUpdateEvent(context.getGateway(), context.getShardInfo(),
                Long.parseUnsignedLong(context.getDispatch().guild())));
    }

    static Mono<MemberJoinEvent> guildMemberAdd(DispatchContext<GuildMemberAdd> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Long.parseUnsignedLong(context.getDispatch().guild());
        MemberData member = context.getDispatch().member();
        UserData user = member.user();
        long userId = Long.parseUnsignedLong(user.id());

        Mono<Void> addMemberId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> ImmutableGuildData.builder()
                        .from(guild)
                        .members(ListUtil.add(guild.members(), member))
                        .memberCount(guild.memberCount().toOptional()
                                .map(count -> Possible.of(count + 1))
                                .orElse(Possible.of(1)))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

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
        long guildId = Long.parseUnsignedLong(context.getDispatch().guild());
        UserData user = context.getDispatch().user();
        long userId = Long.parseUnsignedLong(user.id());

        Mono<Void> removeMemberId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> ImmutableGuildData.builder()
                        .from(guild)
                        .members(ListUtil.remove(guild.members(), member -> member.user().id().equals(user.id())))
                        .memberCount(guild.memberCount().toOptional()
                                .map(count -> Possible.of(count - 1))
                                .orElse(Possible.of(0)))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Member> member = context.getStateHolder().getMemberStore()
                .find(LongLongTuple2.of(guildId, userId))
                .map(data -> new Member(gateway, data, guildId));

        Mono<Void> deleteMember = context.getStateHolder().getMemberStore()
                .delete(LongLongTuple2.of(guildId, userId));

        return member.map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(value -> Mono.when(removeMemberId, deleteMember).thenReturn(value))
                .map(m -> new MemberLeaveEvent(gateway, context.getShardInfo(),
                        new User(gateway, user), guildId, m.orElse(null)));
    }

    static Mono<MemberChunkEvent> guildMembersChunk(DispatchContext<GuildMembersChunk> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        List<MemberData> members = context.getDispatch().members();

        Flux<Tuple2<LongLongTuple2, MemberData>> memberPairs = Flux.fromIterable(members)
                .map(data -> Tuples.of(LongLongTuple2.of(guildId, Long.parseUnsignedLong(data.user().id())),
                        data));

        Flux<Tuple2<Long, UserData>> userPairs = Flux.fromIterable(members)
                .map(data -> Tuples.of(Long.parseUnsignedLong(data.user().id()), data.user()));

        Mono<Void> addMemberIds = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> ImmutableGuildData.builder()
                        .from(guild)
                        .members(ListUtil.addAll(guild.members(), members))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> saveMembers = context.getStateHolder().getMemberStore().save(memberPairs);

        Mono<Void> saveUsers = context.getStateHolder().getUserStore().save(userPairs);

        Mono<Void> saveOfflinePresences = Flux.fromIterable(members)
                .filterWhen(member -> context.getStateHolder().getPresenceStore()
                        .find(LongLongTuple2.of(guildId, Long.parseUnsignedLong(member.user().id())))
                        .hasElement()
                        .map(identity -> !identity))
                .flatMap(member -> context.getStateHolder().getPresenceStore()
                        .save(LongLongTuple2.of(guildId, Long.parseUnsignedLong(member.user().id())),
                                createPresence(member)))
                .then();

        return addMemberIds
                .and(saveMembers)
                .and(saveUsers)
                .and(saveOfflinePresences)
                .thenReturn(new MemberChunkEvent(gateway, context.getShardInfo(), guildId,
                        members.stream()
                                .map(member -> new Member(gateway, member, guildId))
                                .collect(Collectors.toSet())));
    }

    static Mono<MemberUpdateEvent> guildMemberUpdate(DispatchContext<GuildMemberUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        long memberId = Long.parseUnsignedLong(context.getDispatch().user().id());

        List<Long> currentRoles = context.getDispatch().roles()
                .stream()
                .map(Long::parseUnsignedLong)
                .collect(Collectors.toList());
        String currentNick = context.getDispatch().nick().orElse(null);
        String currentPremiumSince = context.getDispatch().premiumSince().orElse(null);

        LongLongTuple2 key = LongLongTuple2.of(guildId, memberId);

        Mono<MemberUpdateEvent> update = context.getStateHolder().getMemberStore()
                .find(key)
                .flatMap(oldMember -> {
                    Member old = new Member(gateway, oldMember, guildId);

                    MemberData newMember = ImmutableMemberData.builder()
                            .from(oldMember)
                            .nick(Possible.of(context.getDispatch().nick()))
                            .roles(context.getDispatch().roles())
                            .premiumSince(context.getDispatch().premiumSince())
                            .build();

                    return context.getStateHolder().getMemberStore()
                            .save(key, newMember)
                            .thenReturn(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, old,
                                    currentRoles, currentNick, currentPremiumSince));
                });

        // TODO: fix premium_since support
        return update.defaultIfEmpty(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, null,
                currentRoles, currentNick, null));
    }

    static Mono<RoleCreateEvent> guildRoleCreate(DispatchContext<GuildRoleCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        RoleData role = context.getDispatch().role();

        Mono<Void> addRoleId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(guild -> ImmutableGuildData.builder()
                        .from(guild)
                        .addRoles(role)
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> saveRole = context.getStateHolder().getRoleStore()
                .save(Long.parseUnsignedLong(role.id()), role);

        return addRoleId
                .and(saveRole)
                .thenReturn(new RoleCreateEvent(gateway, context.getShardInfo(), guildId,
                        new Role(gateway, role, guildId)));
    }

    static Mono<RoleDeleteEvent> guildRoleDelete(DispatchContext<GuildRoleDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        long roleId = Long.parseUnsignedLong(context.getDispatch().roleId());

        @SuppressWarnings("ReactorUnusedPublisher")
        Mono<Void> removeRoleId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .zipWhen(guild -> Mono.just(removeRoleFromGuildMember(guild.members(), context.getDispatch().roleId())))
                .map(t2 -> ImmutableGuildData.builder()
                        .from(t2.getT1())
                        .roles(ListUtil.remove(t2.getT1().roles(),
                                role -> role.id().equals(context.getDispatch().roleId())))
                        .members(Possible.of(t2.getT2()))
                        .build())
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> deleteRole = context.getStateHolder().getRoleStore().delete(roleId);

        @SuppressWarnings("ReactorUnusedPublisher")
        Mono<Void> removeRoleFromMembers = context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMapMany(guild -> Flux.fromIterable(guild.members().toOptional()
                        .map(memberList -> memberList.stream()
                                .map(member -> Long.parseUnsignedLong(member.user().id()))
                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList())))
                .flatMap(memberId -> context.getStateHolder().getMemberStore()
                        .find(LongLongTuple2.of(guildId, memberId)))
                .filter(member -> member.roles().contains(context.getDispatch().roleId()))
                .map(member -> ImmutableMemberData.builder()
                        .from(member)
                        .roles(ListUtil.remove(member.roles(),
                                role -> role.equals(context.getDispatch().roleId())))
                        .build())
                .flatMap(member -> context.getStateHolder().getMemberStore()
                        .save(LongLongTuple2.of(guildId, Long.parseUnsignedLong(member.user().id())), member))
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

    static List<MemberData> removeRoleFromGuildMember(Possible<List<MemberData>> members, String roleId) {
        if (members.isAbsent()) {
            return Collections.emptyList();
        } else {
            return members.get().stream()
                    .filter(member -> member.roles().contains(roleId))
                    .map(member -> ImmutableMemberData.builder()
                            .from(member)
                            .roles(ListUtil.remove(member.roles(), role -> role.equals(roleId)))
                            .build())
                    .collect(Collectors.toList());
        }
    }

    static Mono<RoleUpdateEvent> guildRoleUpdate(DispatchContext<GuildRoleUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = Long.parseUnsignedLong(context.getDispatch().guildId());
        RoleData role = context.getDispatch().role();
        long roleId = Long.parseUnsignedLong(role.id());
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
        GuildData guild = toGuildData(context.getDispatch().guild());
        long guildId = Long.parseUnsignedLong(guild.id());

        Mono<GuildUpdateEvent> update = context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMap(oldGuildData -> {
                    Guild old = new Guild(gateway, oldGuildData);
                    Guild current = new Guild(gateway, guild);

                    return context.getStateHolder().getGuildStore()
                            .save(guildId, guild)
                            .thenReturn(new GuildUpdateEvent(gateway, context.getShardInfo(), current, old));
                });

        return update.defaultIfEmpty(new GuildUpdateEvent(gateway, context.getShardInfo(),
                new Guild(gateway, guild), null));
    }

    private static GuildData toGuildData(PartialGuildData guild) {
        return ImmutableGuildData.builder()
                .id(guild.id())
                .name(guild.name())
                .icon(guild.icon())
                .splash(guild.splash())
                .discoverySplash(guild.discoverySplash())
                .owner(guild.owner())
                .ownerId(guild.ownerId())
                .permissions(guild.permissions())
                .region(guild.region())
                .afkChannelId(guild.afkChannelId())
                .afkTimeout(guild.afkTimeout())
                .embedEnabled(guild.embedEnabled())
                .embedChannelId(guild.embedChannelId())
                .verificationLevel(guild.verificationLevel())
                .defaultMessageNotifications(guild.defaultMessageNotifications())
                .explicitContentFilter(guild.explicitContentFilter())
                .roles(guild.roles())
                .emojis(guild.emojis())
                .features(guild.features())
                .mfaLevel(guild.mfaLevel())
                .applicationId(guild.applicationId())
                .widgetEnabled(guild.widgetEnabled())
                .widgetChannelId(guild.widgetChannelId())
                .systemChannelId(guild.systemChannelId())
                .systemChannelFlags(guild.systemChannelFlags())
                .rulesChannelId(guild.rulesChannelId())
                .maxPresences(guild.maxPresences())
                .maxMembers(guild.maxMembers())
                .vanityUrlCode(guild.vanityUrlCode())
                .description(guild.description())
                .banner(guild.banner())
                .premiumTier(guild.premiumTier())
                .premiumSubscriptionCount(guild.premiumSubscriptionCount())
                .preferredLocale(guild.preferredLocale())
                .publicUpdatesChannelId(guild.publicUpdatesChannelId())
                .build();
    }

}
