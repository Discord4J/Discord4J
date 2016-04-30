package sx.blah.discord.handle.impl.obj;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.impl.events.ChannelUpdateEvent;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.generic.PermissionOverwrite;
import sx.blah.discord.json.requests.ChannelEditRequest;
import sx.blah.discord.json.requests.InviteRequest;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.ChannelResponse;
import sx.blah.discord.json.responses.ExtendedInviteResponse;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.MissingPermissionsException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Channel implements IChannel {

	/**
	 * User-friendly channel name (e.g. "general")
	 */
	protected volatile String name;

	/**
	 * Channel ID.
	 */
	protected final String id;

	/**
	 * Messages that have been sent into this channel
	 */
	protected final MessageList messages;

	/**
	 * Indicates whether or not this channel is a PM channel.
	 */
	protected volatile boolean isPrivate;

	/**
	 * The guild this channel belongs to.
	 */
	protected final IGuild parent;

	/**
	 * The channel's topic message.
	 */
	protected volatile String topic;

	/**
	 * Whether the bot should send out a typing status
	 */
	protected AtomicBoolean isTyping = new AtomicBoolean(false);

	/**
	 * Manages all TimerTasks which send typing statuses.
	 */
	protected static final Timer typingTimer = new Timer("Typing Status Timer", true);

	/**
	 * 10 seconds, the time it takes for one typing status to "wear off".
	 */
	protected static final long TIME_FOR_TYPE_STATUS = 10000;

	/**
	 * The position of this channel in the channel list.
	 */
	protected volatile int position;

	/**
	 * The permission overrides for users (key = user id).
	 */
	protected volatile Map<String, PermissionOverride> userOverrides;

	/**
	 * The permission overrides for roles (key = user id).
	 */
	protected volatile Map<String, PermissionOverride> roleOverrides;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Channel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position) {
		this(client, name, id, parent, topic, position, new HashMap<>(), new HashMap<>());
	}

	public Channel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.parent = parent;
		this.isPrivate = false;
		this.topic = topic;
		this.position = position;
		this.roleOverrides = roleOverrides;
		this.userOverrides = userOverrides;
		if (!(this instanceof IVoiceChannel))
			this.messages = new MessageList(client, this, MessageList.MESSAGE_CHUNK_COUNT);
		else
			this.messages = null;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the CACHED name of the channel.
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

	@Override
	public MessageList getMessages() {
		return messages;
	}

	@Override
	public IMessage getMessageByID(String messageID) {
		if (messages == null)
			return null;

		return messages.get(messageID);
	}

	@Override
	public IGuild getGuild() {
		return parent;
	}

	@Override
	public boolean isPrivate() {
		return isPrivate;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	/**
	 * Sets the CACHED topic for the channel.
	 *
	 * @param topic The new channel topic
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public String mention() {
		return "<#"+this.getID()+">";
	}

	@Override
	public IMessage sendMessage(String content) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		return sendMessage(content, false);
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.SEND_MESSAGES));

		if (client.isReady()) {
//            content = DiscordUtils.escapeString(content);

			MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS+id+"/messages",
					new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, new String[0], tts)), "UTF-8"),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);

			return DiscordUtils.getMessageFromJSON(client, this, response);

		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}

	@Override
	public IMessage sendFile(File file, String content) throws IOException, MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.SEND_MESSAGES, Permissions.ATTACH_FILES));

		if (client.isReady()) {
			//These next two lines of code took WAAAAAY too long to figure out than I care to admit
			MultipartEntityBuilder builder = MultipartEntityBuilder.create()
					.addBinaryBody("file", file, ContentType.create(Files.probeContentType(file.toPath())), file.getName());

			if (content != null)
				builder.addTextBody("content", content);

			HttpEntity fileEntity = builder.build();
			MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(
					DiscordEndpoints.CHANNELS+id+"/messages",
					fileEntity, new BasicNameValuePair("authorization", client.getToken())), MessageResponse.class);
			IMessage message = DiscordUtils.getMessageFromJSON(client, this, response);
			client.getDispatcher().dispatch(new MessageSendEvent(message));
			return message;
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}

	@Override
	public IMessage sendFile(File file) throws IOException, MissingPermissionsException, HTTP429Exception, DiscordException {
		return sendFile(file, null);
	}

	@Override
	public IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean useXkcdPass) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.CREATE_INVITE));

		if (!client.isReady()) {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}

		try {
			ExtendedInviteResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/invites",
					new StringEntity(DiscordUtils.GSON.toJson(new InviteRequest(maxAge, maxUses, temporary, useXkcdPass))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), ExtendedInviteResponse.class);

			return DiscordUtils.getInviteFromJSON(client, response);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}

		return null;
	}

	@Override
	public synchronized void toggleTypingStatus() {
		isTyping.set(!isTyping.get());

		typingTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!isTyping.get()) {
					this.cancel();
					return;
				}
				try {
					Requests.POST.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/typing",
							new BasicNameValuePair("authorization", client.getToken()));
				} catch (HTTP429Exception | DiscordException e) {
					Discord4J.LOGGER.error("Discord4J Internal Exception", e);
				}
			}
		}, 0, TIME_FOR_TYPE_STATUS);
	}

	@Override
	public synchronized boolean getTypingStatus() {
		return isTyping.get();
	}

	private void edit(Optional<String> name, Optional<Integer> position, Optional<String> topic) throws DiscordException, MissingPermissionsException, HTTP429Exception {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		String newName = name.orElse(this.name);
		int newPosition = position.orElse(this.position);
		String newTopic = topic.orElse(this.topic);

		if (newName == null || newName.length() < 2 || newName.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		try {
			ChannelResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.CHANNELS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelEditRequest(newName, newPosition, newTopic))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), ChannelResponse.class);

			IChannel oldChannel = copy();
			IChannel newChannel = DiscordUtils.getChannelFromJSON(client, getGuild(), response);

			client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, newChannel));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeName(String name) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.of(name), Optional.empty(), Optional.empty());
	}

	@Override
	public void changePosition(int position) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.of(position), Optional.empty());
	}

	@Override
	public void changeTopic(String topic) throws HTTP429Exception, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.empty(), Optional.of(topic));
	}

	@Override
	public int getPosition() {
		return position;
	}

	/**
	 * Sets the CACHED position of the channel.
	 *
	 * @param position The position.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void delete() throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS+id,
				new BasicNameValuePair("authorization", client.getToken()));
	}

	@Override
	public Map<String, PermissionOverride> getUserOverrides() {
		return userOverrides;
	}

	@Override
	public Map<String, PermissionOverride> getRoleOverrides() {
		return roleOverrides;
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		if (isPrivate || getGuild().getOwnerID().equals(user.getID()))
			return EnumSet.allOf(Permissions.class);

		List<IRole> roles = user.getRolesForGuild(parent);
		EnumSet<Permissions> permissions = EnumSet.noneOf(Permissions.class);

		roles.stream()
				.map(this::getModifiedPermissions)
				.flatMap(EnumSet::stream)
				.filter(p -> !permissions.contains(p))
				.forEach(permissions::add);

		PermissionOverride override = getUserOverrides().get(user.getID());
		if (override == null)
			return permissions;

		permissions.addAll(override.allow().stream().collect(Collectors.toList()));
		override.deny().forEach(permissions::remove);

		return permissions;
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		EnumSet<Permissions> base = role.getPermissions();
		PermissionOverride override = getRoleOverrides().get(role.getID());

		if (override == null) {
			if ((override = getRoleOverrides().get(parent.getEveryoneRole().getID())) == null)
				return base;
		}

		base.addAll(override.allow().stream().collect(Collectors.toList()));
		override.deny().forEach(base::remove);

		return base;
	}

	/**
	 * CACHES a permissions override for a user in this channel.
	 *
	 * @param userId The user the permissions override is for.
	 * @param override The permissions override.
	 */
	public void addUserOverride(String userId, PermissionOverride override) {
		userOverrides.put(userId, override);
	}

	/**
	 * CACHES a permissions override for a role in this channel.
	 *
	 * @param roleId The role the permissions override is for.
	 * @param override The permissions override.
	 */
	public void addRoleOverride(String roleId, PermissionOverride override) {
		roleOverrides.put(roleId, override);
	}

	@Override
	public void removePermissionsOverride(IUser user) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+user.getID(),
				new BasicNameValuePair("authorization", client.getToken()));

		userOverrides.remove(user.getID());
	}

	@Override
	public void removePermissionsOverride(IRole role) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+role.getID(),
				new BasicNameValuePair("authorization", client.getToken()));

		roleOverrides.remove(role.getID());
	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		overridePermissions("role", role.getID(), toAdd, toRemove);
	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		overridePermissions("member", user.getID(), toAdd, toRemove);
	}

	private void overridePermissions(String type, String id, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		try {
			Requests.PUT.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+id,
					new StringEntity(DiscordUtils.GSON.toJson(new PermissionOverwrite(type, id,
							Permissions.generatePermissionsNumber(toAdd), Permissions.generatePermissionsNumber(toRemove)))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
	}

	@Override
	public List<IInvite> getInvites() throws DiscordException, HTTP429Exception {
		ExtendedInviteResponse[] response = DiscordUtils.GSON.fromJson(
				Requests.GET.makeRequest(DiscordEndpoints.CHANNELS + id + "/invites",
						new BasicNameValuePair("authorization", client.getToken()),
						new BasicNameValuePair("content-type", "application/json")), ExtendedInviteResponse[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteResponse inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public IChannel copy() {
		return new Channel(client, name, id, parent, topic, position, roleOverrides, userOverrides);
	}

	@Override
	public IDiscordClient getClient() {
		return client;
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

		return this.getClass().isAssignableFrom(other.getClass()) && ((IChannel) other).getID().equals(getID());
	}

	/**
	 * Sets the CACHED typing status.
	 *
	 * @param typingStatus The new typing status.
	 */
	public void setTypingStatus(boolean typingStatus) {
		this.isTyping.set(typingStatus);
	}
}
