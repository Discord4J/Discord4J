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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Guild implements IGuild {
	/**
	 * All text channels in the guild.
	 */
	protected final Cache<IChannel> channels;

	/**
	 * All voice channels in the guild.
	 */
	protected final Cache<IVoiceChannel> voiceChannels;

	/**
	 * All users connected to the guild.
	 */
	protected final Cache<IUser> users;

	/**
	 * The joined timetamps for users.
	 */
	protected final Cache<TimeStampHolder> joinTimes;

	/**
	 * The name of the guild.
	 */
	protected volatile String name;

	/**
	 * The ID of this guild.
	 */
	protected final String id;

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
	protected volatile String ownerID;

	/**
	 * The roles the guild contains.
	 */
	protected final Cache<IRole> roles;

	/**
	 * The channel where those who are afk are moved to.
	 */
	protected volatile String afkChannel;
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
	protected final Cache<IEmoji> emojis;

	/**
	 * The total number of members in this guild
	 */
	private int totalMemberCount;

	public Guild(IShard shard, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region, int verification) {
		this(shard, name, id, icon, ownerID, afkChannel, afkTimeout, region, verification,
				new Cache<>((DiscordClientImpl) shard.getClient(), IRole.class), new Cache<>((DiscordClientImpl) shard.getClient(), IChannel.class),
				new Cache<>((DiscordClientImpl) shard.getClient(), IVoiceChannel.class), new Cache<>((DiscordClientImpl) shard.getClient(), IUser.class),
				new Cache<>((DiscordClientImpl) shard.getClient(), TimeStampHolder.class));
	}

	public Guild(IShard shard, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout,
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
	public String getOwnerID() {
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
	public void setOwnerID(String id) {
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
		this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
	}

	@Override
	public List<IChannel> getChannels() {
		return new LinkedList<>(channels.values());
	}

	@Override
	public IChannel getChannelByID(String id) {
		return channels.get(id);
	}

	@Override
	public List<IUser> getUsers() {
		return new LinkedList<>(users.values());
	}

	@Override
	public IUser getUserByID(String id) {
		if (users == null || id == null)
			return null;

		IUser user = users.get(id);

		if (user == null) {
			if (client.getOurUser() != null && id.equals(client.getOurUser().getID()))
				user = client.getOurUser();
			else if (id.equals(ownerID))
				user = getOwner();
		}

		return user;
	}

	@Override
	public List<IChannel> getChannelsByName(String name) {
		return channels.values().stream()
				.filter(channel -> channel.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getVoiceChannelsByName(String name) {
		return voiceChannels.values().stream()
				.filter(channel -> channel.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IUser> getUsersByName(String name) {
		return getUsersByName(name, true);
	}

	@Override
	public List<IUser> getUsersByName(String name, boolean includeNicknames) {
		return users.values().stream()
				.filter(u -> includeNicknames ? u.getDisplayName(this).equals(name) : u.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IUser> getUsersByRole(IRole role) {
		return users.values().stream()
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
	public String getID() {
		return id;
	}

	/**
	 * CACHES a user to the guild.
	 *
	 * @param user The user.
	 */
	public void addUser(IUser user) {
		this.users.put(user);
	}

	/**
	 * CACHES a channel to the guild.
	 *
	 * @param channel The channel.
	 */
	public void addChannel(IChannel channel) {
		if (channel instanceof IVoiceChannel)
			voiceChannels.put((IVoiceChannel) channel);
		else
			this.channels.put(channel);
	}

	@Override
	public List<IRole> getRoles() {
		return new LinkedList<>(roles.values());
	}

	@Override
	public List<IRole> getRolesForUser(IUser user) {
		return user.getRolesForGuild(this);
	}

	/**
	 * CACHES a role to the guild.
	 *
	 * @param role The role.
	 */
	public void addRole(IRole role) {
		this.roles.put(role);
	}

	@Override
	public IRole getRoleByID(String id) {
		return roles.get(id);
	}

	@Override
	public List<IRole> getRolesByName(String name) {
		return roles.values().stream()
				.filter(role -> role.getName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return new LinkedList<>(voiceChannels.values());
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(String id) {
		return voiceChannels.get(id);
	}

	@Override
	public IVoiceChannel getAFKChannel() {
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

	public void setAFKChannel(String id) {
		this.afkChannel = id;
	}

	public void setAfkTimeout(int timeout) {
		this.afkTimeout = timeout;
	}

	@Override
	public IRole createRole() {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		RoleObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS+id+"/roles",
				RoleObject.class);
		return DiscordUtils.getRoleFromJSON(this, response);
	}

	@Override
	public List<IUser> getBannedUsers() {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.BAN));

		BanObject[] bans = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.GUILDS+id+"/bans",
				BanObject[].class);

		return Arrays.stream(bans).map(b -> DiscordUtils.getUserFromJSON(getShard(), b.user)).collect(Collectors.toList());
	}

	@Override
	public void banUser(IUser user) {
		banUser(user, 0);
	}

	@Override
	public void banUser(IUser user, int deleteMessagesForDays) {
		DiscordUtils.checkPermissions(client, this, getRolesForUser(user), EnumSet.of(Permissions.BAN));
		banUser(user.getID(), deleteMessagesForDays);
	}

	@Override
	public void banUser(String userID) {
		IUser user = getUserByID(userID);
		if (getUserByID(userID) == null) {
			DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.BAN));
		} else {
			DiscordUtils.checkPermissions(client, this, getRolesForUser(user), EnumSet.of(Permissions.BAN));
		}

		banUser(userID, 0);
	}

	@Override
	public void banUser(String userID, int deleteMessagesForDays) {
		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.GUILDS + id + "/bans/" + userID + "?delete-message-days=" + deleteMessagesForDays);
	}

	@Override
	public void pardonUser(String userID) {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.BAN));
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+id+"/bans/"+userID);
	}

	@Override
	public void kickUser(IUser user) {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.KICK));
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID());
	}

	@Override
	public void editUserRoles(IUser user, IRole[] roles) {
		DiscordUtils.checkPermissions(client, this, Arrays.asList(roles), EnumSet.of(Permissions.MANAGE_ROLES));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().roles(roles).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}

	}

	@Override
	public void setDeafenUser(IUser user, boolean deafen) {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.VOICE_DEAFEN_MEMBERS));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().deafen(deafen).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setMuteUser(IUser user, boolean mute) {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.VOICE_MUTE_MEMBERS));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().mute(mute).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setUserNickname(IUser user, String nick) {
		boolean isSelf = user.equals(client.getOurUser());
		if (isSelf) {
			DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.CHANGE_NICKNAME));
		} else {
			DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.MANAGE_NICKNAMES));
		}

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS+id+"/members/"+(isSelf ? "@me/nick" : user.getID()),
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MemberEditRequest.Builder().nick(nick == null ? "" : nick).build()));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void edit(String name, IRegion region, VerificationLevel level, Image icon, IVoiceChannel afkChannel, int afkTimeout) {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));

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
				DiscordEndpoints.GUILDS + id, new GuildEditRequest(name, region.getID(), level.ordinal(), icon.getData(),
						afkChannel == null ? null : afkChannel.getID(), afkTimeout));
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
		if (!ownerID.equals(client.getOurUser().getID()))
			throw new MissingPermissionsException("You must be the guild owner to delete guilds!", EnumSet.noneOf(Permissions.class));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+id);
	}

	@Override
	@Deprecated
	public void leaveGuild() {
		if (ownerID.equals(client.getOurUser().getID()))
			throw new DiscordException("Guild owners cannot leave their own guilds! Use deleteGuild() instead.");

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.USERS+"@me/guilds/"+id);
	}

	@Override
	public void leave() {
		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.USERS+"@me/guilds/"+id);
	}

	@Override
	public IChannel createChannel(String name) {
		shard.checkReady("create channel");
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		ChannelObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS+getID()+"/channels",
				new ChannelCreateRequest(name, "text"),
				ChannelObject.class);

		IChannel channel = DiscordUtils.getChannelFromJSON(this, response);
		addChannel(channel);

		return channel;
	}

	@Override
	public IVoiceChannel createVoiceChannel(String name) {
		getShard().checkReady("create voice channel");
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		ChannelObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS+getID()+"/channels",
				new ChannelCreateRequest(name, "voice"),
				ChannelObject.class);

		IVoiceChannel channel = DiscordUtils.getVoiceChannelFromJSON(this, response);
		addChannel(channel);

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
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));
		ExtendedInviteObject[] response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.GUILDS+ id + "/invites",
				ExtendedInviteObject[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public void reorderRoles(IRole... rolesInOrder) {
		if (rolesInOrder.length != getRoles().size())
			throw new DiscordException("The number of roles to reorder does not equal the number of available roles!");

		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		int usersHighest = getRolesForUser(client.getOurUser()).stream()
				.map(IRole::getPosition)
				.max(Comparator.comparing(Function.identity()))
				.orElse(-1);

		ReorderRolesRequest[] request = new ReorderRolesRequest[roles.size()];

		for (int i = 0; i < roles.size(); i++) {
			IRole role = rolesInOrder[i];

			int newPosition = role.getPosition();
			int oldPosition = getRoleByID(role.getID()).getPosition();
			if (newPosition != oldPosition && oldPosition >= usersHighest) { // If the position was changed and the user doesn't have permission to change it.
				throw new MissingPermissionsException("Cannot edit the position of a role higher than or equal to your own.", EnumSet.noneOf(Permissions.class));
			} else {
				request[i] = new ReorderRolesRequest(role.getID(), i);
			}
		}

		((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS + id + "/roles", request);
	}

	@Override
	public int getUsersToBePruned(int days) {
		PruneResponse response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.GUILDS + id + "/prune?days=" + days,
				PruneResponse.class);
		return response.pruned;
	}

	@Override
	public int pruneUsers(int days) {
		PruneResponse response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.GUILDS + id + "/prune?days=" + days,
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

	/**
	 * This gets the CACHED join times map.
	 *
	 * @return The join times.
	 */
	public Cache<TimeStampHolder> getJoinTimes() {
		return joinTimes;
	}

	@Override
	public LocalDateTime getJoinTimeForUser(IUser user) {
		if (!joinTimes.containsKey(user.getID()))
			throw new DiscordException("Cannot find user "+user.getDisplayName(this)+" in this guild!");

		return joinTimes.get(user.getID()).getObject();
	}

	@Override
	public IMessage getMessageByID(String id) {
		IMessage message =  channels.values().stream()
				.map(IChannel::getMessageHistory)
				.flatMap(List::stream)
				.filter(msg -> msg.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);

		if (message == null) {
			for (IChannel channel : channels.values()) {
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
	public IEmoji getEmojiByID(String id) {
		return emojis.get(id);
	}

	@Override
	public IEmoji getEmojiByName(String name) {
		return emojis.values().stream()
				.filter(emoji -> emoji.getName().equals(name))
				.findFirst().orElse(null);
	}

	@Override
	public IWebhook getWebhookByID(String id) {
		return channels.values().stream()
				.map(channel -> channel.getWebhookByID(id))
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		return channels.values().stream()
				.map(IChannel::getWebhooks)
				.flatMap(List::stream)
				.filter(hook -> hook.getDefaultName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public List<IWebhook> getWebhooks() {
		return channels.values().stream()
				.map(IChannel::getWebhooks)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public void loadWebhooks() {
		try {
			DiscordUtils.checkPermissions(getClient(), this, EnumSet.of(Permissions.MANAGE_WEBHOOKS));
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
						DiscordEndpoints.GUILDS + getID() + "/webhooks",
						WebhookObject[].class);

				if (response != null) {
					for (WebhookObject webhookObject : response) {
						Channel channel = (Channel) getChannelByID(webhookObject.channel_id);
						if (getWebhookByID(webhookObject.id) == null) {
							IWebhook newWebhook = DiscordUtils.getWebhookFromJSON(channel, webhookObject);
							client.getDispatcher().dispatch(new WebhookCreateEvent(newWebhook));
							channel.addWebhook(newWebhook);
						} else {
							IWebhook toUpdate = channel.getWebhookByID(webhookObject.id);
							IWebhook oldWebhook = toUpdate.copy();
							toUpdate = DiscordUtils.getWebhookFromJSON(channel, webhookObject);
							if (!oldWebhook.getDefaultName().equals(toUpdate.getDefaultName()) || !String.valueOf(oldWebhook.getDefaultAvatar()).equals(String.valueOf(toUpdate.getDefaultAvatar())))
								client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, toUpdate));

							oldList.remove(oldWebhook);
						}
					}
				}

				oldList.forEach(webhook -> {
					((Channel) webhook.getChannel()).removeWebhook(webhook);
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

		public TimeStampHolder(String id, LocalDateTime obj) {
			super(id, obj);
		}
	}
}
