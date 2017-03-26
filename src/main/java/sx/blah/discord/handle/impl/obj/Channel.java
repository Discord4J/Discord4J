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
import sx.blah.discord.handle.impl.events.WebhookCreateEvent;
import sx.blah.discord.handle.impl.events.WebhookDeleteEvent;
import sx.blah.discord.handle.impl.events.WebhookUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Channel implements IChannel {

	/**
	 * This represents the amount of messages to fetch from discord every time the index goes out of bounds.
	 */
	public static final int MESSAGE_CHUNK_COUNT = 100; //100 is the max amount discord lets you retrieve at one time

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
	public final ConcurrentLinkedDeque<IMessage> messages = new ConcurrentLinkedDeque<>();

	/**
	 * The guild this channel belongs to.
	 */
	protected final IGuild guild;

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
	protected final DiscordClientImpl client;

	public Channel(DiscordClientImpl client, String name, String id, IGuild guild, String topic, int position, Map<String, PermissionOverride> roleOverrides, Map<String, PermissionOverride> userOverrides) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.guild = guild;
		this.topic = topic;
		this.position = position;
		this.roleOverrides = roleOverrides;
		this.userOverrides = userOverrides;
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
	@Deprecated
	public MessageList getMessages() {
		return new MessageList(client, this);
	}

	/**
	 * Adds a message to the internal message CACHE.
	 *
	 * @param message the message.
	 */
	public void addToCache(IMessage message) {
		if (getMaxInternalCacheCount() < 0) {
			messages.addFirst(message);
		} else if (getMaxInternalCacheCount() != 0) {
			if (getInternalCacheCount() == getMaxInternalCacheCount())
				messages.removeLast(); //At max so we need to make room

			messages.addFirst(message);
		}
	}

	private IMessage[] requestHistory(String before, int limit) {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY));

		String queryParams = "?limit=" + limit;

		if (before != null) {
			queryParams += "&before=" + before;
		}
//		} else if (around != null) {
//			queryParams += "&around="+around;
//		} else if (after != null) {
//			queryParams += "&after="+after;
//		}

		MessageObject[] messages = client.REQUESTS.GET.makeRequest(DiscordEndpoints.CHANNELS + id + "/messages" + queryParams, MessageObject[].class);

		if (messages.length == 0) {
			return new IMessage[0];
		}

		IMessage[] messageObjs = new IMessage[messages.length];

		for (int i = 0; i < messages.length; i++) {
			messageObjs[i] = DiscordUtils.getMessageFromJSON(this, messages[i]);
		}

		return messageObjs;
	}

	@Override
	public MessageHistory getMessageHistory() {
		return new MessageHistory(messages);
	}

	private Collection<IMessage> subDeque(int from, int end) {
		List<IMessage> list = new ArrayList<>();
		if (from >= 0 || end < from) { //Skip this step if the indexes are invalid
			for (int i = from; i < end; i++)
				list.add((IMessage) messages.toArray()[i]);
		}
		return list;
	}

	@Override
	public MessageHistory getMessageHistory(int messageCount) {
		if (messageCount <= messages.size())
			return new MessageHistory(subDeque(0, messageCount));
		else {
			final AtomicInteger remaining = new AtomicInteger(messageCount - messages.size());
			final List<IMessage> retrieved = new ArrayList<>(messages);
			while (remaining.get() > 0) {
				RequestBuffer.request(() -> {
					int requestCount = Math.min(remaining.get(), MESSAGE_CHUNK_COUNT);
					IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null, requestCount);

					if (requestCount != chunk.length)
						remaining.set(0); //Got all possible messages already
					else
						remaining.addAndGet(-chunk.length);

					retrieved.addAll(Arrays.asList(chunk));
				}).get();
			}

			return new MessageHistory(retrieved);
		}
	}

	@Override
	public MessageHistory getMessageHistoryFrom(LocalDateTime startDate) {
		return getMessageHistoryFrom(startDate, -1);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(LocalDateTime startDate, int maxCount) {
		final List<IMessage> retrieved = new ArrayList<>(messages.stream()
				.filter(msg -> msg.getTimestamp().compareTo(startDate) <= 0)
				.collect(Collectors.toList()));

		final AtomicReference<String> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null);
		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

				List<IMessage> toAdd = Arrays.stream(chunk)
						.filter(msg -> msg.getTimestamp().compareTo(startDate) <= 0)
						.collect(Collectors.toList());

				retrieved.addAll(toAdd);

				if (chunk.length > 0)
					lastID.set(chunk[chunk.length-1].getID());

				return chunk.length != MESSAGE_CHUNK_COUNT || (maxCount > 0 && retrieved.size() >= maxCount);//Done when the messages retrieved are not matching the requested count
			}).get())
				break;
		}

		if (maxCount > 0)
			return new MessageHistory(retrieved.subList(0, Math.min(retrieved.size(), maxCount)));
		else
			return new MessageHistory(retrieved);
	}

	@Override
	public MessageHistory getMessageHistoryTo(LocalDateTime endDate) {
		return getMessageHistoryTo(endDate, -1);
	}

	@Override
	public MessageHistory getMessageHistoryTo(LocalDateTime endDate, int maxCount) {
		final List<IMessage> retrieved = new ArrayList<>(messages.stream()
				.filter(msg -> msg.getTimestamp().compareTo(endDate) >= 0)
				.collect(Collectors.toList()));

		if (messages.size() == retrieved.size()) { //All elements were copied over, meaning that we're likely not done finding messages
			while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
				if (RequestBuffer.request(() -> {
					IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null, MESSAGE_CHUNK_COUNT);

					List<IMessage> toAdd = Arrays.stream(chunk)
							.filter(msg -> msg.getTimestamp().compareTo(endDate) >= 0)
							.collect(Collectors.toList());

					retrieved.addAll(toAdd);

					return toAdd.size() != chunk.length || chunk.length != MESSAGE_CHUNK_COUNT || (maxCount > 0 && retrieved.size() >= maxCount); //We reached the end of the history or we reached the specified end date
				}).get())
					break;
			}
		}

		if (maxCount > 0)
			return new MessageHistory(retrieved.subList(0, Math.min(retrieved.size(), maxCount)));
		else
			return new MessageHistory(retrieved);
	}

	@Override
	public MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate) {
		return getMessageHistoryIn(startDate, endDate, -1);
	}

	@Override
	public MessageHistory getMessageHistoryIn(LocalDateTime startDate, LocalDateTime endDate, int maxCount) {
		final List<IMessage> retrieved = new ArrayList<>(messages.stream()
				.filter(msg -> msg.getTimestamp().compareTo(startDate) >= 0 && msg.getTimestamp().compareTo(endDate) <= 0)
				.collect(Collectors.toList()));

		final AtomicReference<String> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null);

		if (((IMessage) messages.toArray()[messages.size()-1]).getTimestamp().compareTo(endDate) <= 0) { //When the last message cached matches the criteria there may still be more in history
			while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
				if (RequestBuffer.request(() -> {
					IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

					List<IMessage> toAdd = Arrays.stream(chunk)
							.filter(msg -> msg.getTimestamp().compareTo(startDate) >= 0 && msg.getTimestamp().compareTo(endDate) <= 0)
							.collect(Collectors.toList());

					retrieved.addAll(toAdd);

					if (chunk.length > 0)
						lastID.set(chunk[chunk.length-1].getID());

					return toAdd.size() != chunk.length || chunk.length != MESSAGE_CHUNK_COUNT || (maxCount > 0 && retrieved.size() >= maxCount); //We reached the end of the history or we reached the specified end date
				}).get())
					break;
			}
		}

		if (maxCount > 0)
			return new MessageHistory(retrieved.subList(0, Math.min(retrieved.size(), maxCount)));
		else
			return new MessageHistory(retrieved);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(String id) {
		return getMessageHistoryFrom(id, -1);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(String id, int maxCount) {
		int index = -1;
		for (int i = 0; i < messages.size(); i++) {
			if (((IMessage) messages.toArray()[i]).getID().equals(id)) {
				index = i;
				break;
			}
		}

		final List<IMessage> retrieved = new ArrayList<>(subDeque(index, messages.size()));

		if (index == -1)
			retrieved.add(RequestBuffer.request(() -> {return getMessageByID(id);}).get()); //Ignore intellij on this line, the return statement is required for the IRequest to not resolve to an IVoidRequest

		final AtomicReference<String> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null);
		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

				retrieved.addAll(Arrays.asList(chunk));

				if (chunk.length > 0)
					lastID.set(chunk[chunk.length-1].getID());

				return chunk.length != MESSAGE_CHUNK_COUNT || (maxCount > 0 && retrieved.size() >= maxCount); //We reached the end of the history or we reached the specified end date
			}).get())
				break;
		}

		if (maxCount > 0)
			return new MessageHistory(retrieved.subList(0, Math.min(retrieved.size(), maxCount)));
		else
			return new MessageHistory(retrieved);
	}

	@Override
	public MessageHistory getMessageHistoryTo(String id) {
		return getMessageHistoryTo(id, -1);
	}

	@Override
	public MessageHistory getMessageHistoryTo(String id, int maxCount) {
		final List<IMessage> retrieved = new ArrayList<>();

		for (IMessage message : messages) {
			retrieved.add(message);
			if (message.getID().equals(id))
				return new MessageHistory(retrieved); //Let's end early since we reached the target
		}

		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null, MESSAGE_CHUNK_COUNT);

				for (IMessage message : chunk) {
					retrieved.add(message);
					if (message.getID().equals(id))
						return true; //Finish early
				}

				return chunk.length != MESSAGE_CHUNK_COUNT || (maxCount > 0 && retrieved.size() >= maxCount); //We reached the end of the history or we reached the specified end date
			}).get())
				break;
		}

		if (maxCount > 0)
			return new MessageHistory(retrieved.subList(0, Math.min(retrieved.size(), maxCount)));
		else
			return new MessageHistory(retrieved);
	}

	@Override
	public MessageHistory getMessageHistoryIn(String beginID, String endID) {
		return getMessageHistoryIn(beginID, endID, -1);
	}

	@Override
	public MessageHistory getMessageHistoryIn(String beginID, String endID, int maxCount) {
		int startIndex = -1;
		for (int i = 0; i < messages.size(); i++) {
			if (((IMessage) messages.toArray()[i]).getID().equals(id)) {
				startIndex = i;
				break;
			}
		}

		final List<IMessage> retrieved = new ArrayList<>(subDeque(startIndex, messages.size()));

		if (startIndex == -1)
			retrieved.add(RequestBuffer.request(() -> {return getMessageByID(id);}).get()); //Ignore intellij on this line, the return statement is required for the IRequest to not resolve to an IVoidRequest

		final AtomicReference<String> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null);

		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

				for (IMessage message : chunk) {
					retrieved.add(message);
					if (message.getID().equals(id))
						return true; //Finish early
				}

				if (chunk.length > 0)
					lastID.set(chunk[chunk.length-1].getID());

				return chunk.length != MESSAGE_CHUNK_COUNT || (maxCount > 0 && retrieved.size() >= maxCount); //We reached the end of the history or we reached the specified end date
			}).get())
				break;
		}

		if (maxCount > 0)
			return new MessageHistory(retrieved.subList(0, Math.min(retrieved.size(), maxCount)));
		else
			return new MessageHistory(retrieved);
	}

	@Override
	public MessageHistory getFullMessageHistory() {
		final List<IMessage> retrieved = new ArrayList<>(messages);

		while (true) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getID() : null, MESSAGE_CHUNK_COUNT);

				retrieved.addAll(Arrays.asList(chunk));

				return chunk.length != MESSAGE_CHUNK_COUNT; //We reached the end of the history or we reached the specified end date
			}).get())
				break;
		}

		return new MessageHistory(retrieved);
	}

	@Override
	public List<IMessage> bulkDelete() {
		return bulkDelete(getMessageHistoryTo(LocalDateTime.now().minusWeeks(2)));
	}

	@Override
	public List<IMessage> bulkDelete(List<IMessage> messages) {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_MESSAGES));

		if (isPrivate())
			throw new UnsupportedOperationException("Cannot bulk delete in private channels!");

		if (messages.size() == 1) { //Skip further processing if only one message was provided
			messages.get(0).delete();
			return messages;
		}

		List<IMessage> toDelete = messages.stream()
				.filter(msg -> Long.parseLong(msg.getID()) >= (((System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000) - 1420070400000L) << 22)) // Taken from Jake
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
	public IMessage getMessageByID(String messageID) {
		if (messageID == null)
			return null;

		return messages.stream()
				.filter(msg -> msg.getID().equals(messageID))
				.findAny()
				.orElseGet(() -> {
					try {
						DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY));

						return DiscordUtils.getMessageFromJSON(this, client.REQUESTS.GET.makeRequest(
								DiscordEndpoints.CHANNELS + this.getID() + "/messages/" + messageID,
								MessageObject.class));
					} catch (Exception ignored) {
						return null;
					}
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
	public IMessage sendMessage(String content) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendMessage(content, false);
	}

	@Override
	public IMessage sendMessage(EmbedObject embed) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendMessage(null, embed);
	}

	@Override
	public IMessage sendMessage(String content, boolean tts) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendMessage(content, null, tts);
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendMessage(content, embed, false);
	}

	@Override
	public IMessage sendMessage(String content, EmbedObject embed, boolean tts) throws DiscordException, RateLimitException, MissingPermissionsException {
		getShard().checkReady("send message");
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.SEND_MESSAGES));

		if (embed != null) {
			DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.EMBED_LINKS));
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
	public IMessage sendFile(File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile((String) null, file);
	}

	@Override
	public IMessage sendFiles(File... files) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles((String) null, files);
	}

	@Override
	public IMessage sendFile(String content, File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile(content, false, new FileInputStream(file), file.getName(), null);
	}

	@Override
	public IMessage sendFiles(String content, File... files) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles(content, false, AttachmentPartEntry.from(files));
	}

	@Override
	public IMessage sendFile(EmbedObject embed, File file) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile(null, false, new FileInputStream(file), file.getName(), embed);
	}

	@Override
	public IMessage sendFiles(EmbedObject embed, File... files) throws FileNotFoundException, DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles(null, false, embed, AttachmentPartEntry.from(files));
	}

	@Override
	public IMessage sendFile(String content, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile(content, false, file, fileName, null);
	}

	@Override
	public IMessage sendFiles(String content, AttachmentPartEntry... entry) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles(content, false, null, entry);
	}

	@Override
	public IMessage sendFile(EmbedObject embed, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile(null, false, file, fileName, embed);
	}

	@Override
	public IMessage sendFiles(EmbedObject embed, AttachmentPartEntry... entries) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles(null, false, embed, entries);
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFile(content, tts, file, fileName, null);
	}

	@Override
	public IMessage sendFiles(String content, boolean tts, AttachmentPartEntry... entries) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles(content, tts, null, entries);
	}

	@Override
	public IMessage sendFile(String content, boolean tts, InputStream file, String fileName, EmbedObject embed) throws DiscordException, RateLimitException, MissingPermissionsException {
		return sendFiles(content, tts, embed, new AttachmentPartEntry(fileName, file));
	}

	@Override
	public IMessage sendFiles(String content, boolean tts, EmbedObject embed, AttachmentPartEntry... entries) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(getClient(), this, EnumSet.of(Permissions.SEND_MESSAGES, Permissions.ATTACH_FILES));

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
	public IMessage sendFile(MessageBuilder builder, InputStream file, String fileName) throws DiscordException,
			RateLimitException, MissingPermissionsException {
		return sendFile(builder.getContent() != null && builder.getContent().isEmpty() ? null : builder.getContent(),
				builder.isUsingTTS(), file, fileName, builder.getEmbedObject());
	}

	@Override
	public IInvite createInvite(int maxAge, int maxUses, boolean temporary, boolean unique) throws DiscordException, RateLimitException, MissingPermissionsException {
		getShard().checkReady("create invite");
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.CREATE_INVITE));

		ExtendedInviteObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.CHANNELS+getID()+"/invites",
				new InviteCreateRequest(maxAge, maxUses, temporary, unique),
				ExtendedInviteObject.class);

		return DiscordUtils.getInviteFromJSON(client, response);
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
					if (!isTyping.get() || Channel.this.isDeleted()) {
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

	private void edit(ChannelEditRequest request) {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS));

		try {
			client.REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.CHANNELS + id,
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void edit(String name, int position, String topic) throws DiscordException, RateLimitException, MissingPermissionsException {
		if (name == null || !name.matches("^[a-z0-9-_]{2,100}$"))
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric characters.");

		edit(new ChannelEditRequest.Builder().name(name).position(position).topic(topic).build());
	}

	@Override
	public void changeName(String name) throws DiscordException, RateLimitException, MissingPermissionsException {
		if (name == null || !name.matches("^[a-z0-9-_]{2,100}$"))
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric characters.");

		edit(new ChannelEditRequest.Builder().name(name).build());
	}

	@Override
	public void changePosition(int position) throws DiscordException, RateLimitException, MissingPermissionsException {
		edit(new ChannelEditRequest.Builder().position(position).build());
	}

	@Override
	public void changeTopic(String topic) throws DiscordException, RateLimitException, MissingPermissionsException {
		edit(new ChannelEditRequest.Builder().topic(topic).build());
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
	public void delete() throws DiscordException, RateLimitException, MissingPermissionsException {
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

		List<IRole> roles = user.getRolesForGuild(guild);
		EnumSet<Permissions> permissions = user.getPermissionsForGuild(guild);

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
			if ((override = getRoleOverrides().get(guild.getEveryoneRole().getID())) == null)
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
	public void removePermissionsOverride(IUser user) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, user.getRolesForGuild(guild), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+user.getID());

		userOverrides.remove(user.getID());
	}

	@Override
	public void removePermissionsOverride(IRole role) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, Collections.singletonList(role), EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS+getID()+"/permissions/"+role.getID());

		roleOverrides.remove(role.getID());
	}

	@Override
	public void overrideRolePermissions(IRole role, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws DiscordException, RateLimitException, MissingPermissionsException {
		overridePermissions("role", role.getID(), toAdd, toRemove);
	}

	@Override
	public void overrideUserPermissions(IUser user, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws DiscordException, RateLimitException, MissingPermissionsException {
		overridePermissions("member", user.getID(), toAdd, toRemove);
	}

	private void overridePermissions(String type, String id, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_PERMISSIONS));

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(
				DiscordEndpoints.CHANNELS+getID()+"/permissions/"+id,
				new OverwriteObject(type, null, Permissions.generatePermissionsNumber(toAdd), Permissions.generatePermissionsNumber(toRemove)));
	}

	@Override
	public List<IInvite> getInvites() throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_CHANNEL));
		ExtendedInviteObject[] response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/invites",
				ExtendedInviteObject[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

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
	public List<IMessage> getPinnedMessages() throws DiscordException, RateLimitException {
		List<IMessage> messages = new ArrayList<>();
		MessageObject[] pinnedMessages = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/pins",
				MessageObject[].class);

		for (MessageObject message : pinnedMessages)
			messages.add(DiscordUtils.getMessageFromJSON(this, message));

		return messages;
	}

	@Override
	public void pin(IMessage message) throws DiscordException, RateLimitException, MissingPermissionsException {
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_MESSAGES));

		if (!message.getChannel().equals(this))
			throw new DiscordException("Message channel doesn't match current channel!");

		if (message.isPinned())
			throw new DiscordException("Message already pinned!");

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(DiscordEndpoints.CHANNELS + id + "/pins/" + message.getID());
	}

	@Override
	public void unpin(IMessage message) throws DiscordException, RateLimitException, MissingPermissionsException {
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
	public IWebhook createWebhook(String name) throws DiscordException, RateLimitException, MissingPermissionsException {
		return createWebhook(name, Image.defaultAvatar());
	}

	@Override
	public IWebhook createWebhook(String name, Image avatar) throws DiscordException, RateLimitException, MissingPermissionsException {
		return createWebhook(name, avatar.getData());
	}

	@Override
	public IWebhook createWebhook(String name, String avatar) throws DiscordException, RateLimitException, MissingPermissionsException {
		getShard().checkReady("create webhook");
		DiscordUtils.checkPermissions(client, this, EnumSet.of(Permissions.MANAGE_WEBHOOKS));

		if (name == null || name.length() < 2 || name.length() > 32)
			throw new DiscordException("Webhook name can only be between 2 and 32 characters!");

		WebhookObject response = ((DiscordClientImpl) client).REQUESTS.POST.makeRequest(
				DiscordEndpoints.CHANNELS + getID() + "/webhooks",
				new WebhookCreateRequest(name, avatar),
				WebhookObject.class);

		IWebhook webhook = DiscordUtils.getWebhookFromJSON(this, response);
		addWebhook(webhook);

		return webhook;
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

				WebhookObject[] response = ((DiscordClientImpl) client).REQUESTS.GET.makeRequest(
						DiscordEndpoints.CHANNELS + getID() + "/webhooks",
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
								client.getDispatcher().dispatch(new WebhookUpdateEvent(oldWebhook, toUpdate));

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
		Channel channel = new Channel(client, name, id, guild, topic, position, new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
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
