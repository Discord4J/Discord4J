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

package discord4j.common.store.impl;

import discord4j.common.store.api.layout.DataAccessor;
import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.layout.StoreLayout;
import discord4j.common.store.api.object.ExactResultNotAvailableException;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LocalStoreLayout implements StoreLayout, DataAccessor, GatewayDataUpdater {

    /**
     * Weakly store users so that they can be automatically garbage collected if no member or presence reference them
     */
    private final WeakStorage<AtomicReference<UserData>> userStorage = new WeakStorage<>(
            data -> toLongId(data.get().id()));

    /**
     * Store channels and nested entities (messages)
     */
    private final Storage<ChannelNode, ChannelData> channelStorage = new Storage<>(
            data -> toLongId(data.id()),
            ChannelNode::new,
            ChannelNode::getData,
            ChannelNode::setData);

    /**
     * Store guilds and nested entities (members, roles, emojis, presences, voice states)
     */
    private final GuildStorage guildStorage = new GuildStorage(channelStorage, userStorage);

    private final AtomicReference<AtomicReference<UserData>> selfUser = new AtomicReference<>();
    private final AtomicInteger shardCount = new AtomicInteger();

    @Override
    public Mono<Long> countChannels() {
        return Mono.just(channelStorage.count());
    }

    @Override
    public Mono<Long> countChannelsInGuild(long guildId) {
        return getChannelsInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countEmojis() {
        return MathFlux.sumLong(Flux.fromIterable(guildStorage.nodes())
                .map(node -> node.getEmojiStorage().count()))
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> countEmojisInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId).getEmojiStorage().count());
    }

    @Override
    public Mono<Long> countGuilds() {
        return Mono.just(guildStorage.count());
    }

    @Override
    public Mono<Long> countMembers() {
        return MathFlux.sumLong(Flux.fromIterable(guildStorage.nodes())
                .map(node -> node.getMemberStorage().count()))
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> countMembersInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId).getMemberStorage().count());
    }

    @Override
    public Mono<Long> countExactMembersInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId))
                .filter(GuildNode::isMemberListComplete)
                .switchIfEmpty(Mono.error(ExactResultNotAvailableException::new))
                .map(node -> node.getMemberStorage().count());
    }

    @Override
    public Mono<Long> countMessages() {
        return MathFlux.sumLong(Flux.fromIterable(channelStorage.nodes())
                .map(node -> node.getMessageStorage().count()))
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> countMessagesInChannel(long channelId) {
        return Mono.just(channelStorage.nodeForId(channelId).getMessageStorage().count());
    }

    @Override
    public Mono<Long> countPresences() {
        return MathFlux.sumLong(Flux.fromIterable(guildStorage.nodes())
                .map(node -> node.getPresenceStorage().count()))
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> countPresencesInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId).getPresenceStorage().count());
    }

    @Override
    public Mono<Long> countRoles() {
        return MathFlux.sumLong(Flux.fromIterable(guildStorage.nodes())
                .map(node -> node.getRoleStorage().count()))
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> countRolesInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId).getRoleStorage().count());
    }

    @Override
    public Mono<Long> countUsers() {
        return Mono.just(userStorage.count());
    }

    @Override
    public Mono<Long> countVoiceStates() {
        return MathFlux.sumLong(Flux.fromIterable(guildStorage.nodes())
                .map(node -> node.getVoiceStateStorage().count()))
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Long> countVoiceStatesInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId).getVoiceStateStorage().count());
    }

    @Override
    public Mono<Long> countVoiceStatesInChannel(long guildId, long channelId) {
        return getVoiceStatesInChannel(guildId, channelId).count();
    }

    @Override
    public Flux<ChannelData> getChannels() {
        return Flux.fromIterable(channelStorage.findAll());
    }

    @Override
    public Flux<ChannelData> getChannelsInGuild(long guildId) {
        return Mono.justOrEmpty(guildStorage.find(guildId))
                .flatMapIterable(GuildData::channels)
                .map(LocalStoreLayout::toLongId)
                .flatMap(id -> Mono.justOrEmpty(channelStorage.find(id)));
    }

    @Override
    public Mono<ChannelData> getChannelById(long channelId) {
        return Mono.justOrEmpty(channelStorage.find(channelId));
    }

    @Override
    public Flux<EmojiData> getEmojis() {
        return Flux.fromIterable(guildStorage.nodes())
                .flatMapIterable(node -> node.getEmojiStorage().findAll());
    }

    @Override
    public Flux<EmojiData> getEmojisInGuild(long guildId) {
        return Flux.fromIterable(guildStorage.nodeForId(guildId).getEmojiStorage().findAll());
    }

    @Override
    public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
        return Mono.justOrEmpty(guildStorage.nodeForId(guildId).getEmojiStorage().find(emojiId));
    }

    @Override
    public Flux<GuildData> getGuilds() {
        return Flux.fromIterable(guildStorage.findAll());
    }

    @Override
    public Mono<GuildData> getGuildById(long guildId) {
        return Mono.justOrEmpty(guildStorage.find(guildId));
    }

    @Override
    public Flux<MemberData> getMembers() {
        return Flux.fromIterable(guildStorage.nodes())
                .flatMapIterable(node -> node.getMemberStorage().findAll());
    }

    @Override
    public Flux<MemberData> getMembersInGuild(long guildId) {
        return Flux.fromIterable(guildStorage.nodeForId(guildId).getMemberStorage().findAll());
    }

    @Override
    public Flux<MemberData> getExactMembersInGuild(long guildId) {
        return Mono.just(guildStorage.nodeForId(guildId))
                .filter(GuildNode::isMemberListComplete)
                .switchIfEmpty(Mono.error(ExactResultNotAvailableException::new))
                .flatMapIterable(node -> node.getMemberStorage().findAll());
    }

    @Override
    public Mono<MemberData> getMemberById(long guildId, long userId) {
        return Mono.justOrEmpty(guildStorage.nodeForId(guildId).getMemberStorage().find(userId));
    }

    @Override
    public Flux<MessageData> getMessages() {
        return Flux.fromIterable(channelStorage.nodes())
                .flatMapIterable(node -> node.getMessageStorage().findAll());
    }

    @Override
    public Flux<MessageData> getMessagesInChannel(long channelId) {
        return Flux.fromIterable(channelStorage.nodeForId(channelId).getMessageStorage().findAll());
    }

    @Override
    public Mono<MessageData> getMessageById(long channelId, long messageId) {
        return Mono.justOrEmpty(channelStorage.nodeForId(channelId).getMessageStorage().find(messageId));
    }

    @Override
    public Flux<PresenceData> getPresences() {
        return Flux.fromIterable(guildStorage.nodes())
                .flatMapIterable(node -> node.getPresenceStorage().findAll());
    }

    @Override
    public Flux<PresenceData> getPresencesInGuild(long guildId) {
        return Flux.fromIterable(guildStorage.nodeForId(guildId).getPresenceStorage().findAll());
    }

    @Override
    public Mono<PresenceData> getPresenceById(long guildId, long userId) {
        return Mono.justOrEmpty(guildStorage.nodeForId(guildId).getPresenceStorage().find(userId));
    }

    @Override
    public Flux<RoleData> getRoles() {
        return Flux.fromIterable(guildStorage.nodes())
                .flatMapIterable(node -> node.getRoleStorage().findAll());
    }

    @Override
    public Flux<RoleData> getRolesInGuild(long guildId) {
        return Flux.fromIterable(guildStorage.nodeForId(guildId).getRoleStorage().findAll());
    }

    @Override
    public Mono<RoleData> getRoleById(long guildId, long roleId) {
        return Mono.justOrEmpty(guildStorage.nodeForId(guildId).getRoleStorage().find(roleId));
    }

    @Override
    public Flux<UserData> getUsers() {
        return Flux.fromIterable(userStorage.findAll()).map(AtomicReference::get);
    }

    @Override
    public Mono<UserData> getUserById(long userId) {
        return Mono.justOrEmpty(userStorage.find(userId).map(AtomicReference::get));
    }

    @Override
    public Flux<VoiceStateData> getVoiceStates() {
        return Flux.fromIterable(guildStorage.nodes())
                .flatMapIterable(node -> node.getVoiceStateStorage().findAll());
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId) {
        return getVoiceStatesInGuild(guildId)
                .filter(data -> data.channelId().map(id -> toLongId(id) == channelId).orElse(false));
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInGuild(long guildId) {
        return Flux.fromIterable(guildStorage.nodeForId(guildId).getVoiceStateStorage().findAll());
    }

    @Override
    public Mono<VoiceStateData> getVoiceStateById(long guildId, long userId) {
        return Mono.justOrEmpty(guildStorage.nodeForId(guildId).getVoiceStateStorage().find(userId));
    }

    @Override
    public Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch) {
        return Mono.fromRunnable(() -> {
            ChannelData data = dispatch.channel();
            channelStorage.insert(data);
            data.guildId().toOptional()
                    .map(LocalStoreLayout::toLongId)
                    .ifPresent(guildId -> {
                        guildStorage.nodeForId(guildId).getChannelIds().add(toLongId(data.id()));
                        guildStorage.updateIfPresent(guildId, existing -> GuildData.builder()
                                .from(existing)
                                .addChannel(data.id())
                                .build());
                    });
        });
    }

    @Override
    public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
        return Mono.fromCallable(() -> {
            ChannelData data = dispatch.channel();
            channelStorage.delete(toLongId(data.id()));
            data.guildId().toOptional()
                    .map(LocalStoreLayout::toLongId)
                    .ifPresent(guildId -> {
                        guildStorage.nodeForId(guildId).getChannelIds().remove(toLongId(data.id()));
                        guildStorage.updateIfPresent(guildId, existing -> GuildData.builder()
                                    .from(existing)
                                    .channels(existing.channels().stream()
                                            .filter(id -> !id.equals(data.id()))
                                            .collect(Collectors.toList()))
                                    .build());
                    });
            return data;
        });
    }

    @Override
    public Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch) {
        ChannelData data = dispatch.channel();
        return Mono.justOrEmpty(channelStorage.update(toLongId(data.id()), existing -> data));
    }

    @Override
    public Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch) {
        return Mono.fromRunnable(() -> {
            System.out.println(dispatch);
            GuildCreateData createData = dispatch.guild();
            List<RoleData> roles = createData.roles();
            List<EmojiData> emojis = createData.emojis();
            List<MemberData> members = createData.members();
            List<ChannelData> channels = createData.channels();
            List<PresenceData> presences = createData.presences();
            List<VoiceStateData> voiceStates = createData.voiceStates();
            GuildData guild = GuildData.builder()
                    .from(createData)
                    .roles(roles.stream().map(RoleData::id).collect(Collectors.toList()))
                    .emojis(emojis.stream().map(EmojiData::id)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList()))
                    .members(members.stream().map(data -> data.user().id()).distinct().collect(Collectors.toList()))
                    .channels(channels.stream().map(ChannelData::id).collect(Collectors.toList()))
                    .build();
            GuildNode node = guildStorage.nodeForId(toLongId(guild.id()));
            node.setData(guild);
            roles.forEach(node.getRoleStorage()::insert);
            emojis.forEach(node.getEmojiStorage()::insert);
            members.forEach(node.getMemberStorage()::insert);
            channels.forEach(data -> {
                channelStorage.insert(data);
                node.getChannelIds().add(toLongId(data.id()));
            });
            presences.forEach(node.getPresenceStorage()::insert);
            voiceStates.forEach(node.getVoiceStateStorage()::insert);
        });
    }

    @Override
    public Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch) {
        return Mono.justOrEmpty(guildStorage.delete(toLongId(dispatch.guild().id())));
    }

    @Override
    public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        return Mono.fromCallable(() -> {
            long guildId = toLongId(dispatch.guildId());
            IdentityStorage<EmojiData> emojiStorage = guildStorage.nodeForId(guildId).getEmojiStorage();
            Set<EmojiData> old = new HashSet<>(emojiStorage.map.values());
            emojiStorage.map.clear();
            dispatch.emojis().forEach(emojiStorage::insert);
            guildStorage.updateIfPresent(guildId, existing -> GuildData.builder()
                    .from(existing)
                    .emojis(dispatch.emojis().stream()
                            .map(EmojiData::id)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet()))
                    .build());
            return old;
        });
    }

    @Override
    public Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch) {
        return Mono.fromRunnable(() -> addMember(toLongId(dispatch.guildId()), dispatch.member()));
    }

    @Override
    public Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
        return Mono.fromCallable(() -> {
            long guildId = toLongId(dispatch.guildId());
            MemberData old = guildStorage.nodeForId(guildId).getMemberStorage()
                    .delete(toLongId(dispatch.user().id()))
                    .orElse(null);
            guildStorage.updateIfPresent(guildId, existing -> GuildData.builder()
                    .from(existing)
                    .members(existing.members().stream()
                            .filter(id -> !id.equals(dispatch.user().id()))
                            .collect(Collectors.toList()))
                    .build());
            return old;
        });
    }

    @Override
    public Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
        return Mono.fromRunnable(() -> dispatch.members()
                .forEach(member -> addMember(toLongId(dispatch.guildId()), member)));
    }

    @Override
    public Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
        return Mono.justOrEmpty(guildStorage.nodeForId(toLongId(dispatch.guildId())).getMemberStorage()
                .updateIfPresent(toLongId(dispatch.user().id()), existing -> MemberData.builder()
                        .from(existing)
                        .nick(dispatch.nick())
                        .roles(dispatch.roles())
                        .premiumSince(dispatch.premiumSince())
                        .build()));
    }

    @Override
    public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        return Mono.fromRunnable(() -> {
            long guildId = toLongId(dispatch.guildId());
            guildStorage.nodeForId(guildId).getRoleStorage().insert(dispatch.role());
            guildStorage.updateIfPresent(guildId, existing -> GuildData.builder()
                    .from(existing)
                    .addRole(dispatch.role().id())
                    .build());
        });
    }

    @Override
    public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        return Mono.fromCallable(() -> {
            long guildId = toLongId(dispatch.guildId());
            long roleId = toLongId(dispatch.roleId());
            RoleData old = guildStorage.nodeForId(guildId).getRoleStorage().delete(roleId).orElse(null);
            guildStorage.updateIfPresent(guildId, existing -> GuildData.builder()
                    .from(existing)
                    .roles(existing.roles().stream()
                            .filter(id -> !id.equals(dispatch.roleId()))
                            .collect(Collectors.toList()))
                    .build());
            UserRefStorage<MemberData> memberStorage = guildStorage.nodeForId(guildId).getMemberStorage();
            memberStorage.map.forEach((memberId, __) -> memberStorage
                    .updateIfPresent(memberId, existing -> MemberData.builder()
                            .from(existing)
                            .roles(existing.roles().stream()
                                    .filter(id -> !id.equals(dispatch.roleId()))
                                    .collect(Collectors.toList()))
                            .build()));
            return old;
        });
    }

    @Override
    public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        return Mono.justOrEmpty(guildStorage.nodeForId(toLongId(dispatch.guildId())).getRoleStorage()
                .update(toLongId(dispatch.role().id()), existing -> dispatch.role()));
    }

    @Override
    public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
        return Mono.justOrEmpty(guildStorage
                .updateIfPresent(toLongId(dispatch.guild().id()), existing -> GuildData.builder()
                        .from(existing)
                        .from(dispatch.guild())
                        .roles(dispatch.guild().roles().stream()
                                .map(RoleData::id)
                                .collect(Collectors.toList()))
                        .emojis(dispatch.guild().emojis().stream().map(EmojiData::id)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()))
                        .build()));
    }

    @Override
    public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
        return Mono.fromRunnable(() -> {
            guildStorage.invalidateShard(shardIndex, shardCount.get());
            if (guildStorage.count() == 0) {
                shardCount.set(0);
            }
        });
    }

    @Override
    public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
        return Mono.fromRunnable(() -> {
            MessageData data = dispatch.message();
            long channelId = toLongId(data.channelId());
            channelStorage.nodeForId(channelId).getMessageStorage().insert(data);
            channelStorage.updateIfPresent(channelId, existing -> ChannelData.builder()
                    .from(existing)
                    .lastMessageId(data.id())
                    .build());
        });
    }

    @Override
    public Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch) {
        return Mono.justOrEmpty(channelStorage.nodeForId(toLongId(dispatch.channelId())).getMessageStorage()
                .delete(toLongId(dispatch.id())));
    }

    @Override
    public Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
        return Flux.fromIterable(dispatch.ids())
                .map(LocalStoreLayout::toLongId)
                .map(channelStorage.nodeForId(toLongId(dispatch.channelId())).getMessageStorage()::delete)
                .flatMap(Mono::justOrEmpty)
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
        return Mono.fromRunnable(() -> channelStorage.nodeForId(toLongId(dispatch.channelId())).getMessageStorage()
                .updateIfPresent(toLongId(dispatch.messageId()), existing -> {
                    boolean me = Objects.equals(dispatch.userId(), selfUser.get().get().id());
                    ImmutableMessageData.Builder newMessageBuilder = MessageData.builder().from(existing);

                    if (existing.reactions().isAbsent()) {
                        newMessageBuilder.addReaction(ReactionData.builder()
                                .count(1)
                                .me(me)
                                .emoji(dispatch.emoji())
                                .build());
                    } else {
                        List<ReactionData> reactions = existing.reactions().get();
                        int i = indexOfReaction(reactions, dispatch.emoji());

                        if (i != -1) {
                            // message already has this reaction: bump 1
                            ReactionData oldExisting = reactions.get(i);
                            ReactionData newExisting = ReactionData.builder()
                                    .from(oldExisting)
                                    .me(oldExisting.me() || me)
                                    .count(oldExisting.count() + 1)
                                    .build();
                            newMessageBuilder.reactions(reactions.stream()
                                    .map(r -> r.equals(oldExisting) ? newExisting : r)
                                    .collect(Collectors.toList()));
                        } else {
                            // message doesn't have this reaction: create
                            ReactionData reaction = ReactionData.builder()
                                    .emoji(dispatch.emoji())
                                    .me(me)
                                    .count(1)
                                    .build();
                            newMessageBuilder.addReaction(reaction);
                        }
                    }

                    return newMessageBuilder.build();
                }));
    }

    @Override
    public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        return Mono.fromRunnable(() -> channelStorage.nodeForId(toLongId(dispatch.channelId())).getMessageStorage()
                .updateIfPresent(toLongId(dispatch.messageId()), existing -> {
                    if (existing.reactions().isAbsent()) {
                        return existing;
                    }
                    boolean me = Objects.equals(dispatch.userId(), selfUser.get().get().id());
                    ImmutableMessageData.Builder newMessageBuilder = MessageData.builder().from(existing);

                    List<ReactionData> reactions = existing.reactions().get();
                    int i = indexOfReaction(reactions, dispatch.emoji());

                    if (i != -1) {
                        ReactionData oldExisting = reactions.get(i);
                        if (oldExisting.count() - 1 == 0) {
                            newMessageBuilder.reactions(reactions.stream()
                                    .filter(r -> !r.equals(oldExisting))
                                    .collect(Collectors.toList()));
                        } else {
                            ReactionData newExisting = ReactionData.builder()
                                    .from(oldExisting)
                                    .count(oldExisting.count() - 1)
                                    .me(!me && oldExisting.me())
                                    .build();
                            newMessageBuilder.reactions(reactions.stream()
                                    .map(r -> r.equals(oldExisting) ? newExisting : r)
                                    .collect(Collectors.toList()));
                        }
                    }
                    return newMessageBuilder.build();
                }));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
        return Mono.fromRunnable(() -> channelStorage.nodeForId(toLongId(dispatch.channelId())).getMessageStorage()
                .updateIfPresent(toLongId(dispatch.messageId()), existing -> MessageData.builder()
                        .from(existing)
                        .reactions(Possible.absent())
                        .build()));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
        return Mono.fromRunnable(() -> channelStorage.nodeForId(toLongId(dispatch.channelId())).getMessageStorage()
                .updateIfPresent(toLongId(dispatch.messageId()), existing -> {
                    if (existing.reactions().isAbsent()) {
                        return existing;
                    }
                    ImmutableMessageData.Builder newMessageBuilder = MessageData.builder().from(existing);

                    List<ReactionData> reactions = existing.reactions().get();
                    int i = indexOfReaction(reactions, dispatch.emoji());

                    if (i != -1) {
                        ReactionData oldExisting = reactions.get(i);
                        newMessageBuilder.reactions(reactions.stream()
                                .filter(r -> !r.equals(oldExisting))
                                .collect(Collectors.toList()));
                    }
                    return newMessageBuilder.build();
                }));
    }

    @Override
    public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
        PartialMessageData messageData = dispatch.message();
        return Mono.justOrEmpty(channelStorage.nodeForId(toLongId(messageData.channelId())).getMessageStorage()
                .updateIfPresent(toLongId(messageData.id()), existing -> MessageData.builder()
                        .from(existing)
                        .content(messageData.content().toOptional()
                                .orElse(existing.content()))
                        .embeds(messageData.embeds())
                        .mentions(messageData.mentions())
                        .mentionRoles(messageData.mentionRoles())
                        .mentionEveryone(messageData.mentionEveryone().toOptional()
                                .orElse(existing.mentionEveryone()))
                        .editedTimestamp(messageData.editedTimestamp())
                        .build()));
    }

    @Override
    public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        return Mono.fromCallable(() -> {
            PartialUserData userData = dispatch.user();
            UserData oldUser = userStorage.find(toLongId(userData.id())).map(AtomicReference::get).orElse(null);
            PresenceData oldPresence = guildStorage.nodeForId(toLongId(dispatch.guildId())).getPresenceStorage()
                    .update(toLongId(userData.id()), existing -> createPresence(dispatch))
                    .orElse(null);
            if (oldUser == null && oldPresence == null) {
                return null;
            }
            return PresenceAndUserData.of(oldPresence, oldUser);
        });
    }

    @Override
    public Mono<Void> onReady(Ready dispatch) {
        return Mono.fromRunnable(() -> {
            AtomicReference<UserData> ref = new AtomicReference<>(dispatch.user());
            if (selfUser.compareAndSet(null, ref)) {
                userStorage.insert(ref);
            }
            shardCount.compareAndSet(0, dispatch.shard().toOptional()
                    .map(shard -> shard[1])
                    .orElse(1));
        });
    }

    @Override
    public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
        return Mono.justOrEmpty(userStorage.update(toLongId(dispatch.user().id()), existing -> {
            if (existing != null) {
                existing.set(dispatch.user());
                return existing;
            }
            return new AtomicReference<>(dispatch.user());
        })).map(AtomicReference::get);
    }

    @Override
    public Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch) {
        VoiceStateData data = dispatch.voiceState();
        return Mono.justOrEmpty(data.guildId().toOptional())
                .map(LocalStoreLayout::toLongId)
                .map(guildId -> data.channelId().isPresent()
                        ? guildStorage.nodeForId(guildId).getVoiceStateStorage()
                                .update(toLongId(data.userId()), existing -> data)
                        : guildStorage.nodeForId(guildId).getVoiceStateStorage()
                                .delete(toLongId(data.userId())))
                .flatMap(Mono::justOrEmpty);
    }

    @Override
    public Mono<Void> onGuildMembersCompletion(long guildId) {
        return Mono.fromRunnable(guildStorage.nodeForId(guildId)::completeMemberList);
    }

    @Override
    public DataAccessor getDataAccessor() {
        return this;
    }

    @Override
    public GatewayDataUpdater getGatewayDataUpdater() {
        return this;
    }
    
    static long toLongId(String stringId) {
        return Long.parseUnsignedLong(stringId);
    }

    private void addMember(long guildId, MemberData member) {
        guildStorage.nodeForId(guildId).getMemberStorage().insert(member);
        guildStorage.updateIfPresent(guildId, guildData -> GuildData.builder()
                .from(guildData)
                .addMember(member.user().id())
                .build());
    }

    private static int indexOfReaction(List<ReactionData> reactions, EmojiData emoji) {
        for (int i = 0; i < reactions.size(); i++) {
            ReactionData r = reactions.get(i);
            // (non-null id && matching id) OR (null id && matching name)
            boolean emojiHasId = emoji.id().isPresent();
            if ((emojiHasId && emoji.id().equals(r.emoji().id()))
                    || (!emojiHasId && emoji.name().equals(r.emoji().name()))) {
                return i;
            }
        }
        return -1;
    }

    private static PresenceData createPresence(PresenceUpdate update) {
        return PresenceData.builder()
                .user(update.user())
                .status(update.status())
                .activities(update.activities())
                .clientStatus(update.clientStatus())
                .build();
    }
}
