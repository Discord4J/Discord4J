package sx.blah.discord.util;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

/**
 * A utility class to traverse through a message's contents and step through tokens like mentions, characters, words,
 * etc.
 *
 * @author chrislo27
 */
public class MessageTokenizer {

	private final String content;
	private final IDiscordClient client;
	private int currentPosition = 0;
	/**
	 * The remaining substring.
	 */
	private String remaining;

	/**
	 * Initializes using the message contents and client.
	 *
	 * @param message The message object
	 */
	public MessageTokenizer(IMessage message) {
		this(message.getClient(), message.getContent());
	}

	/**
	 * Initializes with the string contents.
	 *
	 * @param content What you want to traverse
	 * @param client  The Discord client that will be used to get objects from
	 */
	public MessageTokenizer(IDiscordClient client, String content) {
		if (content == null)
			throw new IllegalArgumentException("Content cannot be null!");
		if (content.length() == 0)
			throw new IllegalArgumentException("Content must have length!");

		this.content = content;
		this.client = client;

		stepForward(0);
	}

	/**
	 * Steps forward the current position and updates the internal remaining string.
	 *
	 * @param amount The amount to step forward, must be zero or higher
	 * @return The new current position
	 */
	public int stepForward(int amount) {
		if (amount < 0)
			throw new IllegalArgumentException("Amount cannot be negative!");

		currentPosition += amount;
		currentPosition = Math.min(currentPosition, content.length());
		remaining = content.substring(currentPosition);

		return currentPosition;
	}

	/**
	 * Returns true if the traverser isn't at the end of the string.
	 *
	 * @return True if we have another char to step to
	 */
	public boolean hasNext() {
		return currentPosition < content.length();
	}

	/**
	 * Exactly the same as {@link MessageTokenizer#hasNext()}. Returns true if there is another char to step to.
	 *
	 * @return True if there is more to step to
	 * @see MessageTokenizer#hasNext()
	 */
	public boolean hasNextChar() {
		return hasNext();
	}

	/**
	 * Returns the next character, stepping forward the tokenizer.
	 *
	 * @return The next char
	 * @throws IllegalStateException If there aren't more chars to go to
	 */
	public char nextChar() {
		if (!hasNextChar())
			throw new IllegalStateException("Reached end of string!");

		char c = content.charAt(currentPosition);
		stepForward(1);
		return c;
	}

	/**
	 * Returns true if there is another word to go to. A word is delimited by a space.
	 *
	 * @return True if there is another word to step to
	 */
	public boolean hasNextWord() {
		int index = remaining.lastIndexOf(' ');
		return index < remaining.length() - 2 && index > -1;
	}

	/**
	 * Returns the next word, stepping forward the tokenizer to the next non-space character. A word is delimited by
	 * a space.
	 *
	 * @return The next word
	 */
	public Token nextWord() {
		if (!hasNextWord())
			throw new IllegalStateException("No more words found!");

		int indexOfSpace = remaining.indexOf(' ');
		Token token = new Token(this, currentPosition, currentPosition + indexOfSpace);

		stepForward(indexOfSpace + 1);

		return token;
	}

	/**
	 * Returns the content that the tokenizer is traversing.
	 *
	 * @return The content that is being traversed
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the Discord client this tokenizer uses.
	 *
	 * @return The Discord client
	 */
	public IDiscordClient getClient() {
		return client;
	}

	/**
	 * Returns the current position/index this tokenizer is at.
	 *
	 * @return The current position/index
	 */
	public int getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * Represents a part of a message with the content and position.
	 */
	public static class Token {

		private final MessageTokenizer tokenizer;
		private final int startIndex;
		private final int endIndex;
		private final String content;

		/**
		 * A part of a message with content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		Token(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			if (startIndex < 0 || startIndex >= tokenizer.getContent().length())
				throw new IllegalArgumentException("Start index must be within range of content!");
			if (endIndex <= startIndex)
				throw new IllegalArgumentException("End index cannot be before start index!");
			if (endIndex > tokenizer.getContent().length())
				throw new IllegalArgumentException("End index must be within content's length!");

			this.tokenizer = tokenizer;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			content = tokenizer.getContent().substring(startIndex, endIndex);
		}

		/**
		 * Gets the tokenizer object this token is associated with.
		 *
		 * @return The tokenizer
		 */
		public MessageTokenizer getTokenizer() {
			return tokenizer;
		}

		/**
		 * Gets the content that makes up this token.
		 *
		 * @return The string of content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * Gets the start index which is where this token starts in the tokenizer's contents.
		 *
		 * @return The start index
		 */
		public int getStartIndex() {
			return startIndex;
		}

		/**
		 * Get the end index which is the index at which this token terminates, exclusive. Acts like the second
		 * parameter in
		 * {@link String#substring(int, int)}.
		 *
		 * @return The end index
		 */
		public int getEndIndex() {
			return endIndex;
		}
	}

	public static abstract class MentionToken<T extends IDiscordObject<?>> extends Token {

		private final T mention;

		/**
		 * A mention of any type with its content and position.
		 *
		 * @param tokenizer     The tokenizer
		 * @param startIndex    The start index of the tokenizer's contents
		 * @param endIndex      The end index of the tokenizer's contents, exclusive
		 * @param mentionObject The object the mention is associated with
		 */
		MentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, T mentionObject) {
			super(tokenizer, startIndex, endIndex);

			mention = mentionObject;
		}
	}

	public static abstract class UserMentionToken<T extends IUser> extends MentionToken {

		/**
		 * A user mention with its content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 * @param user       The user object it's associated with
		 */
		UserMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, IUser user) {
			super(tokenizer, startIndex, endIndex, user);
		}
	}

	public static abstract class RoleMentionToken<T extends IRole> extends MentionToken {

		/**
		 * A role mention with its content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 * @param role       The role object it's associated with
		 */
		RoleMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, IRole role) {
			super(tokenizer, startIndex, endIndex, role);
		}
	}

	public static abstract class ChannelMentionToken<T extends IChannel> extends MentionToken {

		/**
		 * A channel mention with its content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 * @param channel    The channel object it's associated with
		 */
		ChannelMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, IChannel channel) {
			super(tokenizer, startIndex, endIndex, channel);
		}
	}

}
