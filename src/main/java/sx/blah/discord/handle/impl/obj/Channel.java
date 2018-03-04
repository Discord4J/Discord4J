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
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.*;
import sx.blah.discord.api.internal.json.requests.*;
import sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.webhook.WebhookUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import java.io.*;
import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The default implementation of {@link IChannel}.
 */
public class Channel implements IChannel {

	/**
	 * The number of messages to fetch from Discord per message history request.
	 */
	public static final int MESSAGE_CHUNK_COUNT = 100; //100 is the max amount discord lets you retrieve at one time

	/**
	 * The name of the channel.
	 */
	protected volatile String name;

	/**
	 * The unique snowflake ID of the channel.
	 */
	protected final long id;

	/**
	 * The cached messages that have been sent in the channel.
	 */
	public final Cache<IMessage> messages;

	/**
	 * The parent guild of the channel.
	 */
	protected final IGuild guild;

	/**
	 * The channel's topic message.
	 */
	protected volatile String topic;

	/**
	 * Holds a reference to the task responsible for maintaining typing status.
	 */
	private AtomicReference<TimerTask> typingTask = new AtomicReference<>(null);

	/**
	 * Manages all TimerTasks which send typing statuses.
	 */
	protected static final Timer typingTimer = new Timer("Typing Status Timer", true);

	/**
	 * The period of time, in seconds, before another typing update must be sent to maintain typing status.
	 */
	protected static final long TIME_FOR_TYPE_STATUS = 10000;

	/**
	 * The position of the channel in the channel list.
	 */
	protected volatile int position;

	/**
	 * The permission overrides for users.
	 */
	public final Cache<sx.blah.discord.handle.obj.PermissionOverride> userOverrides;

	/**
	 * The permission overrides for roles.
	 */
	public final Cache<sx.blah.discord.handle.obj.PermissionOverride> roleOverrides;

	/**
	 * The webhooks for the channel.
	 */
	protected final Cache<IWebhook> webhooks;

	/**
	 * Whether the channel is nsfw.
	 */
	protected boolean isNSFW;

	protected volatile long categoryID;

	/**
	 * The client that owns the channel object.
	 */
	protected final DiscordClientImpl client;

	public Channel(DiscordClientImpl client, String name, long id, IGuild guild, String topic, int position, boolean isNSFW, long categoryID,
				   Cache<sx.blah.discord.handle.obj.PermissionOverride> roleOverrides,
				   Cache<sx.blah.discord.handle.obj.PermissionOverride> userOverrides) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.guild = guild;
		this.topic = topic;
		this.position = position;
		this.roleOverrides = roleOverrides;
		this.userOverrides = userOverrides;
		this.isNSFW = isNSFW;
		this.messages = new Cache<>(client, IMessage.class);
		this.webhooks = new Cache<>(client, IWebhook.class);
		this.categoryID = categoryID;
	}


	@Override
	public String getName() {
		return name;
	}

	/**
	 * Sets the CACHED name of the channel.
	 *
	 * @param name The name of the channel.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getLongID() {
		return id;
	}

	/**
	 * Adds a message to the internal message CACHE.
	 *
	 * @param message The message to add.
	 */
	public void addToCache(IMessage message) {
		if (getMaxInternalCacheCount() < 0) {
			messages.put(message);
		} else if (getMaxInternalCacheCount() != 0) {
			if (getInternalCacheCount() == getMaxInternalCacheCount()) {
				messages.remove(messages.longIDs().stream().mapToLong(it -> it).min().getAsLong()); //Lowest id should be the earliest
			}

			messages.put(message);
		}
	}

	/**
	 * Makes a request to Discord for message history.
	 *
	 * @param before The ID of the message to get message history before.
	 * @param limit The maximum number of messages to request.
	 * @return The received messages.
	 */
	private IMessage[] getHistory(long before, int limit) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.READ_MESSAGES);

		String query = "?before=" + Long.toUnsignedString(before) + "&limit=" + limit;
		MessageObject[] messages = client.REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + getStringID() + "/messages" + query,
				MessageObject[].class);

		return Arrays.stream(messages).map(m -> DiscordUtils.getMessageFromJSON(this, m)).toArray(IMessage[]::new);
	}

	@Override
	public MessageHistory getMessageHistory() {
		return new MessageHistory(messages.values());
	}

	@Override
	public MessageHistory getMessageHistory(int messageCount) {
		if (messageCount <= messages.size()) { // we already have all of the wanted messages in the cache
			return new MessageHistory(messages.values().stream()
				.sorted(new MessageComparator(true))
				.limit(messageCount)
				.collect(Collectors.toList()));
		} else {
			List<IMessage> retrieved = new ArrayList<>(messageCount);
			AtomicLong lastMessage = new AtomicLong(DiscordUtils.getSnowflakeFromTimestamp(Instant.now()));
			int chunkSize = messageCount < MESSAGE_CHUNK_COUNT ? messageCount : MESSAGE_CHUNK_COUNT;

			while (retrieved.size() < messageCount) { // while we dont have messageCount messages
				IMessage[] chunk = getHistory(lastMessage.get(), chunkSize);

				if (chunk.length == 0) break;

				lastMessage.set(chunk[chunk.length - 1].getLongID());
				Collections.addAll(retrieved, chunk);
			}

			return new MessageHistory(retrieved.size() > messageCount ? retrieved.subList(0, messageCount) : retrieved);
		}
	}

	@Override
	public MessageHistory getMessageHistoryFrom(Instant startDate) {
		return getMessageHistoryFrom(startDate, Integer.MAX_VALUE);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(Instant startDate, int maxCount) {
		return getMessageHistoryFrom(DiscordUtils.getSnowflakeFromTimestamp(startDate), maxCount);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id) {
		return getMessageHistoryFrom(id, Integer.MAX_VALUE);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id, int maxCount) {
		return getMessageHistoryIn(id, DiscordUtils.getSnowflakeFromTimestamp(getCreationDate()), maxCount);
	}

	@Override
	public MessageHistory getMessageHistoryTo(Instant endDate) {
		return getMessageHistoryTo(endDate, Integer.MAX_VALUE);
	}

	@Override
	public MessageHistory getMessageHistoryTo(Instant endDate, int maxCount) {
		return getMessageHistoryTo(DiscordUtils.getSnowflakeFromTimestamp(endDate), maxCount);
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id) {
		return getMessageHistoryTo(id, Integer.MAX_VALUE);
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id, int maxCount) {
		return getMessageHistoryIn(DiscordUtils.getSnowflakeFromTimestamp(Instant.now()), id, maxCount);
	}

	@Override
	public MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate) {
		return getMessageHistoryIn(startDate, endDate, Integer.MAX_VALUE);
	}

	@Override
	public MessageHistory getMessageHistoryIn(Instant startDate, Instant endDate, int maxCount) {
		return getMessageHistoryIn(DiscordUtils.getSnowflakeFromTimestamp(startDate),
				DiscordUtils.getSnowflakeFromTimestamp(endDate), maxCount);
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID) {
		return getMessageHistoryIn(beginID, endID, Integer.MAX_VALUE);
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID, int maxCount) {
		final List<IMessage> history = new ArrayList<>();
		final int originalMaxCount = maxCount;
		// Adds 1L so beginID will be included
		long previousMessageID = beginID + 1L;
		int added = -1;

		while ((history.size() < originalMaxCount) && (added != 0)) {
			maxCount = originalMaxCount - history.size();
			final int chunkSize = (maxCount < MESSAGE_CHUNK_COUNT) ? maxCount : MESSAGE_CHUNK_COUNT;
			final IMessage[] chunk = getHistory(previousMessageID, chunkSize);
			added = 0;

			for (final IMessage message : chunk) {
				if (message.getLongID() >= endID) {
					// We want to EXCLUDE previous messages later
					previousMessageID = message.getLongID() - 1L;
					history.add(message);
					added++;
				} else { // We don't need anything else
					return new MessageHistory(history);
				}
			}
		}

		return new MessageHistory(history);
	}

	@Override
	public MessageHistory getFullMessageHistory() {
		return getMessageHistoryTo(getCreationDate());
	}

	@Override
	public List<IMessage> bulkDelete() {
		return bulkDelete(getMessageHistoryTo(Instant.now().minus(Period.ofWeeks(2))));
	}

	@Override
	public List<IMessage> bulkDelete(List<IMessage> messages) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_MESSAGES);

		if (isPrivate())
			throw new UnsupportedOperationException("Cannot bulk delete in private channels!");

		if (messages.size() == 1) { //Skip further processing if only one message was provided
			messages.get(0).delete();
			return messages;
		}

		List<IMessage> toDelete = messages.stream()
				.filter(msg -> msg.getLongID() >= (((System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000) - 1420070400000L) << 22)) // Taken from Jake
				.distinct()
				.collect(Collectors.toList());

		if (toDelete.size() < 1)
			throw new DiscordException("Must provide at least 1 valid message to delete.");

		if (toDelete.size() == 1) { //Bulk delete is no longer valid, time for normal delete.
			toDelete.get(0).delete();
			return toDelete;
		} else if (toDelete.size() > 100) { //Above the max limit, time to create a sublist
			Discord4J.LOGGER.warn(LogMarkers.HANDLE, "More than 100 messages requested to be bulk deleted! Bulk deleting only the first 100...");
			toDelete = toDelete.subList(0, 100);
		}

		client.REQUESTS.POST.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/messages/bulk-delete",
				new BulkDeleteRequest(toDelete));

		return toDelete;
	}

	@Override
	public int getMaxInternalCacheCount() {
		return client.getMaxCacheCount();
	}

	@Override
	public int getInternalCacheCount() {
		synchronized (messages) {
			return messages.size();
		}
	}

	@Override
	public IMessage getMessageByID(long messageID) {
		return messages.get(messageID);
	}

	@Override
	public IMessage fetchMessage(long messageID) {
		return messages.getOrElseGet(messageID, () -> {
			PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY);
			return RequestBuffer.request(() ->
					(IMessage) DiscordUtils.getMessageFromJSON(this, client.REQUESTS.GET.makeRequest(
							DiscordEndpoints.CHANNELS + this.getStringID() + "/messages/" + Long.toUnsignedString(messageID),
							MessageObject.class))
			).get();
		});
	}

	@Override
	public IGuild getGuild() {
		return guild;
	}

	@Override
	public boolean isPrivate() {
		return this instanceof PrivateChannel;
	}

	@Override
	public boolean isNSFW() {
		return isNSFW || DiscordUtils.NSFW_CHANNEL_PATTERN.matcher(name).find();
	}

	/**
	 * Sets the CACHED nsfw state for the channel.
	 *
	 * @param isNSFW The new channel nsfw state.
	 */
	public void setNSFW(boolean isNSFW) {
		this.isNSFW = isNSFW;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	/**
	 * Sets the CACHED topic for the channel.
	 *
	 * @param topic The channel topic.
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public String mention() {
		return "<#" + this.getStringID() + ">";
	}

	@Override
	public IMessage sendMessage(String content) {
		return sendMessage(content, false);
	}

	@Override
	public IMessage sendMessage(EmbedObject embed) {
		return sendMessage(null, embed);
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) {
		return sendMessage(content, null, tts);
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed) {
		return sendMessage(content, embed, false);
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed, boolean tts) {
		getShard().checkReady("send message");
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.SEND_MESSAGES);

		if (embed != null) {
			PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.EMBED_LINKS);
		}

		MessageObject response = null;
		try {
			response = client.REQUESTS.POST.makeRequest(
					DiscordEndpoints.CHANNELS+id+"/messages",
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new MessageRequest(content, embed, tts)),
					MessageObject.class);
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}

		if (response == null || response.id == null) //Message didn't send
			throw new DiscordException("Message was unable to be sent (Discord didn't return a response).");

		return DiscordUtils.getMessageFromJSON(this, response);
	}

	@Override
	public IMessage sendFile(File file) throws FileNotFoundException {
		return sendFile((String) null, file);
	}

	@Override
	public IMessage sendFiles(File... files) throws FileNotFoundException {
		return sendFiles((String) null, files);
	}

	@Override
	public IMessage sendFile(String content, File file) throws FileNotFoundException {
		return sendFile(content, false, new FileInputStream(file), file.getName(), null);
	}

	@Override
	public IMessage sendFiles(String content, File... files) throws FileNotFoundException {
		return sendFiles(content, false, AttachmentPartEntry.from(files));
	}

	@Override
	public IMessage sendFile(EmbedObject embed, File file) throws FileNotFoundException {
		return sendFile(null, false, new FileInputStream(file), file.getName(), embed);
	}

	@Override
	public IMessage sendFiles(EmbedObject embed, File... files) throws FileNotFoundException {
		return sendFiles(null, false, embed, AttachmentPartEntry.from(files));
	}

	@Override
	public IMessage sendFile(String content, InputStream file, String fileName) {
		return sendFile(content, false, file, fileName, null);
	}

	@Override
	public IMessage sendFiles(String content, AttachmentPartEntry... entry) {
		return sendFiles(content, false, null, entry);
	}

	@Override
	public IMessage sendFile(EmbedObject embed, InputStream file, String fileName) {
		return sendFile(null, false, file, fileName, embed);
	}

	@Override
	public IMessage sendFiles(EmbedObject embed, AttachmentPartEntry... entries) {
		return sendFiles(null, false, embed, entries);
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName) {
		return sendFile(content, tts, file, fileName, null);
	}

	@Override
	public IMessage sendFiles(String content, boolean tts, AttachmentPartEntry... entries) {
		return sendFiles(content, tts, null, entries);
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName, EmbedObject embed) {
		return sendFiles(content, tts, embed, new AttachmentPartEntry(fileName, file));
	}

	@Override
	public IMessage sendFiles(String content, boolean tts, EmbedObject embed, AttachmentPartEntry... entries) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.SEND_MESSAGES, Permissions.ATTACH_FILES);

		try {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			if (entries.length == 1) {
				builder.addBinaryBody("file", entries[0].getFileData(), ContentType.APPLICATION_OCTET_STREAM, entries[0].getFileName());
			} else {
				for (int i = 0; i < entries.length; i++) {
					builder.addBinaryBody("file" + i, entries[i].getFileData(), ContentType.APPLICATION_OCTET_STREAM, entries[i].getFileName());
				}
			}

			builder.addTextBody("payload_json", DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(new FilePayloadObject(content, tts, embed)),
						ContentType.MULTIPART_FORM_DATA.withCharset("UTF-8"));

			HttpEntity fileEntity = builder.build();
			MessageObject messageObject = DiscordUtils.MAPPER.readValue(client.REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS + id + "/messages",
					fileEntity, new BasicNameValuePair("Content-Type", "multipart/form-data")), MessageObject.class);

			return DiscordUtils.getMessageFromJSON(this, messageObject);
		} catch (IOException e) {
			throw new DiscordException("JSON Parsing exception!", e);
		}
	}

	@Override
	public IMessage sendFile(MessageBuilder builder, InputStream file, String fileName) {
		return sendFile(builder.getContent() != null && builder.getContent().isEmpty() ? null : builder.getContent(),
				builder.isUsingTTS(), file, fileName, builder.getEmbedObject());
	}

	@Override
	public IExtendedInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique) {
		getShard().checkReady("create invite");
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.CREATE_INVITE);

		ExtendedInviteObject response = (client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.CHANNELS+getStringID()+"/invites",
				new InviteCreateRequest(maxAge, maxUses, temporary, unique),
				ExtendedInviteObject.class);

		return DiscordUtils.getExtendedInviteFromJSON(client, response);
	}

	@Override
	public synchronized void toggleTypingStatus() {
		setTypingStatus(!getTypingStatus());
	}

	@Override
	public void setTypingStatus(boolean typing) {
		if (typing) {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					if (!isPrivate() && isDeleted()) {
						this.cancel();
						return;
					}
					try {
						Discord4J.LOGGER.trace(LogMarkers.HANDLE, "Sending TypingStatus Keep Alive");
						((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS + getLongID() + "/typing");
					} catch (RateLimitException | DiscordException e) {
						Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
					}
				}
			};

			if (typingTask.compareAndSet(null, task)) {
				typingTimer.scheduleAtFixedRate(task, 0, TIME_FOR_TYPE_STATUS);
			}
		} else {
			TimerTask oldTask = typingTask.getAndSet(null);
			if (oldTask != null) {
				oldTask.cancel();
			}
		}
	}

	@Override
	public synchronized boolean getTypingStatus() {
		return typingTask.get() != null;
	}

	/**
	 * Sends a request to edit the channel.
	 *
	 * @param request The request object describing the changes to make.
	 */
	private void edit(ChannelEditRequest request) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS);

		try {
			client.REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.CHANNELS + id,
					DiscordUtils.MAPPER.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void edit(String name, int position, String topic) {
		if (name == null || !DiscordUtils.CHANNEL_NAME_PATTERN.matcher(name).matches())
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric OR non-ASCII characters.");

		edit(new ChannelEditRequest.Builder().name(name).position(position).topic(topic).build());
	}

	@Override
	public void changeName(String name) {
		if (name == null || !DiscordUtils.CHANNEL_NAME_PATTERN.matcher(name).matches())
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric OR non-ASCII characters.");

		edit(new ChannelEditRequest.Builder().name(name).build());
	}

	@Override
	public void changePosition(int position) {
		edit(new ChannelEditRequest.Builder().position(position).build());
	}

	@Override
	public void changeTopic(String topic) {
		edit(new ChannelEditRequest.Builder().topic(topic).build());
	}

	@Override
	public void changeNSFW(boolean isNSFW) {
		edit(new ChannelEditRequest.Builder().nsfw(isNSFW).build());
	}

	@Override
	public int getPosition() {
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
	public void delete() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNELS);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+id);
	}

	@Override
	public LongMap<sx.blah.discord.handle.obj.PermissionOverride> getUserOverrides() {
		return userOverrides.mapCopy();
	}

	@Override
	public LongMap<sx.blah.discord.handle.obj.PermissionOverride> getRoleOverrides() {
		return roleOverrides.mapCopy();
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		return PermissionUtils.getModifiedPermissions(user, guild, userOverrides, roleOverrides);
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		return PermissionUtils.getModifiedPermissions(role, roleOverrides);
	}

	@Override
	public void removePermissionsOverride(IUser user) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_PERMISSIONS);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+user.getStringID());

		userOverrides.remove(user.getLongID());
	}

	@Override
	public void removePermissionsOverride(IRole role) {
		PermissionUtils.requireHierarchicalPermissions(this, client.getOurUser(), Collections.singletonList(role), Permissions.MANAGE_PERMISSIONS);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+role.getStringID());

		roleOverrides.remove(role.getLongID());
	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		overridePermissions("role", role.getStringID(), toAdd, toRemove);
	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		overridePermissions("member", user.getStringID(), toAdd, toRemove);
	}

	/**
	 * Makes a request to Discord to override permissions for a role or member.
	 *
	 * @param type The type of override to make. Either "role" or "member".
	 * @param id The ID of the role or member to make the override for.
	 * @param toAdd The permissions to explicitly allow.
	 * @param toRemove The permissions to explicitly deny.
	 */
	private void overridePermissions(String type, String id, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_PERMISSIONS);

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(
				DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+id,
				new OverwriteObject(type, null, Permissions.generatePermissionsNumber(toAdd), Permissions.generatePermissionsNumber(toRemove)));
	}

	@Override
	public List<IExtendedInvite> getExtendedInvites() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNEL);
		ExtendedInviteObject[] response = client.REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/invites",
				ExtendedInviteObject[].class);

		List<IExtendedInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getExtendedInviteFromJSON(client, inviteResponse));

		return invites;
	}

	@Override
	public List<IUser> getUsersHere() {
		return guild.getUsers().stream().filter((user) -> {
			EnumSet<Permissions> permissions = getModifiedPermissions(user);
			return Permissions.READ_MESSAGES.hasPermission(Permissions.generatePermissionsNumber(permissions), true);
		}).collect(Collectors.toList());
	}

	@Override
	public List<IMessage> getPinnedMessages() {
		List<IMessage> messages = new ArrayList<>();
		MessageObject[] pinnedMessages = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/pins",
				MessageObject[].class);

		for (MessageObject message : pinnedMessages)
			messages.add(DiscordUtils.getMessageFromJSON(this, message));

		return messages;
	}

	@Override
	public void pin(IMessage message) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_MESSAGES);

		if (!message.getChannel().equals(this))
			throw new DiscordException("Message channel doesn't match current channel!");

		if (message.isPinned())
			throw new DiscordException("Message already pinned!");

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.CHANNELS + id + "/pins/" + message.getStringID());
	}

	@Override
	public void unpin(IMessage message) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_MESSAGES);

		if (!message.getChannel().equals(this))
			throw new DiscordException("Message channel doesn't match current channel!");

		if (!message.isPinned())
			throw new DiscordException("Message is not pinned!");

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS + id + "/pins/" + message.getStringID());
	}

	@Override
	public List<IWebhook> getWebhooks() {
		return new LinkedList<>(webhooks.values());
	}

	@Override
	public IWebhook getWebhookByID(long id) {
		return webhooks.get(id);
	}

	@Override
	public List<IWebhook> getWebhooksByName(String name) {
		return webhooks.stream()
				.filter(w -> w.getDefaultName().equals(name))
				.collect(Collectors.toList());
	}

	@Override
	public IWebhook createWebhook(String name) {
		return createWebhook(name, Image.defaultAvatar());
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) {
		return createWebhook(name, avatar.getData());
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) {
		getShard().checkReady("create webhook");
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_WEBHOOKS);

		if (name == null || name.length() < 2 || name.length() > 32)
			throw new DiscordException("Webhook name can only be between 2 and 32 characters!");

		WebhookObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.CHANNELS + getStringID() + "/webhooks",
				new WebhookCreateRequest(name, avatar),
				WebhookObject.class);

		IWebhook webhook = DiscordUtils.getWebhookFromJSON(this, response);
		webhooks.put(webhook);

		return webhook;
	}

	/**
	 * Forcibly loads and caches all webhooks for the channel.
	 */
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
						DiscordEndpoints.CHANNELS + getStringID() + "/webhooks",
						WebhookObject[].class);

				if (response != null) {
					for (WebhookObject webhookObject : response) {
						long webhookId = Long.parseUnsignedLong(webhookObject.id);
						if (getWebhookByID(webhookId) == null) {
							IWebhook newWebhook = DiscordUtils.getWebhookFromJSON(this, webhookObject);
							client.getDispatcher().dispatch(new WebhookCreateEvent(newWebhook));
							webhooks.put(newWebhook);
						} else {
							IWebhook toUpdate = getWebhookByID(webhookId);
							IWebhook oldWebhook = toUpdate.copy();
							toUpdate = DiscordUtils.getWebhookFromJSON(this, webhookObject);
							if (!oldWebhook.getDefaultName().equals(toUpdate.getDefaultName()) || !String.valueOf(oldWebhook.getDefaultAvatar()).equals(String.valueOf(toUpdate.getDefaultAvatar())))
								client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, toUpdate));

							oldList.remove(oldWebhook);
						}
					}
				}

				oldList.forEach(webhook -> {
					webhooks.remove(webhook);
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
	public void changeCategory(ICategory category) {
		PermissionUtils.requirePermissions(this, getClient().getOurUser(), Permissions.MANAGE_CHANNELS);

		Long id = category == null ? null : category.getLongID();
		edit(new ChannelEditRequest.Builder().parentID(id).build());
	}

	@Override
	public ICategory getCategory() {
		if (categoryID == 0L) {
			return null;
		}

		return getGuild().getCategoryByID(categoryID);
	}

	public void setCategoryID(long categoryId) {
		this.categoryID = categoryId;
	}

	@Override
	public IChannel copy() {
		Channel channel = new Channel(client, name, id, guild, topic, position, isNSFW, categoryID,
				new Cache<>(client, sx.blah.discord.handle.obj.PermissionOverride.class),
				new Cache<>(client, sx.blah.discord.handle.obj.PermissionOverride.class));
		channel.typingTask.set(typingTask.get());
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
		return DiscordUtils.equals(this, other);
	}
}
