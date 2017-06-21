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
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.requests.ChannelCreateRequest;
import sx.blah.discord.api.internal.json.requests.GuildEditRequest;
import sx.blah.discord.api.internal.json.requests.MemberEditRequest;
import sx.blah.discord.api.internal.json.requests.ReorderRolesRequest;
import sx.blah.discord.api.internal.json.responses.PruneResponse;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.events.WebhookCreateEvent;
import sx.blah.discord.handle.impl.events.WebhookDeleteEvent;
import sx.blah.discord.handle.impl.events.WebhookUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.cache.Cache;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Guild implements IGuild {
	/**
	 * All text channels in the guild.
	 */
	public final Cache<IChannel> channels;

	/**
	 * All voice channels in the guild.
	 */
	public final Cache<IVoiceChannel> voiceChannels;

	/**
	 * All users connected to the guild.
	 */
	public final Cache<IUser> users;

	/**
	 * The joined timetamps for users.
	 */
	public final Cache<TimeStampHolder> joinTimes;

	/**
	 * The name of the guild.
	 */
	protected volatile String name;

	/**
	 * The ID of this guild.
	 */
	protected final long id;

	/**
	 * The location of the guild icon
	 */
	protected volatile String icon;

	/**
	 * The url pointing to the guild icon
	 */
	protected volatile String iconURL;

	/**
	 * The user id for the owner of the guild
	 */
	protected volatile long ownerID;

	/**
	 * The roles the guild contains.
	 */
	public final Cache<IRole> roles;

	/**
	 * The channel where those who are afk are moved to.
	 */
	protected volatile long afkChannel;
	/**
	 * The time in seconds for a user to be idle to be determined as "afk".
	 */
	protected volatile int afkTimeout;

	/**
	 * The region this guild is located in.
	 */
	protected volatile String regionID;

	/**
	 * The verification level of this guild
	 */
	protected volatile VerificationLevel verification;

	/**
	 * This guild's audio manager.
	 */
	protected volatile AudioManager audioManager;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	/**
	 * The shard this object belongs to.
	 */
	private final IShard shard;

	/**
	 * The list of emojis.
	 */
	public final Cache<IEmoji> emojis;

	/**
	 * The total number of members in this guild
	 */
	private int totalMemberCount;

	/**
	 * The ID of the voice channel that the bot is connecting to in this guild.
	 * This is 0 if a voice connection has already been established in this guild or none was ever attempted.
	 */
	public long connectingVoiceChannelID;

	public Guild(IShard shard, String name, long id, String icon, long ownerID, long afkChannel, int afkTimeout, String region, int verification) {
		this(shard, name, id, icon, ownerID, afkChannel, afkTimeout, region, verification,
				new Cache<>((DiscordClientImpl) shard.getClient(), IRole.class), new Cache<>((DiscordClientImpl) shard.getClient(), IChannel.class),
				new Cache<>((DiscordClientImpl) shard.getClient(), IVoiceChannel.class), new Cache<>((DiscordClientImpl) shard.getClient(), IUser.class),
				new Cache<>((DiscordClientImpl) shard.getClient(), TimeStampHolder.class));
	}

	public Guild(IShard shard, String name, long id, String icon, long ownerID, long afkChannel, int afkTimeout,
				 String region, int verification, Cache<IRole> roles, Cache<IChannel> channels,
				 Cache<IVoiceChannel> voiceChannels, Cache<IUser> users, Cache<TimeStampHolder> joinTimes) {
		this.shard = shard;
		this.client = shard.getClient();
		this.name = name;
		this.voiceChannels = voiceChannels;
		this.channels = channels;
		this.users = users;
		this.id = id;
		this.icon = icon;
		this.joinTimes = joinTimes;
		this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
		this.ownerID = ownerID;
		this.roles = roles;
		this.afkChannel = afkChannel;
		this.afkTimeout = afkTimeout;
		this.regionID = region;
		this.verification = VerificationLevel.get(verification);
		this.audioManager = new AudioManager(this);
		this.emojis = new Cache<>((DiscordClientImpl) client, IEmoji.class);
	}

	@Override
	public long getOwnerLongID() {
		return ownerID;
	}

	@Override
	public IUser getOwner() {
		return client.getUserByID(ownerID);
	}

	/**
	 * Sets the CACHED owner id.
	 *
	 * @param id The user if of the new owner.
	 */
	public void setOwnerID(long id) {
		ownerID = id;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	@Override
	public String getIconURL() {
		return iconURL;
	}

	/**
	 * Sets the CACHED icon id for the guild.
	 *
	 * @param icon The icon id.
	 */
	public void setIcon(String icon) {
		this.icon = icon;
		this.iconURL = String.format(DiscordEndpoints.ICONS, getStringID(), this.icon);
	}

	@Override
	public List<IChannel> getChannels() {
		LinkedList<IChannel> list = new LinkedList<>(channels.values());
		list.sort((c1, c2) -> {
			int originalPos1 = ((Channel) c1).position;
			int originalPos2 = ((Channel) c2).position;
			if (originalPos1 == originalPos2) {
				return c2.getCreationDate().compareTo(c1.getCreationDate());
			} else {
				return originalPos1 - originalPos2;
			}
		});
		return list;
	}

	@Override
	public IChannel getChannelByID(long id) {
		return channels.get(id);
	}

	@Override
	public List<IUser> getUsers() {
		return new LinkedList<>(users.values());
	}

	@Override
	public IUser getUserByID(long id) {
		if (users == null)
			return null;

		IUser user = users.get(id);

		if (user == null) {
			if (client.getOurUser() != null && id == client.getOurUser().getLongID())
				user = client.getOurUser();
			else if (id == ownerID)
				user = getOwner();
		}

		return user;
	}

	@Override
	public List<IChannel> getChannelsByName(String name) {
		return channels.stream()
				.filter(channel -> channel.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getVoiceChannelsByName(String name) {
		return voiceChannels.stream()
				.filter(channel -> channel.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IUser> getUsersByName(String name) {
		return getUsersByName(name, true);
	}

	@Override
	public List<IUser> getUsersByName(String name, boolean includeNicknames) {
		return users.stream()
				.filter(u -> includeNicknames ? u.getDisplayName(this).equals(name) : u.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IUser> getUsersByRole(IRole role) {
		return users.stream()
				.filter(user -> user.getRolesForGuild(this).contains(role))
				.collect(Collectors.toList());
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the CACHED name of the guild.
	 *
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getLongID() {
		return id;
	}

	@Override
	public List<IRole> getRoles() {
		LinkedList<IRole> list = new LinkedList<>(roles.values());
		list.sort((r1, r2) -> {
			int originalPos1 = ((Role) r1).position;
			int originalPos2 = ((Role) r2).position;
			if (originalPos1 == originalPos2) {
				return r2.getCreationDate().compareTo(r1.getCreationDate());
			} else {
				return originalPos1 - originalPos2;
			}
		});
		return list;
	}

	@Override
	public List<IRole> getRolesForUser(IUser user) {
		return user.getRolesForGuild(this);
	}

	@Override
	public IRole getRoleByID(long id) {
		return roles.get(id);
	}

	@Override
	public List<IRole> getRolesByName(String name) {
		return roles.stream()
				.filter(role -> role.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		LinkedList<IVoiceChannel> list = new LinkedList<>(voiceChannels.values());
		list.sort((c1, c2) -> {
			int originalPos1 = ((Channel) c1).position;
			int originalPos2 = ((Channel) c2).position;
			if (originalPos1 == originalPos2) {
				return c2.getCreationDate().compareTo(c1.getCreationDate());
			} else {
				return originalPos1 - originalPos2;
			}
		});
		return list;
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(long id) {
		return voiceChannels.get(id);
	}

	@Override
	public IVoiceChannel getAFKChannel() {
		if (afkChannel == 0)
			return null;

		return getVoiceChannelByID(afkChannel);
	}

	@Override
	public IVoiceChannel getConnectedVoiceChannel() {
		return client.getConnectedVoiceChannels().stream()
				.filter(voiceChannels::containsValue)
				.findFirst().orElse(null);
	}

	@Override
	public int getAFKTimeout() {
		return afkTimeout;
	}

	public void setAFKChannel(long id) {
		this.afkChannel = id;
	}

	public void setAfkTimeout(int timeout) {
		this.afkTimeout = timeout;
	}

	@Override
	public IRole createRole() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_ROLES);

		RoleObject response = ((DiscordClientImpl) client).REQUESTS.POST
				.makeRequest(DiscordEndpoints.GUILDS + getStringID() + "/roles", RoleObject.class);
		return DiscordUtils.getRoleFromJSON(this, response);
	}

	@Override
	public List<IUser> getBannedUsers() {
		return getBans().stream().map(Ban::getUser).collect(Collectors.toList());
	}

	@Override
	public List<Ban> getBans() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.BAN);

		BanObject[] bans = ((DiscordClientImpl) client).REQUESTS.GET
				.makeRequest(DiscordEndpoints.GUILDS + getStringID() + "/bans", BanObject[].class);

		return Arrays.stream(bans)
				.map(b -> new Ban(this, DiscordUtils.getUserFromJSON(getShard(), b.user), b.reason))
				.collect(Collectors.toList());
	}

	@Override
	public void banUser(IUser user) {
		banUser(user, null, 0);
	}

	@Override
	public void banUser(IUser user, int deleteMessagesForDays) {
		banUser(user, null, deleteMessagesForDays);
	}

	@Override
	public void banUser(IUser user, String reason) {
		banUser(user, reason, 0);
	}

	@Override
	public void banUser(IUser user, String reason, int deleteMessagesForDays) {
		banUser(user.getLongID(), reason, deleteMessagesForDays);
	}

	@Override
	public void banUser(long userID) {
		banUser(userID, null, 0);
	}

	@Override
	public void banUser(long userID, int deleteMessagesForDays) {
		banUser(userID, null, deleteMessagesForDays);
	}

	@Override
	public void banUser(long userID, String reason) {
		banUser(userID, reason, 0);
	}

	@Override
	public void banUser(long userID, String reason, int deleteMessagesForDays) {
		IUser user = getUserByID(userID);
		if (getUserByID(userID) == null) {
			PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.BAN);
		} else {
			PermissionUtils.requireHierarchicalPermissions(this, client.getOurUser(), getRolesForUser(user), Permissions.BAN);
		}
		if (reason != null && reason.length() > Ban.MAX_REASON_LENGTH) {
			throw new IllegalArgumentException("Reason length cannot be more than " + Ban.MAX_REASON_LENGTH);
		}
		try {
			((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.GUILDS + getStringID() + "/bans/" + Long.toUnsignedString(userID) + "?delete-message-days=" + deleteMessagesForDays + (reason == null ? "" : ("&reason=" +
					URLEncoder.encode(reason, "UTF-8"))));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pardonUser(long userID) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.BAN);
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS + getStringID() + "/bans/" + Long.toUnsignedString(userID));
	}

	@Override
	public void kickUser(IUser user) {
		kickUser(user, null);
	}

	@Override
	public void kickUser(IUser user, String reason) {
		PermissionUtils.requireHierarchicalPermissions(this, client.getOurUser(), getRolesForUser(user), Permissions.KICK);
		if (reason != null && reason.length() > Ban.MAX_REASON_LENGTH) {
			throw new IllegalArgumentException("Reason length cannot be more than " + Ban.MAX_REASON_LENGTH);
		}
		try {
			((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+getStringID()+"/members/"+user.getStringID() + (reason == null ? "" : ("?reason=" + URLEncoder.encode(reason, "UTF-8"))));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void editUserRoles(IUser user, IRole[] roles) {
		PermissionUtils.requireHierarchicalPermissions(this, client.getOurUser(), Arrays.asList(roles), Permissions.MANAGE_ROLES);

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+getStringID()+"/members/"+user.getStringID(),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().roles(roles).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}

	}

	@Override
	public void setDeafenUser(IUser user, boolean deafen) {
		PermissionUtils.requireHierarchicalPermissions(this, client.getOurUser(), getRolesForUser(user), Permissions.VOICE_DEAFEN_MEMBERS);

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+getStringID()+"/members/"+user.getStringID(),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().deafen(deafen).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setMuteUser(IUser user, boolean mute) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.VOICE_MUTE_MEMBERS);

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+getStringID()+"/members/"+user.getStringID(),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().mute(mute).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setUserNickname(IUser user, String nick) {
		boolean isSelf = user.equals(client.getOurUser());
		if (isSelf) {
			PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.CHANGE_NICKNAME);
		} else {
			PermissionUtils.requireHierarchicalPermissions(this, client.getOurUser(), getRolesForUser(user), Permissions.MANAGE_NICKNAMES);
		}

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+getStringID()+"/members/"+(isSelf ? "@me/nick" : user.getStringID()),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().nick(nick == null ? "" : nick).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void edit(String name, IRegion region, VerificationLevel level, Image icon, IVoiceChannel afkChannel, int afkTimeout) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_SERVER);

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new IllegalArgumentException("Guild name must be between 2 and 100 characters!");
		if (region == null)
			throw new IllegalArgumentException("Region must not be null.");
		if (level == null)
			throw new IllegalArgumentException("Verification level must not be null.");
		if (icon == null)
			throw new IllegalArgumentException("Icon must not be null.");
		if (afkChannel != null && !getVoiceChannels().contains(afkChannel))
			throw new IllegalArgumentException("Invalid AFK voice channel.");
		if (afkTimeout != 60 && afkTimeout != 300 && afkTimeout != 900 && afkTimeout != 1800 && afkTimeout != 3600)
			throw new IllegalArgumentException("AFK timeout must be one of (60, 300, 900, 1800, 3600).");

		((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.GUILDS + getStringID(), new GuildEditRequest(name, region.getID(), level.ordinal(), icon.getData(),
						afkChannel == null ? null : afkChannel.getStringID(), afkTimeout));
	}

	@Override
	public void changeName(String name) {
		edit(name, getRegion(), getVerificationLevel(), this::getIcon, getAFKChannel(), getAFKTimeout());
	}

	@Override
	public void changeRegion(IRegion region) {
		edit(getName(), region, getVerificationLevel(), this::getIcon, getAFKChannel(), getAFKTimeout());
	}

	@Override
	public void changeVerificationLevel(VerificationLevel verificationLevel) {
		edit(getName(), getRegion(), verificationLevel, this::getIcon, getAFKChannel(), getAFKTimeout());
	}

	@Override
	public void changeIcon(Image icon) {
		edit(getName(), getRegion(), getVerificationLevel(), icon, getAFKChannel(), getAFKTimeout());
	}

	@Override
	public void changeAFKChannel(IVoiceChannel afkChannel) {
		edit(getName(), getRegion(), getVerificationLevel(), this::getIcon, afkChannel, getAFKTimeout());
	}

	@Override
	public void changeAFKTimeout(int timeout) {
		edit(getName(), getRegion(), getVerificationLevel(), this::getIcon, getAFKChannel(), timeout);
	}

	@Override
	@Deprecated
	public void deleteGuild() {
		if (ownerID != client.getOurUser().getLongID())
			throw new MissingPermissionsException("You must be the guild owner to delete guilds!", EnumSet.noneOf(Permissions.class));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+getStringID());
	}

	@Override
	@Deprecated
	public void leaveGuild() {
		if (ownerID == client.getOurUser().getLongID())
			throw new DiscordException("Guild owners cannot leave their own guilds! Use deleteGuild() instead.");

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.USERS+"@me/guilds/"+getStringID());
	}

	@Override
	public void leave() {
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.USERS+"@me/guilds/"+getStringID());
	}

	@Override
	public IChannel createChannel(String name) {
		shard.checkReady("create channel");
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNELS);

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		ChannelObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS+getStringID()+"/channels",
				new ChannelCreateRequest(name, "text"),
				ChannelObject.class);

		IChannel channel = DiscordUtils.getChannelFromJSON(this, response);
		channels.put(channel);

		return channel;
	}

	@Override
	public IVoiceChannel createVoiceChannel(String name) {
		getShard().checkReady("create voice channel");
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNELS);

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		ChannelObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS+getStringID()+"/channels",
				new ChannelCreateRequest(name, "voice"),
				ChannelObject.class);

		IVoiceChannel channel = DiscordUtils.getVoiceChannelFromJSON(this, response);
		channels.put(channel);

		return channel;
	}

	@Override
	public IRegion getRegion() {
		return client.getRegionByID(regionID);
	}

	/**
	 * CACHES the region for this guild.
	 *
	 * @param regionID The region.
	 */
	public void setRegion(String regionID) {
		this.regionID = regionID;
	}

	@Override
	public VerificationLevel getVerificationLevel() {
		return verification;
	}

	/**
	 * CACHES the verification for this guild.
	 *
	 * @param verification The verification level.
	 */
	public void setVerificationLevel(int verification) {
		this.verification = VerificationLevel.get(verification);
	}

	@Override
	public IRole getEveryoneRole() {
		return getRoleByID(this.id);
	}

	@Override
	public IChannel getGeneralChannel() {
		return getChannelByID(this.id);
	}

	@Override
	public List<IInvite> getInvites() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_SERVER);
		ExtendedInviteObject[] response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.GUILDS+ getStringID() + "/invites",
				ExtendedInviteObject[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public List<IExtendedInvite> getExtendedInvites() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_SERVER);
		ExtendedInviteObject[] response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.GUILDS+ getStringID() + "/invites",
				ExtendedInviteObject[].class);

		List<IExtendedInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getExtendedInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public void reorderRoles(IRole... rolesInOrder) {
		if (rolesInOrder.length != getRoles().size())
			throw new DiscordException("The number of roles to reorder does not equal the number of available roles!");

		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_ROLES);

		int usersHighest = getRolesForUser(client.getOurUser()).stream()
				.map(IRole::getPosition)
				.max(Comparator.comparing(Function.identity()))
				.orElse(-1);

		ReorderRolesRequest[] request = new ReorderRolesRequest[roles.size()];

		for (int i = 0; i < roles.size(); i++) {
			IRole role = rolesInOrder[i];

			int newPosition = role.getPosition();
			int oldPosition = getRoleByID(role.getLongID()).getPosition();
			if (newPosition != oldPosition && oldPosition >= usersHighest) { // If the position was changed and the user doesn't have permission to change it.
				throw new MissingPermissionsException("Cannot edit the position of a role higher than or equal to your own.", EnumSet.noneOf(Permissions.class));
			} else {
				request[i] = new ReorderRolesRequest(role.getStringID(), i);
			}
		}

		((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS + getStringID() + "/roles", request);
	}

	@Override
	public int getUsersToBePruned(int days) {
		PruneResponse response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.GUILDS + getStringID() + "/prune?days=" + days,
				PruneResponse.class);
		return response.pruned;
	}

	@Override
	public int pruneUsers(int days) {
		PruneResponse response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS + getStringID() + "/prune?days=" + days,
				PruneResponse.class);
		return response.pruned;
	}

	@Override
	public boolean isDeleted() {
		return getClient().getGuildByID(id) != this;
	}

	@Override
	public IAudioManager getAudioManager() {
		return audioManager;
	}

	@Override
	public LocalDateTime getJoinTimeForUser(IUser user) {
		if (!joinTimes.containsKey(user.getLongID()))
			throw new DiscordException("Cannot find user "+user.getDisplayName(this)+" in this guild!");

		return joinTimes.get(user.getLongID()).getObject();
	}

	@Override
	public IMessage getMessageByID(long id) {
		IMessage message =  channels.stream()
				.map(IChannel::getMessageHistory)
				.flatMap(List::stream)
				.filter(msg -> msg.getLongID() == id)
				.findAny().orElse(null);

		if (message == null) {
			Collection<IChannel> toCheck = channels.stream()
					.filter(it -> {
						EnumSet<Permissions> perms = it.getModifiedPermissions(client.getOurUser());
						return perms.contains(Permissions.READ_MESSAGE_HISTORY) && perms.contains(Permissions.READ_MESSAGES);
					})
					.collect(Collectors.toSet());
			for (IChannel channel : toCheck) {
				message = channel.getMessageByID(id);
				if (message != null)
					return message;
			}
		}

		return message;
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
	public IGuild copy() {
		return new Guild(shard, name, id, icon, ownerID, afkChannel, afkTimeout, regionID, verification.ordinal(),
				roles.copy(), channels.copy(), voiceChannels.copy(), users.copy(), joinTimes.copy());
	}

	@Override
	public List<IEmoji> getEmojis() {
		return new LinkedList<>(emojis.values());
	}

	@Override
	public IEmoji getEmojiByID(long id) {
		return emojis.get(id);
	}

	@Override
	public IEmoji getEmojiByName(String name) {
		return emojis.stream()
				.filter(emoji -> emoji.getName().equals(name))
				.findFirst().orElse(null);
	}

	@Override
	public IWebhook getWebhookByID(long id) {
		return channels.stream()
				.map(channel -> channel.getWebhookByID(id))
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		return channels.stream()
				.map(IChannel::getWebhooks)
				.flatMap(List::stream)
				.filter(hook -> hook.getDefaultName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IWebhook> getWebhooks() {
		return channels.stream()
				.map(IChannel::getWebhooks)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public void loadWebhooks() {
		try {
			PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_WEBHOOKS);
		} catch (MissingPermissionsException ignored) {
			return;
		}

		RequestBuffer.request(() -> {
			try {
				List<IWebhook> oldList = getWebhooks()
						.stream()
						.map(IWebhook::copy)
						.collect(Collectors.toCollection(CopyOnWriteArrayList::new));

				WebhookObject[] response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
						DiscordEndpoints.GUILDS + getStringID() + "/webhooks",
						WebhookObject[].class);

				if (response != null) {
					for (WebhookObject webhookObject : response) {
						Channel channel = (Channel) getChannelByID(Long.parseUnsignedLong(webhookObject.channel_id));
						long webhookId = Long.parseUnsignedLong(webhookObject.id);
						if (getWebhookByID(webhookId) == null) {
							IWebhook newWebhook = DiscordUtils.getWebhookFromJSON(channel, webhookObject);
							client.getDispatcher().dispatch(new WebhookCreateEvent(newWebhook));
							channel.webhooks.put(newWebhook);
						} else {
							IWebhook toUpdate = channel.getWebhookByID(webhookId);
							IWebhook oldWebhook = toUpdate.copy();
							toUpdate = DiscordUtils.getWebhookFromJSON(channel, webhookObject);
							if (!oldWebhook.getDefaultName().equals(toUpdate.getDefaultName()) || !String.valueOf(oldWebhook.getDefaultAvatar()).equals(String.valueOf(toUpdate.getDefaultAvatar())))
								client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, toUpdate));

							oldList.remove(oldWebhook);
						}
					}
				}

				oldList.forEach(webhook -> {
					((Channel) webhook.getChannel()).webhooks.remove(webhook);
					client.getDispatcher().dispatch(new WebhookDeleteEvent(webhook));
				});
			} catch (Exception e) {
				Discord4J.LOGGER.warn(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
			}
		});
	}


	@Override
	public int getTotalMemberCount() {
		return totalMemberCount;
	}

	public void setTotalMemberCount(int totalMemberCount){
		this.totalMemberCount = totalMemberCount;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		return DiscordUtils.equals(this, other);
	}

	public static class TimeStampHolder extends IDLinkedObjectWrapper<LocalDateTime> {

		public TimeStampHolder(long id, LocalDateTime obj) {
			super(id, obj);
		}
	}
}
