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

import discord4j.common.json.GuildEmojiResponse;
import discord4j.common.json.GuildMemberResponse;
import discord4j.common.json.UserResponse;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.StateHolder;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.data.stored.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import discord4j.core.util.ArrayUtil;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.json.RequestGuildMembers;
import discord4j.gateway.json.dispatch.*;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.LongObjTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

class GuildDispatchHandlers {

    static Mono<BanEvent> guildBanAdd(DispatchContext<GuildBanAdd> context) {
        User user = new User(context.getGateway(), new UserBean(context.getDispatch().getUser()));
        long guildId = context.getDispatch().getGuildId();

        return Mono.just(new BanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<UnbanEvent> guildBanRemove(DispatchContext<GuildBanRemove> context) {
        User user = new User(context.getGateway(), new UserBean(context.getDispatch().getUser()));
        long guildId = context.getDispatch().getGuildId();

        return Mono.just(new UnbanEvent(context.getGateway(), context.getShardInfo(), user, guildId));
    }

    static Mono<GuildCreateEvent> guildCreate(DispatchContext<GuildCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        GuildBean guildBean = new GuildBean(context.getDispatch());
        if (guildBean.getLarge()) {
            // Solves https://github.com/Discord4J/Discord4J/issues/429
            // Member store cannot have duplicates because keys cannot
            // be duplicated, but array addition in GuildBean can
            guildBean.setMembers(new long[0]);
        }

        Mono<Void> saveGuild = context.getStateHolder().getGuildStore().save(guildBean.getId(), guildBean);
        // TODO optimize to separate into three Publisher<Channel> and saveAll to limit store hits
        Mono<Void> saveChannels = Flux.just(context.getDispatch().getChannels()).flatMap(channel -> {
            ChannelBean channelBean = new ChannelBean(channel, guildBean.getId());
            return context.getStateHolder().getChannelStore().save(channel.getId(), channelBean);
        }).then();

        Mono<Void> saveRoles = context.getStateHolder().getRoleStore()
                .save(Flux.just(context.getDispatch().getRoles())
                        .map(role -> Tuples.of(role.getId(), new RoleBean(role))));

        Mono<Void> saveEmojis = context.getStateHolder().getGuildEmojiStore()
                .save(Flux.just(context.getDispatch().getEmojis())
                        .map(emoji -> Tuples.of(emoji.getId(), new GuildEmojiBean(emoji))));

        Mono<Void> saveMembers = context.getStateHolder().getMemberStore()
                .save(Flux.just(context.getDispatch().getMembers())
                        .map(member -> Tuples.of(LongLongTuple2.of(guildBean.getId(), member.getUser().getId()),
                                new MemberBean(member))));

        Mono<Void> saveUsers = context.getStateHolder().getUserStore()
                .save(Flux.just(context.getDispatch().getMembers())
                        .map(GuildMemberResponse::getUser)
                        .map(UserBean::new)
                        .map(bean -> Tuples.of(bean.getId(), bean)));

        Mono<Void> saveVoiceStates = context.getStateHolder().getVoiceStateStore()
                .save(Flux.just(context.getDispatch().getVoiceStates())
                        .map(voiceState -> Tuples.of(LongLongTuple2.of(guildBean.getId(), voiceState.getUserId()),
                                new VoiceStateBean(voiceState, guildBean.getId()))));

        Mono<Void> savePresences = context.getStateHolder().getPresenceStore()
                .save(Flux.just(context.getDispatch().getPresences())
                        .map(presence -> Tuples.of(LongLongTuple2.of(guildBean.getId(), presence.getUser().get("id").asLong()),
                                new PresenceBean(presence))));

        Mono<Void> startMemberChunk = context.getGateway().getGatewayResources().isMemberRequest() ?
                Mono.just(guildBean)
                        .filter(GuildBean::getLarge)
                        .flatMap(bean -> context.getGateway()
                                .getGatewayClientMap()
                                .get(context.getShardInfo().getIndex())
                                .send(Mono.just(GatewayPayload.requestGuildMembers(
                                        new RequestGuildMembers(bean.getId(), "", 0)))))
                        .then() : Mono.empty();

        Mono<Void> saveOfflinePresences = Mono.just(guildBean.getMembers())
                .map(LongStream::of)
                .map(LongStream::boxed)
                .flatMapMany(Flux::fromStream)
                .filterWhen(id -> context.getStateHolder().getPresenceStore()
                        .find(LongLongTuple2.of(guildBean.getId(), id))
                        .hasElement()
                        .map(identity -> !identity))
                .flatMap(id -> context.getStateHolder().getPresenceStore()
                        .save(LongLongTuple2.of(guildBean.getId(), id), PresenceBean.DEFAULT_OFFLINE))
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
                .thenReturn(new GuildCreateEvent(gateway, context.getShardInfo(), new Guild(gateway, guildBean)));
    }

    static Mono<GuildDeleteEvent> guildDelete(DispatchContext<GuildDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        StateHolder stateHolder = context.getStateHolder();

        long guildId = context.getDispatch().getGuild().getId();
        boolean unavailable = context.getDispatch().getGuild().isUnavailable();

        Mono<Void> deleteGuild = stateHolder.getGuildStore().delete(guildId);

        return stateHolder.getGuildStore()
                .find(context.getDispatch().getGuild().getId())
                .flatMap(guild -> {
                    Flux<Long> channels = Flux.fromStream(() -> LongStream.of(guild.getChannels()).boxed());
                    Flux<Long> roles = Flux.fromStream(() -> LongStream.of(guild.getRoles()).boxed());
                    Flux<Long> emojis = Flux.fromStream(() -> LongStream.of(guild.getEmojis()).boxed());

                    Mono<Void> deleteChannels = stateHolder.getChannelStore().delete(channels);
                    Mono<Void> deleteRoles = stateHolder.getRoleStore().delete(roles);
                    Mono<Void> deleteEmojis = stateHolder.getGuildEmojiStore().delete(emojis);
                    Mono<Void> deleteMembers = stateHolder.getMemberStore()
                            .deleteInRange(LongLongTuple2.of(guild.getId(), 0), LongLongTuple2.of(guild.getId(), -1));
                    // TODO delete messages
                    // TODO delete no longer visible users
                    Mono<Void> deleteVoiceStates = stateHolder.getVoiceStateStore()
                            .deleteInRange(LongLongTuple2.of(guild.getId(), 0), LongLongTuple2.of(guild.getId(), -1));
                    Mono<Void> deletePresences = stateHolder.getPresenceStore()
                            .deleteInRange(LongLongTuple2.of(guild.getId(), 0), LongLongTuple2.of(guild.getId(), -1));

                    return deleteChannels
                            .and(deleteRoles)
                            .and(deleteEmojis)
                            .and(deleteMembers)
                            .and(deleteVoiceStates)
                            .and(deletePresences)
                            .thenReturn(guild);
                })
                .flatMap(deleteGuild::thenReturn)
                .map(bean -> {
                    Guild guild = new Guild(context.getGateway(), bean);
                    return new GuildDeleteEvent(gateway, context.getShardInfo(), guildId, guild, unavailable);
                })
                .switchIfEmpty(deleteGuild.thenReturn(new GuildDeleteEvent(gateway, context.getShardInfo(), guildId, null, unavailable)));
    }

    static Mono<EmojisUpdateEvent> guildEmojisUpdate(DispatchContext<GuildEmojisUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        Mono<Void> updateGuildBean = context.getStateHolder().getGuildStore()
                .find(context.getDispatch().getGuildId())
                .map(GuildBean::new)
                .doOnNext(guild -> {
                    long[] emojis = Arrays.stream(context.getDispatch().getEmojis())
                            .mapToLong(GuildEmojiResponse::getId)
                            .toArray();

                    guild.setEmojis(emojis);
                })
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guild.getId(), guild));

        Mono<Void> saveEmojis = context.getStateHolder().getGuildEmojiStore()
                .saveWithLong(Flux.fromArray(context.getDispatch().getEmojis())
                        .map(GuildEmojiBean::new)
                        .map(bean -> LongObjTuple2.of(bean.getId(), bean)));

        long guildId = context.getDispatch().getGuildId();

        Set<GuildEmoji> emojis = Arrays.stream(context.getDispatch().getEmojis())
                .map(GuildEmojiBean::new)
                .map(bean -> new GuildEmoji(gateway, bean, guildId))
                .collect(Collectors.toSet());

        return updateGuildBean
                .and(saveEmojis)
                .thenReturn(new EmojisUpdateEvent(gateway, context.getShardInfo(), guildId, emojis));
    }

    static Mono<IntegrationsUpdateEvent> guildIntegrationsUpdate(DispatchContext<GuildIntegrationsUpdate> context) {
        return Mono.just(new IntegrationsUpdateEvent(context.getGateway(), context.getShardInfo(),
                context.getDispatch().getGuildId()));
    }

    static Mono<MemberJoinEvent> guildMemberAdd(DispatchContext<GuildMemberAdd> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = context.getDispatch().getGuildId();
        GuildMemberResponse response = context.getDispatch().getMember();
        MemberBean bean = new MemberBean(response);
        UserBean userBean = new UserBean(response.getUser());

        Mono<Void> addMemberId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(GuildBean::new)
                .doOnNext(guild -> guild.setMembers(ArrayUtil.add(guild.getMembers(), response.getUser().getId())))
                .doOnNext(guild -> guild.setMemberCount(guild.getMemberCount() + 1))
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> saveMember = context.getStateHolder().getMemberStore()
                .save(LongLongTuple2.of(guildId, response.getUser().getId()), bean);

        Mono<Void> saveUser = context.getStateHolder().getUserStore()
                .save(response.getUser().getId(), userBean);

        Member member = new Member(gateway, bean, userBean, guildId);

        return addMemberId
                .and(saveMember)
                .and(saveUser)
                .thenReturn(new MemberJoinEvent(gateway, context.getShardInfo(), member, guildId));
    }

    static Mono<MemberLeaveEvent> guildMemberRemove(DispatchContext<GuildMemberRemove> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = context.getDispatch().getGuildId();
        UserResponse response = context.getDispatch().getUser();

        Mono<Void> removeMemberId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(GuildBean::new)
                .doOnNext(guild -> guild.setMembers(ArrayUtil.remove(guild.getMembers(), response.getId())))
                .doOnNext(guild -> guild.setMemberCount(guild.getMemberCount() - 1))
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Member> member = context.getStateHolder().getMemberStore()
                .find(LongLongTuple2.of(guildId, response.getId()))
                .map(bean -> new Member(gateway, bean, new UserBean(response), guildId));

        Mono<Void> deleteMember = context.getStateHolder().getMemberStore()
                .delete(LongLongTuple2.of(guildId, response.getId()));

        User user = new User(gateway, new UserBean(response));

        return member.map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(Mono.when(removeMemberId, deleteMember)::thenReturn)
                .map(m -> new MemberLeaveEvent(gateway, context.getShardInfo(), user, guildId, m.orElse(null)));
    }

    static Mono<MemberChunkEvent> guildMembersChunk(DispatchContext<GuildMembersChunk> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = context.getDispatch().getGuildId();

        Flux<Tuple2<LongLongTuple2, MemberBean>> memberPairs = Flux.fromArray(context.getDispatch().getMembers())
                .map(response -> Tuples.of(response, new MemberBean(response)))
                .map(tuple -> Tuples.of(LongLongTuple2.of(guildId, tuple.getT1().getUser().getId()), tuple.getT2()));

        Flux<Tuple2<Long, UserBean>> userPairs =
                Flux.fromStream(() -> Arrays.stream(context.getDispatch().getMembers()))
                        .map(response -> new UserBean(response.getUser()))
                        .map(bean -> Tuples.of(bean.getId(), bean));

        Mono<Void> addMemberIds = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(GuildBean::new)
                .doOnNext(guild -> {
                    long[] ids = Arrays.stream(context.getDispatch().getMembers())
                            .map(GuildMemberResponse::getUser)
                            .mapToLong(UserResponse::getId).toArray();

                    guild.setMembers(ArrayUtil.addAll(guild.getMembers(), ids));
                })
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> saveMembers = context.getStateHolder().getMemberStore().save(memberPairs);

        Mono<Void> saveUsers = context.getStateHolder().getUserStore().save(userPairs);

        Set<Member> members = Arrays.stream(context.getDispatch().getMembers())
                .map(response -> Tuples.of(new MemberBean(response), new UserBean(response.getUser())))
                .map(tuple -> new Member(gateway, tuple.getT1(), tuple.getT2(), guildId))
                .collect(Collectors.toSet());

        Mono<Void> saveOfflinePresences = Flux.fromIterable(members)
                .map(Member::getId)
                .map(Snowflake::asLong)
                .filterWhen(id -> context.getStateHolder().getPresenceStore()
                        .find(LongLongTuple2.of(guildId, id))
                        .hasElement()
                        .map(identity -> !identity))
                .flatMap(id -> context.getStateHolder().getPresenceStore()
                        .save(LongLongTuple2.of(guildId, id), PresenceBean.DEFAULT_OFFLINE))
                .then();

        return addMemberIds
                .and(saveMembers)
                .and(saveUsers)
                .and(saveOfflinePresences)
                .thenReturn(new MemberChunkEvent(gateway, context.getShardInfo(), guildId, members));
    }

    static Mono<MemberUpdateEvent> guildMemberUpdate(DispatchContext<GuildMemberUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = context.getDispatch().getGuildId();
        long memberId = context.getDispatch().getUser().getId();

        long[] currentRoles = context.getDispatch().getRoles();
        String currentNick = context.getDispatch().getNick();

        LongLongTuple2 key = LongLongTuple2.of(guildId, memberId);

        Mono<MemberUpdateEvent> update = context.getStateHolder().getMemberStore()
                .find(key)
                .flatMap(oldBean -> {
                    UserBean user = new UserBean(context.getDispatch().getUser());
                    Member old = new Member(gateway, oldBean, user, guildId);

                    MemberBean newBean = new MemberBean(oldBean, context.getDispatch());

                    return context.getStateHolder().getMemberStore()
                            .save(key, newBean)
                            .thenReturn(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, old, currentRoles,
                                    currentNick));
                });

        return update.defaultIfEmpty(new MemberUpdateEvent(gateway, context.getShardInfo(), guildId, memberId, null, currentRoles,
                currentNick));
    }

    static Mono<RoleCreateEvent> guildRoleCreate(DispatchContext<GuildRoleCreate> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = context.getDispatch().getGuildId();
        RoleBean bean = new RoleBean(context.getDispatch().getRole());
        Role role = new Role(gateway, bean, guildId);

        Mono<Void> addRoleId = context.getStateHolder().getGuildStore()
                .find(context.getDispatch().getGuildId())
                .map(GuildBean::new)
                .doOnNext(guild -> guild.setRoles(ArrayUtil.add(guild.getRoles(), bean.getId())))
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guild.getId(), guild));

        Mono<Void> saveRole = context.getStateHolder().getRoleStore()
                .save(bean.getId(), bean);

        return addRoleId
                .and(saveRole)
                .thenReturn(new RoleCreateEvent(gateway, context.getShardInfo(), guildId, role));
    }

    static Mono<RoleDeleteEvent> guildRoleDelete(DispatchContext<GuildRoleDelete> context) {
        GatewayDiscordClient gateway = context.getGateway();
        long guildId = context.getDispatch().getGuildId();
        long roleId = context.getDispatch().getRoleId();

        Mono<Void> removeRoleId = context.getStateHolder().getGuildStore()
                .find(guildId)
                .map(GuildBean::new)
                .doOnNext(guild -> guild.setRoles(ArrayUtil.remove(guild.getRoles(), roleId)))
                .flatMap(guild -> context.getStateHolder().getGuildStore().save(guildId, guild));

        Mono<Void> deleteRole = context.getStateHolder().getRoleStore()
                .delete(context.getDispatch().getRoleId());

        Mono<Void> removeRoleFromMembers = context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMapMany(guild -> Flux.fromArray(ArrayUtil.toObject(guild.getMembers())))
                .flatMap(memberId -> context.getStateHolder().getMemberStore()
                        .find(LongLongTuple2.of(guildId, memberId))
                        .map(member -> Tuples.of(memberId, member)))
                .filter(t -> ArrayUtil.contains(t.getT2().getRoles(), roleId))
                .map(t -> {
                    MemberBean member = new MemberBean(t.getT2());
                    member.setRoles(ArrayUtil.remove(member.getRoles(), roleId));
                    return Tuples.of(t.getT1(), member);
                })
                .flatMap(t -> context.getStateHolder().getMemberStore()
                        .save(LongLongTuple2.of(guildId, t.getT1()), t.getT2()))
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
        long guildId = context.getDispatch().getGuildId();

        RoleBean bean = new RoleBean(context.getDispatch().getRole());
        Role current = new Role(gateway, bean, guildId);

        Mono<Void> saveNew = context.getStateHolder().getRoleStore().save(bean.getId(), bean);

        return context.getStateHolder().getRoleStore()
                .find(context.getDispatch().getRole().getId())
                .flatMap(saveNew::thenReturn)
                .map(old -> new RoleUpdateEvent(gateway, context.getShardInfo(), current, new Role(gateway, old, guildId)))
                .switchIfEmpty(saveNew.thenReturn(new RoleUpdateEvent(gateway, context.getShardInfo(), current, null)));
    }

    static Mono<GuildUpdateEvent> guildUpdate(DispatchContext<GuildUpdate> context) {
        GatewayDiscordClient gateway = context.getGateway();

        long guildId = context.getDispatch().getGuildId();

        Mono<GuildUpdateEvent> update = context.getStateHolder().getGuildStore()
                .find(guildId)
                .flatMap(oldBean -> {
                    GuildBean newBean = new GuildBean(oldBean, context.getDispatch());

                    Guild old = new Guild(gateway, oldBean);
                    Guild current = new Guild(gateway, newBean);

                    return context.getStateHolder().getGuildStore()
                            .save(newBean.getId(), newBean)
                            .thenReturn(new GuildUpdateEvent(gateway, context.getShardInfo(), current, old));
                });

        Guild current = new Guild(gateway, new BaseGuildBean(context.getDispatch()));

        return update.defaultIfEmpty(new GuildUpdateEvent(gateway, context.getShardInfo(), current, null));
    }

}
