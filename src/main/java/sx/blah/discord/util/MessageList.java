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

package sx.blah.discord.util;

import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.MessageObject;
import sx.blah.discord.api.internal.json.requests.BulkDeleteRequest;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This class is a custom implementation of {@link List} for retrieving discord messages.
 *
 * The list gets a message on demand, it either fetches it from the cache or it requests the message from Discord
 * if not cached.
 *
 * @deprecated Use {@link MessageHistory} instead, this should NOT be relied on to work properly anymore.
 */
@Deprecated
public class MessageList extends AbstractList<IMessage> implements List<IMessage> {

	/**
	 * This is used to cache message objects to prevent unnecessary queries.
	 */
	private final ConcurrentLinkedDeque<IMessage> messageCache = new ConcurrentLinkedDeque<>();

	/**
	 * This represents the amount of messages to fetch from discord every time the index goes out of bounds.
	 */
	public static final int MESSAGE_CHUNK_COUNT = 100; //100 is the max amount discord lets you retrieve at one time

	/**
	 * Represents a maximum message capacity which will be unlimited (-1). Yay, no magic numbers!
	 */
	public static final int UNLIMITED_CAPACITY = -1;

	/**
	 * This is the max number of guild before the list stops automatically loading its history.
	 */
	@Deprecated
	public static final int MAX_GUILD_COUNT = 10;

	/**
	 * The client that this list is respecting.
	 */
	private final DiscordClientImpl client;

	/**
	 * The channel the messages are from.
	 */
	private final IChannel channel;

	/**
	 * The event listener for this list instance. This is used to update the list when messages are received/removed/etc.
	 */
	private final MessageListEventListener listener;

	/**
	 * This is the maximum amount of messages that will be cached by this list. If negative, it'll store unlimited
	 * messages.
	 */
	private volatile int capacity = 256;

	/**
	 * This determines how efficiently MessageLists run.
	 */
	private static final Map<IDiscordClient, EfficiencyLevel> efficiencies = new ConcurrentHashMap<>();

	/**
	 * @param client The client for this list to respect.
	 * @param channel The channel to retrieve messages from.
	 */
	public MessageList(IDiscordClient client, IChannel channel) {
		if (channel instanceof IVoiceChannel)
			throw new UnsupportedOperationException();

		this.client = (DiscordClientImpl) client;
		this.channel = channel;

		if (getEfficiency() != EfficiencyLevel.HIGH) {
			listener = new MessageListEventListener(this);
			client.getDispatcher().registerListener(listener);

			if (getEfficiency() == EfficiencyLevel.MEDIUM) {
				capacity /= 2;
			}
		} else {
			listener = null;
			capacity = 0;
		}
	}

	/**
	 * @param client The client for this list to respect.
	 * @param channel The channel to retrieve messages from.
	 * @param initialContents The initial amount of messages to have cached when this list is constructed.
	 */
	public MessageList(IDiscordClient client, IChannel channel, int initialContents) {
		this(client, channel);

		if (getEfficiency() == EfficiencyLevel.NONE)
			RequestBuffer.request(() -> load(initialContents));
	}

	/**
	 * This implementation of {@link List#get(int)} first checks if the requested message is cached, if so it retrieves
	 * that object, otherwise it requests messages from discord in chunks of {@link #MESSAGE_CHUNK_COUNT} until it gets
	 * the requested object. If the object cannot be found, it throws an {@link ArrayIndexOutOfBoundsException}.
	 *
	 * @param index The index (starting at 0) of the message in this list.
	 * @return The message object for this index.
	 */
	@Override
	public synchronized IMessage get(int index) {
		while (size() <= index) {
			try {
				if (!load(MESSAGE_CHUNK_COUNT))
					throw new ArrayIndexOutOfBoundsException();
			} catch (Exception e) {
				throw new ArrayIndexOutOfBoundsException("Error querying for additional messages. (Cause: "+e.getClass().getSimpleName()+")");
			}
		}

		IMessage message = (IMessage) messageCache.toArray()[index];

		purge();

		return message;
	}

	/**
	 * This purges the list's internal message cache so that the oldest messages are removed until the list's capacity
	 * requirements are met.
	 *
	 * @return The amount of messages cleared.
	 */
	public int purge() {
		if (capacity >= 0) {
			int start = size();

			Object[] cache = messageCache.toArray();
			for (int i = start-1; i >= capacity; i--) {
				messageCache.remove((IMessage) cache[i]);
			}

			return start-size();
		}

		return 0;
	}

	private boolean queryMessages(int messageCount) {
		if (!hasPermissions())
			return false;

		int initialSize = size();

		String queryParams = "?limit="+messageCount;
		if (initialSize != 0)
			queryParams += "&before="+messageCache.getLast().getStringID();

		MessageObject[] messages = new MessageObject[0];
		try {
			messages = DiscordUtils.MAPPER.readValue(client.REQUESTS.GET.makeRequest(DiscordEndpoints.CHANNELS+channel.getStringID()+"/messages"+queryParams), MessageObject[].class);
		} catch (IOException e) {
			throw new DiscordException("JSON Parsing exception!", e);
		}

		if (messages.length == 0) {
			return false;
		}

		for (MessageObject messageResponse : messages) {
			if (!add(DiscordUtils.getMessageFromJSON((Channel) channel, messageResponse), true))
				return false;
		}

		return size() - initialSize <= messageCount;
	}

	/**
	 * This adds a message object to the internal message cache.
	 *
	 * @param message The message object to cache.
	 * @return True if the object was successfully cached, false if otherwise.
	 */
	@Override
	public boolean add(IMessage message) {
		return add(message, false);
	}

	/**
	 * This method was delegated so that the {@link #get(int)} method won't be broken if queried messages exceed the
	 * list's capacity.
	 *
	 * @param message The message to cache.
	 * @param skipPurge Whether to skip purging the cache, true to skip, false to purge.
	 * @return True if the object was successfully cached, false if otherwise.
	 */
	private synchronized boolean add(IMessage message, boolean skipPurge) {
		if (messageCache.contains(message))
			return false;

		int initialSize = size();

		if (initialSize == 0) {
			messageCache.add(message);
		} else {
			if (MessageComparator.REVERSED.compare(message, messageCache.getFirst()) > -1)
				messageCache.addLast(message);
			else
				messageCache.addFirst(message);
		}

		boolean cacheChanged = initialSize != size();

		if (!skipPurge)
			purge();

		return cacheChanged;
	}

	/**
	 * This checks if a message with the provided id is cached my this list.
	 *
	 * @param id The id.
	 * @return True if found, false if otherwise.
	 */
	public boolean contains(String id) {
		return messageCache.stream().filter(it -> it.getStringID().equals(id)).findFirst().isPresent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Spliterator<IMessage> spliterator() {
		return Spliterators.spliterator(this, 0);
	}

	/**
	 * This implementation of {@link List#size()} gets the size of the internal message cache NOT the total amount of
	 * messages which exist in a channel in total.
	 *
	 * @return The amount of messages in the internal message cache.
	 */
	@Override
	public int size() {
		return messageCache.size();
	}

	@Override
	public void sort(Comparator<? super IMessage> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean remove(Object o) {
		if (!(o instanceof IMessage) || !((IMessage) o).getChannel().equals(channel))
			return false;

		return messageCache.remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized IMessage remove(int index) {
		if (index >= size())
			throw new ArrayIndexOutOfBoundsException();

		IMessage message = get(index);

		boolean result = remove(message);

		return result ? message : null;
	}

	/**
	 * This creates a new {@link List} from this message list.
	 *
	 * @return The copied list. Note: This list is a copy of the current message cache, not a copy of this specific
	 * instance of {@link MessageList}. It will ONLY ever contain the contents of the current message cache.
	 */
	public List<IMessage> copy() {
		return new ArrayList<>(this);
	}

	/**
	 * A utility method to reverse the order of this list.
	 *
	 * @return A reversed COPY of this list.
	 *
	 * @see #copy()
	 */
	public List<IMessage> reverse() {
		List<IMessage> messages = copy();
		messages.sort(MessageComparator.DEFAULT);
		return messages;
	}

	/**
	 * This retrieves the earliest CACHED message.
	 *
	 * @return The earliest message. A cleaner version of {@link #get(int)} with an index of {@link #size()}-1.
	 */
	public IMessage getEarliestMessage() {
		return get(size()-1);
	}

	/**
	 * This retrieves the latest CACHED message.
	 *
	 * @return The latest message. A cleaner version of {@link #get(int)} with an index of 0.
	 */
	public IMessage getLatestMessage() {
		return get(0);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * This retrieves a message object with the specified message id.
	 *
	 * @param id The message id to search for.
	 * @return The message object found, or null if nonexistent.
	 */
	public IMessage get(String id) {
		IMessage message = stream().filter((m) -> m.getStringID().equalsIgnoreCase(id)).findFirst().orElse(null);

		if (message == null && hasPermissions() && client.isReady())
			try {
				return DiscordUtils.getMessageFromJSON((Channel) channel, client.REQUESTS.GET.makeRequest(
						DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages/" + id,
						MessageObject.class));
			} catch (Exception ignored) {}

		return message;
	}

	/**
	 * This attempts to load the specified number of messages into the list's cache. NOTE: this calls {@link #purge()}
	 * after loading.
	 *
	 * @param messageCount The amount of messages to load.
	 * @return True if this action was successful, false if otherwise.
	 *
	 * @throws RateLimitException
	 */
	public boolean load(int messageCount) {
		try {
			boolean success = queryMessages(messageCount);

			purge();

			return success;
		} catch (DiscordException e) {
			Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
		}
		return false;
	}

	/**
	 * Sets the maximum amount of messages to be cached by this list. NOTE: This purges immediately after changing the
	 * capacity.
	 *
	 * @param capacity The capacity, if negative the capacity will be unlimited.
	 */
	public void setCacheCapacity(int capacity) {
		this.capacity = capacity;
		purge();
	}

	/**
	 * Gets the maximum amount of messages to be cached by this list.
	 *
	 * @return The capacity, if negative the capacity will be unlimited.
	 */
	public int getCacheCapacity() {
		return capacity;
	}

	/**
	 * This deletes the message at the specified index.
	 *
	 * @param index The index to delete the message at.
	 * @return The message deleted and removed form the cache.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public IMessage delete(int index) {
		IMessage message = get(index);
		if (message != null) {
			message.delete();
		}
		return message;
	}

	/**
	 * Bulk deletes messages in a sub list created from the start and end indexes. (Note: Only 100 messages can be
	 * deleted at a time).
	 *
	 * @param startIndex The start index (inclusive).
	 * @param endIndex The end index (exclusive).
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteFromRange(int startIndex, int endIndex) {
		List<IMessage> messages = subList(startIndex, endIndex);
		bulkDelete(messages);
		return messages;
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided index to the amount of messages requested. (Note:
	 * Only 100 messages can be deleted at a time).
	 *
	 * @param index The start index (inclusive).
	 * @param amount The amount of messages to attempt to delete.
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteAfter(int index, int amount) {
		return deleteFromRange(index, index+amount);
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided index to the end of the list. (Note: Only 100
	 * messages can be deleted at a time).
	 *
	 * @param index The start index (inclusive).
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteAfter(int index) {
		return deleteAfter(index, size()-index);
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided message to the end of the list. (Note: Only 100
	 * messages can be deleted at a time).
	 *
	 * @param message The start message (inclusive).
	 * @param amount The amount of messages to attempt to delete.
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteAfter(IMessage message, int amount) {
		return deleteAfter(indexOf(message), amount);
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided message to the end of the list. (Note: Only 100
	 * messages can be deleted at a time).
	 *
	 * @param message The start message (inclusive).
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteAfter(IMessage message) {
		return deleteAfter(indexOf(message));
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided index to the beginning of the list. (Note: Only 100
	 * messages can be deleted at a time).
	 *
	 * @param index The end index (inclusive).
	 * @param amount The amount of messages to attempt to delete.
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteBefore(int index, int amount) {
		return deleteFromRange(Math.max(0, index-amount), index+1);
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided index to the beginning of the list. (Note: Only 100
	 * messages can be deleted at a time).
	 *
	 * @param index The end index (inclusive).
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteBefore(int index) {
		return deleteFromRange(0, index+1);
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided message to the beginning of the list. (Note: Only
	 * 100 messages can be deleted at a time).
	 *
	 * @param message The end message (inclusive).
	 * @param amount The amount of messages to attempt to delete.
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteBefore(IMessage message, int amount) {
		return deleteBefore(indexOf(message), amount);
	}

	/**
	 * Bulk deletes messages in a sub list created from the provided message to the beginning of the list. (Note: Only
	 * 100 messages can be deleted at a time).
	 *
	 * @param message The end message (inclusive).
	 * @return The messages deleted.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public List<IMessage> deleteBefore(IMessage message) {
		return deleteBefore(indexOf(message));
	}

	/**
	 * This "bulk deletes" a list of messages. (Note: Only 100 messages can be deleted at a time).
	 *
	 * @param messages The messages to delete.
	 *
	 * @throws DiscordException
	 * @throws RateLimitException
	 * @throws MissingPermissionsException
	 */
	public void bulkDelete(List<IMessage> messages) {
		PermissionUtils.requirePermissions(channel, client.getOurUser(), Permissions.MANAGE_MESSAGES);

		if (channel.isPrivate())
			throw new UnsupportedOperationException("Cannot bulk delete in private channels!");

		if (messages.size() < 2 || messages.size() > 100)
			throw new DiscordException("Must provide at least 2 and fewer than 100 messages to delete.");

		long invalidCount = messages.stream()
				.mapToLong(IIDLinkedObject::getLongID)
				.filter(id -> id < (((System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000) - 1420070400000L) << 22)) // Taken from Jake
				// IF THE ID IS LESS THAN TWO WEEKS AGO SUBTRACT DISCORD EPOCH BITSHIFT LEFT 22 THEN COUNT IT BECAUSE IT'S BAD
				.count();
		if (invalidCount > 0)
			throw new DiscordException(String.format("%d messages cannot be bulk deleted! They are more than 2 weeks old.", invalidCount));

		client.REQUESTS.POST.makeRequest(
				DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages/bulk-delete",
				new BulkDeleteRequest(messages));
	}

	/**
	 * This sets whether MessageLists should automatically fetch message history on initialization. This is
	 * automatically disabled if the number of guilds logged into exceeds {@link MessageList#MAX_GUILD_COUNT}.
	 *
	 * @param download Whether to automatically download history.
	 * @deprecated Use {@link #setEfficiency(EfficiencyLevel)} instead.
	 */
	@Deprecated
	public static void shouldDownloadHistoryAutomatically(boolean download) {
		//shh bby is ok
	}

	/**
	 * This gets whether MessageLists will automatically fetch message history on initialization. This is
	 * automatically disabled if the number of guilds logged into exceeds {@link MessageList#MAX_GUILD_COUNT}.
	 *
	 * @return  Whether it'll automatically download history.
	 * @deprecated Use {@link #getEfficiency()} instead.
	 */
	@Deprecated
	public static boolean downloadsHistoryAutomatically() {
		return new Random().nextBoolean();
	}

	/**
	 * This sets how efficiently MessageLists run.
	 *
	 * @param level The new efficiency level.
	 */
	public static void setEfficiency(IDiscordClient client, EfficiencyLevel level) {
		efficiencies.put(client, level);
	}

	/**
	 * This gets the efficiency level that MessageLists run at.
	 *
	 * @return The current efficiency level.
	 */
	public static EfficiencyLevel getEfficiency(IDiscordClient client) {
		if (!efficiencies.containsKey(client)) {
			return null;
		}

		return efficiencies.get(client);
	}

	/**
	 * This sets how efficiently MessageLists run.
	 *
	 * @param level The new efficiency level.
	 */
	public void setEfficiency(EfficiencyLevel level) {
		setEfficiency(client, level);
	}

	/**
	 * This gets the efficiency level that MessageLists run at.
	 *
	 * @return The current efficiency level.
	 */
	public EfficiencyLevel getEfficiency() {
		return getEfficiency(client);
	}

	private boolean hasPermissions() {
		try {
			PermissionUtils.requirePermissions(channel, client.getOurUser(), Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY);
			return true;
		} catch (MissingPermissionsException e) {
			if (!Discord4J.ignoreChannelWarnings.get())
				Discord4J.LOGGER.debug(LogMarkers.UTIL, "Missing permissions required to read channel {}. If this is an error, report this it the Discord4J dev!", channel.getName());
			return false;
		}
	}

	/**
	 * This is used to automatically update the message list.
	 */
	public static class MessageListEventListener {

		private volatile MessageList list;

		public MessageListEventListener(MessageList list) {
			this.list = list;
		}

		@EventSubscriber
		public void onMessageReceived(MessageReceivedEvent event) {
			if (event.getMessage().getChannel().equals(list.channel)) {
				list.add(event.getMessage());
			}
		}

		@EventSubscriber
		public void onMessageSent(MessageSendEvent event) {
			if (event.getMessage().getChannel().equals(list.channel)) {
				list.add(event.getMessage());
			}
		}

		@EventSubscriber
		public void onMessageDelete(MessageDeleteEvent event) {
			if (event.getMessage().getChannel().equals(list.channel)) {
				list.remove(event.getMessage());
			}
		}

		//The following are to unregister this listener to optimize the event dispatcher.

		@EventSubscriber
		public void onChannelDelete(ChannelDeleteEvent event) {
			if (event.getChannel().equals(list.channel)) {
				list.client.getDispatcher().unregisterListener(this);
			}
		}

		@EventSubscriber
		public void onGuildRemove(GuildLeaveEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getGuild().equals(list.channel.getGuild())) {
				list.client.getDispatcher().unregisterListener(this);
			}
		}
	}

	/**
	 * This enum represents how efficient a MessageList is. By lowering efficiency, more data is cached but can hurt bot
	 * performance.
	 */
	public enum EfficiencyLevel {

		/**
		 * At this level, the message list caches the default amount of messages (256) and past message history is
		 * requested up to {@link #MESSAGE_CHUNK_COUNT} messages.
		 */
		NONE(-1),
		/**
		 * At this level, the message list caches the default amount of messages (256) and past message history is NOT
		 * requested on initialization.
		 */
		LOW(10),
		/**
		 * At this level, the message list caches the half the default amount of messages (128) and past message history
		 * is NOT requested on initialization.
		 */
		MEDIUM(50),
		/**
		 * At this level, the message list does NOT cache any messages and past message history is NOT requested on
		 * initialization. Additionally, the MessageList's built-in event listener is never registered.
		 */
		HIGH(100);

		private int guildsRequired;

		EfficiencyLevel(int guildsRequired) {
			this.guildsRequired = guildsRequired;
		}

		/**
		 * This is the number of guilds required for this efficiency level to be automatically used.
		 *
		 * @return The minimum number of guilds required.
		 */
		public int getGuildsRequired() {
			return guildsRequired;
		}

		/**
		 * This retrieves the correct efficiency level for a given number of joined guilds.
		 *
		 * @param guildNumber The number of joined guilds..
		 * @return The proper efficiency.
		 */
		public static EfficiencyLevel getEfficiencyForGuilds(int guildNumber) {
			return Arrays.stream(EfficiencyLevel.values())
					.filter((EfficiencyLevel level) -> level.getGuildsRequired() <= guildNumber)
					.max((EfficiencyLevel obj1, EfficiencyLevel obj2) ->
							obj1.guildsRequired < obj2.guildsRequired ? -1 : (obj1.guildsRequired > obj2.guildsRequired ? 1 : 0))
					.orElse(HIGH);
		}
	}
}
