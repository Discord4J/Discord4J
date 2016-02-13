/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.handle.impl.obj;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.generic.PermissionOverwrite;
import sx.blah.discord.json.requests.ChannelEditRequest;
import sx.blah.discord.json.requests.InviteRequest;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.ExtendedInviteResponse;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Lambdas;
import sx.blah.discord.util.MessageComparator;
import sx.blah.discord.util.Requests;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Channel implements IChannel {

	/**
	 * User-friendly channel name (e.g. "general")
	 */
	protected String name;

	/**
	 * Channel ID.
	 */
	protected final String id;

	/**
	 * Messages that have been sent into this channel
	 */
	protected final List<IMessage> messages;

	/**
	 * Indicates whether or not this channel is a PM channel.
	 */
	protected boolean isPrivate;

	/**
	 * The guild this channel belongs to.
	 */
	protected final IGuild parent;

	/**
	 * The channel's topic message.
	 */
	protected String topic;

	/**
	 * The last read message.
	 */
	protected String lastReadMessageID = null;

	/**
	 * Whether the bot should send out a typing status
	 */
	protected AtomicBoolean isTyping = new AtomicBoolean(false);

	/**
	 * Keeps track of the time to handle repeated typing status broadcasts
	 */
	protected AtomicLong typingTimer = new AtomicLong(0);

	/**
	 * 5 seconds, the time it takes for one typing status to "wear off"
	 */
	protected static final long TIME_FOR_TYPE_STATUS = 5000;

	/**
	 * The position of this channel in the channel list
	 */
	protected int position;

	/**
	 * The permission overrides for users (key = user id)
	 */
	protected Map<String, PermissionOverride> userOverrides;

	/**
	 * The permission overrides for roles (key = user id)
	 */
	protected Map<String, PermissionOverride> roleOverrides;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Channel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position) {
		this(client, name, id, parent, topic, position, new ArrayList<>(), new HashMap<>(), new HashMap<>());
	}

	public Channel(IDiscordClient client, String name, String id, IGuild parent, String topic, int position, List<IMessage> messages, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.messages = messages;
		this.parent = parent;
		this.isPrivate = false;
		this.topic = topic;
		this.position = position;
		this.roleOverrides = roleOverrides;
		this.userOverrides = userOverrides;
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
	public List<IMessage> getMessages() {
		return messages;
	}

	@Override
	public IMessage getMessageByID(String messageID) {
		return messages.stream()
				.filter(m->m.getID().equalsIgnoreCase(messageID))
				.findAny().orElse(null);
	}

	/**
	 * CACHES a message to the channel.
	 *
	 * @param message The message.
	 */
	public synchronized void addMessage(IMessage message) {
		if (message.getChannel().getID().equalsIgnoreCase(this.getID())) {
			messages.add(message);
			Collections.sort(messages, MessageComparator.INSTANCE);
			if (lastReadMessageID == null)
				lastReadMessageID = message.getID();
		}
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

			IMessage message = DiscordUtils.getMessageFromJSON(client, this, response);
			addMessage(message); //Had to be moved here so that if a message is edited before the MESSAGE_CREATE event, it doesn't error
			client.getDispatcher().dispatch(new MessageSendEvent(message));
			return message;

		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
	}

	@Override
	public IMessage sendFile(File file) throws IOException, MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.SEND_MESSAGES, Permissions.ATTACH_FILES));

		if (client.isReady()) {
			//These next two lines of code took WAAAAAY too long to figure out than I care to admit
			HttpEntity fileEntity = MultipartEntityBuilder.create().addBinaryBody("file", file,
					ContentType.create(Files.probeContentType(file.toPath())), file.getName()).build();
			MessageResponse response = DiscordUtils.GSON.fromJson(Requests.POST.makeRequest(
					DiscordEndpoints.CHANNELS+id+"/messages",
					fileEntity, new BasicNameValuePair("authorization", client.getToken())), MessageResponse.class);
			IMessage message = DiscordUtils.getMessageFromJSON(client, this, response);
			addMessage(message);
			client.getDispatcher().dispatch(new MessageSendEvent(message));
			return message;
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
			return null;
		}
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
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public synchronized void toggleTypingStatus() { //TODO: Use an alternative to a direct thread
		isTyping.set(!isTyping.get());

		if (isTyping.get()) {
			typingTimer.set(System.currentTimeMillis()-TIME_FOR_TYPE_STATUS);
			new Thread(()->{
				while (isTyping.get()) {
					if (typingTimer.get() <= System.currentTimeMillis()-TIME_FOR_TYPE_STATUS) {
						typingTimer.set(System.currentTimeMillis());
						try {
							Requests.POST.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/typing",
									new BasicNameValuePair("authorization", client.getToken()));
						} catch (HTTP429Exception | DiscordException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}

	@Override
	public synchronized boolean getTypingStatus() {
		return isTyping.get();
	}

	@Override
	public String getLastReadMessageID() {
		return lastReadMessageID;
	}

	@Override
	public IMessage getLastReadMessage() {
		return getMessageByID(lastReadMessageID);
	}

	@Override//TODO: make private
	public void edit(Optional<String> name, Optional<Integer> position, Optional<String> topic) throws DiscordException, MissingPermissionsException, HTTP429Exception {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		String newName = name.orElse(this.name);
		int newPosition = position.orElse(this.position);
		String newTopic = topic.orElse(this.topic);

		if (newName == null || newName.length() < 2 || newName.length() > 100)
			throw new DiscordException("Channel name can only be between 2 and 100 characters!");

		try {
			Requests.PATCH.makeRequest(DiscordEndpoints.CHANNELS+id,
					new StringEntity(DiscordUtils.GSON.toJson(new ChannelEditRequest(newName, newPosition, newTopic))),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
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

	/**
	 * Sets the CACHED last read message id.
	 *
	 * @param lastReadMessageID The message id.
	 */
	public void setLastReadMessageID(String lastReadMessageID) {
		this.lastReadMessageID = lastReadMessageID;
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
		List<IRole> roles = user.getRolesForGuild(parent.getID());
		EnumSet<Permissions> permissions = EnumSet.noneOf(Permissions.class);

		roles.stream()
				.map(r->getModifiedPermissions(r))
				.reduce(Lambdas.enumSetReduction())
				.get().stream()
				.filter(p->!permissions.contains(p))
				.forEach(p->permissions.add(p));

		PermissionOverride override = getUserOverrides().get(user.getID());
		if (override == null)
			return permissions;

		for (Permissions permission : override.allow()) {
			permissions.add(permission);
		}
		for (Permissions permission : override.deny()) {
			permissions.remove(permission);
		}

		return permissions;
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		EnumSet<Permissions> base = role.getPermissions();
		PermissionOverride override = getRoleOverrides().get(role.getID());

		if (override == null)
			return base;

		for (Permissions permission : override.allow()) {
			base.add(permission);
		}
		for (Permissions permission : override.deny()) {
			base.remove(permission);
		}

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
	public void removePermissionsOverride(String id) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+id,
				new BasicNameValuePair("authorization", client.getToken()));
		if (roleOverrides.containsKey(id)) {
			roleOverrides.remove(id);
		} else {
			userOverrides.remove(id);
		}
	}

	@Override
	public void overrideRolePermissions(String roleID, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		overridePermissions("role", roleID, toAdd, toRemove);
	}

	@Override
	public void overrideUserPermissions(String userID, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		overridePermissions("member", userID, toAdd, toRemove);
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
			e.printStackTrace();
		}
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
		return this.getClass().isAssignableFrom(other.getClass()) && ((IChannel) other).getID().equals(getID());
	}
}
