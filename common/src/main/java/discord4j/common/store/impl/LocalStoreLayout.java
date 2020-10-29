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

import com.github.benmanes.caffeine.cache.Caffeine;
import discord4j.common.store.api.layout.DataAccessor;
import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.layout.StoreLayout;
import discord4j.common.store.api.object.ExactResultNotAvailableException;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static discord4j.common.store.impl.ImplUtils.*;

public class LocalStoreLayout implements StoreLayout, DataAccessor, GatewayDataUpdater {

    private final StorageConfig config;

    private final ConcurrentMap<Long, GuildContent> contentByGuild = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ChannelContent> contentByChannel = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, StoredChannelData> channels = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, EmojiData> emojis = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StoredGuildData> guilds = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long2, StoredMemberData> members = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StoredMessageData> messages;
    private final ConcurrentMap<Long2, StoredPresenceData> presences = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, RoleData> roles = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, AtomicReference<UserData>> users =
            StorageBackend.caffeine(Caffeine::weakValues).newMap();
    private final ConcurrentMap<Long2, VoiceStateData> voiceStates = new ConcurrentHashMap<>();

    private final Set<Integer> shardsConnected = new HashSet<>();
    private volatile AtomicReference<UserData> selfUser;
    private volatile int shardCount;

    private LocalStoreLayout(StorageConfig config) {
        this.messages = config.getMessageBackend().newMap();
        this.config = config;
    }

    public static LocalStoreLayout create(StorageConfig config) {
        return new LocalStoreLayout(config);
    }

    public static LocalStoreLayout create() {
        return create(StorageConfig.builder().build());
    }

    // ------------ DataAccessor countX methods ------------

    @Override
    public Mono<Long> countChannels() {
        return Mono.just((long) channels.size());
    }

    @Override
    public Mono<Long> countChannelsInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .map(content -> (long) content.channelIds.size());
    }

    @Override
    public Mono<Long> countEmojis() {
        return Mono.just((long) emojis.size());
    }

    @Override
    public Mono<Long> countEmojisInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .map(content -> (long) content.emojiIds.size());
    }

    @Override
    public Mono<Long> countGuilds() {
        return Mono.just((long) guilds.size());
    }

    @Override
    public Mono<Long> countMembers() {
        return Mono.just((long) members.size());
    }

    @Override
    public Mono<Long> countMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .map(content -> (long) content.memberIds.size());
    }

    @Override
    public Mono<Long> countExactMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .filter(GuildContent::isMemberListComplete)
                .switchIfEmpty(Mono.error(ExactResultNotAvailableException::new))
                .map(content -> (long) content.memberIds.size());
    }

    @Override
    public Mono<Long> countMessages() {
        return Mono.just((long) messages.size());
    }

    @Override
    public Mono<Long> countMessagesInChannel(long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .map(content -> (long) content.messageIds.size());
    }

    @Override
    public Mono<Long> countPresences() {
        return Mono.just((long) presences.size());
    }

    @Override
    public Mono<Long> countPresencesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .map(content -> (long) content.presenceIds.size());
    }

    @Override
    public Mono<Long> countRoles() {
        return Mono.just((long) roles.size());
    }

    @Override
    public Mono<Long> countRolesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .map(content -> (long) content.roleIds.size());
    }

    @Override
    public Mono<Long> countUsers() {
        return Mono.just((long) users.size());
    }

    @Override
    public Mono<Long> countVoiceStates() {
        return Mono.just((long) voiceStates.size());
    }

    @Override
    public Mono<Long> countVoiceStatesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .map(content -> (long) content.voiceStateIds.size());
    }

    @Override
    public Mono<Long> countVoiceStatesInChannel(long guildId, long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .map(content -> (long) content.voiceStateIds.size());
    }

    // ------------ DataAccessor getX methods ------------

    @Override
    public Flux<ChannelData> getChannels() {
        return Flux.fromIterable(channels.values()).map(ImmutableChannelData::copyOf);
    }

    @Override
    public Flux<ChannelData> getChannelsInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.channelIds)
                .flatMap(id -> Mono.justOrEmpty(channels.get(id)))
                .map(ImmutableChannelData::copyOf);
    }

    @Override
    public Mono<ChannelData> getChannelById(long channelId) {
        return Mono.justOrEmpty(channels.get(channelId)).map(ImmutableChannelData::copyOf);
    }

    @Override
    public Flux<EmojiData> getEmojis() {
        return Flux.fromIterable(emojis.values());
    }

    @Override
    public Flux<EmojiData> getEmojisInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.emojiIds)
                .flatMap(id -> Mono.justOrEmpty(emojis.get(id)));
    }

    @Override
    public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
        return Mono.justOrEmpty(emojis.get(emojiId));
    }

    @Override
    public Flux<GuildData> getGuilds() {
        return Flux.fromIterable(guilds.values()).map(ImmutableGuildData::copyOf);
    }

    @Override
    public Mono<GuildData> getGuildById(long guildId) {
        return Mono.justOrEmpty(guilds.get(guildId)).map(ImmutableGuildData::copyOf);
    }

    @Override
    public Flux<MemberData> getMembers() {
        return Flux.fromIterable(members.values()).map(ImmutableMemberData::copyOf);
    }

    @Override
    public Flux<MemberData> getMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.memberIds)
                .flatMap(id -> Mono.justOrEmpty(members.get(id)))
                .map(ImmutableMemberData::copyOf);
    }

    @Override
    public Flux<MemberData> getExactMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .filter(GuildContent::isMemberListComplete)
                .switchIfEmpty(Mono.error(ExactResultNotAvailableException::new))
                .flatMapIterable(content -> content.memberIds)
                .flatMap(id -> Mono.justOrEmpty(members.get(id)))
                .map(ImmutableMemberData::copyOf);
    }

    @Override
    public Mono<MemberData> getMemberById(long guildId, long userId) {
        return Mono.justOrEmpty(members.get(new Long2(guildId, userId)))
                .map(ImmutableMemberData::copyOf);
    }

    @Override
    public Flux<MessageData> getMessages() {
        return Flux.fromIterable(messages.values()).map(ImmutableMessageData::copyOf);
    }

    @Override
    public Flux<MessageData> getMessagesInChannel(long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .flatMapIterable(content -> content.messageIds)
                .flatMap(id -> Mono.justOrEmpty(messages.get(id)))
                .map(ImmutableMessageData::copyOf);
    }

    @Override
    public Mono<MessageData> getMessageById(long channelId, long messageId) {
        return Mono.justOrEmpty(messages.get(messageId)).map(ImmutableMessageData::copyOf);
    }

    @Override
    public Flux<PresenceData> getPresences() {
        return Flux.fromIterable(presences.values()).map(ImmutablePresenceData::copyOf);
    }

    @Override
    public Flux<PresenceData> getPresencesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.presenceIds)
                .flatMap(id -> Mono.justOrEmpty(presences.get(id)))
                .map(ImmutablePresenceData::copyOf);
    }

    @Override
    public Mono<PresenceData> getPresenceById(long guildId, long userId) {
        return Mono.justOrEmpty(presences.get(new Long2(guildId, userId))).map(ImmutablePresenceData::copyOf);
    }

    @Override
    public Flux<RoleData> getRoles() {
        return Flux.fromIterable(roles.values());
    }

    @Override
    public Flux<RoleData> getRolesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.roleIds)
                .flatMap(id -> Mono.justOrEmpty(roles.get(id)));
    }

    @Override
    public Mono<RoleData> getRoleById(long guildId, long roleId) {
        return Mono.justOrEmpty(roles.get(roleId));
    }

    @Override
    public Flux<UserData> getUsers() {
        return Flux.fromIterable(users.values()).map(AtomicReference::get);
    }

    @Override
    public Mono<UserData> getUserById(long userId) {
        return Mono.justOrEmpty(users.get(userId)).map(AtomicReference::get);
    }

    @Override
    public Flux<VoiceStateData> getVoiceStates() {
        return Flux.fromIterable(voiceStates.values());
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .flatMapIterable(content -> content.voiceStateIds)
                .flatMap(id -> Mono.justOrEmpty(voiceStates.get(id)));
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.voiceStateIds)
                .flatMap(id -> Mono.justOrEmpty(voiceStates.get(id)));
    }

    @Override
    public Mono<VoiceStateData> getVoiceStateById(long guildId, long userId) {
        return Mono.justOrEmpty(voiceStates.get(new Long2(guildId, userId)));
    }

    // ------------ GatewayDataUpdater methods ------------

    @Override
    public Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch) {
        return Mono.fromRunnable(() -> saveChannel(dispatch.channel()));
    }

    @Override
    public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
        ChannelData data = dispatch.channel();
        return Mono.fromRunnable(() -> data.guildId().toOptional()
                .map(ImplUtils::toLongId)
                .ifPresent(guildId -> {
                    long channelId = toLongId(data.id());
                    ifNonNullDo(contentByChannel.get(channelId), ChannelContent::dispose);
                    ifNonNullDo(guilds.get(guildId), guild -> guild.channelIdSet().remove(channelId));
                })).thenReturn(data);
    }

    @Override
    public Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch) {
        return Mono.fromCallable(() -> saveChannel(dispatch.channel()));
    }
    
    @Override
    public Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch) {
        return Mono.fromRunnable(() -> {
            GuildCreateData createData = dispatch.guild();
            List<RoleData> roles = createData.roles();
            List<EmojiData> emojis = createData.emojis();
            List<MemberData> members = createData.members();
            List<ChannelData> channels = createData.channels();
            List<PresenceData> presences = createData.presences();
            List<VoiceStateData> voiceStates = createData.voiceStates();
            GuildData guild = GuildData.builder()
                    .from(createData)
                    .roles(Collections.emptyList())
                    .emojis(Collections.emptyList())
                    .members(Collections.emptyList())
                    .channels(Collections.emptyList())
                    .build();
            long guildId = toLongId(guild.id());
            guilds.put(guildId, new StoredGuildData(guild));
            roles.forEach(role -> saveRole(guildId, role));
            emojis.forEach(emoji -> saveEmoji(guildId, emoji));
            members.forEach(member -> saveMember(guildId, member));
            channels.forEach(this::saveChannel);
            presences.forEach(presence -> savePresence(guildId, presence));
            voiceStates.forEach(voiceState -> saveOrRemoveVoiceState(guildId, voiceState));
        });
    }

    @Override
    public Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch) {
        long guildId = toLongId(dispatch.guild().id());
        return Mono.fromCallable(() -> ifNonNullMap(contentByGuild.get(guildId), GuildContent::dispose));
    }

    @Override
    public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        long guildId = toLongId(dispatch.guildId());
        return Mono.fromCallable(() -> {
            GuildContent content = computeGuildContent(guildId);
            Set<EmojiData> old = content.emojiIds.stream()
                    .map(emojis::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            emojis.keySet().removeAll(content.emojiIds);
            ifNonNullDo(guilds.get(guildId), guild -> guild.emojiIdSet().removeAll(content.emojiIds));
            content.emojiIds.clear();
            dispatch.emojis().forEach(emoji -> saveEmoji(guildId, emoji));
            return old;
        });
    }

    @Override
    public Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch) {
        return Mono.fromRunnable(() -> saveMember(toLongId(dispatch.guildId()), dispatch.member()));
    }

    @Override
    public Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
        long guildId = toLongId(dispatch.guildId());
        long userId = toLongId(dispatch.user().id());
        return Mono.fromCallable(() -> {
            Long2 memberId = new Long2(guildId, userId);
            computeGuildContent(guildId).memberIds.remove(memberId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.memberIdSet().remove(memberId.b));
            return ifNonNullMap(members.remove(memberId), ImmutableMemberData::copyOf);
        });
    }

    @Override
    public Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
        long guildId = toLongId(dispatch.guildId());
        return Mono.fromRunnable(() -> dispatch.members()
                .forEach(member -> saveMember(guildId, member)));
    }

    @Override
    public Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
        long guildId = toLongId(dispatch.guildId());
        long userId = toLongId(dispatch.user().id());
        return Mono.fromCallable(() -> ifNonNullMap(members.get(new Long2(guildId, userId)), member -> {
            MemberData old = ImmutableMemberData.copyOf(member);
            member.update(dispatch);
            return old;
        }));
    }

    @Override
    public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        return Mono.fromRunnable(() -> saveRole(toLongId(dispatch.guildId()), dispatch.role()));
    }

    @Override
    public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        long guildId = toLongId(dispatch.guildId());
        long roleId = toLongId(dispatch.roleId());
        return Mono.fromCallable(() -> {
            GuildContent content = computeGuildContent(guildId);
            content.roleIds.remove(roleId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.roleIdSet().remove(roleId));
            content.memberIds.stream()
                    .map(members::get)
                    .filter(Objects::nonNull)
                    .forEach(member -> member.roleIdSet().remove(roleId));
            return roles.remove(roleId);
        });
    }

    @Override
    public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        return Mono.fromCallable(() -> saveRole(toLongId(dispatch.guildId()), dispatch.role()));
    }

    @Override
    public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
        long guildId = toLongId(dispatch.guild().id());
        return Mono.fromCallable(() -> ifNonNullMap(guilds.get(guildId),
                existing -> ifNonNullMap(guilds.put(guildId, new StoredGuildData(GuildData.builder()
                        .from(existing)
                        .from(dispatch.guild())
                        .build())), ImmutableGuildData::copyOf)));
    }

    @Override
    public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
        return Mono.fromRunnable(() -> {
            synchronized (shardsConnected) {
                shardsConnected.remove(shardIndex);
                if (config.getInvalidationFilter().contains(cause)) {
                    contentByGuild.entrySet().stream()
                            .filter(entry -> ((entry.getKey() >> 22) % shardCount) == shardIndex)
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toSet())
                            .forEach(GuildContent::dispose);
                }
                if (shardsConnected.isEmpty()) {
                    shardCount = 0;
                }
            }
        });
    }

    @Override
    public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
        MessageData message = dispatch.message();
        long messageId = toLongId(message.id());
        long channelId = toLongId(message.channelId());
        return Mono.fromRunnable(() -> {
            ifNonNullDo(channels.get(channelId), channel -> channel.setLastMessageId(messageId));
            computeChannelContent(channelId).messageIds.add(messageId);
            messages.put(messageId, new StoredMessageData(message));
        });
    }

    @Override
    public Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch) {
        long messageId = toLongId(dispatch.id());
        long channelId = toLongId(dispatch.channelId());
        return Mono.fromCallable(() -> deleteMessage(channelId, messageId));
    }

    @Override
    public Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
        return Mono.fromCallable(() -> dispatch.ids().stream()
                .map(ImplUtils::toLongId)
                .map(messageId -> deleteMessage(toLongId(dispatch.channelId()), messageId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    @Override
    public Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
        boolean me = Objects.equals(dispatch.userId(), selfUser.get().id());
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(toLongId(dispatch.messageId())),
                message -> message.addReaction(dispatch.emoji(), me)));
    }

    @Override
    public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        boolean me = Objects.equals(dispatch.userId(), selfUser.get().id());
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(toLongId(dispatch.messageId())),
                message -> message.removeReaction(dispatch.emoji(), me)));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(toLongId(dispatch.messageId())),
                StoredMessageData::removeAllReactions));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(toLongId(dispatch.messageId())),
                message -> message.removeReactionEmoji(dispatch.emoji())));
    }

    @Override
    public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
        return Mono.fromCallable(() -> ifNonNullMap(messages.get(toLongId(dispatch.message().id())), message -> {
            MessageData old = ImmutableMessageData.copyOf(message);
            message.update(dispatch.message());
            return old;
        }));
    }

    @Override
    public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        return Mono.fromCallable(() -> savePresence(toLongId(dispatch.guildId()), createPresence(dispatch)));
    }

    @Override
    public Mono<Void> onReady(Ready dispatch) {
        return Mono.fromRunnable(() -> {
            int[] shardInfo = dispatch.shard().toOptional().orElseGet(() -> new int[] {0, 1});
            synchronized (shardsConnected) {
                if (selfUser == null) {
                    selfUser = new AtomicReference<>(dispatch.user());
                    users.put(toLongId(dispatch.user().id()), selfUser);
                }
                if (shardCount == 0) {
                    shardCount = shardInfo[1];
                }
                shardsConnected.add(shardInfo[0]);
            }
        });
    }

    @Override
    public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
        return Mono.fromCallable(() -> ifNonNullMap(users.get(toLongId(dispatch.user().id())),
                userRef -> userRef.getAndSet(dispatch.user())));
    }

    @Override
    public Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch) {
        VoiceStateData voiceState = dispatch.voiceState();
        return Mono.justOrEmpty(voiceState.guildId().toOptional())
                .map(ImplUtils::toLongId)
                .flatMap(guildId -> Mono.fromCallable(() -> saveOrRemoveVoiceState(guildId, voiceState)));
    }

    @Override
    public Mono<Void> onGuildMembersCompletion(long guildId) {
        return Mono.fromRunnable(() -> ifNonNullDo(contentByGuild.get(guildId), GuildContent::completeMemberList));
    }

    @Override
    public DataAccessor getDataAccessor() {
        return this;
    }

    @Override
    public GatewayDataUpdater getGatewayDataUpdater() {
        return this;
    }

    // ------------ Private methods ------------

    private GuildContent computeGuildContent(long guildId) {
        return contentByGuild.computeIfAbsent(guildId, GuildContent::new);
    }

    private ChannelContent computeChannelContent(long channelId) {
        return contentByChannel.computeIfAbsent(channelId, ChannelContent::new);
    }

    private @Nullable ChannelData saveChannel(ChannelData channel) {
        return channel.guildId().toOptional()
                .map(ImplUtils::toLongId)
                .map(guildId -> {
                    long channelId = toLongId(channel.id());
                    computeGuildContent(guildId).channelIds.add(channelId);
                    ifNonNullDo(guilds.get(guildId), guild -> guild.channelIdSet().add(channelId));
                    return ifNonNullMap(channels.put(channelId, new StoredChannelData(channel)),
                            ImmutableChannelData::copyOf);
                })
                .orElse(null);
    }

    private @Nullable RoleData saveRole(long guildId, RoleData role) {
        long roleId = toLongId(role.id());
        computeGuildContent(guildId).roleIds.add(roleId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.roleIdSet().add(roleId));
        return roles.put(roleId, role);
    }

    private void saveEmoji(long guildId, EmojiData emoji) {
        emoji.id().map(ImplUtils::toLongId).ifPresent(emojiId -> {
            computeGuildContent(guildId).emojiIds.add(emojiId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.emojiIdSet().add(emojiId));
            emojis.put(emojiId, emoji);
        });
    }

    private void saveMember(long guildId, MemberData member) {
        Long2 memberId = new Long2(guildId, toLongId(member.user().id()));
        computeGuildContent(guildId).memberIds.add(memberId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.memberIdSet().add(memberId.b));
        AtomicReference<UserData> userRef = Objects.requireNonNull(
                computeUserRef(memberId.b, member, ImplUtils::userFromMember));
        members.put(memberId, new StoredMemberData(member, userRef));
    }

    private @Nullable PresenceAndUserData savePresence(long guildId, PresenceData presence) {
        Long2 presenceId = new Long2(guildId, toLongId(presence.user().id()));
        UserData oldUser = Optional.ofNullable(users.get(presenceId.b)).map(AtomicReference::get).orElse(null);
        return ifNonNullMap(computeUserRef(presenceId.b, presence, ImplUtils::userFromPresence), userRef -> {
            computeGuildContent(guildId).presenceIds.add(presenceId);
            PresenceData oldPresence = presences.put(presenceId, new StoredPresenceData(presence, userRef));
            if (oldPresence == null && oldUser == null) {
                return null;
            }
            return PresenceAndUserData.of(ifNonNullMap(oldPresence, ImmutablePresenceData::copyOf), oldUser);
        });
    }

    private @Nullable VoiceStateData saveOrRemoveVoiceState(long guildId, VoiceStateData voiceState) {
        Long2 voiceStateId = new Long2(guildId, toLongId(voiceState.userId()));
        if (voiceState.channelId().isPresent()) {
            computeGuildContent(guildId).voiceStateIds.add(voiceStateId);
            computeChannelContent(toLongId(voiceState.channelId().get())).voiceStateIds.add(voiceStateId);
            return voiceStates.put(voiceStateId, voiceState);
        } else {
            computeGuildContent(guildId).voiceStateIds.remove(voiceStateId);
            computeChannelContent(toLongId(voiceState.channelId().get())).voiceStateIds.remove(voiceStateId);
            return voiceStates.remove(voiceStateId);
        }
    }

    private @Nullable MessageData deleteMessage(long channelId, long messageId) {
        computeChannelContent(channelId).messageIds.remove(messageId);
        return ifNonNullMap(messages.remove(messageId), ImmutableMessageData::copyOf);
    }

    private @Nullable <T> AtomicReference<UserData> computeUserRef(long userId, T newData,
                                                                   BiFunction<T, UserData, UserData> userUpdater) {
        for (;;) {
            AtomicReference<UserData> existing = users.get(userId);
            AtomicReference<UserData> ref;
            if (existing == null) {
                UserData newUser = userUpdater.apply(newData, null);
                if (newUser == null) {
                    return null;
                } else {
                    ref = new AtomicReference<>(newUser);
                    if (users.putIfAbsent(userId, ref) != null) {
                        continue;
                    }
                }
            } else {
                ref = existing;
                UserData oldUser = ref.get();
                UserData newUser = userUpdater.apply(newData, oldUser);
                if (newUser != null) {
                    if (!ref.compareAndSet(oldUser, newUser)) {
                        continue;
                    }
                }
            }
            return ref;
        }
    }

    // ------------ Internal classes ------------

    private class GuildContent {

        private final long guildId;
        private final Set<Long> channelIds = Collections.synchronizedSet(new HashSet<>());
        private final Set<Long> emojiIds = Collections.synchronizedSet(new HashSet<>());
        private final Set<Long2> memberIds = Collections.synchronizedSet(new HashSet<>());
        private final Set<Long2> presenceIds = Collections.synchronizedSet(new HashSet<>());
        private final Set<Long> roleIds = Collections.synchronizedSet(new HashSet<>());
        private final Set<Long2> voiceStateIds = Collections.synchronizedSet(new HashSet<>());
        private volatile boolean memberListComplete;

        public GuildContent(long guildId) {
            this.guildId = guildId;
        }

        private void completeMemberList() {
            memberListComplete = true;
        }

        private boolean isMemberListComplete() {
            return memberListComplete;
        }

        private @Nullable GuildData dispose() {
            GuildData old = guilds.remove(guildId);
            contentByGuild.remove(guildId);
            contentByChannel.values().stream()
                    .filter(content -> channelIds.contains(content.channelId))
                    .collect(Collectors.toSet())
                    .forEach(ChannelContent::dispose);
            emojis.keySet().removeAll(emojiIds);
            members.keySet().removeAll(memberIds);
            presences.keySet().removeAll(presenceIds);
            roles.keySet().removeAll(roleIds);
            voiceStates.keySet().removeAll(voiceStateIds);
            return old;
        }
    }

    private class ChannelContent {

        private final long channelId;
        private final Set<Long> messageIds = Collections.synchronizedSet(new HashSet<>());
        private final Set<Long2> voiceStateIds = Collections.synchronizedSet(new HashSet<>());

        public ChannelContent(long channelId) {
            this.channelId = channelId;
        }

        private void dispose() {
            channels.remove(channelId);
            contentByChannel.remove(channelId);
            messages.keySet().removeAll(messageIds);
            voiceStates.keySet().removeAll(voiceStateIds);
        }
    }

    private static class Long2 {
        private final long a, b;

        private Long2(long a, long b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Long2)) return false;
            Long2 long2 = (Long2) o;
            return a == long2.a &&
                    b == long2.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }
}
