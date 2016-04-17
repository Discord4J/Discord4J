package sx.blah.discord.util;

import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.EventSubscriber;
import sx.blah.discord.api.internal.Requests;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.responses.MessageResponse;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This class is a custom implementation of {@link List} for retrieving discord messages.
 *
 * The list gets a message on demand, it either fetches it from the cache or it requests the message from Discord
 * if not cached.
 */
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
	 * The client that this list is respecting.
	 */
	private final IDiscordClient client;

	/**
	 * The channel the messages are from.
	 */
	private final IChannel channel;

	/**
	 * The event listener for this list instance. This is used to update the list when messages are received/removed/etc.
	 */
	private final MessageListEventListener listener;

	/**
	 * This is true if the client object has permission to read this channel's messages.
	 */
	private volatile boolean hasPermission;

	/**
	 * This is the maximum amount of messages that will be cached by this list. If negative, it'll store unlimited
	 * messages.
	 */
	private volatile int capacity = 256;

	/**
	 * @param client The client for this list to respect.
	 * @param channel The channel to retrieve messages from.
	 */
	public MessageList(IDiscordClient client, IChannel channel) {
		if (channel instanceof IVoiceChannel)
			throw new UnsupportedOperationException();

		this.client = client;
		this.channel = channel;

		updatePermissions();

		listener = new MessageListEventListener(this);
		client.getDispatcher().registerListener(listener);
	}

	/**
	 * @param client The client for this list to respect.
	 * @param channel The channel to retrieve messages from.
	 * @param initialContents The initial amount of messages to have cached when this list is constructed.
	 */
	public MessageList(IDiscordClient client, IChannel channel, int initialContents) {
		this(client, channel);

		try {
			load(initialContents);
		} catch (HTTP429Exception e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
		}
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

			for (int i = start-1; i >= capacity; i--) {
				messageCache.remove(messageCache.toArray()[i]);
			}

			return start-size();
		}

		return 0;
	}

	private boolean queryMessages(int messageCount) throws DiscordException, HTTP429Exception {
		if (!hasPermission)
			return false;

		int initialSize = size();

		String queryParams = "?limit="+messageCount;
		if (initialSize != 0)
			queryParams += "&before="+messageCache.getLast().getID();

		String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages"+queryParams,
				new BasicNameValuePair("authorization", client.getToken()));

		if (response == null)
			return false;

		MessageResponse[] messages = DiscordUtils.GSON.fromJson(response, MessageResponse[].class);

		if (messages.length == 0) {
			return false;
		}

		for (MessageResponse messageResponse : messages)
			if (!add(DiscordUtils.getMessageFromJSON(client, channel, messageResponse), true))
				return false;

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

		remove(message);

		return message;
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
		return stream().filter((m) -> m.getID().equalsIgnoreCase(id)).findFirst().orElse(null);
	}

	/**
	 * This attempts to load the specified number of messages into the list's cache. NOTE: this calls {@link #purge()}
	 * after loading.
	 *
	 * @param messageCount The amount of messages to load.
	 * @return True if this action was successful, false if otherwise.
	 *
	 * @throws HTTP429Exception
	 */
	public boolean load(int messageCount) throws HTTP429Exception {
		try {
			boolean success = queryMessages(messageCount);

			purge();

			return success;
		} catch (DiscordException e) {
			Discord4J.LOGGER.error("Discord4J Internal Exception", e);
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

	private void updatePermissions() {
		try {
			DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY));
			hasPermission = true;
		} catch (MissingPermissionsException e) {
			Discord4J.LOGGER.warn("Missing permissions required to read channel {}. If this is an error, report this it the Discord4J dev!", channel.getName());
			hasPermission = false;
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

		//The following are to update the hasPermission boolean

		@EventSubscriber
		public void onRoleUpdate(RoleUpdateEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getGuild().equals(list.channel.getGuild()) &&
					list.client.getOurUser().getRolesForGuild(list.channel.getGuild()).contains(event.getNewRole()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onGuildUpdate(GuildUpdateEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getNewGuild().equals(list.channel.getGuild()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onUserRoleUpdate(UserRoleUpdateEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getUser().equals(list.client.getOurUser()) && event.getGuild().equals(list.channel.getGuild()))
				list.updatePermissions();
		}

		@EventSubscriber
		public void onGuildTransferOwnership(GuildTransferOwnershipEvent event) {
			if (!(list.channel instanceof IPrivateChannel) && event.getGuild().equals(list.channel.getGuild())) {
				list.updatePermissions();
			}
		}

		@EventSubscriber
		public void onChannelUpdateEvent(ChannelUpdateEvent event) {
			if (event.getNewChannel().equals(list.channel))
				list.updatePermissions();
		}
	}
}
