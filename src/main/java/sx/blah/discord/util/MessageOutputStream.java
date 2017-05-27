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

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static sx.blah.discord.handle.obj.IMessage.MAX_MESSAGE_LENGTH;

/**
 * This represents an {@link OutputStream} which will take any input and write it to a message, this message will be be
 * edited whenever bytes are written to it until its length exceeds the maximum ({@link IMessage#MAX_MESSAGE_LENGTH}).
 * In which case a new message will be sent.
 * <b>Note: Messages only get written/edited when either {@link #flush()} or {@link #close()} are called.</b>
 */
public class MessageOutputStream extends OutputStream {
	private final IChannel channel;
	private final List<IMessage> messages = new CopyOnWriteArrayList<>();
	private final AtomicReference<Queue<Character>> buf = new AtomicReference<>(new ConcurrentLinkedQueue<>());
	private final AtomicBoolean isClosed = new AtomicBoolean(false);

	/**
	 * Creates the output stream instance.
	 *
	 * @param channel The channel to send messages to.
	 */
	public MessageOutputStream(IChannel channel) {
		this.channel = channel;
	}

	@Override
	public void write(int b) throws IOException {
		if (isClosed.get())
			throw new IOException("This stream is closed.");

		final char[] message = Character.toChars(b);
		if (buf.get().size()+message.length > MAX_MESSAGE_LENGTH)
			flush();

		for (char character : message)
			buf.get().add(character);
	}

	/**
	 * This gets the messages created by this stream.
	 *
	 * @return The messages.
	 */
	public List<IMessage> getMessages() {
		return new ArrayList<>(messages);
	}

	/**
	 * This gets the most recent message created by the stream.
	 *
	 * @return The most recent message or null if none exist.
	 */
	public IMessage getCurrentMessage() {
		return messages.stream().findFirst().orElse(null);
	}

	/**
	 * This gets the channel associated with the stream.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public void flush() throws IOException {
		if (isClosed.get())
			throw new IOException("This stream is closed.");

		final AtomicReference<IOException> exceptionReference = new AtomicReference<>();

		if (!buf.get().isEmpty())
			RequestBuffer.request(() -> {
				try {
					IMessage currentMessage = getCurrentMessage();
					if (currentMessage != null) {
						String toAdd = getStringFromCharBuffer(MAX_MESSAGE_LENGTH - currentMessage.getContent().length());
						messages.set(messages.indexOf(currentMessage), currentMessage.edit(currentMessage.getContent() + toAdd));
					} else {
						messages.add(channel.sendMessage(getStringFromCharBuffer(MAX_MESSAGE_LENGTH)));
					}
				} catch (RateLimitException rle) {
					throw rle;
				} catch (Exception e) {
					exceptionReference.set(new IOException(e));
				}
			});

		if (exceptionReference.get() != null)
			throw exceptionReference.get();
	}

	private String getStringFromCharBuffer(int length) {
		Queue<Character> buffer = buf.get();
		if (length < 0)
			length = buffer.size();

		length = Math.min(length, buffer.size());

		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(buffer.poll());
		}

		return builder.toString();
	}

	@Override
	public void close() throws IOException {
		flush();
		isClosed.set(true);
	}
}
