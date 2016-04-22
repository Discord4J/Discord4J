package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.handle.AudioChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.generic.RoleResponse;
import sx.blah.discord.json.requests.*;
import sx.blah.discord.json.responses.*;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Image;
import sx.blah.discord.api.internal.Requests;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;

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
	 * The name of the guild.
	 */
	protected String name;

	/**
	 * The ID of this guild.
	 */
	protected final String id;

	/**
	 * The location of the guild icon
	 */
	protected String icon;

	/**
	 * The url pointing to the guild icon
	 */
	protected String iconURL;

	/**
	 * The user id for the owner of the guild
	 */
	protected String ownerID;

	/**
	 * The roles the guild contains.
	 */
	protected final List<IRole> roles;

	/**
	 * The channel where those who are afk are moved to.
	 */
	protected String afkChannel;
	/**
	 * The time in seconds for a user to be idle to be determined as "afk".
	 */
	protected int afkTimeout;

	/**
	 * The region this guild is located in.
	 */
	protected String regionID;

	/**
	 * This guild's audio channel.
	 */
	protected AudioChannel audioChannel;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region) {
		this(client, name, id, icon, ownerID, afkChannel, afkTimeout, region, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	public Guild(IDiscordClient client, String name, String id, String icon, String ownerID, String afkChannel, int afkTimeout, String region, List<IRole> roles, List<IChannel> channels, List<IVoiceChannel> voiceChannels, List<IUser> users) {
		this.client = client;
		this.name = name;
		this.voiceChannels = voiceChannels;
		this.channels = channels;
		this.users = users;
		this.id = id;
		this.icon = icon;
		this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
		this.ownerID = ownerID;
		this.roles = roles;
		this.afkChannel = afkChannel;
		this.afkTimeout = afkTimeout;
		this.regionID = region;
		this.audioChannel = new AudioChannel(client);
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
		return users.stream()
				.filter(u -> u != null
						&& u.getID() != null
						&& u.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
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
	public IRole createRole() throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		RoleResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.GUILDS+id+"/roles",
				new BasicNameValuePair("authorization", client.getToken())), RoleResponse.class);
		IRole role = DiscordUtils.getRoleFromJSON(this, response);
		return role;
	}

	@Override
	public List<IUser> getBannedUsers() throws HTTP429Exception, DiscordException {
		UserResponse[] users = DiscordUtils.GSON.fromJson(Requests.GET.makeRequest(DiscordEndpoints.GUILDS+id+"/bans",
				new BasicNameValuePair("authorization", client.getToken())), UserResponse[].class);
		List<IUser> banned = new ArrayList<>();
		for (UserResponse user : users) {
			banned.add(DiscordUtils.getUserFromJSON(client, user));
		}
		return banned;
	}

	@Override
	public void banUser(IUser user) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		banUser(user, 0);
	}

	@Override
	public void banUser(IUser user, int deleteMessagesForDays) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.BAN));

		Requests.PUT.makeRequest(DiscordEndpoints.GUILDS+id+"/bans/"+user.getID()+"?delete-message-days="+deleteMessagesForDays,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void pardonUser(String userID) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.BAN));

		Requests.DELETE.makeRequest(DiscordEndpoints.GUILDS+id+"/bans/"+userID,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void kickUser(IUser user) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.KICK));

		Requests.DELETE.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void editUserRoles(IUser user, IRole[] roles) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		try {
			Requests.PATCH.makeRequest(DiscordEndpoints.GUILDS+id+"/members/"+user.getID(),
					new StringEntity(DiscordUtils.GSON.toJson(new MemberEditRequest(roles))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	private void edit(Optional<String> name, Optional<String> regionID, Optional<Image> icon, Optional<String> afkChannelID, Optional<Integer> afkTimeout) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));

		try {
			DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.GUILDS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new EditGuildRequest(name.orElse(this.name), regionID.orElse(this.regionID),
							icon == null ? this.icon : (icon.isPresent() ? icon.get().getData() : null),
							afkChannelID == null ? this.afkChannel : afkChannelID.orElse(null), afkTimeout.orElse(this.afkTimeout)))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeName(String name) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.of(name), Optional.empty(), null, null, Optional.empty());
	}

	@Override
	public void changeRegion(IRegion region) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.of(region.getID()), null, null, Optional.empty());
	}

	@Override
	public void changeIcon(Optional<Image> icon) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.empty(), icon, null, Optional.empty());
	}

	@Override
	public void changeAFKChannel(Optional<IVoiceChannel> channel) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		String id = channel.isPresent() ? channel.get().getID() : null;
		edit(Optional.empty(), Optional.empty(), null, Optional.ofNullable(id), Optional.empty());
	}

	@Override
	public void changeAFKTimeout(int timeout) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.empty(), null, null, Optional.of(timeout));
	}

	@Override
	public void deleteGuild() throws DiscordException, HTTP429Exception, MissingPermissionsException {
		if (!ownerID.equals(client.getOurUser().getID()))
			throw new MissingPermissionsException("You must be the guild owner to delete guilds!");

		Requests.DELETE.makeRequest(DiscordEndpoints.GUILDS+id,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public void leaveGuild() throws DiscordException, HTTP429Exception {
		if (ownerID.equals(client.getOurUser().getID()))
			throw new DiscordException("Guild owners cannot leave their own guilds! Use deleteGuild() instead.");

		Requests.DELETE.makeRequest(DiscordEndpoints.USERS+"@me/guilds/"+id,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public IChannel createChannel(String name) throws DiscordException, MissingPermissionsException, HTTP429Exception {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		if (!client.isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.GUILDS+getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelCreateRequest(name, "text"))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")),
					ChannelResponse.class);

			IChannel channel = DiscordUtils.getChannelFromJSON(client, this, response);
			addChannel(channel);

			return channel;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
		return null;
	}

	@Override
	public IVoiceChannel createVoiceChannel(String name) throws DiscordException, MissingPermissionsException, HTTP429Exception {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		if (!client.isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}

		if (name == null || name.length() < 2 || name.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");
		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.GUILDS+getID()+"/channels",
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelCreateRequest(name, "voice"))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")),
					ChannelResponse.class);

			IVoiceChannel channel = DiscordUtils.getVoiceChannelFromJSON(client, this, response);
			addVoiceChannel(channel);

			return channel;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
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
	public void transferOwnership(IUser newOwner) throws HTTP429Exception, MissingPermissionsException, DiscordException {
		if (!getOwnerID().equals(client.getOurUser().getID()))
			throw new MissingPermissionsException("Cannot transfer ownership when you aren't the current owner!");
		try {
			DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.GUILDS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new TransferOwnershipRequest(newOwner.getID()))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), GuildResponse.class);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public IRole getEveryoneRole() {
		return getRoles().stream().filter(r -> r.getName().equals("@everyone")).findFirst().orElse(null);
	}

	@Override
	public List<IInvite> getInvites() throws DiscordException, HTTP429Exception {
		ExtendedInviteResponse[] response = DiscordUtils.GSON.fromJson(
				Requests.GET.makeRequest(DiscordEndpoints.GUILDS+ id + "/invites",
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), ExtendedInviteResponse[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteResponse inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public void reorderRoles(IRole... rolesInOrder) throws DiscordException, HTTP429Exception, MissingPermissionsException {
		if (rolesInOrder.length != getRoles().size())
			throw new DiscordException("The number of roles to reorder does not equal the number of available roles!");

		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_ROLES));

		ReorderRolesRequest[] request = new ReorderRolesRequest[rolesInOrder.length];

		for (int i = 0; i < rolesInOrder.length; i++) {
			request[i] = new ReorderRolesRequest(rolesInOrder[i].getID(),
					rolesInOrder[i].getName().equals("@everyone") ? -1 : i+1);
		}

		try {
			RoleResponse[] response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(
					DiscordEndpoints.GUILDS + id + "/roles", new StringEntity(DiscordUtils.GSON.toJson(request)),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), RoleResponse[].class);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public int getUsersToBePruned(int days) throws DiscordException, HTTP429Exception {
		PruneResponse response = DiscordUtils.GSON.fromJson(
				Requests.GET.makeRequest(DiscordEndpoints.GUILDS + id + "/prune?days=" + days,
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), PruneResponse.class);
		return response.pruned;
	}

	@Override
	public int pruneUsers(int days) throws DiscordException, HTTP429Exception {
		PruneResponse response = DiscordUtils.GSON.fromJson(
				Requests.POST.makeRequest(DiscordEndpoints.GUILDS + id + "/prune?days=" + days,
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), PruneResponse.class);
		return response.pruned;
	}

	@Override
	public LocalDateTime getCreationDate() {
		return DiscordUtils.getSnowflakeTimeFromID(id);
	}

	@Override
	public void addBot(String applicationID, Optional<EnumSet<Permissions>> permissions) throws MissingPermissionsException, DiscordException, HTTP429Exception {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_SERVER));

		try {
			Requests.POST.makeRequest(DiscordEndpoints.AUTHORIZE+"?client_id="+applicationID+"&scope=bot",
					new StringEntity(DiscordUtils.GSON.toJson(new BotAddRequest(id,
							Permissions.generatePermissionsNumber(permissions.orElse(EnumSet.noneOf(Permissions.class)))))),
					new BasicNameValuePair("authorization", client.getToken()));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public AudioChannel getAudioChannel() throws DiscordException {
		return audioChannel;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
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
