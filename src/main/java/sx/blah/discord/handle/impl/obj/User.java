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
import sx.blah.discord.util.LogMarkers;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class User implements IUser {

	/**
	 * User ID.
	 */
	protected final String id;

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
	protected final Map<String, List<IRole>> roles = new ConcurrentHashMap<>();

	/**
	 * The nicknames this user has. (Key = guild id).
	 */
	protected final Map<String, String> nicks = new ConcurrentHashMap<>();

	/**
	 * The voice states this user has.
	 */
	protected final Map<String, IVoiceState> voiceStates = new ConcurrentHashMap<>();

	public User(IShard shard, String name, String id, String discriminator, String avatar, IPresence presence, boolean isBot) {
		this.shard = shard;
		this.client = shard.getClient();
		this.id = id;
		this.name = name;
		this.discriminator = discriminator;
		this.avatar = avatar;
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar,
				(this.avatar != null && this.avatar.startsWith("a_")) ? "gif" : "webp");
		this.presence = presence;
		this.isBot = isBot;
	}

	@Override
	public String getID() {
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
		this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar,
				(this.avatar != null && this.avatar.startsWith("a_")) ? "gif" : "webp");
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
		return roles.getOrDefault(guild.getID(), new ArrayList<>());
	}

	@Override
	public EnumSet<Permissions> getPermissionsForGuild(IGuild guild){
		EnumSet<Permissions> permissions = EnumSet.noneOf(Permissions.class);
		getRolesForGuild(guild).forEach(role -> permissions.addAll(role.getPermissions()));
		return permissions;
	}

	@Override
	public String getNicknameForGuild(IGuild guild) {
		return nicks.get(guild.getID());
	}

	@Override
	public IVoiceState getVoiceStateForGuild(IGuild guild) {
		return voiceStates.computeIfAbsent(guild.getID(), (String key) -> new VoiceState(guild, this));
	}

	@Override
	public Map<String, IVoiceState> getVoiceStates() {
		return voiceStates;
	}

	@Override
	public void moveToVoiceChannel(IVoiceChannel channel) {
		IVoiceChannel oldChannel = getVoiceStateForGuild(channel.getGuild()).getChannel();

		if (oldChannel == null)
			throw new DiscordException("User must already be in a voice channel before they can be moved to another.");

		// client must have permission to both move members and connect to the channel.
		DiscordUtils.checkPermissions(client.getOurUser(), channel, EnumSet.of(Permissions.VOICE_MOVE_MEMBERS, Permissions.VOICE_CONNECT));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS + channel.getGuild().getID() + "/members/" + id,
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().channel(channel.getID()).build()));
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
	public void addNick(String guildID, String nick) {
		if (nick == null) {
			if (nicks.containsKey(guildID))
				nicks.remove(guildID);
		} else {
			nicks.put(guildID, nick);
		}
	}

	/**
	 * CACHES a role to the user.
	 *
	 * @param guildID The guild the role is for.
	 * @param role    The role.
	 */
	public void addRole(String guildID, IRole role) {
		roles.computeIfAbsent(guildID, (String id) -> new ArrayList<>()).add(role);
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
	public IPrivateChannel getOrCreatePMChannel() throws DiscordException, RateLimitException {
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
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IUser) other).getID().equals(getID());
	}

	@Override
	public void addRole(IRole role) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, role.getGuild(), Collections.singletonList(role), EnumSet.of(Permissions.MANAGE_ROLES));
		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.GUILDS+role.getGuild().getID()+"/members/"+id+"/roles/"+role.getID());
	}

	@Override
	public void removeRole(IRole role) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, role.getGuild(), Collections.singletonList(role), EnumSet.of(Permissions.MANAGE_ROLES));
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+role.getGuild().getID()+"/members/"+id+"/roles/"+role.getID());
	}
}
