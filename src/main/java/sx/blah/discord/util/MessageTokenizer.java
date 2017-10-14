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
 * Used to traverse through a message's content and step through tokens like mentions, characters, words, etc.
 *
 * <p>The tokenizer has a pointer of the index it is at in the message. Every time a next method is called, the
 * pointer moves <b>past</b> the next found token.
 *
 * <p>For example, if the content is <code>this is a string of words</code>, when {@link #nextWord()} is first called,
 * it will return <code>this</code>, and move to the first space. Calling {@link #nextChar()} will return that space
 * and move to <code>i</code>.
 *
 * @author chrislo27
 */
public class MessageTokenizer {

	/**
	 * Regex for matching any Discord mention format.
	 */
	public static final String ANY_MENTION_REGEX = "<(?:(?:@[!&]?)|#)(\\d+)>";
	/**
	 * Regex for matching custom emoji.
	 */
	public static final String CUSTOM_EMOJI_REGEX = "<:[A-Za-z0-9_]{2,}:\\d+>";
	/**
	 * Regex for matching invite URLs.
	 */
	public static final String INVITE_REGEX = "(?:discord\\.gg/)([\\w-]+)";
	/**
	 * Regex for matching a word.
	 */
	public static final String WORD_REGEX = "(?:\\s|\\n)+";

	/**
	 * Pattern for Discord's mention formats.
	 */
	public static final Pattern ANY_MENTION_PATTERN = Pattern.compile(ANY_MENTION_REGEX);
	/**
	 * Pattern for Discord's custom emoji.
	 */
	public static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile(CUSTOM_EMOJI_REGEX);
	/**
	 * Pattern for Discord invite URLs.
	 */
	public static final Pattern INVITE_PATTERN = Pattern.compile(INVITE_REGEX);
	/**
	 * Pattern for words.
	 */
	public static final Pattern WORD_PATTERN = Pattern.compile(WORD_REGEX);

	/**
	 * The content of the message that is being tokenized.
	 */
	private final String content;
	/**
	 * The client that owns the tokenizer.
	 */
	private final IDiscordClient client;
	/**
	 * The current position of the pointer in the message.
	 */
	private volatile int currentPosition = 0;

	/**
	 * The remaining substring.
	 */
	private volatile String remaining;

	public MessageTokenizer(IMessage message) {
		this(message.getClient(), message.getContent());
	}

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
	 * Moves the pointer forward by the given amount.
	 *
	 * @param amount The amount to move forward.
	 * @return The new pointer position.
	 */
	public int stepForward(int amount) {
		return stepTo(currentPosition + amount);
	}

	/**
	 * Moves the pointer forward to the given position.
	 *
	 * @param index The index to move to.
	 * @return The new pointer position.
	 */
	public int stepTo(int index) {
		currentPosition = Math.max(0, Math.min(index, content.length()));
		remaining = content.substring(currentPosition);

		return currentPosition;
	}

	/**
	 * Gets whether the pointer is not at the end of the content.
	 *
	 * @return Whether the pointer is not at the end of the content.
	 */
	public boolean hasNext() {
		return currentPosition < content.length();
	}

	/**
	 * Gets whether the pointer is not at the end of the content.
	 *
	 * <p>This is equivalent to {@link #hasNext()}.
	 *
	 * @return Whether the pointer is not at the end of the content.
	 */
	public boolean hasNextChar() {
		return hasNext();
	}

	/**
	 * Gets the next character in the content and moves the pointer forward.
	 *
	 * @return The next character in the content.
	 * @throws IllegalStateException If there is no next character.
	 */
	public char nextChar() {
		if (!hasNextChar())
			throw new IllegalStateException("Reached end of string!");

		char c = content.charAt(currentPosition);
		stepForward(1);
		return c;
	}

	/**
	 * Gets whether the content has the given string sequence.
	 *
	 * @param sequence The string sequence to look for.
	 * @return Whether the content has the given string sequence.
	 */
	public boolean hasNextSequence(String sequence) {
		return remaining.contains(sequence);
	}

	/**
	 * Gets the next sequence as a token.
	 *
	 * @param sequence The string sequence to look for.
	 * @return The next sequence as a token.
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
	 * Gets whether the content has a next word. A word is delimited by whitespace.
	 *
	 * <p>This is equivalent to {@link #hasNext()}.
	 *
	 * @return Whether the content has a next word.
	 */
	public boolean hasNextWord() {
		return hasNext();
	}

	/**
	 * Gets the next word in the content and moves the pointer forward.
	 *
	 * @return The next word in the content.
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
	 * Gets whether the pointer is not at the end of the content.
	 *
	 * <p>This is equivalent to {@link #hasNext()}.
	 *
	 * @return Whether the pointer is not at the end of the content.
	 */
	public boolean hasNextLine() {
		return hasNext();
	}

	/**
	 * Gets the next line of content as a token.
	 *
	 * @return The next line of content as a token.
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
	 * Gets whether the content matches the given pattern.
	 *
	 * @param pattern The pattern to match with.
	 * @return Whether the content matches the given pattern.
	 */
	public boolean hasNextRegex(Pattern pattern) {
		return hasNext() && pattern.matcher(remaining).find();
	}

	/**
	 * Gets the next string of the content that matches the given pattern as a token.
	 *
	 * @param pattern The pattern to match with.
	 * @return The next string of the content that matches the given pattern as a token.
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
	 * Gets whether the content has an invite.
	 *
	 * <p>This is equivalent to <code>hasNextRegex(INVITE_PATTERN)</code>
	 *
	 * @return Whether the content has an invite.
	 */
	public boolean hasNextInvite() {
		return hasNextRegex(INVITE_PATTERN);
	}

	/**
	 * Gets the next invite in the content and moves the pointer forward.
	 *
	 * @return The next invite in the content.
	 */
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
	 * Gets whether the content has a mention.
	 *
	 * <p>This is equivalent to <code>hasNextRegex(ANY_MENTION_PATTERN)</code>
	 *
	 * @return Whether the content has a mention.
	 */
	public boolean hasNextMention() {
		return hasNextRegex(ANY_MENTION_PATTERN);
	}

	/**
	 * Gets the next mention in the content and moves the pointer forward.
	 *
	 * @return The next mention in the content.
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
	 * Gets whether the content has a custom emoji.
	 *
	 * <p>This is equivalent to <code>hasNextRegex(CUSTOM_EMOJI_PATTERN)</code>
	 *
	 * @return Whether the content has a custom emoji.
	 */
	public boolean hasNextEmoji() {
		return hasNextRegex(CUSTOM_EMOJI_PATTERN);
	}

	/**
	 * Gets the next custom emoji in the content and moves the pointer forward.
	 *
	 * @return The next custom emoji in the content.
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
	 * Gets whether the content has a unicode emoji.
	 *
	 * @param emoji The unicode emoji to search for.
	 * @return Whether the content has a unicode emoji.
	 */
	public boolean hasNextUnicodeEmoji(Emoji emoji) {
		return hasNextSequence(emoji.getUnicode());
	}

	/**
	 * Gets the next unicode emoji in the content and moves the pointer forward.
	 *
	 * @param emoji The unicode emoji to search for.
	 * @return The next unicode emoji in the content.
	 */
	public UnicodeEmojiToken nextUnicodeEmoji(Emoji emoji) {
		Token t = nextSequence(emoji.getUnicode());
		return new UnicodeEmojiToken(this, t.startIndex, t.endIndex);
	}

	/**
	 * Gets the content of the tokenizer.
	 *
	 * @return The content of the tokenizer.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Gets the client that owns the tokenizer.
	 *
	 * @return The client that owns the tokenizer.
	 */
	public IDiscordClient getClient() {
		return client;
	}

	/**
	 * Gets the current position of the pointer in the message.
	 *
	 * @return The current position of the pointer in the message.
	 */
	public int getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * Gets the remaining substring of the original content.
	 *
	 * @return The remaining substring.
	 */
	public String getRemainingContent() {
		return remaining;
	}

	/**
	 * A part of a message with the content and position.
	 */
	public static class Token {

		/**
		 * The tokenizer which produced the token.
		 */
		private final MessageTokenizer tokenizer;
		/**
		 * The start index of the tokenizer's contents. (Inclusive)
		 */
		private final int startIndex;
		/**
		 * The end index of the tokenizer's contents. (Exclusive)
		 */
		private final int endIndex;
		/**
		 * The content of the token.
		 */
		private final String content;

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
		 * Gets the tokenizer which produced the token.
		 *
		 * @return The tokenizer which produced the token.
		 */
		public MessageTokenizer getTokenizer() {
			return tokenizer;
		}

		/**
		 * Gets the content of the token.
		 *
		 * @return The content of the token.
		 */
		public String getContent() {
			return content;
		}

		/**
		 * Gets the start index of the tokenizer's contents. (Inclusive)
		 *
		 * @return The start index of the tokenizer's contents.
		 */
		public int getStartIndex() {
			return startIndex;
		}

		/**
		 * Gets the end index of the tokenizer's contents. (Exclusive)
		 *
		 * @return The end index of the tokenizer's contents.
		 */
		public int getEndIndex() {
			return endIndex;
		}

		@Override
		public String toString() {
			return content;
		}
	}

	/**
	 * A token for a mention.
	 *
	 * @param <T> The type of object that is mentioned.
	 */
	public static abstract class MentionToken<T extends IDiscordObject> extends Token {

		/**
		 * The mentioned object.
		 */
		protected T mention;

		private MentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex, T mentionObject) {
			super(tokenizer, startIndex, endIndex);

			mention = mentionObject;
		}

		/**
		 * Gets the mentioned object.
		 *
		 * @return The mentioned object.
		 */
		public T getMentionObject() {
			return mention;
		}
	}

	/**
	 * A mention token for a user.
	 */
	public static class UserMentionToken extends MentionToken<IUser> {

		/**
		 * Whether the mention was a nickname mention.
		 */
		private final boolean isNickname;

		private UserMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex, null);

			mention = tokenizer.getClient().getUserByID(Long.parseUnsignedLong(getContent().replaceAll("<@!?", "").replace(">", "")));

			isNickname = getContent().contains("<@!");
		}

		/**
		 * Gets whether the mention was a nickname mention.
		 *
		 * @return Whether the mention was a nickname mention.
		 */
		public boolean isNickname() {
			return isNickname;
		}
	}

	/**
	 * A mention token for a role.
	 */
	public static class RoleMentionToken extends MentionToken<IRole> {

		private RoleMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex, null);

			mention = tokenizer.getClient().getRoleByID(Long.parseUnsignedLong(getContent().replace("<@&", "").replace(">", "")));
		}
	}

	/**
	 * A mention token for a channel.
	 */
	public static class ChannelMentionToken extends MentionToken<IChannel> {

		private ChannelMentionToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex, null);

			mention = tokenizer.getClient().getChannelByID(Long.parseUnsignedLong(getContent().replace("<#", "").replace(">", "")));
		}
	}

	/**
	 * A token for a custom emoji.
	 */
	public static class CustomEmojiToken extends Token {

		/**
		 * The custom emoji.
		 */
		private final IEmoji emoji;

		private CustomEmojiToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex);

			final String content = getContent();
			final long emojiId = Long.parseUnsignedLong(content.substring(content.lastIndexOf(":") + 1, content.lastIndexOf('>')));

			emoji = tokenizer.getClient().getGuilds().stream()
					.map(guild -> guild.getEmojiByID(emojiId)).filter(Objects::nonNull).findFirst()
					.orElse(null);
		}

		/**
		 * Gets the custom emoji.
		 *
		 * @return The custom emoji.
		 */
		public IEmoji getEmoji() {
			return emoji;
		}
	}

	/**
	 * A token for an invite.
	 */
	public static class InviteToken extends Token {

		/**
		 * The invite.
		 */
		private final IInvite invite;

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
		 * Gets the invite.
		 *
		 * @return The invite.
		 */
		public IInvite getInvite() {
			return invite;
		}
	}

	/**
	 * A token for a unicode emoji.
	 */
	public static class UnicodeEmojiToken extends Token {

		/**
		 * The unicode emoji.
		 */
		private final Emoji emoji;

		private UnicodeEmojiToken(MessageTokenizer tokenizer, int startIndex, int endIndex) {
			super(tokenizer, startIndex, endIndex);

			String content = getContent();
			boolean isUnicode = EmojiManager.isEmoji(content);
			emoji = isUnicode ? EmojiManager.getByUnicode(content) : EmojiManager.getForAlias(content);
		}

		/**
		 * Gets the unicode emoji.
		 *
		 * @return The unicode emoji.
		 */
		public Emoji getEmoji() {
			return emoji;
		}
	}

}
