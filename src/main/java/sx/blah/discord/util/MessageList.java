package sx.blah.discord.util;

import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.api.DiscordEndpoints;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.json.responses.MessageResponse;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

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
	private ArrayDeque<IMessage> messageCache = new ArrayDeque<>();

	/**
	 * This represents the amount of messages to fetch from discord every time the index goes out of bounds.
	 */
	private static final int MESSAGE_CHUNK_COUNT = 50;

	/**
	 * The client that this list is respecting.
	 */
	private IDiscordClient client;

	/**
	 * The channel the messages are from.
	 */
	private IChannel channel;

	/**
	 * @param client The client for this list to respect.
	 * @param channel The channel to retrieve messages from.
	 */
	public MessageList(IDiscordClient client, IChannel channel) {
		if (channel instanceof IVoiceChannel)
			throw new UnsupportedOperationException();

		try {
			DiscordUtils.checkPermissions(client, channel, EnumSet.of(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY));
		} catch (MissingPermissionsException e) {
			throw new UnsupportedOperationException("The user is messing the required permissions");
		}

		this.client = client;
		this.channel = channel;
	}

	@Override
	public IMessage get(int index) {
		while (size() <= index) {
			try {
				if (!queryMessages(MESSAGE_CHUNK_COUNT))
					throw new ArrayIndexOutOfBoundsException();
			} catch (DiscordException | HTTP429Exception e) {
				throw new ArrayIndexOutOfBoundsException("Error querying for additional messages. (Cause: "+e.getClass().getSimpleName()+")");
			}
		}

		return (IMessage) messageCache.toArray()[index];
	}

	private boolean queryMessages(int messageCount) throws DiscordException, HTTP429Exception {
		int initialSize = size();

		String queryParams = "?limit="+messageCount;
		if (initialSize != 0)
			queryParams += "&before="+messageCache.getLast().getID();

		String response = Requests.GET.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages"+queryParams,
				new BasicNameValuePair("authorization", client.getToken()));

		if (response == null)
			return false;

		MessageResponse[] messages = DiscordUtils.GSON.fromJson(response, MessageResponse[].class);

		for (MessageResponse messageResponse : messages)
			if (!add(DiscordUtils.getMessageFromJSON(client, channel, messageResponse)))
				return false;

		return size() - initialSize == messageCount;
	}

	@Override
	public boolean add(IMessage message) {
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

		return initialSize != size();
	}

	@Override
	public void forEach(Consumer<? super IMessage> action) {
		Objects.requireNonNull(action);

		final int expectedModCount = modCount;

		for (int i = 0; modCount == expectedModCount && i < size(); i++) {
			action.accept(get(i));
		}

		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
	}

	@Override
	public Spliterator<IMessage> spliterator() {
		return Spliterators.spliterator(this, 0);
	}

	@Override
	public Stream<IMessage> stream() {
		return super.stream();
	}

	@Override
	public Stream<IMessage> parallelStream() {
		return super.parallelStream();
	}

	@Override
	public int size() {
		return messageCache.size();
	}

	@Override
	public boolean removeIf(Predicate<? super IMessage> filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceAll(UnaryOperator<IMessage> operator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sort(Comparator<? super IMessage> c) {
		throw new UnsupportedOperationException();
	}
}
