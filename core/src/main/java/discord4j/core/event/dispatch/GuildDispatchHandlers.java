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

import discord4j.common.json.payload.dispatch.*;
import discord4j.common.json.response.EmojiResponse;
import discord4j.common.json.response.GuildEmojiResponse;
import discord4j.common.json.response.GuildMemberResponse;
import discord4j.common.json.response.UserResponse;
import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.event.domain.guild.*;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleDeleteEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.bean.*;
import discord4j.core.store.StoreHolder;
import discord4j.core.util.ArrayUtil;
import discord4j.store.primitive.LongObjStore;
import discord4j.store.util.LongLongTuple2;
import discord4j.store.util.LongObjTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GuildDispatchHandlers {

    static Flux<BanEvent> guildBanAdd(DispatchContext<GuildBanAdd> context) {
        User user = new User(context.getServiceMediator(), new UserBean(context.getDispatch().getUser()));
        long guildId = context.getDispatch().getGuildId();

        return Flux.just(new BanEvent(context.getServiceMediator().getDiscordClient(), user, guildId));
    }

    static Flux<UnbanEvent> guildBanRemove(DispatchContext<GuildBanRemove> context) {
        User user = new User(context.getServiceMediator(), new UserBean(context.getDispatch().getUser()));
        long guildId = context.getDispatch().getGuildId();

        return Flux.just(new UnbanEvent(context.getServiceMediator().getDiscordClient(), user, guildId));
    }

    static Flux<GuildCreateEvent> guildCreate(DispatchContext<GuildCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        GuildBean bean = new GuildBean(context.getDispatch().getGuild());

        return serviceMediator.getStoreHolder().getGuildStore()
                .save(bean.getId(), bean)
                .thenReturn(new GuildCreateEvent(serviceMediator.getDiscordClient(), new Guild(serviceMediator, bean)))
                .flux();
    }

    static Flux<GuildDeleteEvent> guildDelete(DispatchContext<GuildDelete> context) {
//        ServiceMediator serviceMediator = context.getServiceMediator();
//        DiscordClient client = serviceMediator.getDiscordClient();
//        long guildId = context.getDispatch().getGuild().getId();
//
//        StoreHolder storeHolder = serviceMediator.getStoreHolder();
//
//        serviceMediator.getStoreHolder().getGuildStore()
//                .find(guildId)
//                .flatMap(guild -> guild.getChannels())
//
//        Mono<Void> deleteTextChannels = storeHolder.getTextChannelStore().deleteAll()
        return Flux.empty();
    }

    static Flux<EmojisUpdateEvent> guildEmojisUpdate(DispatchContext<GuildEmojisUpdate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();

        long[] emojiIds = Arrays.stream(context.getDispatch().getEmojis())
                .mapToLong(GuildEmojiResponse::getId)
                .toArray();

        Stream<LongObjTuple2<GuildEmojiBean>> emojiBeans = Arrays.stream(context.getDispatch().getEmojis())
                .map(GuildEmojiBean::new)
                .map(bean -> LongObjTuple2.of(bean.getId(), bean));

        Mono<Void> updateGuildBean = serviceMediator.getStoreHolder().getGuildStore()
                .find(context.getDispatch().getGuildId())
                .doOnNext(guild -> guild.setEmojis(emojiIds))
                .flatMap(guild -> serviceMediator.getStoreHolder().getGuildStore().save(guild.getId(), guild));

        Mono<Void> saveEmojis = serviceMediator.getStoreHolder().getGuildEmojiStore()
                .saveWithLong(Flux.fromStream(emojiBeans));

        DiscordClient client = context.getServiceMediator().getDiscordClient();
        long guildId = context.getDispatch().getGuildId();
        Set<GuildEmoji> emojis = emojiBeans
                .map(LongObjTuple2::getT2)
                .map(bean -> new GuildEmoji(serviceMediator, bean, guildId))
                .collect(Collectors.toSet());

        return updateGuildBean
                .then(saveEmojis)
                .thenReturn(new EmojisUpdateEvent(client, guildId, emojis))
                .flux();
    }

    static Flux<IntegrationsUpdateEvent> guildIntegrationsUpdate(
            DispatchContext<GuildIntegrationsUpdate> context) {
        return Flux.just(new IntegrationsUpdateEvent(context.getServiceMediator().getDiscordClient(),
                context.getDispatch().getGuildId()));
    }

    static Flux<MemberJoinEvent> guildMemberAdd(DispatchContext<GuildMemberAdd> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        long guildId = context.getDispatch().getGuildId();
        GuildMemberResponse response = context.getDispatch().getMember();
        MemberBean bean = new MemberBean(response);

        Mono<Void> addMemberId = serviceMediator.getStoreHolder().getGuildStore()
                .find(guildId)
                .doOnNext(guild -> guild.setMembers(ArrayUtil.add(guild.getMembers(), response.getUser().getId())))
                .flatMap(guild -> serviceMediator.getStoreHolder().getGuildStore().save(guildId, guild));

        Mono<Void> saveMember = serviceMediator.getStoreHolder().getMemberStore()
                .save(LongLongTuple2.of(guildId, response.getUser().getId()), bean);

        Member member = new Member(serviceMediator, bean, new UserBean(response.getUser()), guildId);

        return addMemberId
                .then(saveMember)
                .thenReturn(new MemberJoinEvent(serviceMediator.getDiscordClient(), member, guildId))
                .flux();
    }

    static Flux<MemberLeaveEvent> guildMemberRemove(DispatchContext<GuildMemberRemove> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        long guildId = context.getDispatch().getGuildId();
        UserResponse response = context.getDispatch().getUser();

        Mono<Void> removeMemberId = serviceMediator.getStoreHolder().getGuildStore()
                .find(context.getDispatch().getGuildId())
                .doOnNext(guild -> guild.setMembers(ArrayUtil.remove(guild.getMembers(), response.getId())))
                .flatMap(guild -> serviceMediator.getStoreHolder().getGuildStore().save(guildId, guild));

        Mono<Void> deleteMember = serviceMediator.getStoreHolder().getMemberStore()
                .delete(LongLongTuple2.of(context.getDispatch().getGuildId(), context.getDispatch().getUser().getId()));

        User user = new User(serviceMediator, new UserBean(response));

        return removeMemberId
                .then(deleteMember)
                .thenReturn(new MemberLeaveEvent(serviceMediator.getDiscordClient(), user, guildId))
                .flux();
    }

    static Flux<MemberChunkEvent> guildMembersChunk(DispatchContext<GuildMembersChunk> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        long guildId = context.getDispatch().getGuildId();

       long[] memberIds = Arrays.stream(context.getDispatch().getMembers())
               .map(GuildMemberResponse::getUser)
               .mapToLong(UserResponse::getId)
               .toArray();

        Stream<Tuple2<LongLongTuple2, MemberBean>> memberBeans = Arrays.stream(context.getDispatch().getMembers())
                .map(response ->
                        Tuples.of(LongLongTuple2.of(guildId, response.getUser().getId()), new MemberBean(response)));

       Mono<Void> addMemberIds = serviceMediator.getStoreHolder().getGuildStore()
               .find(context.getDispatch().getGuildId())
               .doOnNext(guild -> guild.setMembers(ArrayUtil.addAll(guild.getMembers(), memberIds)))
               .flatMap(guild -> serviceMediator.getStoreHolder().getGuildStore().save(guild.getId(), guild));

        Mono<Void> saveMembers = serviceMediator.getStoreHolder().getMemberStore()
                .save(Flux.fromStream(memberBeans));

        Set<Member> members = memberBeans
                .map(t -> new Member(serviceMediator, t.getT2(), new UserBean(_______), guildId))
                .collect(Collectors.toSet());

        return addMemberIds
                .then(saveMembers)
                .thenReturn(new MemberChunkEvent(context.getServiceMediator().getDiscordClient(), guildId, members))
                .flux();
    }

    static Flux<MemberUpdateEvent> guildMemberUpdate(DispatchContext<GuildMemberUpdate> context) {
        // TODO
        return Flux.empty();
    }

    static Flux<RoleCreateEvent> guildRoleCreate(DispatchContext<GuildRoleCreate> context) {
        ServiceMediator serviceMediator = context.getServiceMediator();
        DiscordClient client = serviceMediator.getDiscordClient();
        long guildId = context.getDispatch().getGuildId();
        RoleBean bean = new RoleBean(context.getDispatch().getRole());
        Role role = new Role(serviceMediator, bean, guildId);

        Mono<Void> addRoleId = serviceMediator.getStoreHolder().getGuildStore()
                .find(context.getDispatch().getGuildId())
                .doOnNext(guild -> guild.setRoles(ArrayUtil.add(guild.getRoles(), bean.getId())))
                .flatMap(guild -> serviceMediator.getStoreHolder().getGuildStore().save(guild.getId(), guild));

        Mono<Void> saveRole = serviceMediator.getStoreHolder().getRoleStore()
                .save(bean.getId(), bean);

        return addRoleId
                .then(saveRole)
                .thenReturn(new RoleCreateEvent(client, guildId, role))
                .flux();
    }

    static Flux<RoleDeleteEvent> guildRoleDelete(DispatchContext<GuildRoleDelete> context) {

    }

    static Flux<RoleUpdateEvent> guildRoleUpdate(DispatchContext<GuildRoleUpdate> context) {
        // TODO
        return Flux.empty();
    }

    static Flux<GuildUpdateEvent> guildUpdate(DispatchContext<GuildUpdate> context) {
        // TODO
        return Flux.empty();
    }

}
