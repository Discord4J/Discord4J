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
import sx.blah.discord.util.cache.Cache;
import sx.blah.discord.util.cache.LongMap;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
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
	protected final long id;

	/**
	 * Messages that have been sent into this channel
	 */
	public final Cache<IMessage> messages;

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
	public final Cache<PermissionOverride> userOverrides;

	/**
	 * The permission overrides for roles (key = role id).
	 */
	public final Cache<PermissionOverride> roleOverrides;

	/**
	 * The webhooks for this channel.
	 */
	protected final Cache<IWebhook> webhooks;

	/**
	 * The client that created this object.
	 */
	protected final DiscordClientImpl client;

	public Channel(DiscordClientImpl client, String name, long id, IGuild guild, String topic, int position, Cache<PermissionOverride> roleOverrides, Cache<PermissionOverride> userOverrides) {
		this.client = client;
		this.name = name;
		this.id = id;
		this.guild = guild;
		this.topic = topic;
		this.position = position;
		this.roleOverrides = roleOverrides;
		this.userOverrides = userOverrides;
		this.messages = new Cache<>(client, IMessage.class);
		this.webhooks = new Cache<>(client, IWebhook.class);
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
	public long getLongID() {
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
			messages.put(message);
		} else if (getMaxInternalCacheCount() != 0) {
			if (getInternalCacheCount() == getMaxInternalCacheCount()) {
				messages.remove(messages.longIDs().stream().mapToLong(it -> it).min().getAsLong()); //Lowest id should be the earliest
			}

			messages.put(message);
		}
	}

	private IMessage[] requestHistory(Long before, int limit) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY);

		String queryParams = "?limit=" + limit;

		if (before != null) {
			queryParams += "&before=" + Long.toUnsignedString(before);
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
		return new MessageHistory(messages.values());
	}

	private static Collection<IMessage> subDeque(int from, int end, IMessage[] array) {
		List<IMessage> list = new ArrayList<>();
		if (from >= 0 || end < from) { //Skip this step if the indexes are invalid
			for (int i = from; i < end; i++)
				list.add(array[i]);
		}
		return list;
	}

	@Override
	public MessageHistory getMessageHistory(int messageCount) {
		if (messageCount <= messages.size())
			return new MessageHistory(new ArrayList<>(messages.values()).subList(0, messageCount));
		else {
			final AtomicInteger remaining = new AtomicInteger(messageCount - messages.size());
			final List<IMessage> retrieved = new ArrayList<>(messages.values());
			while (remaining.get() > 0) {
				RequestBuffer.request(() -> {
					int requestCount = Math.min(remaining.get(), MESSAGE_CHUNK_COUNT);
					IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null, requestCount);

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

		final AtomicReference<Long> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null);
		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

				List<IMessage> toAdd = Arrays.stream(chunk)
						.filter(msg -> msg.getTimestamp().compareTo(startDate) <= 0)
						.collect(Collectors.toList());

				retrieved.addAll(toAdd);

				if (chunk.length > 0)
					lastID.set(chunk[chunk.length-1].getLongID());

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
					IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null, MESSAGE_CHUNK_COUNT);

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

		final AtomicReference<Long> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null);

		if (((IMessage) messages.values().toArray()[messages.size()-1]).getTimestamp().compareTo(endDate) <= 0) { //When the last message cached matches the criteria there may still be more in history
			while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
				if (RequestBuffer.request(() -> {
					IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

					List<IMessage> toAdd = Arrays.stream(chunk)
							.filter(msg -> msg.getTimestamp().compareTo(startDate) >= 0 && msg.getTimestamp().compareTo(endDate) <= 0)
							.collect(Collectors.toList());

					retrieved.addAll(toAdd);

					if (chunk.length > 0)
						lastID.set(chunk[chunk.length-1].getLongID());

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
	public MessageHistory getMessageHistoryFrom(long id) {
		return getMessageHistoryFrom(id, -1);
	}

	@Override
	public MessageHistory getMessageHistoryFrom(long id, int maxCount) {
		IMessage[] array = messages.values().toArray(new IMessage[messages.size()]);
		int index = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i].getLongID() == id) {
				index = i;
				break;
			}
		}

		final List<IMessage> retrieved = new ArrayList<>(subDeque(index, array.length, array));

		if (index == -1)
			retrieved.add(RequestBuffer.request(() -> {return getMessageByID(id);}).get()); //Ignore intellij on this line, the return statement is required for the IRequest to not resolve to an IVoidRequest

		final AtomicReference<Long> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null);
		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

				retrieved.addAll(Arrays.asList(chunk));

				if (chunk.length > 0)
					lastID.set(chunk[chunk.length-1].getLongID());

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
	public MessageHistory getMessageHistoryTo(long id) {
		return getMessageHistoryTo(id, -1);
	}

	@Override
	public MessageHistory getMessageHistoryTo(long id, int maxCount) {
		final List<IMessage> retrieved = new ArrayList<>();

		for (IMessage message : messages.values()) {
			retrieved.add(message);
			if (message.getLongID() == id)
				return new MessageHistory(retrieved); //Let's end early since we reached the target
		}

		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null, MESSAGE_CHUNK_COUNT);

				for (IMessage message : chunk) {
					retrieved.add(message);
					if (message.getLongID() == id)
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
	public MessageHistory getMessageHistoryIn(long beginID, long endID) {
		return getMessageHistoryIn(beginID, endID, -1);
	}

	@Override
	public MessageHistory getMessageHistoryIn(long beginID, long endID, int maxCount) {
		IMessage[] array = messages.values().toArray(new IMessage[messages.size()]);
		int startIndex = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i].getLongID() == id) {
				startIndex = i;
				break;
			}
		}

		final List<IMessage> retrieved = new ArrayList<>(subDeque(startIndex, array.length, array));

		if (startIndex == -1)
			retrieved.add(RequestBuffer.request(() -> {return getMessageByID(id);}).get()); //Ignore intellij on this line, the return statement is required for the IRequest to not resolve to an IVoidRequest

		final AtomicReference<Long> lastID = new AtomicReference<>(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null);

		while ((maxCount > 0 && retrieved.size() < maxCount) || maxCount <= 0) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(lastID.get(), MESSAGE_CHUNK_COUNT);

				for (IMessage message : chunk) {
					retrieved.add(message);
					if (message.getLongID() == id)
						return true; //Finish early
				}

				if (chunk.length > 0)
					lastID.set(chunk[chunk.length-1].getLongID());

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
		final List<IMessage> retrieved = new ArrayList<>(messages.values());

		while (true) {
			if (RequestBuffer.request(() -> {
				IMessage[] chunk = requestHistory(retrieved.size() > 0 ? retrieved.get(retrieved.size()-1).getLongID() : null, MESSAGE_CHUNK_COUNT);

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
	@Deprecated
	public IMessage getMessageByID(String messageID) {
		if (messageID == null)
			return null;
		return getMessageByID(Long.parseUnsignedLong(messageID));
	}

	@Override
	public IMessage getMessageByID(long messageID) {
		return messages.getOrElseGet(messageID, () -> {
			PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY);
			return RequestBuffer.request(() -> {
				return DiscordUtils.getMessageFromJSON(this, client.REQUESTS.GET.makeRequest(
						DiscordEndpoints.CHANNELS + this.getStringID() + "/messages/" + Long.toUnsignedString(messageID),
						MessageObject.class));
			}).get();
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
		return DiscordUtils.NSFW_CHANNEL_PATTERN.matcher(name).find();
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
						((DiscordClientImpl) client).REQUESTS.POST.makeRequest(DiscordEndpoints.CHANNELS + getLongID() + "/typing");
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
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNEL, Permissions.MANAGE_CHANNELS);

		try {
			client.REQUESTS.PATCH.makeRequest(
					DiscordEndpoints.CHANNELS + id,
					DiscordUtils.MAPPER_NO_NULLS.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void edit(String name, int position, String topic) {
		if (name == null || !name.matches("^[a-z0-9-_]{2,100}$"))
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric characters.");

		edit(new ChannelEditRequest.Builder().name(name).position(position).topic(topic).build());
	}

	@Override
	public void changeName(String name) {
		if (name == null || !name.matches("^[a-z0-9-_]{2,100}$"))
			throw new IllegalArgumentException("Channel name must be 2-100 alphanumeric characters.");

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
	public LongMap<PermissionOverride> getUserOverridesLong() {
		return userOverrides.mapCopy();
	}

	@Override
	public LongMap<PermissionOverride> getRoleOverridesLong() {
		return roleOverrides.mapCopy();
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IUser user) {
		if (isPrivate() || getGuild().getOwnerLongID() == user.getLongID())
			return EnumSet.allOf(Permissions.class);

		List<IRole> roles = user.getRolesForGuild(guild);
		EnumSet<Permissions> permissions = user.getPermissionsForGuild(guild);

		if (!permissions.contains(Permissions.ADMINISTRATOR)) {
			PermissionOverride override = userOverrides.get(user.getLongID());
			List<PermissionOverride> overrideRoles = roles.stream()
					.filter(r -> roleOverrides.containsKey(r.getLongID()))
					.map(role -> roleOverrides.get(role.getLongID()))
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
		}

		return permissions;
	}

	@Override
	public EnumSet<Permissions> getModifiedPermissions(IRole role) {
		EnumSet<Permissions> base = role.getPermissions();
		PermissionOverride override = roleOverrides.get(role.getLongID());

		if (override == null) {
			if ((override = roleOverrides.get(guild.getEveryoneRole().getLongID())) == null)
				return base;
		}

		base.addAll(new ArrayList<>(override.allow()));
		override.deny().forEach(base::remove);

		return base;
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

	private void overridePermissions(String type, String id, EnumSet<Permissions> toAdd, EnumSet<Permissions> toRemove) {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_PERMISSIONS);

		((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(
				DiscordEndpoints.CHANNELS+getStringID()+"/permissions/"+id,
				new OverwriteObject(type, null, Permissions.generatePermissionsNumber(toAdd), Permissions.generatePermissionsNumber(toRemove)));
	}

	@Override
	public List<IInvite> getInvites() {
		PermissionUtils.requirePermissions(this, client.getOurUser(), Permissions.MANAGE_CHANNEL);
		ExtendedInviteObject[] response = client.REQUESTS.GET.makeRequest(
				DiscordEndpoints.CHANNELS + id + "/invites",
				ExtendedInviteObject[].class);

		List<IInvite> invites = new ArrayList<>();
		for (ExtendedInviteObject inviteResponse : response)
			invites.add(DiscordUtils.getInviteFromJSON(client, inviteResponse));

		return invites;
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
			throw new DiscordException("Message already unpinned!");

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
	public IChannel copy() {
		Channel channel = new Channel(client, name, id, guild, topic, position, new Cache<>(client, PermissionOverride.class), new Cache<>(client, PermissionOverride.class));
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
		return DiscordUtils.equals(this, other);
	}
}
