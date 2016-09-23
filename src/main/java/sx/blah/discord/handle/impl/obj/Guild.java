package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.events.GuildUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.api.internal.json.generic.RoleResponse;
import sx.blah.discord.api.internal.json.requests.*;
import sx.blah.discord.api.internal.json.responses.*;
import sx.blah.discord.util.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Guild implements IGuild {
	/**
	 * All text channels in the guild.
	 */
	protected final List<IChannel> channels;

	/**
	 * All voice channels in the guild.
	 */
	protected final List<IVoiceChannel> voiceChannels;

	/**
	 * All users connected to the guild.
	 */
	protected final List<IUser> users;

	/**
	 * The joined timetamps for users.
	 */
	protected final Map<IUser, LocalDateTime> joinTimes;

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
	protected final List<IRole> roles;

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
	 * This guild's audio manager.
	 */
	protected volatile AudioManager audioManager;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region) {
		this(client, name, id, icon, ownerID, afkChannel, afkTimeout, region, new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>(), new ConcurrentHashMap<>());
	}

	public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region, List<IRole> roles, List<IChannel> channels, List<IVoiceChannel> voiceChannels, List<IUser> users, Map<IUser, LocalDateTime> joinTimes) {
		this.client = client;
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
		this.audioManager = new AudioManager(this);
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
		return channels;
	}

	@Override
	public IChannel getChannelByID(String id) {
		return channels.stream()
				.filter(c -> c.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IUser> getUsers() {
		return users;
	}

	@Override
	public IUser getUserByID(String id) {
		if (users == null)
			return null;
		return Arrays.stream(users.toArray(new IUser[users.size()]))
				.filter(u -> u != null && u.getID() != null && u.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IChannel> getChannelsByName(String name) {
		return getChannelsByName(name, false);
	}
	
	@Override
	public List<IChannel> getChannelsByName(String name, boolean ignoreCase) {
		return channels.stream()
				.filter((channel) ->
					(ignoreCase) ? channel.getName().equalsIgnoreCase(name) : channel.getName().equals(name)
				)
				.collect(Collectors.toList());
	}

	@Override
	public List<IVoiceChannel> getVoiceChannelsByName(String name) {
		return getVoiceChannelsByName(name, false);
	}
	
	@Override
	public List<IVoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase) {
		return voiceChannels.stream()
				.filter((channel) ->
					(ignoreCase) ? channel.getName().equalsIgnoreCase(name) : channel.getName().equals(name)
				)
				.collect(Collectors.toList());
	}

	@Override
	public List<IUser> getUsersByName(String name, boolean includeNicknames, boolean ignoreCase) {
		return users.stream()
				.filter((user) ->
					(ignoreCase) ? user.getName().equalsIgnoreCase(name) || (includeNicknames && user.getNicknameForGuild(this).orElse("").equalsIgnoreCase(name))
						: user.getName().equals(name) || (includeNicknames && user.getNicknameForGuild(this).orElse("").equals(name))
				)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<IUser> getUsersByName(String name, boolean includeNicknames) {
		return getUsersByName(name, includeNicknames, false);
	}
	
	@Override
	public List<IUser> getUsersByName(String name) {
		return getUsersByName(name, true, false);
	}
	
	@Override
	public List<IUser> getUsersByRole(IRole role) {
		return users.stream().filter((user) -> user.getRolesForGuild(this).contains(role))
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
		if (!this.users.contains(user) && user != null)
			this.users.add(user);
	}

	/**
	 * CACHES a channel to the guild.
	 *
	 * @param channel The channel.
	 */
	public void addChannel(IChannel channel) {
		if (!this.channels.contains(channel) && !(channel instanceof IVoiceChannel) && !(channel instanceof IPrivateChannel))
			this.channels.add(channel);
	}

	@Override
	public List<IRole> getRoles() {
		return roles;
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
		if (!this.roles.contains(role))
			this.roles.add(role);
	}

	@Override
	public IRole getRoleByID(String id) {
		return roles.stream()
				.filter(r -> r.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IRole> getRolesByName(String name, boolean ignoreCase) {
		return roles.stream()
				.filter((role) -> 
					(ignoreCase) ? role.getName().equalsIgnoreCase(name) : role.getName().equals(name)
				)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<IRole> getRolesByName(String name) {
		return getRolesByName(name, false);
	}

	@Override
	public List<IVoiceChannel> getVoiceChannels() {
		return voiceChannels;
	}

	@Override
	public IVoiceChannel getVoiceChannelByID(String id) {
		return voiceChannels.stream()
				.filter(c -> c.getID().equals(id))
				.findAny().orElse(null);
	}

	@Override
	public IVoiceChannel getAFKChannel() {
		return getVoiceChannelByID(afkChannel);
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

	public void addVoiceChannel(IVoiceChannel channel) {
		if (!voiceChannels.contains(channel) && !(channel instanceof IPrivateChannel))
			voiceChannels.add(channel);
	}

	@Override
	public IRole createRole() throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		RoleResponse response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.GUILDS+id+"/roles",
				new BasicNameValuePair("authorization", client.getToken())), RoleResponse.class);
		IRole role = DiscordUtils.getRoleFromJSON(this, response);
		return role;
	}

	@Override
	public List<IUser> getBannedUsers() throws RateLimitException, DiscordException {
		UserResponse[] users = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.GET.makeRequest(DiscordEndpoints.GUILDS+id+"/bans",
				new BasicNameValuePair("authorization", client.getToken())), UserResponse[].class);
		List<IUser> banned = new ArrayList<>();
		for (UserResponse user : users) {
			banned.add(DiscordUtils.getUserFromJSON(client, user));
		}
		return banned;
	}

	@Override
	public void banUser(IUser user) throws MissingPermissionsException, RateLimitException, DiscordException {
		banUser(user, 0);
	}

	@Override
	public void banUser(IUser user, int deleteMessagesForDays) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.BAN));

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.GUILDS+id+"/bans/"+user.getID()+"?delete-message-days="+deleteMessagesForDays,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void pardonUser(String userID) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.BAN));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+id+"/bans/"+userID,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void kickUser(IUser user) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.KICK));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void editUserRoles(IUser user, IRole[] roles) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, Arrays.asList(roles), EnumSet.of(Permissions.MANAGE_ROLES));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new MemberEditRequest(roles))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setDeafenUser(IUser user, boolean deafen) throws MissingPermissionsException, DiscordException, RateLimitException {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.VOICE_DEAFEN_MEMBERS));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new MemberEditRequest(deafen))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setMuteUser(IUser user, boolean mute) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.VOICE_MUTE_MEMBERS));

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new MemberEditRequest(mute, true))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void setUserNickname(IUser user, String nick) throws MissingPermissionsException, DiscordException, RateLimitException {
		boolean isSelf = user.equals(client.getOurUser());
		if (isSelf) {
			DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.CHANGE_NICKNAME));
		} else {
			DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(this), EnumSet.of(Permissions.MANAGE_NICKNAMES));
		}

		try {
			((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+(isSelf ? "@me/nick" : user.getID()),
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new MemberEditRequest(nick == null ? "" : nick, true))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	private void edit(Optional<String> name, Optional<String> regionID, Optional<Image> icon, Optional<String> afkChannelID, Optional<Integer> afkTimeout) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));

		try {
			GuildResponse response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new EditGuildRequest(name.orElse(this.name), regionID.orElse(this.regionID),
							icon == null ? this.icon : (icon.isPresent() ? icon.get().getData() : null),
							afkChannelID == null ? this.afkChannel : afkChannelID.orElse(null), afkTimeout.orElse(this.afkTimeout)))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);

			IGuild oldGuild = copy();
			IGuild newGuild = DiscordUtils.getGuildFromJSON(client, response);

			client.getDispatcher().dispatch(new GuildUpdateEvent(oldGuild, newGuild));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeName(String name) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.of(name), Optional.empty(), null, null, Optional.empty());
	}

	@Override
	public void changeRegion(IRegion region) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.of(region.getID()), null, null, Optional.empty());
	}

	public void changeIcon(Image icon) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.empty(), Optional.ofNullable(icon), null, Optional.empty());
	}

	@Override
	public void changeAFKChannel(IVoiceChannel channel) throws RateLimitException, DiscordException, MissingPermissionsException {
		String id = channel != null ? channel.getID() : null;
		edit(Optional.empty(), Optional.empty(), null, Optional.ofNullable(id), Optional.empty());
	}

	@Override
	public void changeAFKTimeout(int timeout) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.empty(), null, null, Optional.of(timeout));
	}

	@Override
	public void deleteGuild() throws DiscordException, RateLimitException, MissingPermissionsException {
		if (!ownerID.equals(client.getOurUser().getID()))
			throw new MissingPermissionsException("You must be the guild owner to delete guilds!");

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.GUILDS+id,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void leaveGuild() throws DiscordException, RateLimitException {
		if (ownerID.equals(client.getOurUser().getID()))
			throw new DiscordException("Guild owners cannot leave their own guilds! Use deleteGuild() instead.");

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.USERS+"@me/guilds/"+id,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public IChannel createChannel(String name) throws DiscordException, MissingPermissionsException, RateLimitException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		if (!client.isReady()) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Bot has not signed in yet!");
			return null;
		}

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.GUILDS+getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelCreateRequest(name, "text"))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")),
					ChannelResponse.class);

			IChannel channel = DiscordUtils.getChannelFromJSON(client, this, response);
			addChannel(channel);

			return channel;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public IVoiceChannel createVoiceChannel(String name) throws DiscordException, MissingPermissionsException, RateLimitException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		if (!client.isReady()) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Bot has not signed in yet!");
			return null;
		}

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.GUILDS+getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelCreateRequest(name, "voice"))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")),
					ChannelResponse.class);

			IVoiceChannel channel = DiscordUtils.getVoiceChannelFromJSON(client, this, response);
			addVoiceChannel(channel);

			return channel;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
		return null;
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
	public void transferOwnership(IUser newOwner) throws RateLimitException, MissingPermissionsException, DiscordException {
		if (!getOwnerID().equals(client.getOurUser().getID()))
			throw new MissingPermissionsException("Cannot transfer ownership when you aren't the current owner!");
		try {
			DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.GUILDS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new TransferOwnershipRequest(newOwner.getID()))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public IRole getEveryoneRole() {
		return getRoleByID(this.id);
	}

	@Override
	public List<IInvite> getInvites() throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));
		ExtendedInviteResponse[] response = DiscordUtils.GSON.fromJson(
				((DiscordClientImpl) client).REQUESTS.GET.makeRequest(DiscordEndpoints.GUILDS+ id + "/invites",
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), ExtendedInviteResponse[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteResponse inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public void reorderRoles(IRole... rolesInOrder) throws DiscordException, RateLimitException, MissingPermissionsException {
		if (rolesInOrder.length != getRoles().size())
			throw new DiscordException("The number of roles to reorder does not equal the number of available roles!");

		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		ReorderRolesRequest[] request = new ReorderRolesRequest[rolesInOrder.length];

		for (int i = 0; i < rolesInOrder.length; i++) {
			int position = rolesInOrder[i].getName().equals("@everyone") ? -1 : i+1;
			if (position != rolesInOrder[i].getPosition()) {
				IRole highest = getRolesForUser(client.getOurUser()).stream().sorted((o1, o2) -> {
					if (o1.getPosition() < o2.getPosition()) {
						return -1;
					} else if (o2.getPosition() < o1.getPosition()) {
						return 1;
					} else {
						return 0;
					}
				}).findFirst().orElse(null);
				if (highest != null && highest.getPosition() <= position)
					throw new MissingPermissionsException("Cannot edit the position of a role with a higher/equal position as your user's highest role.");
			}
			request[i] = new ReorderRolesRequest(rolesInOrder[i].getID(), position);
		}

		try {
			RoleResponse[] response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.GUILDS + id + "/roles", new StringEntity(DiscordUtils.GSON.toJson(request)),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), RoleResponse[].class);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public int getUsersToBePruned(int days) throws DiscordException, RateLimitException {
		PruneResponse response = DiscordUtils.GSON.fromJson(
				((DiscordClientImpl) client).REQUESTS.GET.makeRequest(DiscordEndpoints.GUILDS + id + "/prune?days=" + days,
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), PruneResponse.class);
		return response.pruned;
	}

	@Override
	public int pruneUsers(int days) throws DiscordException, RateLimitException {
		PruneResponse response = DiscordUtils.GSON.fromJson(
				((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.GUILDS + id + "/prune?days=" + days,
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), PruneResponse.class);
		return response.pruned;
	}

	@Override
	public void addBot(String applicationID, EnumSet<Permissions> permissions) throws MissingPermissionsException, DiscordException, RateLimitException {
		if (client.isBot())
			throw new DiscordException("Bot accounts are not allowed to add other bots!");

		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));

		try {
			((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.AUTHORIZE+"?client_id="+applicationID+"&scope=bot",
					new StringEntity(DiscordUtils.GSON.toJson(new BotAddRequest(id,
							Permissions.generatePermissionsNumber(permissions)))),
					new BasicNameValuePair("authorization", client.getToken()));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
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
	public Map<IUser, LocalDateTime> getJoinTimes() {
		return joinTimes;
	}

	@Override
	public LocalDateTime getJoinTimeForUser(IUser user) throws DiscordException {
		if (!joinTimes.containsKey(user))
			throw new DiscordException("Cannot find user "+user.getDisplayName(this)+" in this guild!");

		return joinTimes.get(user);
	}

	@Override
	public IMessage getMessageByID(String id) {
		IMessage message =  channels.stream()
									.map(IChannel::getMessages)
									.flatMap(List::stream)
									.filter(msg -> msg.getID().equalsIgnoreCase(id))
									.findAny().orElse(null);

		if (message == null) {
			for (IChannel channel : channels) {
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
	public IGuild copy() {
		return new Guild(client, name, id, icon, ownerID, afkChannel, afkTimeout, regionID, roles, channels,
				voiceChannels, users, joinTimes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IGuild) other).getID().equals(getID());
	}
}
