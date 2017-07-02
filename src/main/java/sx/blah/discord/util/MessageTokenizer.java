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

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

import java.util.Objects;
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

	public static final String ANY_MENTION_REGEX = "<(?:(?:@[!&]?)|#)(\\d+)>";
	public static final String CUSTOM_EMOJI_REGEX = "<:[A-Za-z0-9_]{2,}:\\d+>";
	public static final String INVITE_REGEX = "(?:discord\\.gg/)([\\w-]+)";
	public static final String WORD_REGEX = "(?:\\s|\\n)+";
	public static final Pattern ANY_MENTION_PATTERN = Pattern.compile(ANY_MENTION_REGEX);
	public static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile(CUSTOM_EMOJI_REGEX);
	public static final Pattern INVITE_PATTERN = Pattern.compile(INVITE_REGEX);
	public static final Pattern WORD_PATTERN = Pattern.compile(WORD_REGEX);

	private final String content;
	private final IDiscordClient client;
	private volatile int currentPosition = 0;
	/**
	 * The remaining substring.
	 */
	private volatile String remaining;

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
		return stepTo(currentPosition + amount);
	}

	/**
	 * Steps to the position provided and updates the internal remaining string.
	 *
	 * @param index The index to step to
	 * @return The new current position
	 * * @see MessageTokenizer#stepTo(int)
	 * @deprecated Use {@link #stepTo(int)}
	 */
	@Deprecated
	public int stepForwardTo(int index) {
		return stepTo(index);
	}

	/**
	 * Steps to the position provided and updates the internal remaining string.
	 *
	 * @param index The index to step to
	 * @return The new current position
	 */
	public int stepTo(int index) {
		currentPosition = Math.max(0, Math.min(index, content.length()));
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
	 * Returns true if the tokenizer has the sequence provided.
	 * @param sequence The string sequence to look for
	 * @return True if it contains the sequence
	 */
	public boolean hasNextSequence(String sequence) {
		return remaining.contains(sequence);
	}

	/**
	 * Returns a Token of the following sequence.
	 * @param sequence The string sequence to look for
	 * @return The token
	 */
	public Token nextSequence(String sequence) {
		if (!hasNextSequence(sequence))
			throw new IllegalStateException("The sequence \"" + sequence + "\" was not found!");

		final int index = remaining.indexOf(sequence);

		Token t = new Token(this, currentPosition + index, currentPosition + index + sequence.length());

		stepForward(index + sequence.length());
		return t;
	}

	/**
	 * Returns true if there is another word to go to. A word is delimited by whitespace or newlines.
	 *
	 * @return True if there is another word to step to
	 */
	public boolean hasNextWord() {
		return hasNext();
	}

	/**
	 * Returns the next word, stepping forward the tokenizer to the next non-space character. A word is delimited by
	 * whitespace/newlines.
	 *
	 * @return The next word
	 */
	public Token nextWord() {
		if (!hasNextWord())
			throw new IllegalStateException("No more words found!");

		{
			Matcher matcher = WORD_PATTERN.matcher(remaining);
			if (matcher.find()) {
				if (matcher.start() == 0) {
					stepTo(currentPosition + matcher.end());
				}
			}
		}

		Matcher matcher = WORD_PATTERN.matcher(remaining);
		final int end;
		boolean found = true;
		if (!matcher.find()) {
			end = content.length();
			found = false;
		} else {
			end = currentPosition + matcher.start();
		}

		Token token = new Token(this, currentPosition, end);

		stepTo(found ? (currentPosition + matcher.end()) : content.length());

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

		Matcher matcher = pattern.matcher(remaining);
		if (!matcher.find())
			throw new IllegalStateException("Couldn't find any matches!");
		final int start = currentPosition + matcher.start();
		final int end = currentPosition + matcher.end();

		stepTo(end);

		return new Token(this, start, end);
	}

	/**
	 * Returns true if there is an invite to go to.
	 *
	 * @return True if there is an invite to go to.
	 */
	public boolean hasNextInvite() {
		return hasNextRegex(INVITE_PATTERN);
	}

	public InviteToken nextInvite() {
		if (!hasNextInvite())
			throw new IllegalStateException("No more invites found!");

		Matcher matcher = INVITE_PATTERN.matcher(remaining);
		if (!matcher.find())
			throw new IllegalStateException("Couldn't find any matches!");
		final int start = currentPosition + matcher.start();
		final int end = currentPosition + matcher.end();

		stepTo(end);

		return new InviteToken(this, start, end);
	}

	/**
	 * Returns true if there is a mention to go to.
	 *
	 * @return True if there is a mention to go to.
	 */
	public boolean hasNextMention() {
		return hasNextRegex(ANY_MENTION_PATTERN);
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

		Token t = nextRegex(ANY_MENTION_PATTERN);
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
		return hasNextRegex(CUSTOM_EMOJI_PATTERN);
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

		Token t = nextRegex(CUSTOM_EMOJI_PATTERN);
		final int lessThan = t.getStartIndex();
		final int greaterThan = t.getEndIndex();

		return new CustomEmojiToken(this, lessThan, greaterThan);
	}

	/**
	 * Returns true if there is a Unicode emoji that is the same as the provided one to go to.
	 *
	 * @param emoji The emoji to look for
	 * @return True if there is another custom emoji.
	 */
	public boolean hasNextUnicodeEmoji(Emoji emoji) {
		return hasNextSequence(emoji.getUnicode());
	}

	public UnicodeEmojiToken nextUnicodeEmoji(Emoji emoji) {
		Token t = nextSequence(emoji.getUnicode());
		return new UnicodeEmojiToken(this, t.startIndex, t.endIndex);
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
	 * Returns the internal substring based on the current position.
	 *
	 * @return The remaining content
	 */
	public String getRemainingContent() {
		return remaining;
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

			mention = tokenizer.getClient().getUserByID(Long.parseUnsignedLong(getContent().replaceAll("<@!?", "").replace(">", "")));

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

			mention = tokenizer.getClient().getRoleByID(Long.parseUnsignedLong(getContent().replace("<@&", "").replace(">", "")));
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

			mention = tokenizer.getClient().getChannelByID(Long.parseUnsignedLong(getContent().replace("<#", "").replace(">", "")));
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
			final long emojiId = Long.parseUnsignedLong(content.substring(content.lastIndexOf(":") + 1, content.lastIndexOf('>')));

			emoji = tokenizer.getClient().getGuilds().stream()
					.map(guild -> guild.getEmojiByID(emojiId)).filter(Objects::nonNull).findFirst()
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

	public static class InviteToken extends Token {

		/**
		 * The invite.
		 */
		private final IInvite invite;

		/**
		 * An invite link from a message with content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		private InviteToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex);

			invite = RequestBuffer.request(() -> {
				try {
					return tokenizer.getClient()
							.getInviteForCode(getContent().substring(getContent().lastIndexOf("/")));
				} catch (DiscordException e) {
					Discord4J.LOGGER.error(LogMarkers.UTIL, "Discord4J Internal Exception", e);
				}

				return null;
			}).get();
		}

		/**
		 * Return the invite.
		 *
		 * @return The invite.
		 */
		public IInvite getInvite() {
			return invite;
		}
	}

	public static class UnicodeEmojiToken extends Token {

		/**
		 * The {@link Emoji}.
		 */
		private final Emoji emoji;

		/**
		 * A Unicode {@link Emoji} from a message with content and position.
		 *
		 * @param tokenizer  The tokenizer
		 * @param startIndex The start index of the tokenizer's contents
		 * @param endIndex   The end index of the tokenizer's contents, exclusive
		 */
		private UnicodeEmojiToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex);

			String content = getContent();
			boolean isUnicode = EmojiManager.isEmoji(content);
			emoji = isUnicode ? EmojiManager.getByUnicode(content) : EmojiManager.getForAlias(content);
		}

		/**
		 * Return the emoji object.
		 *
		 * @return The emoji.
		 * @see Emoji
		 */
		public Emoji getEmoji() {
			return emoji;
		}
	}

}
