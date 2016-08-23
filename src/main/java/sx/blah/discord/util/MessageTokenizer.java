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
	 * Initialize using the message contents and client.
	 *
	 * @param message The message object.
	 */
	public MessageTokenizer(IMessage message) {
		this(message.getClient(), message.getContent());
	}

	/**
	 * Initialize with the string contents.
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
	}

	/**
	 * Return the content that the tokenizer is traversing.
	 *
	 * @return
	 */
	public String getContent() {
		return content;
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
		 * @param startIndex The start index of the tokenizer's contents.
		 * @param endIndex   The end index of the tokenizer's contents, exclusive.
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
		 * Get the tokenizer object this token is associated with.
		 *
		 * @return The tokenizer
		 */
		public MessageTokenizer getTokenizer() {
			return tokenizer;
		}

		/**
		 * Get the content that makes up this token.
		 *
		 * @return The string of content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * Get the start index which is where this token starts in the tokenizer's contents.
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
		 * @param startIndex    The start index of the tokenizer's contents.
		 * @param endIndex      The end index of the tokenizer's contents, exclusive.
		 * @param mentionObject The object the mention is associated with.
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
		 * @param startIndex The start index of the tokenizer's contents.
		 * @param endIndex   The end index of the tokenizer's contents, exclusive.
		 * @param user       The user object it's associated with.
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
		 * @param startIndex The start index of the tokenizer's contents.
		 * @param endIndex   The end index of the tokenizer's contents, exclusive.
		 * @param role       The role object it's associated with.
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
		 * @param startIndex The start index of the tokenizer's contents.
		 * @param endIndex   The end index of the tokenizer's contents, exclusive.
		 * @param channel    The channel object it's associated with.
		 */
		ChannelMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, IChannel channel) {
			super(tokenizer, startIndex, endIndex, channel);
		}
	}

}
