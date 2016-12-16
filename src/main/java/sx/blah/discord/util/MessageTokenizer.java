package sx.blah.discord.util;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to traverse through a message's contents and step through tokens like mentions, characters, words,
 * etc. The tokenizer has an internal pointer of where it's at in the message. Everytime you call nextX, the internal
 * pointer will move <b>past</b> the token it found. For example, if the string is <code>this is a string of
 * words</code>, when {@link #nextWord()} is first called, it will return <code>this</code>, and move to the first
 * space. Calling {@link #nextChar()} will return that space, and move to <code>i</code>, in <code>is a...</code>,
 * and so forth.
 *
 * @author chrislo27
 */
public class MessageTokenizer {

	public static final String ANY_MENTION_REGEX = "<((@[!&]?)|#)\\d+>";
	public static final String CUSTOM_EMOJI_REGEX = "<:[A-Za-z0-9_]{2,}:\\d+>";

	private final String content;
	private final IDiscordClient client;
	private final Pattern anyMentionPattern = Pattern.compile(ANY_MENTION_REGEX);
	private final Pattern customEmojiPattern = Pattern.compile(CUSTOM_EMOJI_REGEX);
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
	 * @throws IllegalArgumentException If content is null, empty, or client is null
	 */
	public MessageTokenizer(IDiscordClient client, String content) {
		if (content == null)
			throw new IllegalArgumentException("Content cannot be null!");
		if (content.length() == 0)
			throw new IllegalArgumentException("Content must have length!");
		if (client == null)
			throw new IllegalArgumentException("Client cannot be null!");

		this.content = content;
		this.client = client;

		stepForward(0);
	}

	/**
	 * Steps forward the current position and updates the internal remaining string.
	 *
	 * @param amount The amount to step forward
	 * @return The new current position
	 */
	public int stepForward(int amount) {
		currentPosition += amount;
		currentPosition = Math.max(0, Math.min(currentPosition, content.length()));
		remaining = content.substring(currentPosition);

		return currentPosition;
	}

	/**
	 * Steps to the desired index.
	 *
	 * @param index The index to step to
	 * @return The new current position
	 * @see MessageTokenizer#stepForward(int)
	 */
	public int stepTo(int index) {
		return stepForward(index - currentPosition);
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
		int index = remaining.indexOf(' ');
		return hasNext() && index < remaining.length() - 1;
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

		int spaceIndex = remaining.indexOf(' ');
		int nlIndex = remaining.indexOf('\n');

		int indexOfSpace = -1;
		if (spaceIndex == -1 && nlIndex == -1) {
			indexOfSpace = content.length() - currentPosition;
		} else if (spaceIndex == -1) {
			indexOfSpace = nlIndex;
		} else if (nlIndex == -1) {
			indexOfSpace = spaceIndex;
		}
		Token token = new Token(this, currentPosition, currentPosition + indexOfSpace);

		stepForward(indexOfSpace + 1);

		return token;
	}

	/**
	 * Returns true if there is a line to go to.
	 *
	 * @return True if there is a line available
	 */
	public boolean hasNextLine() {
		return hasNext();
	}

	/**
	 * Returns the current text, up to the next newline/end, stepping forward the tokenizer to the next line.
	 *
	 * @return The line
	 */
	public Token nextLine() {
		if (!hasNextLine())
			throw new IllegalStateException("No more lines found!");

		int indexOfNewline = remaining.indexOf('\n');
		if (indexOfNewline == -1) {
			indexOfNewline = content.length() - currentPosition;
		}
		Token token = new Token(this, currentPosition, currentPosition + indexOfNewline);

		stepForward(indexOfNewline + 1);

		return token;
	}

	/**
	 * Returns true if an occurence of the regex pattern exists.
	 *
	 * @param pattern The regex pattern
	 * @return True if there is an occurence
	 */
	public boolean hasNextRegex(Pattern pattern) {
		return hasNext() && pattern.matcher(remaining).find();
	}

	/**
	 * Returns the next occurrence of the regular expression, stepping forward the tokenizer to the next line.
	 *
	 * @param pattern The regex pattern
	 * @return The token of the regex occurrence
	 */
	public Token nextRegex(Pattern pattern) {
		if (!hasNextRegex(pattern))
			throw new IllegalStateException("No more occurrences found!");

		Matcher matcher = anyMentionPattern.matcher(remaining);
		if (!matcher.find())
			throw new IllegalStateException("Couldn't find any matches!");
		final int start = currentPosition + matcher.start();
		final int end = currentPosition + matcher.end();

		stepTo(end);

		return new Token(this, start, end);
	}

	/**
	 * Returns true if there is a mention to go to.
	 *
	 * @return True if there is a mention to go to.
	 */
	public boolean hasNextMention() {
		return hasNextRegex(anyMentionPattern);
	}

	/**
	 * Returns the next mention, stepping forward the tokenizer to the end of the mention (exclusive).
	 *
	 * @return The next mention token
	 * @see MentionToken
	 */
	public MentionToken nextMention() {
		if (!hasNextMention())
			throw new IllegalStateException("No more mentions found!");

		Token t = nextRegex(anyMentionPattern);
		final int lessThan = t.getStartIndex();
		final int greaterThan = t.getEndIndex();
		final String matched = t.getContent();
		final char type = matched.charAt(1);

		if (type == '@') {
			if (matched.charAt(2) == '&') {
				return new RoleMentionToken(this, lessThan, greaterThan);
			}

			return new UserMentionToken(this, lessThan, greaterThan);
		} else if (type == '#') {
			return new ChannelMentionToken(this, lessThan, greaterThan);
		}

		// should NEVER happen because hasNextMention will ensure we get a mention
		throw new IllegalStateException("Couldn't find a mention even though it was found!");
	}

	/**
	 * Returns true if there is another custom emoji to go to.
	 *
	 * @return True if there is another custom emoji.
	 */
	public boolean hasNextEmoji() {
		return hasNextRegex(customEmojiPattern);
	}

	/**
	 * Returns the next custom emoji, stepping forward the tokenizer to the end of the emoji (exclusive).
	 *
	 * @return The next custom emoji token
	 * @see CustomEmojiToken
	 */
	public CustomEmojiToken nextEmoji() {
		if (!hasNextEmoji())
			throw new IllegalStateException("No more custom server emojis found!");

		Token t = nextRegex(customEmojiPattern);
		final int lessThan = t.getStartIndex();
		final int greaterThan = t.getEndIndex();

		return new CustomEmojiToken(this, lessThan, greaterThan);
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
				throw new IllegalArgumentException("Start index must be within range of content! (Got " + startIndex +
						" for startIndex, must be between 0 and " + (tokenizer.getContent().length() - 1) +
						", inclusive)");
			if (endIndex <= startIndex)
				throw new IllegalArgumentException(
						"End index cannot be before or at start index! (Start index is " + startIndex + ", got " +
								endIndex + ")");
			if (endIndex > tokenizer.getContent().length())
				throw new IllegalArgumentException(
						"End index must be within content's length! (End index is " + endIndex + ", length is " +
								tokenizer.getContent().length() + ")");

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

		@Override
		public String toString() {
			return content;
		}
	}

	public static abstract class MentionToken<T extends IDiscordObject> extends Token {

		protected T mention;

		/**
		 * A mention of any type with its content and position.
		 *
		 * @param tokenizer     The tokenizer
		 * @param startIndex    The start index of the tokenizer's contents
		 * @param endIndex      The end index of the tokenizer's contents, exclusive
		 * @param mentionObject The object the mention is associated with
		 */
		private MentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, T mentionObject) {
			super(tokenizer, startIndex, endIndex);

			mention = mentionObject;
		}

		/**
		 * Returns the object associated with the mention (IUser, IRole, IChannel, etc).
		 *
		 * @return The object associated with the mention
		 */
		public T getMentionObject() {
			return mention;
		}
	}

	public static class UserMentionToken extends MentionToken<IUser> {

		private final boolean isNickname;

		/**
		 * A user mention with its content and position. It will grab the user from the content.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		private UserMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex, null);

			mention = tokenizer.getClient().getUserByID(getContent().replaceAll("<@!?", "").replace(">", ""));

			isNickname = getContent().contains("<@!");
		}

		/**
		 * Returns true if the mention type is for nicknames. (<@!)
		 *
		 * @return True if the mention is for nicknames, false if it's for the username
		 */
		public boolean isNickname() {
			return isNickname;
		}
	}

	public static class RoleMentionToken extends MentionToken<IRole> {

		/**
		 * A role mention with its content and position. It will grab the role from the content.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		private RoleMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex, null);

			mention = tokenizer.getClient().getRoleByID(getContent().replace("<@&", "").replace(">", ""));
		}
	}

	public static class ChannelMentionToken extends MentionToken<IChannel> {

		/**
		 * A channel mention with its content and position. It will grab the channel from the content.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		private ChannelMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex, null);

			mention = tokenizer.getClient().getChannelByID(getContent().replace("<#", "").replace(">", ""));
		}
	}

	public static class CustomEmojiToken extends Token {

		/**
		 * The emoji.
		 */
		private final IEmoji emoji;

		/**
		 * A custom server emoji from a message with content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		private CustomEmojiToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex);

			final String content = getContent();
			final String emojiId = content.substring(content.lastIndexOf(":") + 1, content.length());

			emoji = tokenizer.getClient().getGuilds().stream()
					.map(guild -> guild.getEmojiByID(emojiId) != null ? guild.getEmojiByID(emojiId) : null).findFirst()
					.orElse(null);
		}

		/**
		 * Return the emoji.
		 *
		 * @return The emoji
		 */
		public IEmoji getEmoji() {
			return emoji;
		}
	}

}
