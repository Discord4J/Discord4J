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

package discord4j.common.store.legacy;

import discord4j.common.store.api.layout.DataAccessor;
import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.layout.StoreLayout;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import discord4j.discordjson.possible.Possible;
import discord4j.store.api.service.StoreService;
import discord4j.store.api.util.LongLongTuple2;
import discord4j.store.api.util.LongObjTuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

public class LegacyStoreLayout implements StoreLayout, DataAccessor, GatewayDataUpdater {

    private static final Logger log = Loggers.getLogger(LegacyStoreLayout.class);

    private final StateHolder stateHolder;

    private LegacyStoreLayout(StoreService storeService) {
        this.stateHolder = new StateHolder(storeService);
    }

    public static LegacyStoreLayout of(StoreService storeService) {
        return new LegacyStoreLayout(storeService);
    }

    @Override
    public DataAccessor getDataAccessor() {
        return this;
    }

    @Override
    public GatewayDataUpdater getGatewayDataUpdater() {
        return this;
    }

    /// //////////////////////////////////////////////////////////////////////////
    /// / Query model methods
    /// //////////////////////////////////////////////////////////////////////////

    @Override
    public Mono<Long> countChannels() {
        return stateHolder.getChannelStore().count();
    }

    @Override
    public Mono<Long> countChannelsInGuild(long guildId) {
        return getChannelsInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countStickers() {
        return stateHolder.getGuildStickerStore().count();
    }

    @Override
    public Mono<Long> countStickersInGuild(long guildId) {
        return getStickersInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countEmojis() {
        return stateHolder.getGuildEmojiStore().count();
    }

    @Override
    public Mono<Long> countEmojisInGuild(long guildId) {
        return getEmojisInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countGuilds() {
        return stateHolder.getGuildStore().count();
    }

    @Override
    public Mono<Long> countMembers() {
        return stateHolder.getMemberStore().count();
    }

    @Override
    public Mono<Long> countMembersInGuild(long guildId) {
        return getMembersInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countExactMembersInGuild(long guildId) {
        // TODO: can we provide a different implementation?
        return countMembersInGuild(guildId);
    }

    @Override
    public Mono<Long> countMessages() {
        return stateHolder.getMessageStore().count();
    }

    @Override
    public Mono<Long> countMessagesInChannel(long channelId) {
        return getMessagesInChannel(channelId).count();
    }

    @Override
    public Mono<Long> countPresences() {
        return stateHolder.getPresenceStore().count();
    }

    @Override
    public Mono<Long> countPresencesInGuild(long guildId) {
        return getPresencesInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countRoles() {
        return stateHolder.getRoleStore().count();
    }

    @Override
    public Mono<Long> countRolesInGuild(long guildId) {
        return getRolesInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countUsers() {
        return stateHolder.getUserStore().count();
    }

    @Override
    public Mono<Long> countVoiceStates() {
        return stateHolder.getVoiceStateStore().count();
    }

    @Override
    public Mono<Long> countVoiceStatesInGuild(long guildId) {
        return getVoiceStatesInGuild(guildId).count();
    }

    @Override
    public Mono<Long> countVoiceStatesInChannel(long guildId, long channelId) {
        return getVoiceStatesInChannel(guildId, channelId).count();
    }

    @Override
    public Flux<ChannelData> getChannels() {
        return stateHolder.getChannelStore().values();
    }

    @Override
    public Flux<ChannelData> getChannelsInGuild(long guildId) {
        return stateHolder.getGuildStore().find(guildId)
            .flatMapMany(guild -> Flux.fromStream(guild.channels().stream().map(Snowflake::asLong)))
            .flatMap(id -> stateHolder.getChannelStore().find(id));
    }

    @Override
    public Mono<ChannelData> getChannelById(long channelId) {
        return stateHolder.getChannelStore().find(channelId);
    }

    @Override
    public Flux<StickerData> getStickers() {
        return stateHolder.getGuildStickerStore().values();
    }

    @Override
    public Flux<StickerData> getStickersInGuild(long guildId) {
        return stateHolder.getGuildStore().find(guildId)
            .flatMapMany(guild -> Flux.fromStream(guild.stickers().toOptional().orElse(Collections.emptyList()).stream().map(Snowflake::asLong)))
            .flatMap(id -> stateHolder.getGuildStickerStore().find(id));
    }

    @Override
    public Mono<StickerData> getStickerById(long guildId, long stickerId) {
        return stateHolder.getGuildStickerStore().find(stickerId);
    }

    @Override
    public Flux<EmojiData> getEmojis() {
        return stateHolder.getGuildEmojiStore().values();
    }

    @Override
    public Flux<EmojiData> getEmojisInGuild(long guildId) {
        return stateHolder.getGuildStore().find(guildId)
            .flatMapMany(guild -> Flux.fromStream(guild.emojis().stream().map(Snowflake::asLong)))
            .flatMap(id -> stateHolder.getGuildEmojiStore().find(id));
    }

    @Override
    public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
        return stateHolder.getGuildEmojiStore().find(emojiId);
    }

    @Override
    public Flux<GuildData> getGuilds() {
        return stateHolder.getGuildStore().values();
    }

    @Override
    public Mono<GuildData> getGuildById(long guildId) {
        return stateHolder.getGuildStore().find(guildId);
    }

    @Override
    public Flux<GuildScheduledEventData> getScheduledEventsInGuild(long guildId) {
        return stateHolder.getGuildEventsStore()
            .findInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, Long.MAX_VALUE));
    }

    @Override
    public Mono<GuildScheduledEventData> getScheduledEventById(long guildId, long eventId) {
        return stateHolder.getGuildEventsStore().find(LongLongTuple2.of(guildId, eventId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Flux<Id> getScheduledEventUsersInEvent(long guildId, long eventId) {
        return stateHolder.getGuildEventsUsersStore().find(LongLongTuple2.of(guildId, eventId))
            .flatMapIterable(list -> list)
            .map(value -> Id.of((Long) value));
    }

    @Override
    public Flux<MemberData> getMembers() {
        return stateHolder.getMemberStore().values();
    }

    @Override
    public Flux<MemberData> getMembersInGuild(long guildId) {
        return stateHolder.getMemberStore()
            .findInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, Long.MAX_VALUE));
    }

    @Override
    public Flux<MemberData> getExactMembersInGuild(long guildId) {
        return stateHolder.getMemberStore()
            .findInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, Long.MAX_VALUE));
    }

    @Override
    public Mono<MemberData> getMemberById(long guildId, long userId) {
        return stateHolder.getMemberStore().find(LongLongTuple2.of(guildId, userId));
    }

    @Override
    public Flux<MessageData> getMessages() {
        return stateHolder.getMessageStore().values();
    }

    @Override
    public Flux<MessageData> getMessagesInChannel(long channelId) {
        return stateHolder.getMessageStore().values()
            .filter(data -> Snowflake.asLong(data.channelId()) == channelId);
    }

    @Override
    public Mono<MessageData> getMessageById(long channelId, long messageId) {
        return stateHolder.getMessageStore().find(messageId);
    }

    @Override
    public Flux<PresenceData> getPresences() {
        return stateHolder.getPresenceStore().values();
    }

    @Override
    public Flux<PresenceData> getPresencesInGuild(long guildId) {
        return stateHolder.getPresenceStore()
            .findInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, Long.MAX_VALUE));
    }

    @Override
    public Mono<PresenceData> getPresenceById(long guildId, long userId) {
        return stateHolder.getPresenceStore().find(LongLongTuple2.of(guildId, userId));
    }

    @Override
    public Flux<RoleData> getRoles() {
        return stateHolder.getRoleStore().values();
    }

    @Override
    public Flux<RoleData> getRolesInGuild(long guildId) {
        return stateHolder.getGuildStore().find(guildId)
            .flatMapMany(guild -> Flux.fromStream(guild.roles().stream().map(Snowflake::asLong)))
            .flatMap(id -> stateHolder.getRoleStore().find(id));
    }

    @Override
    public Mono<RoleData> getRoleById(long guildId, long roleId) {
        return stateHolder.getRoleStore().find(roleId);
    }

    @Override
    public Flux<UserData> getUsers() {
        return stateHolder.getUserStore().values();
    }

    @Override
    public Mono<UserData> getUserById(long userId) {
        return stateHolder.getUserStore().find(userId);
    }

    @Override
    public Flux<VoiceStateData> getVoiceStates() {
        return stateHolder.getVoiceStateStore().values();
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId) {
        return stateHolder.getVoiceStateStore()
            .findInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, Long.MAX_VALUE))
            .filter(data -> data.channelId()
                .filter(id -> Snowflake.asLong(id) == channelId)
                .isPresent());
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInGuild(long guildId) {
        return stateHolder.getVoiceStateStore()
            .findInRange(LongLongTuple2.of(guildId, 0), LongLongTuple2.of(guildId, Long.MAX_VALUE));
    }

    @Override
    public Mono<VoiceStateData> getVoiceStateById(long guildId, long userId) {
        return stateHolder.getVoiceStateStore().find(LongLongTuple2.of(guildId, userId));
    }

    @Override
    public Mono<StageInstanceData> getStageInstanceByChannelId(long channelId) {
        return stateHolder.getStageInstanceStore().find(channelId);
    }

    @Override
    public Mono<ThreadMemberData> getThreadMemberById(long threadId, long userId) {
        return stateHolder.getThreadMemberStore().find(LongLongTuple2.of(threadId, userId));
    }

    @Override
    public Flux<ThreadMemberData> getMembersInThread(long threadId) {
        return stateHolder.getThreadMemberStore()
                .findInRange(LongLongTuple2.of(threadId, 0), LongLongTuple2.of(threadId, Long.MAX_VALUE));
    }

    /// //////////////////////////////////////////////////////////////////////////
    /// / Command model methods
    /// //////////////////////////////////////////////////////////////////////////

    @Override
    public Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch) {
        Type type = Type.of(dispatch.channel().type());
        switch (type) {
            case GUILD_TEXT:
            case GUILD_VOICE:
            case GUILD_CATEGORY:
            case GUILD_NEWS:
            case GUILD_STORE:
            case GUILD_STAGE_VOICE: return saveChannel(dispatch);
            case DM:
            case GROUP_DM: return Mono.empty();
            default: throw new IllegalArgumentException("Unhandled channel type " + dispatch.channel().type());
        }
    }

    private Mono<Void> saveChannel(ChannelCreate data) {
        ChannelData channel = data.channel();

        Mono<Void> addChannelToGuild = stateHolder.getGuildStore()
            .find(Snowflake.asLong(channel.guildId().get()))
            .map(guildData -> GuildData.builder()
                .from(guildData)
                .channels(ListUtil.add(guildData.channels(), channel.id()))
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(Snowflake.asLong(guild.id()), guild));

        Mono<Void> saveChannel = stateHolder.getChannelStore()
            .save(Snowflake.asLong(channel.id()), channel);

        return addChannelToGuild.then(saveChannel);
    }

    @Override
    public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
        Type type = Type.of(dispatch.channel().type());
        switch (type) {
            case GUILD_TEXT:
            case GUILD_VOICE:
            case GUILD_CATEGORY:
            case GUILD_NEWS:
            case GUILD_STORE:
            case GUILD_STAGE_VOICE: return deleteChannel(dispatch);
            case DM:
            case GROUP_DM: return Mono.empty();
            default: throw new IllegalArgumentException("Unhandled channel type " + dispatch.channel().type());
        }
    }

    private Mono<ChannelData> deleteChannel(ChannelDelete data) {
        ChannelData channel = data.channel();

        Mono<Void> removeChannelFromGuild = stateHolder.getGuildStore()
            .find(Snowflake.asLong(channel.guildId().get()))
            .map(guildData -> GuildData.builder()
                .from(guildData)
                .channels(ListUtil.remove(guildData.channels(), ch -> channel.id().equals(ch)))
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(Snowflake.asLong(guild.id()), guild));

        Mono<Void> deleteChannel = stateHolder.getChannelStore()
            .delete(Snowflake.asLong(channel.id()));

        return removeChannelFromGuild.then(deleteChannel).thenReturn(channel);
    }

    @Override
    public Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch) {
        Type type = Type.of(dispatch.channel().type());
        switch (type) {
            case GUILD_TEXT:
            case GUILD_VOICE:
            case GUILD_CATEGORY:
            case GUILD_NEWS:
            case GUILD_STORE:
            case GUILD_STAGE_VOICE: return updateChannel(dispatch.channel());
            case DM:
            case GROUP_DM: return Mono.empty();
            default: throw new IllegalArgumentException("Unhandled channel type " + dispatch.channel().type());
        }
    }

    private enum Type {
        UNKNOWN(-1),
        GUILD_TEXT(0),
        DM(1),
        GUILD_VOICE(2),
        GROUP_DM(3),
        GUILD_CATEGORY(4),
        GUILD_NEWS(5),
        GUILD_STORE(6),
        GUILD_NEWS_THREAD(10),
        GUILD_PUBLIC_THREAD(11),
        GUILD_PRIVATE_THREAD(12),
        GUILD_STAGE_VOICE(13);

        private final int value;

        Type(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Type of(final int value) {
            switch (value) {
                case 0: return GUILD_TEXT;
                case 1: return DM;
                case 2: return GUILD_VOICE;
                case 3: return GROUP_DM;
                case 4: return GUILD_CATEGORY;
                case 5: return GUILD_NEWS;
                case 6: return GUILD_STORE;
                case 10: return GUILD_NEWS_THREAD;
                case 11: return GUILD_PUBLIC_THREAD;
                case 12: return GUILD_PRIVATE_THREAD;
                case 13: return GUILD_STAGE_VOICE;
                default: return UNKNOWN;
            }
        }
    }

    private Mono<ChannelData> updateChannel(ChannelData channel) {
        Mono<Void> saveNew = stateHolder.getChannelStore().save(Snowflake.asLong(channel.id()), channel);

        return stateHolder.getChannelStore()
            .find(Snowflake.asLong(channel.id()))
            .flatMap(saveNew::thenReturn)
            .switchIfEmpty(saveNew.then(Mono.empty()));
    }

    @Override
    public Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch) {
        GuildCreateData createData;
        GuildData guild;
        // TODO is there a better solution to this?
        if (dispatch.guild().large()) {
            // Solves https://github.com/Discord4J/Discord4J/issues/429
            // Member store cannot have duplicates because keys cannot
            // be duplicated, but array addition in GuildBean can
            //guildBean.setMembers(new long[0]);
            createData = GuildCreateData.builder()
                .from(dispatch.guild())
                .members(Collections.emptyList())
                .build();
            guild = GuildData.builder()
                .from(createData)
                .roles(createData.roles().stream().map(RoleData::id).collect(Collectors.toList()))
                .emojis(createData.emojis().stream().map(EmojiData::id).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
                .channels(createData.channels().stream().map(ChannelData::id).collect(Collectors.toList()))
                .build();
        } else {
            createData = dispatch.guild();
            guild = GuildData.builder()
                .from(createData)
                .roles(createData.roles().stream().map(RoleData::id).collect(Collectors.toList()))
                .emojis(createData.emojis().stream().map(EmojiData::id).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
                .members(createData.members().stream().map(data -> data.user().id()).distinct().collect(Collectors.toList()))
                .channels(createData.channels().stream().map(ChannelData::id).collect(Collectors.toList()))
                .build();
        }

        long guildId = Snowflake.asLong(guild.id());

        Mono<Void> saveGuild = stateHolder.getGuildStore().save(guildId, guild)
            .doOnSubscribe(s -> log.trace("GuildCreate doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildCreate doFinally {}: {}", guildId, s));

        Mono<Void> saveChannels = stateHolder.getChannelStore()
            .save(Flux.fromIterable(createData.channels())
                .map(channel -> Tuples.of(Snowflake.asLong(channel.id()),
                    ChannelData.builder().from(channel).guildId(guild.id()).build())));

        Mono<Void> saveRoles = stateHolder.getRoleStore()
            .save(Flux.fromIterable(createData.roles())
                .map(role -> Tuples.of(Snowflake.asLong(role.id()), role)));

        Mono<Void> saveEmojis = stateHolder.getGuildEmojiStore()
            .save(Flux.fromIterable(createData.emojis())
                .map(emoji -> Tuples.of(Snowflake.asLong(emoji.id()
                    .orElseThrow(NoSuchElementException::new)), emoji)));

        Mono<Void> saveMembers = stateHolder.getMemberStore()
            .save(Flux.fromIterable(createData.members())
                .map(member -> Tuples.of(LongLongTuple2.of(guildId,
                    Snowflake.asLong(member.user().id())), member)));

        Mono<Void> saveUsers = stateHolder.getUserStore()
            .save(Flux.fromIterable(createData.members())
                .map(MemberData::user)
                .map(user -> Tuples.of(Snowflake.asLong(user.id()), user)));

        Mono<Void> saveVoiceStates = stateHolder.getVoiceStateStore()
            .save(Flux.fromIterable(createData.voiceStates())
                .map(voiceState -> Tuples.of(LongLongTuple2.of(guildId,
                        Snowflake.asLong(voiceState.userId())),
                    VoiceStateData.builder()
                        .from(voiceState)
                        .guildId(guild.id())
                        .build())));

        Mono<Void> saveThreads = stateHolder.getChannelStore()
                .save(Flux.fromIterable(createData.threads())
                        .map(thread -> Tuples.of(Snowflake.asLong(thread.id()), thread)));

        Mono<Void> savePresences = stateHolder.getPresenceStore()
            .save(Flux.fromIterable(createData.presences())
                .map(presence -> Tuples.of(LongLongTuple2.of(guildId,
                    Snowflake.asLong(presence.user().id())), presence)));

        Mono<Void> saveOfflinePresences = Flux.fromIterable(createData.members())
            .filterWhen(member -> stateHolder.getPresenceStore()
                .find(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())))
                .hasElement()
                .map(id -> !id))
            .flatMap(member -> stateHolder.getPresenceStore()
                .save(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())),
                    createPresence(member)))
            .then();

        return saveGuild
                .and(saveChannels)
                .and(saveRoles)
                .and(saveEmojis)
                .and(saveMembers)
                .and(saveUsers)
                .and(saveVoiceStates)
                .and(saveThreads)
                .and(savePresences)
                .and(saveOfflinePresences);
    }

    private PresenceData createPresence(MemberData member) {
        return PresenceData.builder()
            .user(PartialUserData.builder()
                .id(member.user().id())
                .globalName(Possible.of(member.user().globalName()))
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
            .status("offline")
            .clientStatus(ClientStatusData.builder()
                .desktop(Possible.absent())
                .mobile(Possible.absent())
                .web(Possible.absent())
                .build())
            .build();
    }

    @Override
    public Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch) {
        long guildId = Snowflake.asLong(dispatch.guild().id());

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
                    // TODO delete thread members
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
            .flatMap(deleteGuild::thenReturn);
    }

    @Override
    public Mono<Set<StickerData>> onGuildStickersUpdate(int shardIndex, GuildStickersUpdate dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());

        Mono<Void> updateGuildBean = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .emojis(dispatch.stickers().stream()
                    .map(StickerData::id)
                    .collect(Collectors.toList()))
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildStickersUpdate doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildStickersUpdate doFinally {}: {}", guildId, s));

        Mono<Void> saveStickers = stateHolder.getGuildStickerStore()
            .saveWithLong(Flux.fromIterable(dispatch.stickers())
                .map(sticker -> LongObjTuple2.of(Snowflake.asLong(sticker.id()), sticker)));

        return stateHolder.getGuildStore()
            .find(guildId)
            .flatMapMany(guild -> updateGuildBean
                .and(saveStickers)
                .thenMany(Flux.fromIterable(guild.stickers().toOptional().orElse(Collections.emptyList())).map(Snowflake::asLong)
                    .flatMap(id -> stateHolder.getGuildStickerStore().find(id))))
            .collect(Collectors.toSet());
    }

    @Override
    public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());

        Mono<Void> updateGuildBean = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .emojis(dispatch.emojis().stream()
                    .map(EmojiData::id)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()))
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildEmojisUpdate doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildEmojisUpdate doFinally {}: {}", guildId, s));

        Mono<Void> saveEmojis = stateHolder.getGuildEmojiStore()
            .saveWithLong(Flux.fromIterable(dispatch.emojis())
                .map(emoji -> LongObjTuple2.of(emoji.id()
                    .map(Snowflake::asLong)
                    .orElseThrow(NoSuchElementException::new), emoji)));

        return stateHolder.getGuildStore()
            .find(guildId)
            .flatMapMany(guild -> updateGuildBean
                .and(saveEmojis)
                .thenMany(Flux.fromIterable(guild.emojis()).map(Snowflake::asLong)
                    .flatMap(id -> stateHolder.getGuildEmojiStore().find(id))))
            .collect(Collectors.toSet());
    }

    @Override
    public Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        MemberData member = dispatch.member();
        UserData user = member.user();
        long userId = Snowflake.asLong(user.id());

        Mono<Void> addMemberId = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .members(ListUtil.add(guild.members(), member.user().id()))
                .memberCount(guild.memberCount() + 1)
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildMemberAdd doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildMemberAdd doFinally {}: {}", guildId, s));

        Mono<Void> saveMember = stateHolder.getMemberStore()
            .save(LongLongTuple2.of(guildId, userId), member);

        Mono<Void> saveUser = stateHolder.getUserStore()
            .save(userId, user);

        return addMemberId
            .and(saveMember)
            .and(saveUser);
    }

    @Override
    public Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        UserData userData = dispatch.user();
        long userId = Snowflake.asLong(userData.id());

        Mono<Void> removeMemberId = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .members(ListUtil.remove(guild.members(), member -> member.equals(userData.id())))
                .memberCount(guild.memberCount() - 1)
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildMemberRemove doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildMemberRemove doFinally {}: {}", guildId, s));

        Mono<MemberData> member = stateHolder.getMemberStore()
            .find(LongLongTuple2.of(guildId, userId));

        Mono<Void> deleteMember = stateHolder.getMemberStore()
            .delete(LongLongTuple2.of(guildId, userId));

        Mono<Void> deletePresence = stateHolder.getPresenceStore()
            .delete(LongLongTuple2.of(guildId, userId));

        Mono<Void> deleteOrphanUser = stateHolder.getMemberStore()
            .keys().filter(key -> key.getT1() != guildId && key.getT2() == userId)
            .hasElements()
            .flatMap(hasMutualServers -> Mono.just(userId)
                .filter(__ -> !hasMutualServers)
                .flatMap(stateHolder.getUserStore()::delete));

        return member.flatMap(value -> Mono.when(removeMemberId, deleteMember, deletePresence, deleteOrphanUser)
            .thenReturn(value));
    }

    @Override
    public Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        List<MemberData> members = dispatch.members();

        Flux<Tuple2<LongLongTuple2, MemberData>> memberPairs = Flux.fromIterable(members)
            .map(data -> Tuples.of(LongLongTuple2.of(guildId, Snowflake.asLong(data.user().id())),
                data));

        Flux<Tuple2<Long, UserData>> userPairs = Flux.fromIterable(members)
            .map(data -> Tuples.of(Snowflake.asLong(data.user().id()), data.user()));

        Mono<Void> addMemberIds = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .members(ListUtil.addAllDistinct(guild.members(), members.stream()
                    .map(data -> data.user().id())
                    .collect(Collectors.toList())))
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildMembersChunk doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildMembersChunk doFinally {}: {}", guildId, s));

        Mono<Void> saveMembers = stateHolder.getMemberStore().save(memberPairs);

        Mono<Void> saveUsers = stateHolder.getUserStore().save(userPairs);

        Mono<Void> saveOfflinePresences = Flux.fromIterable(members)
            .filterWhen(member -> stateHolder.getPresenceStore()
                .find(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())))
                .hasElement()
                .map(identity -> !identity))
            .flatMap(member -> stateHolder.getPresenceStore()
                .save(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())),
                    createPresence(member)))
            .then();

        return addMemberIds
            .and(saveMembers)
            .and(saveUsers)
            .and(saveOfflinePresences);
    }

    @Override
    public Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        long memberId = Snowflake.asLong(dispatch.user().id());

        LongLongTuple2 key = LongLongTuple2.of(guildId, memberId);

        return stateHolder.getMemberStore()
            .find(key)
            .flatMap(oldMember -> {
                MemberData newMember = MemberData.builder()
                    .from(oldMember)
                    .avatar(dispatch.avatar())
                    .banner(Possible.of(dispatch.banner()))
                    .roles(dispatch.roles().stream().map(Id::of).collect(Collectors.toList()))
                    .user(dispatch.user())
                    .nick(dispatch.nick())
                    .joinedAt(dispatch.joinedAt())
                    .premiumSince(dispatch.premiumSince())
                    .pending(dispatch.pending())
                    .build();

                return stateHolder.getMemberStore().save(key, newMember).thenReturn(oldMember);
            });
    }

    @Override
    public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        RoleData role = dispatch.role();

        Mono<Void> addRoleId = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .addRole(role.id())
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildRoleCreate doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildRoleCreate doFinally {}: {}", guildId, s));

        Mono<Void> saveRole = stateHolder.getRoleStore()
            .save(Snowflake.asLong(role.id()), role);

        return addRoleId.and(saveRole);
    }

    @Override
    public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        long roleId = Snowflake.asLong(dispatch.roleId());

        @SuppressWarnings("ReactiveStreamsUnusedPublisher")
        Mono<Void> removeRoleId = stateHolder.getGuildStore()
            .find(guildId)
            .map(guild -> GuildData.builder()
                .from(guild)
                .roles(ListUtil.remove(guild.roles(), role -> role.equals(dispatch.roleId())))
                .build())
            .flatMap(guild -> stateHolder.getGuildStore().save(guildId, guild))
            .doOnSubscribe(s -> log.trace("GuildRoleDelete doOnSubscribe {}", guildId))
            .doFinally(s -> log.trace("GuildRoleDelete doFinally {}: {}", guildId, s));

        Mono<Void> deleteRole = stateHolder.getRoleStore().delete(roleId);

        @SuppressWarnings("ReactiveStreamsUnusedPublisher")
        Mono<Void> removeRoleFromMembers = stateHolder.getGuildStore()
            .find(guildId)
            .flatMapMany(guild -> Flux.fromIterable(guild.members())
                .map(Snowflake::asLong))
            .flatMap(memberId -> stateHolder.getMemberStore()
                .find(LongLongTuple2.of(guildId, memberId)))
            .filter(member -> member.roles().contains(dispatch.roleId()))
            .map(member -> MemberData.builder()
                .from(member)
                .roles(ListUtil.remove(member.roles(),
                    role -> role.equals(dispatch.roleId())))
                .build())
            .flatMap(member -> stateHolder.getMemberStore()
                .save(LongLongTuple2.of(guildId, Snowflake.asLong(member.user().id())), member))
            .then();

        return stateHolder.getRoleStore()
            .find(roleId)
            .flatMap(removeRoleId::thenReturn)
            .flatMap(deleteRole::thenReturn)
            .flatMap(removeRoleFromMembers::thenReturn);
    }

    @Override
    public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        RoleData role = dispatch.role();
        long roleId = Snowflake.asLong(role.id());

        Mono<Void> saveNew = stateHolder.getRoleStore().save(roleId, role);

        return stateHolder.getRoleStore()
            .find(roleId)
            .flatMap(saveNew::thenReturn)
            .switchIfEmpty(saveNew.then(Mono.empty()));
    }

    @Override
    public Mono<Void> onGuildScheduledEventCreate(int shardIndex, GuildScheduledEventCreate dispatch) {
        LongLongTuple2 key = LongLongTuple2.of(dispatch.scheduledEvent().guildId().asLong(),
            dispatch.scheduledEvent().id().asLong());

        return stateHolder.getGuildEventsStore().save(key, dispatch.scheduledEvent());
    }

    @Override
    public Mono<GuildScheduledEventData> onGuildScheduledEventUpdate(int shardIndex,
                                                                     GuildScheduledEventUpdate dispatch) {
        LongLongTuple2 key = LongLongTuple2.of(dispatch.scheduledEvent().guildId().asLong(),
            dispatch.scheduledEvent().id().asLong());

        Mono<Void> saveNew = stateHolder.getGuildEventsStore().save(key, dispatch.scheduledEvent());

        return stateHolder.getGuildEventsStore()
            .find(key)
            .flatMap(saveNew::thenReturn)
            .switchIfEmpty(saveNew.then(Mono.empty()));
    }

    @Override
    public Mono<GuildScheduledEventData> onGuildScheduledEventDelete(int shardIndex,
                                                                     GuildScheduledEventDelete dispatch) {
        LongLongTuple2 key = LongLongTuple2.of(dispatch.scheduledEvent().guildId().asLong(),
            dispatch.scheduledEvent().id().asLong());

        Mono<Void> deletion = stateHolder.getGuildEventsStore().delete(key);

        return stateHolder.getGuildEventsStore()
            .find(key)
            .flatMap(deletion::thenReturn)
            .switchIfEmpty(deletion.then(Mono.empty()));
    }


    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> onGuildScheduledEventUserAdd(int shardIndex, GuildScheduledEventUserAdd dispatch) {
        LongLongTuple2 key = LongLongTuple2.of(dispatch.guildId().asLong(), dispatch.scheduledEventId().asLong());

        return stateHolder.getGuildEventsUsersStore().find(key)
            .defaultIfEmpty(new HashSet<Long>())
            .map(set -> {
                set.add(dispatch.userId().asLong());
                return set;
            })
            .flatMap(set -> stateHolder.getGuildEventsUsersStore().save(key, set));
    }

    @Override
    public Mono<Void> onGuildScheduledEventUserRemove(int shardIndex, GuildScheduledEventUserRemove dispatch) {
        LongLongTuple2 key = LongLongTuple2.of(dispatch.guildId().asLong(), dispatch.scheduledEventId().asLong());

        return stateHolder.getGuildEventsUsersStore().find(key)
            .defaultIfEmpty(new HashSet<Long>())
            .map(set -> {
                set.remove(dispatch.userId().asLong());
                return set;
            })
            .flatMap(set -> stateHolder.getGuildEventsUsersStore().save(key, set));
    }

    @Override
    public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
        long guildId = Snowflake.asLong(dispatch.guild().id());

        return stateHolder.getGuildStore()
            .find(guildId)
            .flatMap(oldGuildData -> {
                GuildData newGuildData = GuildData.builder()
                    .from(oldGuildData)
                    .from(dispatch.guild())
                    .roles(dispatch.guild().roles().stream()
                        .map(RoleData::id)
                        .collect(Collectors.toList()))
                    .emojis(dispatch.guild().emojis().stream()
                        .map(EmojiData::id)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()))
                    .build();

                return stateHolder.getGuildStore()
                    .save(guildId, newGuildData)
                    .doOnSubscribe(s -> log.trace("GuildUpdate doOnSubscribe {}", guildId))
                    .doFinally(s -> log.trace("GuildUpdate doFinally {}: {}", guildId, s))
                    .thenReturn(oldGuildData);
            });
    }

    @Override
    public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
        // TODO implement onShardInvalidation
        return Mono.empty();
    }

    @Override
    public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
        MessageData message = dispatch.message();
        long messageId = Snowflake.asLong(message.id());
        long channelId = Snowflake.asLong(message.channelId());

        Mono<Void> saveMessage = stateHolder.getMessageStore()
            .save(messageId, message);

        Mono<Void> editLastMessageId = stateHolder.getChannelStore()
            .find(channelId)
            .map(channel -> ChannelData.builder()
                .from(channel)
                .lastMessageId(message.id())
                .build())
            .flatMap(channelBean -> stateHolder.getChannelStore().save(channelId, channelBean));

        return saveMessage.and(editLastMessageId);
    }

    @Override
    public Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch) {
        long messageId = Snowflake.asLong(dispatch.id());

        Mono<Void> deleteMessage = stateHolder.getMessageStore().delete(messageId);

        return stateHolder.getMessageStore()
            .find(messageId)
            .flatMap(deleteMessage::thenReturn);
    }

    @Override
    public Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
        List<Long> messageIds = dispatch.ids().stream()
            .map(Snowflake::asLong)
            .collect(Collectors.toList());

        Mono<Void> deleteMessages = stateHolder.getMessageStore()
            .delete(Flux.fromIterable(messageIds));

        return Flux.fromIterable(messageIds)
            .flatMap(stateHolder.getMessageStore()::find)
            .flatMap(deleteMessages::thenReturn)
            .collect(Collectors.toSet());
    }

    @Override
    public Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
        long selfId = 0L; // TODO include selfId as parameter
        long userId = Snowflake.asLong(dispatch.userId());
        long messageId = Snowflake.asLong(dispatch.messageId());

        // add reaction to message
        return stateHolder.getMessageStore()
            .find(messageId)
            .map(oldMessage -> {
                boolean me = Objects.equals(userId, selfId);
                ImmutableMessageData.Builder newMessageBuilder = MessageData.builder().from(oldMessage);

                if (oldMessage.reactions().isAbsent()) {
                    // If burst, increment the burst count, otherwise the normal count
                    ReactionCountDetailsData countDetailsData = ReactionCountDetailsData.builder()
                        .normal(dispatch.burst() ? 0 : 1)
                        .burst(dispatch.burst() ? 1 : 0)
                        .build();

                    ImmutableReactionData.Builder reactionBuilder = ReactionData.builder()
                        .count(1)
                        .countDetails(countDetailsData)
                        .me(me)
                        // If I added the reaction, and this is a burst, this is a me_burst
                        .meBurst(me && dispatch.burst())
                        .emoji(dispatch.emoji());

                    if (!dispatch.burstColors().isAbsent()) {
                        reactionBuilder.burstColors(dispatch.burstColors().get());
                    }

                    newMessageBuilder.addReaction(reactionBuilder.build());
                } else {
                    List<ReactionData> reactions = oldMessage.reactions().get();
                    int i = indexOfReactionByEmojiData(reactions, dispatch.emoji());

                    if (i < reactions.size()) {
                        // message already has this reaction: bump 1
                        ReactionData oldExisting = reactions.get(i);

                        // Prepare the updated ReactionCountDetails object depending on if this is a burst or not
                        ReactionCountDetailsData countDetailsData;
                        if (dispatch.burst()) {
                            countDetailsData = ReactionCountDetailsData.builder()
                                .normal(oldExisting.countDetails().normal())
                                .burst(oldExisting.countDetails().burst() + 1)
                                .build();
                        } else {
                            countDetailsData = ReactionCountDetailsData.builder()
                                .normal(oldExisting.countDetails().normal() + 1)
                                .burst(oldExisting.countDetails().burst())
                                .build();
                        }

                        ReactionData newExisting = ReactionData.builder()
                            .from(oldExisting)
                            .me(oldExisting.me() || me)
                            // If the change is me adding a reaction, and this is a burst that was dispatched
                            // then this is a me_burst
                            .meBurst(!oldExisting.me() && me && dispatch.burst())
                            .count(oldExisting.count() + 1)
                            .countDetails(countDetailsData)
                            .build();
                        newMessageBuilder.reactions(ListUtil.replace(reactions,
                            oldExisting, newExisting));
                    } else {
                        // message doesn't have this reaction: create
                        ReactionCountDetailsData countDetailsData = ReactionCountDetailsData.builder()
                            .normal(dispatch.burst() ? 0 : 1)
                            .burst(dispatch.burst() ? 1 : 0)
                            .build();

                        ReactionData reaction = ReactionData.builder()
                            .emoji(dispatch.emoji())
                            .count(1)
                            .countDetails(countDetailsData)
                            .me(me)
                            // If I added the reaction, and this is a burst, this is a me_burst
                            .meBurst(me && dispatch.burst())
                            .build();
                        newMessageBuilder.reactions(ListUtil.add(reactions, reaction));
                    }
                }

                return newMessageBuilder.build();
            })
            .flatMap(message -> stateHolder.getMessageStore().save(messageId, message));
    }

    @Override
    public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        long selfId = 0L; // TODO include selfId as parameter
        long userId = Snowflake.asLong(dispatch.userId());
        long messageId = Snowflake.asLong(dispatch.messageId());

        // remove reactor from message
        return stateHolder.getMessageStore()
            .find(messageId)
            .filter(message -> !message.reactions().isAbsent())
            .map(oldMessage -> {
                boolean me = Objects.equals(userId, selfId);
                ImmutableMessageData.Builder newMessageBuilder = MessageData.builder().from(oldMessage);

                List<ReactionData> reactions = oldMessage.reactions().get();
                int i = indexOfReactionByEmojiData(reactions, dispatch.emoji());

                if (i < reactions.size()) {
                    ReactionData existing = reactions.get(i);
                    if (existing.count() - 1 == 0) {
                        newMessageBuilder.reactions(ListUtil.remove(reactions,
                            reaction -> reaction.equals(existing)));
                    } else {
                        ReactionCountDetailsData countDetailsData;
                        if (dispatch.burst()) {
                            countDetailsData = ReactionCountDetailsData.builder()
                                .normal(existing.countDetails().normal())
                                .burst(existing.countDetails().burst() - 1)
                                .build();
                        } else {
                            countDetailsData = ReactionCountDetailsData.builder()
                                .normal(existing.countDetails().normal() - 1)
                                .burst(existing.countDetails().burst())
                                .build();
                        }

                        ReactionData newExisting = ReactionData.builder()
                            .from(existing)
                            .count(existing.count() - 1)
                            .countDetails(countDetailsData)
                            .me(!me && existing.me())
                            .meBurst(existing.meBurst() && me)
                            .build();
                        newMessageBuilder.reactions(ListUtil.replace(reactions, existing, newExisting));
                    }
                }
                return newMessageBuilder.build();
            })
            .flatMap(message -> stateHolder.getMessageStore().save(messageId, message));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
        long messageId = Snowflake.asLong(dispatch.messageId());

        return stateHolder.getMessageStore()
            .find(messageId)
            .map(message -> MessageData.builder()
                .from(message)
                .reactions(Possible.absent())
                .build())
            .flatMap(message -> stateHolder.getMessageStore().save(messageId, message));
    }

    @Override
    public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
        long messageId = Snowflake.asLong(dispatch.messageId());

        return stateHolder.getMessageStore()
            .find(messageId)
            .filter(message -> !message.reactions().isAbsent())
            .map(oldMessage -> {
                ImmutableMessageData.Builder newMessageBuilder = MessageData.builder().from(oldMessage);

                List<ReactionData> reactions = oldMessage.reactions().get();
                int i = indexOfReactionByEmojiData(reactions, dispatch.emoji());

                if (i < reactions.size()) {
                    ReactionData existing = reactions.get(i);
                    newMessageBuilder.reactions(ListUtil.remove(reactions,
                        reaction -> reaction.equals(existing)));
                }
                return newMessageBuilder.build();
            })
            .flatMap(message -> stateHolder.getMessageStore().save(messageId, message));
    }

    private int indexOfReactionByEmojiData(List<ReactionData> reactions, EmojiData emojiData) {
        int i;
        for (i = 0; i < reactions.size(); i++) {
            ReactionData r = reactions.get(i);
            // (non-null id && matching id) OR (null id && matching name)
            boolean emojiHasId = emojiData.id().isPresent();
            if ((emojiHasId && emojiData.id().equals(r.emoji().id()))
                || (!emojiHasId && emojiData.name().equals(r.emoji().name()))) {
                break;
            }
        }
        return i;
    }

    @Override
    public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
        PartialMessageUpdateData messageData = dispatch.message();
        long messageId = Snowflake.asLong(messageData.id());

        return stateHolder.getMessageStore()
            .find(messageId)
            .flatMap(oldMessageData -> {
                // updating the content and embed of the bean in the store
                boolean contentChanged = !messageData.content().isAbsent() &&
                    !Objects.equals(oldMessageData.content(), messageData.content().get());
                boolean embedsChanged = !Objects.equals(oldMessageData.embeds(), messageData.embeds());

                MessageData newMessageData = MessageData.builder()
                    .from(oldMessageData)
                    .content(messageData.content().toOptional()
                        .orElse(oldMessageData.content()))
                    .embeds(messageData.embeds())
                    .mentions(messageData.mentions())
                    .mentionRoles(messageData.mentionRoles())
                    .mentionEveryone(messageData.mentionEveryone().toOptional()
                        .orElse(oldMessageData.mentionEveryone()))
                    .editedTimestamp(messageData.editedTimestamp())
                    .build();

                return stateHolder.getMessageStore()
                    .save(messageId, newMessageData)
                    .thenReturn(oldMessageData);
            });
    }

    @Override
    public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        long guildId = Snowflake.asLong(dispatch.guildId());
        PartialUserData userData = dispatch.user();
        long userId = Snowflake.asLong(userData.id());
        LongLongTuple2 key = LongLongTuple2.of(guildId, userId);
        PresenceData presenceData = PresenceData.builder()
            .user(dispatch.user())
            .status(dispatch.status())
            .activities(dispatch.activities())
            .clientStatus(dispatch.clientStatus())
            .build();

        Mono<Void> saveNew = stateHolder.getPresenceStore().save(key, presenceData);

        Mono<Optional<PresenceData>> savePresence = stateHolder.getPresenceStore()
            .find(key)
            .flatMap(saveNew::thenReturn)
            .map(Optional::of)
            .switchIfEmpty(saveNew.thenReturn(Optional.empty()));

        Mono<Optional<UserData>> saveUser = stateHolder.getUserStore()
            .find(userId)
            .flatMap(oldUserData -> {
                UserData newUserData = UserData.builder()
                    .from(oldUserData)
                    .globalName(userData.globalName().isAbsent() ? oldUserData.globalName() :
                        Possible.flatOpt(userData.globalName()))
                    .username(userData.username().toOptional()
                        .orElse(oldUserData.username()))
                    .discriminator(userData.discriminator().toOptional()
                        .orElse(oldUserData.discriminator()))
                    .avatar(userData.avatar().isAbsent() ? oldUserData.avatar() :
                        Possible.flatOpt(userData.avatar()))
                    .build();

                return stateHolder.getUserStore().save(userId, newUserData).thenReturn(oldUserData);
            })
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty());

        return Mono.zip(savePresence, saveUser,
            (p, u) -> PresenceAndUserData.of(p.orElse(null), u.orElse(null)));
    }

    @Override
    public Mono<Void> onReady(Ready dispatch) {
        UserData userData = dispatch.user();
        long userId = Snowflake.asLong(userData.id());

        return stateHolder.getUserStore().save(userId, userData);
    }

    @Override
    public Mono<Void> onStageInstanceCreate(int shardIndex, StageInstanceCreate dispatch) {
        StageInstanceData stageInstance = dispatch.stageInstance();
        long channelId = Snowflake.asLong(stageInstance.id());

        return stateHolder.getStageInstanceStore().save(channelId, stageInstance);
    }

    @Override
    public Mono<StageInstanceData> onStageInstanceUpdate(int shardIndex, StageInstanceUpdate dispatch) {
        StageInstanceData stageInstance = dispatch.stageInstance();
        long channelId = Snowflake.asLong(stageInstance.id());

        Mono<Void> saveNew = stateHolder.getStageInstanceStore().save(channelId, stageInstance);

        return stateHolder.getStageInstanceStore()
            .find(channelId)
            .flatMap(saveNew::thenReturn)
            .switchIfEmpty(saveNew.then(Mono.empty()));
    }

    @Override
    public Mono<StageInstanceData> onStageInstanceDelete(int shardIndex, StageInstanceDelete dispatch) {
        StageInstanceData stageInstance = dispatch.stageInstance();
        long channelId = Snowflake.asLong(stageInstance.id());

        Mono<Void> delete = stateHolder.getStageInstanceStore().delete(channelId);

        return stateHolder.getStageInstanceStore()
            .find(channelId)
            .flatMap(delete::thenReturn)
            .switchIfEmpty(delete.then(Mono.empty()));
    }

    @Override
    public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
        UserData userData = dispatch.user();
        long userId = Snowflake.asLong(userData.id());

        Mono<Void> saveNew = stateHolder.getUserStore().save(userId, userData);

        return stateHolder.getUserStore()
            .find(userId)
            .flatMap(saveNew::thenReturn)
            .switchIfEmpty(saveNew.then(Mono.empty()));
    }

    @Override
    public Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch) {
        VoiceStateData voiceStateData = dispatch.voiceState();

        long guildId = Snowflake.asLong(voiceStateData.guildId().get());
        long userId = Snowflake.asLong(voiceStateData.userId());

        LongLongTuple2 key = LongLongTuple2.of(guildId, userId);

        Mono<Void> saveNewOrRemove = voiceStateData.channelId().isPresent()
            ? stateHolder.getVoiceStateStore().save(key, voiceStateData)
            : stateHolder.getVoiceStateStore().delete(key);

        return stateHolder.getVoiceStateStore()
            .find(key)
            .flatMap(saveNewOrRemove::thenReturn)
            .switchIfEmpty(saveNewOrRemove.then(Mono.empty()));
    }

    @Override
    public Mono<Void> onGuildMembersCompletion(long guildId) {
        // TODO needs implementation
        return Mono.empty();
    }

    @Override
    public Mono<Void> onThreadCreate(int shardIndex, ThreadCreate dispatch) {
        return stateHolder.getChannelStore()
            .save(dispatch.thread().id().asLong(), dispatch.thread());
    }

    @Override
    public Mono<ChannelData> onThreadUpdate(int shardIndex, ThreadUpdate dispatch) {
        return updateChannel(dispatch.thread());
    }

    @Override
    public Mono<Void> onThreadDelete(int shardIndex, ThreadDelete dispatch) {
        long threadId = Snowflake.asLong(dispatch.thread().id());
        Mono<Void> deleteThread = stateHolder.getChannelStore()
            .delete(threadId);

        Mono<Void> deleteThreadMembers = stateHolder.getThreadMemberStore()
            .deleteInRange(LongLongTuple2.of(threadId, 0), LongLongTuple2.of(threadId, -1));

        return deleteThread.and(deleteThreadMembers);
    }

    @Override
    public Mono<Void> onThreadListSync(int shardIndex, ThreadListSync dispatch) {

        Mono<Void> saveThreads = Flux.fromIterable(dispatch.threads())
            .flatMap(thread -> stateHolder.getChannelStore().save(thread.id().asLong(), thread))
            .then();

        Mono<Void> saveThreadMembers = Flux.fromIterable(dispatch.members())
            .flatMap(threadMember -> {
                LongLongTuple2 id = LongLongTuple2.of(threadMember.id().get().asLong(),
                    threadMember.userId().get().asLong());
                return stateHolder.getThreadMemberStore().save(id, threadMember);
            })
            .then();

        return saveThreads.and(saveThreadMembers);
    }

    @Override
    public Mono<ThreadMemberData> onThreadMemberUpdate(int shardIndex, ThreadMemberUpdate dispatch) {
        ThreadMemberData member = dispatch.member();
        LongLongTuple2 id = LongLongTuple2.of(member.id().get().asLong(), member.userId().get().asLong());

        Mono<Void> saveNew = stateHolder.getThreadMemberStore().save(id, member);

        return stateHolder.getThreadMemberStore().find(id)
            .flatMap(saveNew::thenReturn)
            .switchIfEmpty(saveNew.then(Mono.empty()));
    }

    @Override
    public Mono<List<ThreadMemberData>> onThreadMembersUpdate(int shardIndex, ThreadMembersUpdate dispatch) {
        Mono<Void> addThreadMembers = Mono.justOrEmpty(dispatch.addedMembers().toOptional())
            .flatMapIterable(it -> it)
            .flatMap(threadMember -> {
                LongLongTuple2 id = LongLongTuple2.of(threadMember.id().get().asLong(),
                    threadMember.userId().get().asLong());
                return stateHolder.getThreadMemberStore().save(id, threadMember);
            })
            .then();

        long threadId = dispatch.id().asLong();
        Mono<List<ThreadMemberData>> removeThreadMembers = Mono.justOrEmpty(dispatch.removedMemberIds().toOptional())
            .flatMapIterable(it -> it)
            .map(id -> LongLongTuple2.of(threadId, id.asLong()))
            .flatMap(id -> {
                Mono<Void> delete = stateHolder.getThreadMemberStore().delete(id);

                return stateHolder.getThreadMemberStore().find(id)
                    .flatMap(delete::thenReturn)
                    .switchIfEmpty(delete.then(Mono.empty()));
            })
            .collectList();

        return addThreadMembers.then(removeThreadMembers);
    }
}
