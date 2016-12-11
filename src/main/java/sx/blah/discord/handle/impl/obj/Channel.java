package sx.blah.discord.handle.impl.obj;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.requests.InviteCreateRequest;
import sx.blah.discord.api.internal.json.requests.WebhookCreateRequest;
import sx.blah.discord.handle.impl.events.ChannelUpdateEvent;
import sx.blah.discord.handle.impl.events.WebhookCreateEvent;
import sx.blah.discord.handle.impl.events.WebhookDeleteEvent;
import sx.blah.discord.handle.impl.events.WebhookUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.api.internal.json.requests.ChannelEditRequest;
import sx.blah.discord.api.internal.json.requests.MessageRequest;
import sx.blah.discord.util.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
	private AtomicBoolean isTyping = new AtomicBoolean(false);

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
	protected final Map<String, PermissionOverride> userOverrides;

	/**
	 * The permission overrides for roles (key = role id).
	 */
	protected final Map<String, PermissionOverride> roleOverrides;

	/**
	 * The webhooks for this channel.
	 */
	protected final List<IWebhook> webhooks;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Channel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.parent = parent;
		this.topic = topic;
		this.position = position;
		this.roleOverrides = roleOverrides;
		this.userOverrides = userOverrides;
		if (!(this instanceof IVoiceChannel)) {
			this.messages = new MessageList(client, this, MessageList.MESSAGE_CHUNK_COUNT);
		} else {
			this.messages = null;
		}
		this.webhooks = new CopyOnWriteArrayList<>();
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
		return this instanceof PrivateChannel;
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
		return "<#" + this.getID() + ">";
	}

	@Override
	public IMessage sendMessage(String content) throws MissingPermissionsException, RateLimitException, DiscordException {
		return sendMessage(content, false);
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) throws MissingPermissionsException, RateLimitException, DiscordException {
		return sendMessage(content, null, tts);
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed, boolean tts) throws RateLimitException,
			DiscordException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.SEND_MESSAGES));

		if (embed != null) {
			DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.EMBED_LINKS));
		}

		if (client.isReady()) {
//            content = DiscordUtils.escapeString(content);

			MessageObject response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS+id+"/messages",
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new MessageRequest(content, embed, tts)), "UTF-8")), MessageObject.class);

			if (response == null || response.id == null) //Message didn't send
				throw new DiscordException("Message was unable to be sent.");

			return DiscordUtils.getMessageFromJSON(this, response);

		} else {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Attempt to send message before bot is ready!");
			return null;
		}
	}

	@Override
	public IMessage sendFile(File file) throws FileNotFoundException, RateLimitException, DiscordException, MissingPermissionsException {
		return sendFile(null, file);
	}

	@Override
	public IMessage sendFile(String content, File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile(content, false, new FileInputStream(file), file.getName());
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(getClient(), this, EnumSet.of(Permissions.SEND_MESSAGES, Permissions.ATTACH_FILES));

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		if (content != null) builder.addTextBody("content", content, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
		builder.addTextBody("tts", String.valueOf(tts));
		builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, fileName);

		HttpEntity fileEntity = builder.build();
		String response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS+id+"/messages"
				, fileEntity, new BasicNameValuePair("Content-Type", "multipart/form-data"));
		MessageObject messageObject = DiscordUtils.GSON.fromJson(response, MessageObject.class);

		return DiscordUtils.getMessageFromJSON(this, messageObject);
	}

	@Override
	public IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.CREATE_INVITE));

		if (!client.isReady()) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Attempt to create invite before bot is ready!");
			return null;
		}

		try {
			ExtendedInviteObject response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/invites",
					new StringEntity(DiscordUtils.GSON.toJson(new InviteCreateRequest(maxAge, maxUses, temporary, unique)))),
					ExtendedInviteObject.class);

			return DiscordUtils.getInviteFromJSON(client, response);
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}

		return null;
	}

	@Override
	public synchronized void toggleTypingStatus() {
		setTypingStatus(!this.isTyping.get());
	}

	@Override
	public void setTypingStatus(boolean typing) {
		isTyping.set(typing);

		if (isTyping.get())
			typingTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (!isTyping.get()) {
						this.cancel();
						return;
					}
					try {
						((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS + getID() + "/typing");
					} catch (RateLimitException | DiscordException e) {
						Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
					}
				}
			}, 0, TIME_FOR_TYPE_STATUS);
	}

	@Override
	public synchronized boolean getTypingStatus() {
		return isTyping.get();
	}

	private void edit(Optional<String> name, Optional<Integer> position, Optional<String> topic) throws DiscordException, MissingPermissionsException, RateLimitException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		String newName = name.orElse(this.name);
		int newPosition = position.orElse(this.position);
		String newTopic = topic.orElse(this.topic);

		if (newName == null || newName.length() < 2 || newName.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		try {
			ChannelObject response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(DiscordEndpoints.CHANNELS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelEditRequest(newName, newPosition, newTopic)))),
					ChannelObject.class);

			IChannel oldChannel = copy();
			IChannel newChannel = DiscordUtils.getChannelFromJSON(getGuild(), response);

			client.getDispatcher().dispatch(new ChannelUpdateEvent(oldChannel, newChannel));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void changeName(String name) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.of(name), Optional.empty(), Optional.empty());
	}

	@Override
	public void changePosition(int position) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.of(position), Optional.empty());
	}

	@Override
	public void changeTopic(String topic) throws RateLimitException, DiscordException, MissingPermissionsException {
		edit(Optional.empty(), Optional.empty(), Optional.of(topic));
	}

	@Override
	public int getPosition() {
		getGuild().getChannels().sort((c1, c2) -> {
			int originalPos1 = ((Channel) c1).position;
			int originalPos2 = ((Channel) c2).position;
			if (originalPos1 == originalPos2) {
				return c2.getCreationDate().compareTo(c1.getCreationDate());
			} else {
				return originalPos1 - originalPos2;
			}
		});
		return getGuild().getChannels().indexOf(this);
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
	public void delete() throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNELS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+id);
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
		if (isPrivate() || getGuild().getOwnerID().equals(user.getID()))
			return EnumSet.allOf(Permissions.class);

		List<IRole> roles = user.getRolesForGuild(parent);
		EnumSet<Permissions> permissions = user.getPermissionsForGuild(parent);

		PermissionOverride override = getUserOverrides().get(user.getID());
		List<PermissionOverride> overrideRoles = roles.stream()
				.filter(r -> roleOverrides.containsKey(r.getID()))
				.map(role -> roleOverrides.get(role.getID()))
				.collect(Collectors.toList());
		Collections.reverse(overrideRoles);
		for (PermissionOverride roleOverride : overrideRoles) {
			permissions.addAll(roleOverride.allow());
			permissions.removeAll(roleOverride.deny());
		}

		if (override != null) {
			permissions.addAll(override.allow());
			permissions.removeAll(override.deny());
		}

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
	 * @param userId   The user the permissions override is for.
	 * @param override The permissions override.
	 */
	public void addUserOverride(String userId, PermissionOverride override) {
		userOverrides.put(userId, override);
	}

	/**
	 * CACHES a permissions override for a role in this channel.
	 *
	 * @param roleId   The role the permissions override is for.
	 * @param override The permissions override.
	 */
	public void addRoleOverride(String roleId, PermissionOverride override) {
		roleOverrides.put(roleId, override);
	}

	@Override
	public void removePermissionsOverride(IUser user) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(parent), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+user.getID());

		userOverrides.remove(user.getID());
	}

	@Override
	public void removePermissionsOverride(IRole role) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, Collections.singletonList(role), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+role.getID());

		roleOverrides.remove(role.getID());
	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, RateLimitException, DiscordException {
		overridePermissions("role", role.getID(), toAdd, toRemove);
	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, RateLimitException, DiscordException {
		overridePermissions("member", user.getID(), toAdd, toRemove);
	}

	private void overridePermissions(String type, String id, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, RateLimitException, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		try {
			((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+id,
					new StringEntity(DiscordUtils.GSON_NO_NULLS.toJson(new OverwriteObject(type, null,
							Permissions.generatePermissionsNumber(toAdd), Permissions.generatePermissionsNumber(toRemove)))));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public List<IInvite> getInvites() throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL));
		ExtendedInviteObject[] response = DiscordUtils.GSON.fromJson(
				((DiscordClientImpl) client).REQUESTS.GET.makeRequest(DiscordEndpoints.CHANNELS + id + "/invites"),
				ExtendedInviteObject[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public List<IUser> getUsersHere() {
		return parent.getUsers().stream().filter((user) -> {
			EnumSet<Permissions> permissions = getModifiedPermissions(user);
			return Permissions.READ_MESSAGES.hasPermission(Permissions.generatePermissionsNumber(permissions), true);
		}).collect(Collectors.toList());
	}

	@Override
	public List<IMessage> getPinnedMessages() throws RateLimitException, DiscordException {
		List<IMessage> messages = new ArrayList<>();
		MessageObject[] pinnedMessages = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/pins"),
				MessageObject[].class);

		for (MessageObject message : pinnedMessages)
			messages.add(DiscordUtils.getMessageFromJSON(this, message));

		return messages;
	}

	@Override
	public void pin(IMessage message) throws RateLimitException, DiscordException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_MESSAGES));

		if (!message.getChannel().equals(this))
			throw new DiscordException("Message channel doesn't match current channel!");

		if (message.isPinned())
			throw new DiscordException("Message already pinned!");

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.CHANNELS + id + "/pins/" + message.getID());
	}

	@Override
	public void unpin(IMessage message) throws RateLimitException, DiscordException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_MESSAGES));

		if (!message.getChannel().equals(this))
			throw new DiscordException("Message channel doesn't match current channel!");

		if (!message.isPinned())
			throw new DiscordException("Message already unpinned!");

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS + id + "/pins/" + message.getID());
	}

	@Override
	public List<IWebhook> getWebhooks() {
		return webhooks;
	}

	@Override
	public IWebhook getWebhookByID(String id) {
		return webhooks.stream()
				.filter(w -> w.getID().equalsIgnoreCase(id))
				.findAny().orElse(null);
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		return webhooks.stream()
				.filter(w -> w.getDefaultName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public IWebhook createWebhook(String name) throws MissingPermissionsException, DiscordException, RateLimitException {
		return createWebhook(name, Image.defaultAvatar());
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) throws MissingPermissionsException, DiscordException, RateLimitException {
		return createWebhook(name, avatar.getData());
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) throws MissingPermissionsException, DiscordException, RateLimitException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_WEBHOOKS));

		if (!client.isReady()) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Attempt to create webhook before bot is ready!");
			return null;
		}

		if (name == null || name.length() < 2 || name.length() > 32)
			throw new DiscordException("Webhook name can only be between 2 and 32 characters!");
		try {
			WebhookObject response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS + getID() + "/webhooks",
					new StringEntity(DiscordUtils.GSON.toJson(new WebhookCreateRequest(name, avatar)))),
					WebhookObject.class);

			IWebhook webhook = DiscordUtils.getWebhookFromJSON(this, response);
			addWebhook(webhook);

			return webhook;
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
		return null;
	}

	/**
	 * CACHES a webhook to the channel.
	 *
	 * @param webhook The webhook.
	 */
	public void addWebhook(IWebhook webhook) {
		if (!this.webhooks.contains(webhook))
			this.webhooks.add(webhook);
	}

	/**
	 * Removes a webhook from the CACHE of the channel.
	 *
	 * @param webhook The webhook.
	 */
	public void removeWebhook(IWebhook webhook) {
		this.webhooks.remove(webhook);
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

				WebhookObject[] response = DiscordUtils.GSON.fromJson(((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
						DiscordEndpoints.CHANNELS + getID() + "/webhooks"),
						WebhookObject[].class);

				if (response != null) {
					for (WebhookObject webhookObject : response) {
						if (getWebhookByID(webhookObject.id) == null) {
							IWebhook newWebhook = DiscordUtils.getWebhookFromJSON(this, webhookObject);
							client.getDispatcher().dispatch(new WebhookCreateEvent(newWebhook));
							addWebhook(newWebhook);
						} else {
							IWebhook toUpdate = getWebhookByID(webhookObject.id);
							IWebhook oldWebhook = toUpdate.copy();
							toUpdate = DiscordUtils.getWebhookFromJSON(this, webhookObject);
							if (!oldWebhook.getDefaultName().equals(toUpdate.getDefaultName()) || !String.valueOf(oldWebhook.getDefaultAvatar()).equals(String.valueOf(toUpdate.getDefaultAvatar())))
								client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, toUpdate, this));

							oldList.remove(oldWebhook);
						}
					}
				}

				oldList.forEach(webhook -> {
					removeWebhook(webhook);
					client.getDispatcher().dispatch(new WebhookDeleteEvent(webhook));
				});
			} catch (Exception e) {
				Discord4J.LOGGER.warn(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
			}
		});
	}

	@Override
	public boolean isDeleted() {
		return getGuild().getChannelByID(id) != this;
	}

	@Override
	public IChannel copy() {
		Channel channel = new Channel(client, name, id, parent, topic, position, roleOverrides, userOverrides);
		channel.isTyping.set(isTyping.get());
		channel.roleOverrides.putAll(roleOverrides);
		channel.userOverrides.putAll(userOverrides);
		return channel;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IShard getShard() {
		if (isPrivate()) return getClient().getShards().get(0);
		return getGuild().getShard();
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
}
