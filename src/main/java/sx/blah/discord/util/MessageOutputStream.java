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
 * An output stream which will take any input and write it to a message.
 *
 * <p>This message written by the stream will be be edited whenever bytes are written to it until its length exceeds
 * {@value IMessage#MAX_MESSAGE_LENGTH}. When the length is exceeded, a new message is sent.
 *
 * <p>Messages only get written/edited when either {@link #flush()} or {@link #close()} are called.
 */
public class MessageOutputStream extends OutputStream {

	/**
	 * The channel to send the message to.
	 */
	private final IChannel channel;
	/**
	 * The messages written by the output stream.
	 */
	private final List<IMessage> messages = new CopyOnWriteArrayList<>();
	/**
	 * A buffer of characters to write.
	 */
	private final AtomicReference<Queue<Character>> buf = new AtomicReference<>(new ConcurrentLinkedQueue<>());
	/**
	 * Whether the stream is closed.
	 */
	private final AtomicBoolean isClosed = new AtomicBoolean(false);

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
	 * Gets the messages created by the stream.
	 *
	 * @return The messages created by the stream.
	 */
	public List<IMessage> getMessages() {
		return new ArrayList<>(messages);
	}

	/**
	 * Gets the most recent message created by the stream.
	 *
	 * @return The most recent message created by the stream.
	 */
	public IMessage getCurrentMessage() {
		return messages.stream().findFirst().orElse(null);
	}

	/**
	 * Gets the channel the stream sends messages to.
	 *
	 * @return The channel the stream sends messages to.
	 */
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public void flush() throws IOException {
		if (isClosed.get())
			throw new IOException("This stream is closed.");

		final AtomicReference<IOException> exceptionReference = new AtomicReference<>();

		if (buf.get().size() > 0)
			RequestBuffer.request(() -> {
				try {
					IMessage currentMessage = getCurrentMessage();
					if (currentMessage != null) {
						String toAdd = getStringFromCharBuffer(MAX_MESSAGE_LENGTH - currentMessage.getContent().length());
						messages.set(messages.indexOf(currentMessage), currentMessage.edit(currentMessage.getContent() + toAdd));
					} else {
						messages.add(channel.sendMessage(getStringFromCharBuffer(MAX_MESSAGE_LENGTH)));
					}
				} catch (Exception e) {
					if (e instanceof RateLimitException)
						throw (RateLimitException) e;
					else
						exceptionReference.set(new IOException(e));
				}
			});

		if (exceptionReference.get() != null)
			throw exceptionReference.get();
	}

	/**
	 * Gets a string from the stream's buffered characters.
	 *
	 * @param length The length of the string to get.
	 * @return A string from the stream's buffered characters.
	 */
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
