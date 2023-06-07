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

package discord4j.common.store.selective;

import discord4j.common.store.api.layout.DataAccessor;
import discord4j.discordjson.json.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

/**
 * {@link DataAccessor} that no-ops certain caches based on the passed in {@link StoreFlag}s
 */
public class SelectiveDataAccessor implements DataAccessor {

	private final EnumSet<StoreFlag> enabledStoreFlags;
	private final DataAccessor delegate;

	public SelectiveDataAccessor(EnumSet<StoreFlag> enabledStoreFlags, DataAccessor delegate) {
		this.enabledStoreFlags = enabledStoreFlags;
		this.delegate = delegate;
	}

	@Override
	public Mono<Long> countChannels() {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.countChannels()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countChannelsInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.countChannelsInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Flux<ChannelData> getChannels() {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.getChannels()
				: Flux.empty();
	}

	@Override
	public Flux<ChannelData> getChannelsInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.getChannelsInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Mono<ChannelData> getChannelById(long channelId) {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.getChannelById(channelId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countEmojis() {
		return enabledStoreFlags.contains(StoreFlag.EMOJI)
				? delegate.countEmojis()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countEmojisInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.EMOJI)
				? delegate.countEmojisInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Flux<EmojiData> getEmojis() {
		return enabledStoreFlags.contains(StoreFlag.EMOJI)
				? delegate.getEmojis()
				: Flux.empty();
	}

	@Override
	public Flux<EmojiData> getEmojisInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.EMOJI)
				? delegate.getEmojisInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Mono<EmojiData> getEmojiById(long guildId, long emojiId) {
		return enabledStoreFlags.contains(StoreFlag.EMOJI)
				? delegate.getEmojiById(guildId, emojiId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countGuilds() {
		return enabledStoreFlags.contains(StoreFlag.GUILD)
				? delegate.countGuilds()
				: Mono.just(0L);
	}

	@Override
	public Flux<GuildData> getGuilds() {
		return enabledStoreFlags.contains(StoreFlag.GUILD)
				? delegate.getGuilds()
				: Flux.empty();
	}

	@Override
	public Mono<GuildData> getGuildById(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.GUILD)
				? delegate.getGuildById(guildId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countMembers() {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.countMembers()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countMembersInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.countChannelsInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countExactMembersInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.countExactMembersInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Flux<MemberData> getMembers() {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.getMembers()
				: Flux.empty();
	}

	@Override
	public Flux<MemberData> getMembersInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.getMembersInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Flux<MemberData> getExactMembersInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.getExactMembersInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Mono<MemberData> getMemberById(long guildId, long userId) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.getMemberById(guildId, userId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countMessages() {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.countMessages()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countMessagesInChannel(long channelId) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.countMessagesInChannel(channelId)
				: Mono.just(0L);
	}

	@Override
	public Flux<MessageData> getMessages() {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.getMessages()
				: Flux.empty();
	}

	@Override
	public Flux<MessageData> getMessagesInChannel(long channelId) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.getMessagesInChannel(channelId)
				: Flux.empty();
	}

	@Override
	public Mono<MessageData> getMessageById(long channelId, long messageId) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.getMessageById(channelId, messageId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countPresences() {
		return enabledStoreFlags.contains(StoreFlag.PRESENCE)
				? delegate.countPresences()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countPresencesInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.PRESENCE)
				? delegate.countPresencesInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Flux<PresenceData> getPresences() {
		return enabledStoreFlags.contains(StoreFlag.PRESENCE)
				? delegate.getPresences()
				: Flux.empty();
	}

	@Override
	public Flux<PresenceData> getPresencesInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.PRESENCE)
				? delegate.getPresencesInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Mono<PresenceData> getPresenceById(long guildId, long userId) {
		return enabledStoreFlags.contains(StoreFlag.PRESENCE)
				? delegate.getPresenceById(guildId, userId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countRoles() {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.countRoles()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countRolesInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.countRolesInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Flux<RoleData> getRoles() {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.getRoles()
				: Flux.empty();
	}

	@Override
	public Flux<RoleData> getRolesInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.getRolesInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Mono<RoleData> getRoleById(long guildId, long roleId) {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.getRoleById(guildId, roleId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countUsers() {
		return enabledStoreFlags.contains(StoreFlag.USER)
				? delegate.countUsers()
				: Mono.just(0L);
	}

	@Override
	public Flux<UserData> getUsers() {
		return enabledStoreFlags.contains(StoreFlag.USER)
				? delegate.getUsers()
				: Flux.empty();
	}

	@Override
	public Mono<UserData> getUserById(long userId) {
		return enabledStoreFlags.contains(StoreFlag.USER)
				? delegate.getUserById(userId)
				: Mono.empty();
	}


	@Override
	public Mono<Long> countVoiceStates() {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.countVoiceStates()
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countVoiceStatesInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.countVoiceStatesInGuild(guildId)
				: Mono.just(0L);
	}

	@Override
	public Mono<Long> countVoiceStatesInChannel(long guildId, long channelId) {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.countVoiceStatesInChannel(guildId, channelId)
				: Mono.just(0L);
	}

	@Override
	public Flux<VoiceStateData> getVoiceStates() {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.getVoiceStates()
				: Flux.empty();
	}

	@Override
	public Flux<VoiceStateData> getVoiceStatesInChannel(long guildId, long channelId) {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.getVoiceStatesInChannel(guildId, channelId)
				: Flux.empty();
	}

	@Override
	public Flux<VoiceStateData> getVoiceStatesInGuild(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.getVoiceStatesInGuild(guildId)
				: Flux.empty();
	}

	@Override
	public Mono<VoiceStateData> getVoiceStateById(long guildId, long userId) {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.getVoiceStateById(guildId, userId)
				: Mono.empty();
	}
}

