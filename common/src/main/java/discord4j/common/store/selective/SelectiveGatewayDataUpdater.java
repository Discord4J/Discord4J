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

import discord4j.common.store.api.layout.GatewayDataUpdater;
import discord4j.common.store.api.object.InvalidationCause;
import discord4j.common.store.api.object.PresenceAndUserData;
import discord4j.discordjson.json.*;
import discord4j.discordjson.json.gateway.*;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.Set;

/**
 * {@link GatewayDataUpdater} that no-ops certain caches based on the passed in {@link StoreFlag}s
 */
public class SelectiveGatewayDataUpdater implements GatewayDataUpdater {

	private final EnumSet<StoreFlag> enabledStoreFlags;
	private final GatewayDataUpdater delegate;

	public SelectiveGatewayDataUpdater(EnumSet<StoreFlag> enabledStoreFlags, GatewayDataUpdater delegate) {
		this.enabledStoreFlags = enabledStoreFlags;
		this.delegate = delegate;
	}


	@Override
	public Mono<Void> onShardInvalidation(int shardIndex, InvalidationCause cause) {
		return delegate.onShardInvalidation(shardIndex, cause);
	}

	@Override
	public Mono<Void> onReady(Ready dispatch) {
		return delegate.onReady(dispatch);
	}


	@Override
	public Mono<Void> onChannelCreate(int shardIndex, ChannelCreate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.onChannelCreate(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<ChannelData> onChannelDelete(int shardIndex, ChannelDelete dispatch) {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.onChannelDelete(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<ChannelData> onChannelUpdate(int shardIndex, ChannelUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.CHANNEL)
				? delegate.onChannelUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<Set<EmojiData>> onGuildEmojisUpdate(int shardIndex, GuildEmojisUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.EMOJI)
				? delegate.onGuildEmojisUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<Void> onGuildCreate(int shardIndex, GuildCreate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.GUILD)
				? delegate.onGuildCreate(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<GuildData> onGuildDelete(int shardIndex, GuildDelete dispatch) {
		return enabledStoreFlags.contains(StoreFlag.GUILD)
				? delegate.onGuildDelete(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<GuildData> onGuildUpdate(int shardIndex, GuildUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.GUILD)
				? delegate.onGuildUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<Void> onGuildMemberAdd(int shardIndex, GuildMemberAdd dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.onGuildMemberAdd(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<MemberData> onGuildMemberRemove(int shardIndex, GuildMemberRemove dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.onGuildMemberRemove(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Void> onGuildMembersChunk(int shardIndex, GuildMembersChunk dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.onGuildMembersChunk(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<MemberData> onGuildMemberUpdate(int shardIndex, GuildMemberUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.onGuildMemberUpdate(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Void> onGuildMembersCompletion(long guildId) {
		return enabledStoreFlags.contains(StoreFlag.MEMBER)
				? delegate.onGuildMembersCompletion(guildId)
				: Mono.empty();
	}


	@Override
	public Mono<Void> onMessageCreate(int shardIndex, MessageCreate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageCreate(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<MessageData> onMessageDelete(int shardIndex, MessageDelete dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageDelete(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Set<MessageData>> onMessageDeleteBulk(int shardIndex, MessageDeleteBulk dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageDeleteBulk(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Void> onMessageReactionAdd(int shardIndex, MessageReactionAdd dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageReactionAdd(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Void> onMessageReactionRemove(int shardIndex, MessageReactionRemove dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageReactionRemove(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Void> onMessageReactionRemoveAll(int shardIndex, MessageReactionRemoveAll dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageReactionRemoveAll(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<Void> onMessageReactionRemoveEmoji(int shardIndex, MessageReactionRemoveEmoji dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageReactionRemoveEmoji(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<MessageData> onMessageUpdate(int shardIndex, MessageUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.MESSAGE)
				? delegate.onMessageUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<PresenceAndUserData> onPresenceUpdate(int shardIndex, PresenceUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.PRESENCE)
				? delegate.onPresenceUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<Void> onGuildRoleCreate(int shardIndex, GuildRoleCreate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.onGuildRoleCreate(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<RoleData> onGuildRoleDelete(int shardIndex, GuildRoleDelete dispatch) {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.onGuildRoleDelete(shardIndex, dispatch)
				: Mono.empty();
	}

	@Override
	public Mono<RoleData> onGuildRoleUpdate(int shardIndex, GuildRoleUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.ROLE)
				? delegate.onGuildRoleUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<UserData> onUserUpdate(int shardIndex, UserUpdate dispatch) {
		return enabledStoreFlags.contains(StoreFlag.USER)
				? delegate.onUserUpdate(shardIndex, dispatch)
				: Mono.empty();
	}


	@Override
	public Mono<VoiceStateData> onVoiceStateUpdateDispatch(int shardIndex, VoiceStateUpdateDispatch dispatch) {
		return enabledStoreFlags.contains(StoreFlag.VOICE_STATE)
				? delegate.onVoiceStateUpdateDispatch(shardIndex, dispatch)
				: Mono.empty();
	}

}
