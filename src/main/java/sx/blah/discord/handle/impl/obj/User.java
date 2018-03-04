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

import java.awt.Color;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * The default implementation of {@link IUser}.
 */
public class User implements IUser {

	/**
	 * The unique snowflake ID of the user.
	 */
	protected final long id;

	/**
	 * The client the user belongs to.
	 */
	protected final IDiscordClient client;

	/**
	 * The shard the user belongs to.
	 */
	protected final IShard shard;

	/**
	 * The user's name.
	 */
	protected volatile String name;

	/**
	 * The user's avatar hash.
	 */
	protected volatile String avatar;

	/**
	 * The user's discriminator.
	 */
	protected volatile String discriminator;

	/**
	 * Whether the user is a bot.
	 */
	protected volatile boolean isBot;

	/**
	 * The user's presence.
	 */
	protected volatile IPresence presence;
	/**
	 * The user's avatar URL.
	 */
	protected volatile String avatarURL;

	/**
	 * The roles the user has in each guild.
	 */
	public final Cache<RolesHolder> roles;

	/**
	 * The nickname the user has in each guild.
	 */
	public final Cache<NickHolder> nicks;

	/**
	 * The voice state the user has in each guild.
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
	 * Sets the CACHED name of the user.
	 *
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAvatar() {
		return avatar;
	}

	/**
	 * Sets the CACHED avatar of the user.
	 *
	 * @param avatar The avatar.
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
	 * @param presence The presence.
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
	 * Sets the CACHED discriminator of the user.
	 *
	 * @param discriminator The discriminator.
	 */
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	@Override
	public List<IRole> getRolesForGuild(IGuild guild) {
		if (roles != null) {
			RolesHolder retrievedRoles = roles.get(guild.getLongID());
			if (retrievedRoles != null && retrievedRoles.getObject() != null)
				return retrievedRoles.getObject().stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedList::new));
		}

		return new LinkedList<>();
	}

	@Override
	public Color getColorForGuild(IGuild guild) {
		return getRolesForGuild(guild).stream()
				.filter(r -> r.getColor().getRGB() != 0)
				.max(Comparator.comparing(IRole::getPosition))
				.map(IRole::getColor)
				.orElse(new Color(0, true));
	}

	@Override
	public EnumSet<Permissions> getPermissionsForGuild(IGuild guild) {
		if (guild.getOwner().equals(this)) {
			return EnumSet.allOf(Permissions.class);
		}

		IRole everyone = guild.getEveryoneRole();
		EnumSet<Permissions> permissions = everyone.getPermissions();

		for (IRole role : guild.getRolesForUser(this)) {
			permissions.addAll(role.getPermissions());
		}

		if (permissions.contains(Permissions.ADMINISTRATOR)) {
			return EnumSet.allOf(Permissions.class);
		}

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
	public LongMap<IVoiceState> getVoiceStates() {
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
	 * Sets the CACHED nickname of the user in a guild.
	 *
	 * @param guildID The unique snowflake ID of the guild to cache the nickname for.
	 * @param nick The nickname.
	 */
	public void addNick(long guildID, String nick) {
		nicks.put(new NickHolder(guildID, nick));
	}

	/**
	 * Adds a CACHEDED role of the user in a guild.
	 *
	 * @param guildID The unique snowflake ID of the guild to cache the role for.
	 * @param role The role.
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
	public boolean hasRole(IRole role) {
		return getRolesForGuild(role.getGuild()).contains(role);
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

/**
 * Associates a guild ID to the roles a user has in the guild.
 */
class RolesHolder extends IDLinkedObjectWrapper<Collection<IRole>> {

	RolesHolder(long id, Collection<IRole> roles) {
		super(id, roles);
	}
}

/**
 * Associates a guild ID to the nickname a user has in the guild.
 */
class NickHolder extends IDLinkedObjectWrapper<String> {

	NickHolder(long id, String string) {
		super(id, string);
	}
}
