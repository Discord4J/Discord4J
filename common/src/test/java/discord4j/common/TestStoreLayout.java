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

package discord4j.common;

import discord4j.common.store.api.layout.DataAccessor;
import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.layout.StoreLayout;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.List;
import java.util.Set;

public class TestStoreLayout implements StoreLayout, DataAccessor, GatewayDataUpdater {

    private static final Logger log = Loggers.getLogger(TestStoreLayout.class);

    private final String name;

    public TestStoreLayout(String name) {
        this.name = name;
    }

    @Override
    public DataAccessor getDataAccessor() {
        return this;
    }

    @Override
    public GatewayDataUpdater getGatewayDataUpdater() {
        return this;
    }

    @Override
    public Mono<Long> countChannels() {
        return Mono.just(0L).log(name + ".countChannels");
    }

    @Override
    public Mono<Long> countChannelsInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countChannelsInGuild");
    }

    @Override
    public Mono<Long> countStickers() {
        return Mono.just(0L).log(name + ".countStickers");
    }

    @Override
    public Mono<Long> countStickersInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countStickersInGuild");
    }

    @Override
    public Mono<Long> countEmojis() {
        return Mono.just(0L).log(name + ".countEmojis");
    }

    @Override
    public Mono<Long> countEmojisInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countEmojisInGuild");
    }

    @Override
    public Mono<Long> countGuilds() {
        return Mono.just(0L).log(name + ".countGuilds");
    }

    @Override
    public Mono<Long> countMembers() {
        return Mono.just(0L).log(name + ".countMembers");
    }

    @Override
    public Mono<Long> countMembersInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countMembersInGuild");
    }

    @Override
    public Mono<Long> countExactMembersInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countExactMembersInGuild");
    }

    @Override
    public Mono<Long> countMessages() {
        return Mono.just(0L).log(name + ".countMessages");
    }

    @Override
    public Mono<Long> countMessagesInChannel(long channelId) {
        return Mono.just(0L).log(name + ".countMessagesInChannel");
    }

    @Override
    public Mono<Long> countPresences() {
        return Mono.just(0L).log(name + ".countPresences");
    }

    @Override
    public Mono<Long> countPresencesInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countPresencesInGuild");
    }

    @Override
    public Mono<Long> countRoles() {
        return Mono.just(0L).log(name + ".countRoles");
    }

    @Override
    public Mono<Long> countRolesInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countRolesInGuild");
    }

    @Override
    public Mono<Long> countUsers() {
        return Mono.just(0L).log(name + ".countUsers");
    }

    @Override
    public Mono<Long> countVoiceStates() {
        return Mono.just(0L).log(name + ".countVoiceStates");
    }

    @Override
    public Mono<Long> countVoiceStatesInGuild(long guildId) {
        return Mono.just(0L).log(name + ".countVoiceStatesInGuild");
    }

    @Override
    public Mono<Long> countVoiceStatesInChannel(long guildId, long channelId) {
        return Mono.just(0L).log(name + ".countVoiceStatesInChannel");
    }

    @Override
    public Flux<ChannelData> getChannels() {
        return Flux.<ChannelData>empty().log(name + ".getChannels");
    }

    @Override
    public Flux<ChannelData> getChannelsInGuild(long guildId) {
        return Flux.<ChannelData>empty().log(name + ".getChannelsInGuild");
    }

    @Override
    public Mono<ChannelData> getChannelById(long channelId) {
        return Mono.<ChannelData>empty().log(name + ".getChannelById");
    }

    @Override
    public Flux<StickerData> getStickers() {
        return Flux.<StickerData>empty().log(name + ".getStickers");
    }

    @Override
    public Flux<StickerData> getStickersInGuild(long guildId) {
        return Flux.<StickerData>empty().log(name + ".getStickersInGuild");
    }

    @Override
    public Mono<StickerData> getStickerById(long guildId, long stickerId) {
        return Mono.<StickerData>empty().log(name + ".getStickerById");
    }

    @Override
    public Flux<EmojiData> getEmojis() {
        return Flux.<EmojiData>empty().log(name + ".getEmojis");
    }

    @Override
    public Flux<EmojiData> getEmojisInGuild(long guildId) {
        return Flux.<EmojiData>empty().log(name + ".getEmojisInGuild");
    }

    @Override
    public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
        return Mono.<EmojiData>empty().log(name + ".getEmojiById");
    }

    @Override
    public Flux<GuildData> getGuilds() {
        return Flux.<GuildData>empty().log(name + ".getGuilds");
    }

    @Override
    public Mono<GuildData> getGuildById(long guildId) {
        return Mono.<GuildData>empty().log(name + ".getGuildById");
    }

    @Override
    public Flux<GuildScheduledEventData> getScheduledEventsInGuild(long guildId) {
        return Flux.<GuildScheduledEventData>empty().log(name + ".getScheduledEventsInGuild");
    }

    @Override
    public Mono<GuildScheduledEventData> getScheduledEventById(long guildId, long eventId) {
        return Mono.<GuildScheduledEventData>empty().log(name + ".getScheduledEventById");
    }

    @Override
    public Flux<Id> getScheduledEventUsersInEvent(long guildId, long eventId) {
        return Flux.<Id>empty().log(name + ".getScheduledEventUsersInEvent");
    }

    @Override
    public Flux<MemberData> getMembers() {
        return Flux.<MemberData>empty().log(name + ".getMembers");
    }

    @Override
    public Flux<MemberData> getMembersInGuild(long guildId) {
        return Flux.<MemberData>empty().log(name + ".getMembersInGuild");
    }

    @Override
    public Flux<MemberData> getExactMembersInGuild(long guildId) {
        return Flux.<MemberData>empty().log(name + ".getExactMembersInGuild");
    }

    @Override
    public Mono<MemberData> getMemberById(long guildId, long userId) {
        return Mono.<MemberData>empty().log(name + ".getMemberById");
    }

    @Override
    public Flux<MessageData> getMessages() {
        return Flux.<MessageData>empty().log(name + ".getMessages");
    }

    @Override
    public Flux<MessageData> getMessagesInChannel(long channelId) {
        return Flux.<MessageData>empty().log(name + ".getMessagesInChannel");
    }

    @Override
    public Mono<MessageData> getMessageById(long channelId, long messageId) {
        return Mono.<MessageData>empty().log(name + ".getMessageById");
    }

    @Override
    public Flux<PresenceData> getPresences() {
        return Flux.<PresenceData>empty().log(name + ".getPresences");
    }

    @Override
    public Flux<PresenceData> getPresencesInGuild(long guildId) {
        return Flux.<PresenceData>empty().log(name + ".getPresencesInGuild");
    }

    @Override
    public Mono<PresenceData> getPresenceById(long guildId, long userId) {
        return Mono.<PresenceData>empty().log(name + ".getPresenceById");
    }

    @Override
    public Flux<RoleData> getRoles() {
        return Flux.<RoleData>empty().log(name + ".getRoles");
    }

    @Override
    public Flux<RoleData> getRolesInGuild(long guildId) {
        return Flux.<RoleData>empty().log(name + ".getRolesInGuild");
    }

    @Override
    public Mono<RoleData> getRoleById(long guildId, long roleId) {
        return Mono.<RoleData>empty().log(name + ".getRoleById");
    }

    @Override
    public Flux<UserData> getUsers() {
        return Flux.<UserData>empty().log(name + ".getUsers");
    }

    @Override
    public Mono<UserData> getUserById(long userId) {
        return Mono.<UserData>empty().log(name + ".getUserById");
    }

    @Override
    public Flux<VoiceStateData> getVoiceStates() {
        return Flux.<VoiceStateData>empty().log(name + ".getVoiceStates");
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId) {
        return Flux.<VoiceStateData>empty().log(name + ".getVoiceStatesInChannel");
    }

    @Override
    public Flux<VoiceStateData> getVoiceStatesInGuild(long guildId) {
        return Flux.<VoiceStateData>empty().log(name + ".getVoiceStatesInGuild");
    }

    @Override
    public Mono<VoiceStateData> getVoiceStateById(long guildId, long userId) {
        return Mono.<VoiceStateData>empty().log(name + ".getVoiceStateById");
    }

    @Override
    public Mono<StageInstanceData> getStageInstanceByChannelId(long channelId) {
        return Mono.<StageInstanceData>empty().log(name + ".getStageInstanceByChannelId");
    }

    @Override
    public Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onChannelCreate");
    }

    @Override
    public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
        return Mono.<ChannelData>empty().log(name + ".onChannelDelete");
    }

    @Override
    public Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch) {
        return Mono.<ChannelData>empty().log(name + ".onChannelUpdate");
    }

    @Override
    public Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildCreate");
    }

    @Override
    public Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch) {
        return Mono.<GuildData>empty().log(name + ".onGuildDelete");
    }

    @Override
    public Mono<Set<StickerData>> onGuildStickersUpdate(int shardIndex, GuildStickersUpdate dispatch) {
        return Mono.<Set<StickerData>>empty().log(name + ".onGuildStickersUpdate");
    }

    @Override
    public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
        return Mono.<Set<EmojiData>>empty().log(name + ".onGuildEmojisUpdate");
    }

    @Override
    public Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildMemberAdd");
    }

    @Override
    public Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
        return Mono.<MemberData>empty().log(name + ".onGuildMemberRemove");
    }

    @Override
    public Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildMembersChunk");
    }

    @Override
    public Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
        return Mono.<MemberData>empty().log(name + ".onGuildMemberUpdate");
    }

    @Override
    public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildRoleCreate");
    }

    @Override
    public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
        return Mono.<RoleData>empty().log(name + ".onGuildRoleDelete");
    }

    @Override
    public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
        return Mono.<RoleData>empty().log(name + ".onGuildRoleUpdate");
    }

    @Override
    public Mono<Void> onGuildScheduledEventCreate(int shardIndex, GuildScheduledEventCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildScheduledEventCreate");
    }

    @Override
    public Mono<GuildScheduledEventData> onGuildScheduledEventUpdate(int shardIndex, GuildScheduledEventUpdate dispatch) {
        return Mono.<GuildScheduledEventData>empty().log(name + ".onGuildScheduledEventUpdate");
    }

    @Override
    public Mono<GuildScheduledEventData> onGuildScheduledEventDelete(int shardIndex, GuildScheduledEventDelete dispatch) {
        return Mono.<GuildScheduledEventData>empty().log(name + ".onGuildScheduledEventDelete");
    }

    @Override
    public Mono<Void> onGuildScheduledEventUserAdd(int shardIndex, GuildScheduledEventUserAdd dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildScheduledEventUserAdd");
    }

    @Override
    public Mono<Void> onGuildScheduledEventUserRemove(int shardIndex, GuildScheduledEventUserRemove dispatch) {
        return Mono.<Void>empty().log(name + ".onGuildScheduledEventUserRemove");
    }

    @Override
    public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
        return Mono.<GuildData>empty().log(name + ".onGuildUpdate");
    }

    @Override
    public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
        return Mono.<Void>empty().log(name + ".onShardInvalidation");
    }

    @Override
    public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onMessageCreate");
    }

    @Override
    public Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch) {
        return Mono.<MessageData>empty().log(name + ".onMessageDelete");
    }

    @Override
    public Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
        return Mono.<Set<MessageData>>empty().log(name + ".onMessageDeleteBulk");
    }

    @Override
    public Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
        return Mono.<Void>empty().log(name + ".onMessageReactionAdd");
    }

    @Override
    public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
        return Mono.<Void>empty().log(name + ".onMessageReactionRemove");
    }

    @Override
    public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
        return Mono.<Void>empty().log(name + ".onMessageReactionRemoveAll");
    }

    @Override
    public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
        return Mono.<Void>empty().log(name + ".onMessageReactionRemoveEmoji");
    }

    @Override
    public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
        return Mono.<MessageData>empty().log(name + ".onMessageUpdate");
    }

    @Override
    public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
        return Mono.<PresenceAndUserData>empty().log(name + ".onPresenceUpdate");
    }

    @Override
    public Mono<Void> onReady(Ready dispatch) {
        return Mono.<Void>empty().log(name + ".onReady");
    }

    @Override
    public Mono<Void> onStageInstanceCreate(int shardIndex, StageInstanceCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onStageInstanceCreate");
    }

    @Override
    public Mono<StageInstanceData> onStageInstanceUpdate(int shardIndex, StageInstanceUpdate dispatch) {
        return Mono.<StageInstanceData>empty().log(name + ".onStageInstanceUpdate");
    }

    @Override
    public Mono<StageInstanceData> onStageInstanceDelete(int shardIndex, StageInstanceDelete dispatch) {
        return Mono.<StageInstanceData>empty().log(name + ".onStageInstanceDelete");
    }

    @Override
    public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
        return Mono.<UserData>empty().log(name + ".onUserUpdate");
    }

    @Override
    public Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch) {
        return Mono.<VoiceStateData>empty().log(name + ".onVoiceStateUpdateDispatch");
    }

    @Override
    public Mono<Void> onGuildMembersCompletion(long guildId) {
        return Mono.<Void>empty().log(name + ".onGuildMembersCompletion");
    }

    @Override
    public Mono<Void> onThreadCreate(int shardIndex, ThreadCreate dispatch) {
        return Mono.<Void>empty().log(name + ".onThreadCreate");
    }

    @Override
    public Mono<ChannelData> onThreadUpdate(int shardIndex, ThreadUpdate dispatch) {
        return Mono.<ChannelData>empty().log(name + ".onThreadUpdate");
    }

    @Override
    public Mono<Void> onThreadDelete(int shardIndex, ThreadDelete dispatch) {
        return Mono.<Void>empty().log(name + ".onThreadDelete");
    }

    @Override
    public Mono<Void> onThreadListSync(int shardIndex, ThreadListSync dispatch) {
        return Mono.<Void>empty().log(name + ".onThreadListSync");
    }

    @Override
    public Mono<ThreadMemberData> onThreadMemberUpdate(int shardIndex, ThreadMemberUpdate dispatch) {
        return Mono.<ThreadMemberData>empty().log(name + ".onThreadMemberUpdate");
    }

    @Override
    public Mono<List<ThreadMemberData>> onThreadMembersUpdate(int shardIndex, ThreadMembersUpdate dispatch) {
        return Mono.<List<ThreadMemberData>>empty().log(name + ".onThreadMembersUpdate");
    }
}
