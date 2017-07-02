/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package sx.blah.discord.handle.impl.obj;

import com.fasterxml.jackson.core.JsonProcessingException;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.requests.MemberEditRequest;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.IDLinkedObjectWrapper;
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class User implements IUser {

	/**
	 * User ID.
	 */
	protected final long id;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	/**
	 * The shard this object belongs to.
	 */
	protected final IShard shard;

	/**
	 * Display name of the user.
	 */
	protected volatile String name;

	/**
	 * The user's avatar location.
	 */
	protected volatile String avatar;

	/**
	 * User discriminator.
	 * Distinguishes users with the same name.
	 */
	protected volatile String discriminator;

	/**
	 * Whether this user is a bot or not.
	 */
	protected volatile boolean isBot;

	/**
	 * This user's presence.
	 */
	protected volatile IPresence presence;
	/**
	 * The user's avatar in URL form.
	 */
	protected volatile String avatarURL;

	/**
	 * The roles the user is a part of. (Key = guild id).
	 */
	public final Cache<RolesHolder> roles;

	/**
	 * The nicknames this user has. (Key = guild id).
	 */
	public final Cache<NickHolder> nicks;

	/**
	 * The voice states this user has.
	 */
	public final Cache<IVoiceState> voiceStates;

	public User(IShard shard, String name, long id, String discriminator, String avatar, IPresence presence, boolean isBot) {
		this(shard, shard == null ? null : shard.getClient(), name, id, discriminator, avatar, presence, isBot);
	}

	public User(IShard shard, IDiscordClient client, String name, long id, String discriminator, String avatar, IPresence presence, boolean isBot) {
		this.shard = shard;
		this.client = client;
		this.id = id;
		this.name = name;
		this.discriminator = discriminator;
		setAvatar(avatar);
		this.presence = presence;
		this.isBot = isBot;
		this.roles = new Cache<>((DiscordClientImpl) client, RolesHolder.class);
		this.nicks = new Cache<>((DiscordClientImpl) client, NickHolder.class);
		this.voiceStates = new Cache<>((DiscordClientImpl) client, IVoiceState.class);
	}

	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the user's CACHED username.
	 *
	 * @param name The username.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Deprecated
	public Status getStatus() {
		return null;
	}

	@Override
	public String getAvatar() {
		return avatar;
	}

	/**
	 * Sets the user's CACHED avatar id.
	 *
	 * @param avatar The user's avatar id.
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
		this.avatarURL = this.avatar == null ?
				String.format(DiscordEndpoints.DEFAULT_AVATAR, Integer.parseInt(discriminator) % 5)
				: String.format(DiscordEndpoints.AVATARS, this.id, this.avatar, (this.avatar.startsWith("a_")) ? "gif" : "webp");
	}

	@Override
	public String getAvatarURL() {
		return avatarURL;
	}

	@Override
	public IPresence getPresence() {
		return presence;
	}

	/**
	 * Sets the CACHED presence of the user.
	 *
	 * @param presence The new presence.
	 */
	public void setPresence(IPresence presence) {
		this.presence = presence;
	}

	@Override
	public String getDisplayName(IGuild guild) {
		if (guild == null || getNicknameForGuild(guild) == null)
			return getName();

		return getNicknameForGuild(guild);
	}

	@Override
	public String mention() {
		return mention(true);
	}

	@Override
	public String mention(boolean mentionWithNickname) {
		return "<@" + (mentionWithNickname ? "!" : "") + id + ">";
	}

	@Override
	public String getDiscriminator() {
		return discriminator;
	}

	/**
	 * Sets the CACHED discriminator for the user.
	 *
	 * @param discriminator The user's new discriminator.
	 */
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	@Override
	public List<IRole> getRolesForGuild(IGuild guild) {
		if (roles != null) {
			RolesHolder retrievedRoles = roles.get(guild.getLongID());
			if (retrievedRoles != null && retrievedRoles.getObject() != null)
				return new LinkedList<>(retrievedRoles.getObject());
		}

		return new LinkedList<>();
	}

	@Override
	public EnumSet<Permissions> getPermissionsForGuild(IGuild guild) {
		if (guild.getOwner().equals(this))
			return EnumSet.allOf(Permissions.class);
		EnumSet<Permissions> permissions = EnumSet.noneOf(Permissions.class);
		getRolesForGuild(guild).forEach(role -> permissions.addAll(role.getPermissions()));
		return permissions;
	}

	@Override
	public String getNicknameForGuild(IGuild guild) {
		NickHolder holder = nicks.get(guild.getLongID());
		return holder == null ? null : holder.getObject();
	}

	@Override
	public IVoiceState getVoiceStateForGuild(IGuild guild) {
		voiceStates.putIfAbsent(guild.getLongID(), () -> new VoiceState(guild, this));
		return voiceStates.get(guild.getLongID());
	}

	@Override
	public LongMap<IVoiceState> getVoiceStatesLong() {
		return voiceStates.mapCopy();
	}

	@Override
	public void moveToVoiceChannel(IVoiceChannel channel) {
		IVoiceChannel oldChannel = getVoiceStateForGuild(channel.getGuild()).getChannel();

		if (oldChannel == null)
			throw new DiscordException("User must already be in a voice channel before they can be moved to another.");

		// client must have permission to both move members and connect to the channel.
		PermissionUtils.requirePermissions(channel, client.getOurUser(), Permissions.VOICE_MOVE_MEMBERS, Permissions.VOICE_CONNECT);

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS + channel.getGuild().getStringID() + "/members/" + id,
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().channel(channel.getStringID()).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}

	}

	/**
	 * CACHES a nickname to the user.
	 *
	 * @param guildID The guild the nickname is for.
	 * @param nick    The nickname, or null to remove it.
	 */
	public void addNick(long guildID, String nick) {
		nicks.put(new NickHolder(guildID, nick));
	}

	/**
	 * CACHES a role to the user.
	 *
	 * @param guildID The guild the role is for.
	 * @param role    The role.
	 */
	public void addRole(long guildID, IRole role) {
		roles.putIfAbsent(guildID, () -> new RolesHolder(guildID, new CopyOnWriteArraySet<>()));
		roles.get(guildID).getObject().add(role);
	}

	@Override
	public void addRole(IRole role) {
		PermissionUtils.requireHierarchicalPermissions(role.getGuild(), client.getOurUser(), Collections.singletonList(role), Permissions.MANAGE_ROLES);
		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.GUILDS + role.getGuild().getStringID() + "/members/" + id + "/roles/" + role.getStringID());
	}

	@Override
	public void removeRole(IRole role) {
		PermissionUtils.requireHierarchicalPermissions(role.getGuild(), client.getOurUser(), Collections.singletonList(role), Permissions.MANAGE_ROLES);
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS + role.getGuild().getStringID() + "/members/" + id + "/roles/" + role.getStringID());
	}

	@Override
	public IUser copy() {
		User newUser = new User(shard, name, id, discriminator, avatar, presence, isBot);
		newUser.voiceStates.putAll(voiceStates);
		newUser.setPresence(presence.copy());
		newUser.nicks.putAll(nicks);
		newUser.roles.putAll(roles);
		return newUser;
	}

	@Override
	public boolean isBot() {
		return isBot;
	}

	@Override
	public IPrivateChannel getOrCreatePMChannel() {
		return client.getOrCreatePMChannel(this);
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IShard getShard() {
		return shard;
	}

	@Override
	public String toString() {
		return mention();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		return DiscordUtils.equals(this, other);
	}
}

class RolesHolder extends IDLinkedObjectWrapper<Collection<IRole>> {

	RolesHolder(long id, Collection<IRole> roles) {
		super(id, roles);
	}
}

class NickHolder extends IDLinkedObjectWrapper<String> {

	NickHolder(long id, String string) {
		super(id, string);
	}
}
