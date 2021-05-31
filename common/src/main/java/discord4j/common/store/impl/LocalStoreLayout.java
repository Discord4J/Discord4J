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
import discord4j.discordjson.Id;
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

/**
 * A {@link StoreLayout} that stores entities in the heap of the local JVM. This implementation is entirely
 * thread-safe: atomicity guarantees are made so that the cache won't be set in an inconsistent state if two events
 * are received concurrently. However, it is still up to the user to ensure that events are received in the right order.
 */
public class LocalStoreLayout implements StoreLayout, DataAccessor, GatewayDataUpdater {

    private final StorageConfig config;

    private final ConcurrentMap<Long, GuildContent> contentByGuild = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ChannelContent> contentByChannel = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, StoredChannelData> channels = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StoredEmojiData> emojis = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StoredGuildData> guilds = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long2, StoredMemberData> members = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long2, StoredMessageData> messages;
    private final ConcurrentMap<Long2, StoredPresenceData> presences = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, StoredRoleData> roles = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, AtomicReference<StoredUserData>> users =
            StorageBackend.caffeine(Caffeine::weakValues).newMap();
    private final ConcurrentMap<Long2, StoredVoiceStateData> voiceStates = new ConcurrentHashMap<>();

    private final Set<Integer> shardsConnected = new HashSet<>();
    private volatile AtomicReference<StoredUserData> selfUser;
    private volatile int shardCount;

    private LocalStoreLayout(StorageConfig config) {
        this.messages = config.getMessageBackend().newMap((k, v, reason) -> {
            if (k != null && reason.wasEvicted()) {
                ifNonNullDo(contentByChannel.get(k.a), content -> content.messageIds.remove(k));
            }
        });
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
        return Flux.fromIterable(channels.values()).map(StoredChannelData::toImmutable);
    }

    @Override
    public Flux<ChannelData> getChannelsInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.channelIds)
                .flatMap(id -> Mono.justOrEmpty(channels.get(id)))
                .map(StoredChannelData::toImmutable);
    }

    @Override
    public Mono<ChannelData> getChannelById(long channelId) {
        return Mono.justOrEmpty(channels.get(channelId)).map(StoredChannelData::toImmutable);
    }

    @Override
    public Flux<EmojiData> getEmojis() {
        return Flux.fromIterable(emojis.values()).map(StoredEmojiData::toImmutable);
    }

    @Override
    public Flux<EmojiData> getEmojisInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.emojiIds)
                .flatMap(id -> Mono.justOrEmpty(emojis.get(id)))
                .map(StoredEmojiData::toImmutable);
    }

    @Override
    public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
        return Mono.justOrEmpty(emojis.get(emojiId)).map(StoredEmojiData::toImmutable);
    }

    @Override
    public Flux<GuildData> getGuilds() {
        return Flux.fromIterable(guilds.values()).map(StoredGuildData::toImmutable);
    }

    @Override
    public Mono<GuildData> getGuildById(long guildId) {
        return Mono.justOrEmpty(guilds.get(guildId)).map(StoredGuildData::toImmutable);
    }

    @Override
    public Flux<MemberData> getMembers() {
        return Flux.fromIterable(members.values()).map(StoredMemberData::toImmutable);
    }

    @Override
    public Flux<MemberData> getMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.memberIds)
                .flatMap(id -> Mono.justOrEmpty(members.get(id)))
                .map(StoredMemberData::toImmutable);
    }

    @Override
    public Flux<MemberData> getExactMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .filter(GuildContent::isMemberListComplete)
                .switchIfEmpty(Mono.error(ExactResultNotAvailableException::new))
                .flatMapIterable(content -> content.memberIds)
                .flatMap(id -> Mono.justOrEmpty(members.get(id)))
                .map(StoredMemberData::toImmutable);
    }

    @Override
    public Mono<MemberData> getMemberById(long guildId, long userId) {
        return Mono.justOrEmpty(members.get(new Long2(guildId, userId)))
                .map(StoredMemberData::toImmutable);
    }

    @Override
    public Flux<MessageData> getMessages() {
        return Flux.fromIterable(messages.values()).map(this::toImmutableMessage);
    }

    @Override
    public Flux<MessageData> getMessagesInChannel(long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .flatMapIterable(content -> content.messageIds)
                .flatMap(id -> Mono.justOrEmpty(messages.get(id)))
                .map(this::toImmutableMessage);
    }

    @Override
    public Mono<MessageData> getMessageById(long channelId, long messageId) {
        return Mono.justOrEmpty(messages.get(new Long2(channelId, messageId)))
                .map(this::toImmutableMessage);
    }

    @Override
    public Flux<PresenceData> getPresences() {
        return Flux.fromIterable(presences.values()).map(StoredPresenceData::toImmutable);
    }

    @Override
    public Flux<PresenceData> getPresencesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.presenceIds)
                .flatMap(id -> Mono.justOrEmpty(presences.get(id)))
                .map(StoredPresenceData::toImmutable);
    }

    @Override
    public Mono<PresenceData> getPresenceById(long guildId, long userId) {
        return Mono.justOrEmpty(presences.get(new Long2(guildId, userId)))
                .map(StoredPresenceData::toImmutable);
    }

    @Override
    public Flux<RoleData> getRoles() {
        return Flux.fromIterable(roles.values()).map(StoredRoleData::toImmutable);
    }

    @Override
    public Flux<RoleData> getRolesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.roleIds)
                .flatMap(id -> Mono.justOrEmpty(roles.get(id)))
                .map(StoredRoleData::toImmutable);
    }

    @Override
    public Mono<RoleData> getRoleById(long guildId, long roleId) {
        return Mono.justOrEmpty(roles.get(roleId)).map(StoredRoleData::toImmutable);
    }

    @Override
    public Flux<UserData> getUsers() {
        return Flux.fromIterable(users.values())
                .map(AtomicReference::get)
                .map(StoredUserData::toImmutable);
    }

    @Override
    public Mono<UserData> getUserById(long userId) {
        return Mono.justOrEmpty(users.get(userId))
                .map(AtomicReference::get)
                .map(StoredUserData::toImmutable);
    }

    @Override
    public Flux<VoiceStateData> getVoiceStates() {
        return Flux.fromIterable(voiceStates.values()).map(this::toImmutableVoiceState);
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .flatMapIterable(content -> content.voiceStateIds)
                .flatMap(id -> Mono.justOrEmpty(voiceStates.get(id)))
                .map(this::toImmutableVoiceState);
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.voiceStateIds)
                .flatMap(id -> Mono.justOrEmpty(voiceStates.get(id)))
                .map(this::toImmutableVoiceState);
    }

    @Override
    public Mono<VoiceStateData> getVoiceStateById(long guildId, long userId) {
        return Mono.justOrEmpty(voiceStates.get(new Long2(guildId, userId)))
                .map(this::toImmutableVoiceState);
    }

    // ------------ GatewayDataUpdater methods ------------

    @Override
    public Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch) {
        return Mono.fromRunnable(() -> dispatch.channel().guildId().toOptional()
                .ifPresent(guildId -> saveChannel(guildId.asLong(), dispatch.channel())));
    }

    @Override
    public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
        ChannelData data = dispatch.channel();
        return Mono.fromRunnable(() -> data.guildId().toOptional()
                .map(Id::asLong)
                .ifPresent(guildId -> {
                    long channelId = data.id().asLong();
                    GuildContent guildContent = computeGuildContent(guildId);
                    guildContent.channelIds.remove(channelId);
                    ifNonNullDo(contentByChannel.get(channelId), ChannelContent::dispose);
                    ifNonNullDo(guilds.get(guildId), guild -> guild.channelIdSet().remove(channelId));
                })).thenReturn(data);
    }

    @Override
    public Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch) {
        return Mono.fromCallable(() -> dispatch.channel().guildId().toOptional()
                .map(guildId -> saveChannel(guildId.asLong(), dispatch.channel()))
                .orElse(null));
    }

    @Override
    public Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch) {
        return Mono.fromRunnable(() -> {
            long guildId = dispatch.guild().id().asLong();
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
            guilds.put(guildId, new StoredGuildData(guild));
            roles.forEach(role -> saveRole(guildId, role));
            emojis.forEach(emoji -> saveEmoji(guildId, emoji));
            members.forEach(member -> saveMember(guildId, member));
            channels.forEach(channel -> saveChannel(guildId, channel));
            presences.forEach(presence -> savePresence(guildId, presence));
            voiceStates.forEach(voiceState -> saveOrRemoveVoiceState(guildId, voiceState));
        });
    }

    @Override
    public Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch) {
        long guildId = dispatch.guild().id().asLong();
        return Mono.fromCallable(() -> ifNonNullMap(contentByGuild.get(guildId), GuildContent::dispose));
    }

    @Override
    public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        long guildId = dispatch.guildId().asLong();
        return Mono.fromCallable(() -> {
            GuildContent content = computeGuildContent(guildId);
            Set<EmojiData> old = content.emojiIds.stream()
                    .map(emojis::get)
                    .filter(Objects::nonNull)
                    .map(StoredEmojiData::toImmutable)
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
        return Mono.fromRunnable(() -> saveMember(dispatch.guildId().asLong(), dispatch.member()));
    }

    @Override
    public Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
        long guildId = dispatch.guildId().asLong();
        long userId = dispatch.user().id().asLong();
        return Mono.fromCallable(() -> {
            Long2 memberId = new Long2(guildId, userId);
            GuildContent guildContent = computeGuildContent(guildId);
            guildContent.memberIds.remove(memberId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.memberIdSet().remove(memberId.b));
            return ifNonNullMap(members.remove(memberId), StoredMemberData::toImmutable);
        });
    }

    @Override
    public Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
        long guildId = dispatch.guildId().asLong();
        return Mono.fromRunnable(() -> dispatch.members()
                .forEach(member -> saveMember(guildId, member)));
    }

    @Override
    public Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
        long guildId = dispatch.guildId().asLong();
        long userId = dispatch.user().id().asLong();
        Long2 id = new Long2(guildId, userId);
        return Mono.fromCallable(() -> ifNonNullMap(
                atomicGetAndReplace(members, id, oldMember -> new StoredMemberData(oldMember, dispatch)),
                StoredMemberData::toImmutable));
    }

    @Override
    public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        return Mono.fromRunnable(() -> saveRole(dispatch.guildId().asLong(), dispatch.role()));
    }

    @Override
    public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        long guildId = dispatch.guildId().asLong();
        long roleId = dispatch.roleId().asLong();
        return Mono.fromCallable(() -> {
            GuildContent guildContent = computeGuildContent(guildId);
            guildContent.roleIds.remove(roleId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.roleIdSet().remove(roleId));
            guildContent.memberIds.stream()
                    .map(members::get)
                    .filter(Objects::nonNull)
                    .forEach(member -> member.roleIdSet().remove(roleId));
            return ifNonNullMap(roles.remove(roleId), StoredRoleData::toImmutable);
        });
    }

    @Override
    public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        return Mono.fromCallable(() -> saveRole(dispatch.guildId().asLong(), dispatch.role()));
    }

    @Override
    public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
        long guildId = dispatch.guild().id().asLong();
        return Mono.fromCallable(() -> ifNonNullMap(
                atomicGetAndReplace(guilds, guildId, oldGuild -> new StoredGuildData(GuildData.builder()
                        .from(oldGuild.toImmutable())
                        .from(dispatch.guild())
                        .build())),
                StoredGuildData::toImmutable));
    }

    @Override
    public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
        return Mono.fromRunnable(() -> {
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
        });
    }

    @Override
    public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
        MessageData message = dispatch.message();
        long channelId = message.channelId().asLong();
        long messageId = message.id().asLong();
        Long2 id = new Long2(channelId, messageId);
        return Mono.fromRunnable(() -> {
            ChannelContent channelContent = computeChannelContent(id.a);
            ifNonNullDo(channels.get(id.a), channel -> channel.setLastMessageId(id.b));
            channelContent.messageIds.add(id);
            AtomicReference<StoredUserData> userRef = Objects.requireNonNull(
                    computeUserRef(id.b, message, ImplUtils::userFromMessage));
            messages.put(id, new StoredMessageData(message, userRef));
        });
    }

    @Override
    public Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch) {
        long messageId = dispatch.id().asLong();
        long channelId = dispatch.channelId().asLong();
        return Mono.fromCallable(() -> deleteMessage(channelId, messageId));
    }

    @Override
    public Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
        return Mono.fromCallable(() -> dispatch.ids().stream()
                .map(Id::asLong)
                .map(messageId -> deleteMessage(dispatch.channelId().asLong(), messageId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    @Override
    public Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
        boolean me = dispatch.userId().asLong() == selfUser.get().longId();
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(new Long2(channelId, messageId)),
                message -> message.addReaction(dispatch.emoji(), me)));
    }

    @Override
    public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        boolean me = dispatch.userId().asLong() == selfUser.get().longId();
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(new Long2(channelId, messageId)),
                message -> message.removeReaction(dispatch.emoji(), me)));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(new Long2(channelId, messageId)),
                StoredMessageData::removeAllReactions));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> ifNonNullDo(messages.get(new Long2(channelId, messageId)),
                message -> message.removeReactionEmoji(dispatch.emoji())));
    }

    @Override
    public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
        long channelId = dispatch.message().channelId().asLong();
        long messageId = dispatch.message().id().asLong();
        Long2 id = new Long2(channelId, messageId);
        return Mono.fromCallable(() -> ifNonNullMap(
                atomicGetAndReplace(messages, id,
                        oldMessage -> new StoredMessageData(oldMessage, dispatch.message())),
                this::toImmutableMessage));
    }

    @Override
    public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        return Mono.fromCallable(() -> savePresence(dispatch.guildId().asLong(), createPresence(dispatch)));
    }

    @Override
    public Mono<Void> onReady(Ready dispatch) {
        return Mono.fromRunnable(() -> {
            int[] shardInfo = dispatch.shard().toOptional().orElseGet(() -> new int[] {0, 1});
            if (selfUser == null) {
                StoredUserData storedUserData = new StoredUserData(dispatch.user());
                selfUser = new AtomicReference<>(storedUserData);
                users.put(storedUserData.longId(), selfUser);
            }
            if (shardCount == 0) {
                shardCount = shardInfo[1];
            }
            shardsConnected.add(shardInfo[0]);
        });
    }

    @Override
    public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
        return Mono.fromCallable(() -> ifNonNullMap(
                        users.get(dispatch.user().id().asLong()),
                        userRef -> userRef.getAndSet(new StoredUserData(dispatch.user()))))
                .map(StoredUserData::toImmutable);
    }

    @Override
    public Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch) {
        VoiceStateData voiceState = dispatch.voiceState();
        return Mono.justOrEmpty(voiceState.guildId().toOptional())
                .map(Id::asLong)
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

    private @Nullable ChannelData saveChannel(long guildId, ChannelData channel) {
        long channelId = channel.id().asLong();
        GuildContent guildContent = computeGuildContent(guildId);
        guildContent.channelIds.add(channelId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.channelIdSet().add(channelId));
        return ifNonNullMap(channels.put(channelId, new StoredChannelData(channel, guildId)),
                StoredChannelData::toImmutable);
    }

    private @Nullable RoleData saveRole(long guildId, RoleData role) {
        long roleId = role.id().asLong();
        GuildContent guildContent = computeGuildContent(guildId);
        guildContent.roleIds.add(roleId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.roleIdSet().add(roleId));
        return ifNonNullMap(roles.put(roleId, new StoredRoleData(role)), StoredRoleData::toImmutable);
    }

    private void saveEmoji(long guildId, EmojiData emoji) {
        emoji.id().map(Id::asLong).ifPresent(emojiId -> {
            // synchronization already done by callers of saveEmoji (onGuildCreate and onGuildEmojisUpdate)
            computeGuildContent(guildId).emojiIds.add(emojiId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.emojiIdSet().add(emojiId));
            AtomicReference<StoredUserData> userRef = ifNonNullMap(
                    emoji.user().toOptional()
                            .map(user -> user.id().asLong())
                            .orElse(null),
                    userId -> computeUserRef(userId, emoji, ImplUtils::userFromEmoji));
            emojis.put(emojiId, new StoredEmojiData(emoji, userRef));
        });
    }

    private void saveMember(long guildId, MemberData member) {
        Long2 memberId = new Long2(guildId, member.user().id().asLong());
        GuildContent guildContent = computeGuildContent(guildId);
        guildContent.memberIds.add(memberId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.memberIdSet().add(memberId.b));
        AtomicReference<StoredUserData> userRef = Objects.requireNonNull(
                computeUserRef(memberId.b, member, ImplUtils::userFromMember));
        members.put(memberId, new StoredMemberData(member, userRef));
    }

    private @Nullable PresenceAndUserData savePresence(long guildId, PresenceData presence) {
        Long2 presenceId = new Long2(guildId, presence.user().id().asLong());
        UserData oldUser = Optional.ofNullable(users.get(presenceId.b))
                .map(AtomicReference::get)
                .map(StoredUserData::toImmutable)
                .orElse(null);
        return ifNonNullMap(computeUserRef(presenceId.b, presence, ImplUtils::userFromPresence), userRef -> {
            GuildContent guildContent = computeGuildContent(guildId);
            guildContent.presenceIds.add(presenceId);
            StoredPresenceData oldPresence = presences.put(presenceId, new StoredPresenceData(presence, userRef));
            if (oldPresence == null && oldUser == null) {
                return null;
            }
            return PresenceAndUserData.of(ifNonNullMap(oldPresence, StoredPresenceData::toImmutable), oldUser);
        });
    }

    private @Nullable VoiceStateData saveOrRemoveVoiceState(long guildId, VoiceStateData voiceState) {
        Long2 voiceStateId = new Long2(guildId, voiceState.userId().asLong());
        GuildContent guildContent = computeGuildContent(guildId);
        if (voiceState.channelId().isPresent()) {
            guildContent.voiceStateIds.add(voiceStateId);
            computeChannelContent(voiceState.channelId().get().asLong()).voiceStateIds.add(voiceStateId);
            return ifNonNullMap(
                    voiceStates.put(voiceStateId, new StoredVoiceStateData(voiceState)),
                    this::toImmutableVoiceState);
        } else {
            guildContent.voiceStateIds.remove(voiceStateId);
            computeChannelContent(voiceState.channelId().get().asLong()).voiceStateIds.remove(voiceStateId);
            return ifNonNullMap(voiceStates.remove(voiceStateId), this::toImmutableVoiceState);
        }
    }

    private @Nullable MessageData deleteMessage(long channelId, long messageId) {
        Long2 id = new Long2(channelId, messageId);
        ChannelContent channelContent = computeChannelContent(channelId);
        channelContent.messageIds.remove(id);
        return ifNonNullMap(messages.remove(id), this::toImmutableMessage);
    }

    private MessageData toImmutableMessage(StoredMessageData storedMessageData) {
        Long2 id = new Long2(storedMessageData.guildId(), storedMessageData.id());
        if (id.a == -1) {
            return storedMessageData.toImmutable(null);
        }
        return storedMessageData.toImmutable(members.get(id));
    }

    private VoiceStateData toImmutableVoiceState(StoredVoiceStateData storedVoiceStateData) {
        Long2 id = new Long2(storedVoiceStateData.guildId(), storedVoiceStateData.userId());
        if (id.a == -1) {
            return storedVoiceStateData.toImmutable(null);
        }
        return storedVoiceStateData.toImmutable(members.get(id));
    }

    private @Nullable
    <T> AtomicReference<StoredUserData> computeUserRef(long userId, T newData,
                                                       BiFunction<T, StoredUserData, StoredUserData> userUpdater) {
        for (;;) {
            AtomicReference<StoredUserData> existing = users.get(userId);
            AtomicReference<StoredUserData> ref;
            if (existing == null) {
                StoredUserData newUser = userUpdater.apply(newData, null);
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
                StoredUserData oldUser = ref.get();
                StoredUserData newUser = userUpdater.apply(newData, oldUser);
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
        private final Set<Long> channelIds = new HashSet<>();
        private final Set<Long> emojiIds = new HashSet<>();
        private final Set<Long2> memberIds = new HashSet<>();
        private final Set<Long2> presenceIds = new HashSet<>();
        private final Set<Long> roleIds = new HashSet<>();
        private final Set<Long2> voiceStateIds = new HashSet<>();
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
            StoredGuildData old = guilds.remove(guildId);
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
            return ifNonNullMap(old, StoredGuildData::toImmutable);
        }
    }

    private class ChannelContent {

        private final long channelId;
        private final Set<Long2> messageIds = new HashSet<>();
        private final Set<Long2> voiceStateIds = new HashSet<>();

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
