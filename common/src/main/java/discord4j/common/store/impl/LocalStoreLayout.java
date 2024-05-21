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
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static discord4j.common.store.impl.ImplUtils.*;

/**
 * A {@link StoreLayout} that stores entities in the heap of the local JVM. This implementation is entirely thread-safe:
 * atomicity guarantees are made so that the cache won't be set in an inconsistent state if two events are received
 * concurrently. However, it is still up to the user to ensure that events are received in the right order.
 */
public class LocalStoreLayout implements StoreLayout, DataAccessor, GatewayDataUpdater {

    private final StorageConfig config;

    private final ConcurrentMap<Long, GuildContent> contentByGuild = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, ChannelContent> contentByChannel = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, ImmutableChannelData> channels = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, WithUser<ImmutableEmojiData>> emojis =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, ImmutableStickerData> stickers =
        new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, WrappedGuildData> guilds = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long2, WithUser<ImmutableMemberData>> members =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<Long2, WithUser<ImmutableMessageData>> messages;

    private final ConcurrentMap<Long2, WithUser<ImmutablePresenceData>> presences =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, RoleData> roles = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, StageInstanceData> stageInstances = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, GuildScheduledEventData> scheduledEvents = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long2, List<Long>> scheduledEventsUsers = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, AtomicReference<ImmutableUserData>> users =
            StorageBackend.caffeine(Caffeine::weakValues).newMap();

    private final ConcurrentMap<Long2, ImmutableVoiceStateData> voiceStates =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<Long2, ImmutableThreadMemberData> threadMembers =
            new ConcurrentHashMap<>();

    private final Set<Integer> shardsConnected = new HashSet<>();
    private volatile AtomicReference<ImmutableUserData> selfUser;
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
    public Mono<Long> countStickers() {
        return Mono.just((long) stickers.size());
    }

    @Override
    public Mono<Long> countStickersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
            .map(content -> (long) content.stickerIds.size());
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
        return Flux.fromIterable(channels.values());
    }

    @Override
    public Flux<ChannelData> getChannelsInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.channelIds)
                .flatMap(id -> Mono.justOrEmpty(channels.get(id)));
    }

    @Override
    public Mono<ChannelData> getChannelById(long channelId) {
        return Mono.justOrEmpty(channels.get(channelId));
    }

    @Override
    public Flux<StickerData> getStickers() {
        return Flux.fromIterable(stickers.values());
    }

    @Override
    public Flux<StickerData> getStickersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
            .flatMapIterable(content -> content.stickerIds)
            .flatMap(id -> Mono.justOrEmpty(stickers.get(id)));
    }

    @Override
    public Mono<StickerData> getStickerById(long guildId, long stickerId) {
        return Mono.justOrEmpty(stickers.get(stickerId));
    }

    @Override
    public Flux<EmojiData> getEmojis() {
        return Flux.fromIterable(emojis.values()).map(WithUser::get);
    }

    @Override
    public Flux<EmojiData> getEmojisInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.emojiIds)
                .flatMap(id -> Mono.justOrEmpty(emojis.get(id)))
                .map(WithUser::get);
    }

    @Override
    public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
        return Mono.justOrEmpty(emojis.get(emojiId)).map(WithUser::get);
    }

    @Override
    public Flux<GuildData> getGuilds() {
        return Flux.fromIterable(guilds.values()).map(WrappedGuildData::unwrap);
    }

    @Override
    public Mono<GuildData> getGuildById(long guildId) {
        return Mono.justOrEmpty(guilds.get(guildId)).map(WrappedGuildData::unwrap);
    }

    @Override
    public Flux<GuildScheduledEventData> getScheduledEventsInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
            .flatMapIterable(content -> content.eventIds)
            .mapNotNull(scheduledEvents::get);
    }

    @Override
    public Mono<GuildScheduledEventData> getScheduledEventById(long guildId, long eventId) {
        return Mono.justOrEmpty(scheduledEvents.get(eventId))
            .filter(event -> event.guildId().asLong() == guildId);
    }

    @Override
    public Flux<Id> getScheduledEventUsersInEvent(long guildId, long eventId) {
        return Flux.fromIterable(scheduledEventsUsers.getOrDefault(new Long2(guildId, eventId), new ArrayList<>()))
            .map(Id::of);
    }

    @Override
    public Flux<MemberData> getMembers() {
        return Flux.fromIterable(members.values()).map(WithUser::get);
    }

    @Override
    public Flux<MemberData> getMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.memberIds)
                .flatMap(id -> Mono.justOrEmpty(members.get(id)))
                .map(WithUser::get);
    }

    @Override
    public Flux<MemberData> getExactMembersInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .filter(GuildContent::isMemberListComplete)
                .switchIfEmpty(Mono.error(ExactResultNotAvailableException::new))
                .flatMapIterable(content -> content.memberIds)
                .flatMap(id -> Mono.justOrEmpty(members.get(id)))
                .map(WithUser::get);
    }

    @Override
    public Mono<MemberData> getMemberById(long guildId, long userId) {
        return Mono.justOrEmpty(members.get(new Long2(guildId, userId)))
                .map(WithUser::get);
    }

    @Override
    public Flux<MessageData> getMessages() {
        return Flux.fromIterable(messages.values()).map(WithUser::get);
    }

    @Override
    public Flux<MessageData> getMessagesInChannel(long channelId) {
        return Mono.justOrEmpty(contentByChannel.get(channelId))
                .flatMapIterable(content -> content.messageIds)
                .flatMap(id -> Mono.justOrEmpty(messages.get(id)))
                .map(WithUser::get);
    }

    @Override
    public Mono<MessageData> getMessageById(long channelId, long messageId) {
        return Mono.justOrEmpty(messages.get(new Long2(channelId, messageId)))
                .map(WithUser::get);
    }

    @Override
    public Flux<PresenceData> getPresences() {
        return Flux.fromIterable(presences.values()).map(WithUser::get);
    }

    @Override
    public Flux<PresenceData> getPresencesInGuild(long guildId) {
        return Mono.justOrEmpty(contentByGuild.get(guildId))
                .flatMapIterable(content -> content.presenceIds)
                .flatMap(id -> Mono.justOrEmpty(presences.get(id)))
                .map(WithUser::get);
    }

    @Override
    public Mono<PresenceData> getPresenceById(long guildId, long userId) {
        return Mono.justOrEmpty(presences.get(new Long2(guildId, userId)))
                .map(WithUser::get);
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
        return Flux.fromIterable(users.values())
                .map(AtomicReference::get);
    }

    @Override
    public Mono<UserData> getUserById(long userId) {
        return Mono.justOrEmpty(users.get(userId))
                .map(AtomicReference::get);
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
        return Mono.fromRunnable(() -> dispatch.channel().guildId().toOptional()
                .ifPresent(guildId -> saveChannel(guildId.asLong(), dispatch.channel())));
    }

    @Override
    public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
        ChannelData data = dispatch.channel();
        return Mono.fromRunnable(() -> data.guildId().toOptional()
                .map(Id::asLong)
                .ifPresent(guildId -> {
                    Id channelId = data.id();
                    GuildContent guildContent = computeGuildContent(guildId);
                    guildContent.channelIds.remove(channelId.asLong());
                    ifNonNullDo(contentByChannel.get(channelId.asLong()), ChannelContent::dispose);
                    ifNonNullDo(guilds.get(guildId), guild -> guild.getChannels().remove(channelId));
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
            List<ChannelData> threads = createData.threads();
            List<PresenceData> presences = createData.presences();
            List<VoiceStateData> voiceStates = createData.voiceStates();
            ImmutableGuildData guild = ImmutableGuildData.builder()
                    .from(createData)
                    .roles(Collections.emptyList())
                    .emojis(Collections.emptyList())
                    .members(Collections.emptyList())
                    .channels(Collections.emptyList())
                    .build();
            guilds.put(guildId, new WrappedGuildData(guild));
            roles.forEach(role -> saveRole(guildId, role));
            emojis.forEach(emoji -> saveEmoji(guildId, emoji));
            members.forEach(member -> saveMember(guildId, member));
            channels.forEach(channel -> saveChannel(guildId, channel));
            threads.forEach(channel -> this.channels.put(channel.id().asLong(), ImmutableChannelData.copyOf(channel)));
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
    public Mono<Set<StickerData>> onGuildStickersUpdate(int shardIndex, GuildStickersUpdate dispatch) {
        long guildId = dispatch.guildId().asLong();
        return Mono.fromCallable(() -> {
            GuildContent content = computeGuildContent(guildId);
            Set<StickerData> old = content.stickerIds.stream()
                .map(stickers::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            stickers.keySet().removeAll(content.stickerIds);
            ifNonNullDo(guilds.get(guildId), guild -> guild.getStickers().clear());
            content.stickerIds.clear();
            dispatch.stickers().forEach(sticker -> saveSticker(guildId, sticker));
            return old;
        });
    }

    @Override
    public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        long guildId = dispatch.guildId().asLong();
        return Mono.fromCallable(() -> {
            GuildContent content = computeGuildContent(guildId);
            Set<EmojiData> old = content.emojiIds.stream()
                    .map(emojis::get)
                    .filter(Objects::nonNull)
                    .map(WithUser::get)
                    .collect(Collectors.toSet());
            emojis.keySet().removeAll(content.emojiIds);
            ifNonNullDo(guilds.get(guildId), guild -> guild.getEmojis().clear());
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
            ifNonNullDo(guilds.get(guildId), guild -> guild.getMembers().remove(Id.of(memberId.b)));
            return ifNonNullMap(members.remove(memberId), WithUser::get);
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
        return Mono.fromCallable(() -> {
            MemberData oldData = ifNonNullMap(members.get(id), WithUser::get);
            members.computeIfPresent(id, (k, old) -> {
                AtomicReference<ImmutableUserData> ref = old.userRef();
                if (ref != null) {
                    ref.set(ImmutableUserData.copyOf(dispatch.user()));
                }
                return new WithUser<>(ImmutableMemberData.builder()
                        .from(old.get())
                        .nick(dispatch.nick())
                        .roles(dispatch.roles().stream().map(Id::of).collect(Collectors.toList()))
                        .joinedAt(dispatch.joinedAt())
                        .premiumSince(dispatch.premiumSince())
                        .build(), ref, ImmutableMemberData::withUser);
            });
            return oldData;
        });
    }

    @Override
    public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        return Mono.fromRunnable(() -> saveRole(dispatch.guildId().asLong(), dispatch.role()));
    }

    @Override
    public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        long guildId = dispatch.guildId().asLong();
        Id roleId = dispatch.roleId();
        return Mono.fromCallable(() -> {
            GuildContent guildContent = computeGuildContent(guildId);
            guildContent.roleIds.remove(roleId.asLong());
            ifNonNullDo(guilds.get(guildId), guild -> guild.getRoles().remove(roleId));
            guildContent.memberIds.forEach(id2 -> members.computeIfPresent(id2,
                    (k, member) -> member.update(m -> m.withRoles(remove(m.roles(), roleId)))));
            return roles.remove(roleId.asLong());
        });
    }

    @Override
    public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        return Mono.fromCallable(() -> saveRole(dispatch.guildId().asLong(), dispatch.role()));
    }

    @Override
    public Mono<Void> onGuildScheduledEventCreate(int shardIndex, GuildScheduledEventCreate dispatch) {
        final long eventId = dispatch.scheduledEvent().id().asLong();

        // Add event to guild->events index
        GuildContent guildContent = computeGuildContent(dispatch.scheduledEvent().guildId().asLong());
        guildContent.eventIds.add(eventId);

        // Store the event
        return Mono.fromRunnable(() -> scheduledEvents.put(eventId, ImmutableGuildScheduledEventData.copyOf(dispatch.scheduledEvent())));
    }

    @Override
    public Mono<GuildScheduledEventData> onGuildScheduledEventUpdate(int shardIndex, GuildScheduledEventUpdate dispatch) {
        final long eventId = dispatch.scheduledEvent().id().asLong();

        // Update the event
        return Mono.fromCallable(() -> scheduledEvents.replace(eventId, ImmutableGuildScheduledEventData.copyOf(dispatch.scheduledEvent())));
    }

    @Override
    public Mono<GuildScheduledEventData> onGuildScheduledEventDelete(int shardIndex, GuildScheduledEventDelete dispatch) {
        final long eventId = dispatch.scheduledEvent().id().asLong();

        // Remove event from guild->events index
        GuildContent guildContent = computeGuildContent(dispatch.scheduledEvent().guildId().asLong());
        guildContent.eventIds.remove(eventId);

        // Remove the event
        return Mono.fromRunnable(() -> {
            scheduledEvents.remove(eventId);
            scheduledEventsUsers.remove(new Long2(dispatch.scheduledEvent().guildId().asLong(), eventId));
        });
    }

    @Override
    public Mono<Void> onGuildScheduledEventUserAdd(int shardIndex, GuildScheduledEventUserAdd dispatch) {
        final Long2 key = new Long2(dispatch.guildId().asLong(), dispatch.scheduledEventId().asLong());

        return Mono.fromRunnable(() -> scheduledEventsUsers.computeIfAbsent(key, ignored -> new ArrayList<>()).add(dispatch.userId().asLong()));
    }

    @Override
    public Mono<Void> onGuildScheduledEventUserRemove(int shardIndex, GuildScheduledEventUserRemove dispatch) {
        final Long2 key = new Long2(dispatch.guildId().asLong(), dispatch.scheduledEventId().asLong());

        return Mono.fromRunnable(() -> scheduledEventsUsers.computeIfAbsent(key, ignored -> new ArrayList<>()).remove(dispatch.userId().asLong()));
    }

    @Override
    public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
        long guildId = dispatch.guild().id().asLong();
        return Mono.fromCallable(() -> {
            WrappedGuildData old = guilds.get(guildId);
            guilds.computeIfPresent(guildId, (k, oldGuild) -> new WrappedGuildData(GuildData.builder()
                    .from(oldGuild.unwrap())
                    .from(dispatch.guild())
                    .build()));
            return ifNonNullMap(old, WrappedGuildData::unwrap);
        });
    }

    @Override
    public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
        return Mono.fromRunnable(() -> {
            shardsConnected.remove(shardIndex);
            if (config.getInvalidationFilter().contains(cause)) {
                contentByGuild.entrySet().stream()
                        .filter(entry -> ((entry.getKey() >> 22) % shardCount) == shardIndex)
                        .map(Map.Entry::getValue)
                        .forEach(GuildContent::dispose);
            }
            if (shardsConnected.isEmpty()) {
                shardCount = 0;
            }
        });
    }

    @Override
    public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
        ImmutableMessageData message = ImmutableMessageData.copyOf(dispatch.message());
        long channelId = message.channelId().asLong();
        long messageId = message.id().asLong();
        Long2 id = new Long2(channelId, messageId);
        return Mono.fromRunnable(() -> {
            ChannelContent channelContent = computeChannelContent(id.a);
            channels.computeIfPresent(id.a, (k, channel) -> channel.withLastMessageIdOrNull(id.b));
            channelContent.messageIds.add(id);
            AtomicReference<ImmutableUserData> userRef = computeUserRef(message.author().id().asLong(), message,
                    (m, old) -> ImmutableUserData.copyOf(m.author()));
            messages.put(id, new WithUser<>(message.withAuthor(EmptyUser.INSTANCE), userRef,
                    ImmutableMessageData::withAuthor));
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
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> messages.computeIfPresent(new Long2(channelId, messageId),
                (k, message) -> message.update(m -> addReaction(m, dispatch))));
    }

    @Override
    public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> messages.computeIfPresent(new Long2(channelId, messageId),
                (k, message) -> message.update(m -> removeReaction(m, dispatch))));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> messages.computeIfPresent(new Long2(channelId, messageId),
                (k, message) -> message.update(m -> m.withReactions(Possible.absent()))));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
        long channelId = dispatch.channelId().asLong();
        long messageId = dispatch.messageId().asLong();
        return Mono.fromRunnable(() -> messages.computeIfPresent(new Long2(channelId, messageId),
                (k, message) -> message.update(m -> m.withReactions(Possible.of(m.reactions()
                        .toOptional()
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(r -> !EmojiKey.predicateEquals(dispatch.emoji()).test(r))
                        .collect(Collectors.toList()))))));
    }

    @Override
    public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
        ImmutablePartialMessageData edited = ImmutablePartialMessageData.copyOf(dispatch.message());
        long channelId = edited.channelId().asLong();
        long messageId = edited.id().asLong();
        Long2 id = new Long2(channelId, messageId);
        return Mono.fromCallable(() -> {
            MessageData old = ifNonNullMap(messages.get(id), WithUser::get);
            messages.computeIfPresent(id, (k, message) ->
                    message.update(m -> ImmutableMessageData.builder()
                            .from(m)
                            .channelId(edited.channelId())
                            .guildId(edited.guildId())
                            .content(edited.contentOrElse(m.content()))
                            .timestamp(edited.timestampOrElse(m.timestamp()))
                            .editedTimestamp(edited.editedTimestamp())
                            .tts(edited.ttsOrElse(m.tts()))
                            .mentionEveryone(edited.mentionEveryoneOrElse(m.mentionEveryone()))
                            .mentions(edited.mentions())
                            .mentionRoles(edited.mentionRoles())
                            .mentionChannels(edited.mentionChannels())
                            .attachments(edited.attachments())
                            .embeds(edited.embeds())
                            .nonce(edited.isNoncePresent() ? edited.nonce() : m.nonce())
                            .pinned(edited.pinnedOrElse(m.pinned()))
                            .webhookId(edited.isWebhookIdPresent() ? edited.webhookId() : m.webhookId())
                            .type(edited.typeOrElse(m.type()))
                            .activity(edited.isActivityPresent() ? edited.activity() : m.activity())
                            .application(edited.isApplicationPresent() ? edited.application() : m.application())
                            .messageReference(edited.isMessageReferencePresent()
                                    ? edited.messageReference() : m.messageReference())
                            .flags(edited.isFlagsPresent() ? edited.flags() : m.flags())
                            .reactions(edited.isReactionsPresent() ? edited.reactions() : m.reactions())
                            .build()));
            return old;
        });
    }

    @Override
    public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        return Mono.fromCallable(() -> savePresence(dispatch.guildId().asLong(), createPresence(dispatch)));
    }

    @Override
    public Mono<Void> onReady(Ready dispatch) {
        return Mono.fromRunnable(() -> {
            int[] shardInfo = dispatch.shard().toOptional().orElseGet(() -> new int[]{0, 1});
            if (selfUser == null) {
                ImmutableUserData userData = ImmutableUserData.copyOf(dispatch.user());
                selfUser = new AtomicReference<>(userData);
                users.put(userData.id().asLong(), selfUser);
            }
            if (shardCount == 0) {
                shardCount = shardInfo[1];
            }
            shardsConnected.add(shardInfo[0]);
        });
    }

    @Override
    public Mono<Void> onStageInstanceCreate(int shardIndex, StageInstanceCreate dispatch) {
        return Mono.fromRunnable(() -> stageInstances.put(dispatch.stageInstance().channelId().asLong(), dispatch.stageInstance()));
    }

    @Override
    public Mono<StageInstanceData> onStageInstanceUpdate(int shardIndex, StageInstanceUpdate dispatch) {
        return Mono.fromRunnable(() -> stageInstances.replace(dispatch.stageInstance().channelId().asLong(), dispatch.stageInstance()));
    }

    @Override
    public Mono<StageInstanceData> onStageInstanceDelete(int shardIndex, StageInstanceDelete dispatch) {
        return Mono.fromRunnable(() -> stageInstances.remove(dispatch.stageInstance().channelId().asLong()));
    }

    @Override
    public Mono<StageInstanceData> getStageInstanceByChannelId(long channelId) {
        return Mono.fromCallable(() -> stageInstances.get(channelId));
    }

    @Override
    public Mono<ThreadMemberData> getThreadMemberById(long threadId, long userId) {
        return Mono.justOrEmpty(threadMembers.get(new Long2(threadId, userId)));
    }

    @Override
    public Flux<ThreadMemberData> getMembersInThread(long threadId) {
        return Mono.justOrEmpty(contentByChannel.get(threadId))
                .flatMapIterable(c -> c.threadMembersIds)
                .map(threadMembers::get);
    }

    @Override
    public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
        return Mono.fromCallable(() -> ifNonNullMap(
                users.get(dispatch.user().id().asLong()),
                userRef -> userRef.getAndSet(ImmutableUserData.copyOf(dispatch.user()))));
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
    public Mono<Void> onThreadCreate(int shardIndex, ThreadCreate dispatch) {
        long channelId = dispatch.thread().id().asLong();
        return Mono.fromRunnable(() -> channels.put(channelId, ImmutableChannelData.copyOf(dispatch.thread())));
    }

    @Override
    public Mono<ChannelData> onThreadUpdate(int shardIndex, ThreadUpdate dispatch) {
        long channelId = dispatch.thread().id().asLong();
        return Mono.justOrEmpty(channels.put(channelId, ImmutableChannelData.copyOf(dispatch.thread())));
    }

    @Override
    public Mono<Void> onThreadDelete(int shardIndex, ThreadDelete dispatch) {
        long channelId = dispatch.thread().id().asLong();
        return Mono.fromRunnable(() -> ifNonNullDo(contentByChannel.get(channelId), ChannelContent::dispose));
    }

    @Override
    public Mono<Void> onThreadListSync(int shardIndex, ThreadListSync dispatch) {
        return Mono.fromRunnable(() -> {
            dispatch.threads().forEach(thread -> channels.put(thread.id().asLong(), ImmutableChannelData.copyOf(thread)));
            dispatch.members().forEach(member -> saveThreadMember(member));
        });
    }

    @Override
    public Mono<ThreadMemberData> onThreadMemberUpdate(int shardIndex, ThreadMemberUpdate dispatch) {
        return Mono.fromCallable(() -> saveThreadMember(dispatch.member()));
    }

    @Override
    public Mono<List<ThreadMemberData>> onThreadMembersUpdate(int shardIndex, ThreadMembersUpdate dispatch) {
        long threadId = dispatch.id().asLong();
        return Mono.fromCallable(() -> {
            ChannelContent content = computeChannelContent(threadId);
            List<ThreadMemberData> old = content.threadMembersIds.stream()
                    .map(threadMembers::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            dispatch.addedMembers().toOptional().orElse(Collections.emptyList()).forEach(threadMember -> {
                Long2 id = new Long2(threadId, threadMember.userId().get().asLong());
                content.threadMembersIds.add(id);
                threadMembers.put(id, ImmutableThreadMemberData.copyOf(threadMember));
            });

            dispatch.removedMemberIds().toOptional().orElse(Collections.emptyList()).forEach(id -> {
                Long2 key = new Long2(threadId, id.asLong());
                content.threadMembersIds.remove(key);
                threadMembers.remove(key);
            });

            return old;
        });
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

    @Nullable
    private ChannelData saveChannel(long guildId, ChannelData channel) {
        long channelId = channel.id().asLong();
        GuildContent guildContent = computeGuildContent(guildId);
        guildContent.channelIds.add(channelId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.getChannels().add(Id.of(channelId)));
        return channels.put(channelId, ImmutableChannelData.copyOf(channel).withGuildId(guildId));
    }

    @Nullable
    private RoleData saveRole(long guildId, RoleData role) {
        long roleId = role.id().asLong();
        GuildContent guildContent = computeGuildContent(guildId);
        guildContent.roleIds.add(roleId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.getRoles().add(Id.of(roleId)));
        return roles.put(roleId, ImmutableRoleData.copyOf(role));
    }

    private void saveSticker(long guildId, StickerData sticker) {
        long stickerId = sticker.id().asLong();
        computeGuildContent(guildId).stickerIds.add(stickerId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.getStickers().add(Id.of(stickerId)));
        stickers.put(stickerId, ImmutableStickerData.copyOf(sticker));
    }

    private void saveEmoji(long guildId, EmojiData emoji) {
        emoji.id().map(Id::asLong).ifPresent(emojiId -> {
            computeGuildContent(guildId).emojiIds.add(emojiId);
            ifNonNullDo(guilds.get(guildId), guild -> guild.getEmojis().add(Id.of(emojiId)));
            AtomicReference<ImmutableUserData> userRef = ifNonNullMap(
                    emoji.user().toOptional().map(user -> user.id().asLong()).orElse(null),
                    userId -> computeUserRef(userId, emoji, (e, u) -> ImmutableUserData.copyOf(e.user().get())));
            emojis.put(emojiId, new WithUser<>(ImmutableEmojiData.copyOf(emoji).withUser(Possible.absent()), userRef,
                    (e, u) -> e.withUser(Possible.of(u))));
        });
    }

    private void saveMember(long guildId, MemberData member) {
        Long2 memberId = new Long2(guildId, member.user().id().asLong());
        GuildContent guildContent = computeGuildContent(guildId);
        guildContent.memberIds.add(memberId);
        ifNonNullDo(guilds.get(guildId), guild -> guild.getMembers().add(Id.of(memberId.b)));
        AtomicReference<ImmutableUserData> userRef = computeUserRef(memberId.b, member,
                (m, u) -> ImmutableUserData.copyOf(m.user()));
        members.put(memberId, new WithUser<>(ImmutableMemberData.copyOf(member).withUser(EmptyUser.INSTANCE), userRef,
                ImmutableMemberData::withUser));
    }

    @Nullable
    private ThreadMemberData saveThreadMember(ThreadMemberData threadMember) {
        long threadId = threadMember.id().get().asLong();
        ChannelContent content = computeChannelContent(threadId);
        Long2 id = new Long2(threadId, threadMember.userId().get().asLong());
        content.threadMembersIds.add(id);
        return threadMembers.put(id, ImmutableThreadMemberData.copyOf(threadMember));
    }

    @Nullable
    private PresenceAndUserData savePresence(long guildId, PresenceData presence) {
        Long2 presenceId = new Long2(guildId, presence.user().id().asLong());
        ImmutableUserData oldUser = ifNonNullMap(users.get(presenceId.b), AtomicReference::get);
        return ifNonNullMap(computeUserRef(presenceId.b, presence, LocalStoreLayout::userFromPresence), userRef -> {
            GuildContent guildContent = computeGuildContent(guildId);
            guildContent.presenceIds.add(presenceId);
            WithUser<ImmutablePresenceData> oldPresence = presences.put(presenceId, new WithUser<>(
                    ImmutablePresenceData.copyOf(presence).withUser(EmptyPartialUser.INSTANCE), userRef,
                    (p, u) -> p.withUser(PartialUserData.builder()
                            .id(u.id())
                            .avatar(Possible.of(u.avatar()))
                            .globalName(Possible.of(u.globalName()))
                            .username(u.username())
                            .discriminator(u.discriminator())
                            .build())));
            if (oldPresence == null && oldUser == null) {
                return null;
            }
            return PresenceAndUserData.of(ifNonNullMap(oldPresence, WithUser::get), oldUser);
        });
    }

    @Nullable
    private static ImmutableUserData userFromPresence(PresenceData newPresence, @Nullable ImmutableUserData oldUser) {
        if (oldUser == null) return null;
        ImmutablePartialUserData partialUserData = ImmutablePartialUserData.copyOf(newPresence.user());
        return UserData.builder()
                .from(oldUser)
                .globalName(or(Possible.flatOpt(partialUserData.globalName()), oldUser::globalName))
                .username(partialUserData.usernameOrElse(oldUser.username()))
                .discriminator(partialUserData.discriminatorOrElse(oldUser.discriminator()))
                .avatar(or(Possible.flatOpt(partialUserData.avatar()), oldUser::avatar))
                .banner(Possible.of(or(Possible.flatOpt(partialUserData.banner()),
                        () -> Possible.flatOpt(oldUser.banner()))))
                .accentColor(Possible.of(or(Possible.flatOpt(partialUserData.accentColor()),
                        () -> Possible.flatOpt(oldUser.accentColor()))))
                .build();
    }

    @Nullable
    private VoiceStateData saveOrRemoveVoiceState(long guildId, VoiceStateData voiceState) {
        Long2 voiceStateId = new Long2(guildId, voiceState.userId().asLong());
        GuildContent guildContent = computeGuildContent(guildId);
        VoiceStateData old = voiceStates.remove(voiceStateId);
        if (old != null && old.channelId().isPresent()) {
            computeChannelContent(old.channelId().get().asLong()).voiceStateIds.remove(voiceStateId);
        }
        if (voiceState.channelId().isPresent()) {
            guildContent.voiceStateIds.add(voiceStateId);
            computeChannelContent(voiceState.channelId().get().asLong()).voiceStateIds.add(voiceStateId);
            voiceStates.put(voiceStateId, ImmutableVoiceStateData.copyOf(voiceState)
                    .withGuildId(guildId)
                    .withMember(Possible.absent()));
        } else {
            guildContent.voiceStateIds.remove(voiceStateId);
        }
        return old;
    }

    @Nullable
    private MessageData deleteMessage(long channelId, long messageId) {
        Long2 id = new Long2(channelId, messageId);
        ChannelContent channelContent = computeChannelContent(channelId);
        channelContent.messageIds.remove(id);
        return ifNonNullMap(messages.remove(id), WithUser::get);
    }

    private ImmutableMessageData addReaction(ImmutableMessageData message, MessageReactionAdd dispatch) {
        boolean me = dispatch.userId().asLong() == selfUser.get().id().asLong();
        List<ReactionData> reactions = message.reactions().toOptional().orElse(Collections.emptyList());
        if (reactions.stream().anyMatch(EmojiKey.predicateEquals(dispatch.emoji()))) {
            return message.withReactions(Possible.of(reactions.stream()
                    .map(r -> EmojiKey.predicateEquals(dispatch.emoji()).test(r) ? ImmutableReactionData.builder()
                            .from(r)
                            .count(r.count() + 1)
                            .me(r.me() || me)
                            .meBurst(false) // BOTs cannot super-react
                            .build() : r)
                    .collect(Collectors.toList())));
        }
        return message.withReactions(Possible.of(
                add(reactions, ImmutableReactionData.of(1, ImmutableReactionCountDetailsData.of(1, 0), me, false, dispatch.emoji(), Collections.emptyList()))));
    }

    private ImmutableMessageData removeReaction(ImmutableMessageData message, MessageReactionRemove dispatch) {
        boolean me = dispatch.userId().asLong() == selfUser.get().id().asLong();
        List<ReactionData> reactions = message.reactions().toOptional().orElse(Collections.emptyList());
        return message.withReactions(Possible.of(reactions.stream()
                .map(r -> EmojiKey.predicateEquals(dispatch.emoji()).test(r) ? ImmutableReactionData.builder()
                        .from(r)
                        .count(r.count() - 1)
                        .me(!me && r.me())
                        .build() : r)
                .filter(r -> r.count() > 0)
                .collect(Collectors.toList())));
    }

    @Nullable
    private <T> AtomicReference<ImmutableUserData> computeUserRef(long userId, T newData,
                                                                  BiFunction<T, ImmutableUserData, ImmutableUserData>
                                                                         userUpdater) {
        for (; ; ) {
            AtomicReference<ImmutableUserData> existing = users.get(userId);
            AtomicReference<ImmutableUserData> ref;
            if (existing == null) {
                ImmutableUserData newUser = userUpdater.apply(newData, null);
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
                ImmutableUserData oldUser = ref.get();
                ImmutableUserData newUser = userUpdater.apply(newData, oldUser);
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

    private static class EmojiKey {

        private final long id;
        private final String name;

        private EmojiKey(EmojiData emoji) {
            this.id = emoji.id().map(Id::asLong).orElse(-1L);
            this.name = emoji.name().orElse(null);
        }

        private static Predicate<ReactionData> predicateEquals(EmojiData emoji) {
            return r -> new EmojiKey(r.emoji()).equals(new EmojiKey(emoji));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EmojiKey emojiKey = (EmojiKey) o;
            return id != -1 ? id == emojiKey.id : Objects.equals(name, emojiKey.name);
        }

        @Override
        public int hashCode() {
            return id != -1 ? Long.hashCode(id) : Objects.hash(name);
        }
    }

    private class GuildContent {

        private final long guildId;
        private final Set<Long> channelIds = new HashSet<>();
        private final Set<Long> emojiIds = new HashSet<>();
        private final Set<Long> eventIds = new HashSet<>();
        private final Set<Long> stickerIds = new HashSet<>();
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

        @Nullable
        private GuildData dispose() {
            WrappedGuildData old = guilds.remove(guildId);
            contentByGuild.remove(guildId);
            contentByChannel.values().stream()
                    .filter(content -> channelIds.contains(content.channelId))
                    .collect(Collectors.toSet())
                    .forEach(ChannelContent::dispose);
            emojis.keySet().removeAll(emojiIds);
            stickers.keySet().removeAll(stickerIds);
            members.keySet().removeAll(memberIds);
            presences.keySet().removeAll(presenceIds);
            roles.keySet().removeAll(roleIds);
            scheduledEvents.keySet().removeAll(eventIds);
            voiceStates.keySet().removeAll(voiceStateIds);
            return ifNonNullMap(old, WrappedGuildData::unwrap);
        }
    }

    private class ChannelContent {

        private final long channelId;
        private final Set<Long2> messageIds = new HashSet<>();
        private final Set<Long2> threadMembersIds = new HashSet<>();
        private final Set<Long2> voiceStateIds = new HashSet<>();

        public ChannelContent(long channelId) {
            this.channelId = channelId;
        }

        private void dispose() {
            channels.remove(channelId);
            contentByChannel.remove(channelId);
            threadMembers.keySet().removeAll(threadMembersIds);
            messages.keySet().removeAll(messageIds);
        }
    }
}
