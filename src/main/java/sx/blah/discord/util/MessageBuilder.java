package sx.blah.discord.util;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Utility class designed to make message sending easier.
 */
public class MessageBuilder {

	private String content = "";
	private IChannel channel;
	private IDiscordClient client;
	private boolean tts = false;
	private String nonce = null;

	public MessageBuilder(IDiscordClient client) {
		this.client = client;
	}

	/**
	 * Sets the content of the message.
	 *
	 * @param content The message contents.
	 * @return The message builder instance.
	 */
	public MessageBuilder withContent(String content) {
		this.content = "";
		return appendContent(content);
	}

	/**
	 * Sets the content of the message with a given style.
	 *
	 * @param content The message contents.
	 * @param styles The style to be applied to the content.
	 * @return The message builder instance.
	 */
	public MessageBuilder withContent(String content, Styles styles) {
		this.content = "";
		return appendContent(content, styles);
	}

	/**
	 * Appends extra text to the current content.
	 *
	 * @param content The content to append.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendContent(String content) {
		this.content += content;
		return this;
	}

	/**
	 * Appends extra text to the current content with given style.
	 *
	 * @param content The content to append.
	 * @param styles The style to be applied to the new content.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendContent(String content, Styles styles) {
		this.content += (styles.getMarkdown()+content+styles.getReverseMarkdown());
		return this;
	}

	/**
	 * Sets the channel that the message should go to.
	 *
	 * @param channelID The channel to send the message to.
	 * @return The message builder instance.
	 */
	public MessageBuilder withChannel(String channelID) {
		this.channel = client.getChannelByID(channelID);
		return this;
	}

	/**
	 * Sets the channel that the message should go to.
	 *
	 * @param channel The channel to send the mssage to.
	 * @return The message builder instance.
	 */
	public MessageBuilder withChannel(IChannel channel) {
		this.channel = channel;
		return this;
	}

	/**
	 * Sets the message to have tts enabled.
	 *
	 * @return The message builder instance.
	 */
	public MessageBuilder withTTS() {
		tts = true;
		return this;
	}

	/**
	 * Sets the message to have NONCE token.
	 *
	 * @return The message builder instance.
	 */
	public MessageBuilder withNonce(String nonce) {
		this.nonce = nonce;
		return this;
	}

	/**
	 * This sets the content to a multiline code block with no language highlighting.
	 *
	 * @param content The content inside the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder withQuote(String content) {
		return withCode("", content);
	}

	/**
	 * Adds a multiline code block with no language highlighting.
	 *
	 * @param content The content inside the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendQuote(String content) {
		return appendCode("", content);
	}

	/**
	 * Sets the content to a multiline code block with specific language syntax highlighting.
	 *
	 * @param language The language to do syntax highlighting for.
	 * @param content The content of the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder withCode(String language, String content) {
		this.content = "";
		return appendCode(language, content);
	}

	/**
	 * Adds a multiline code block with specific language syntax highlighting.
	 *
	 * @param language The language to do syntax highlighting for.
	 * @param content The content of the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendCode(String language, String content) {
		return appendContent(language+"\n"+content, Styles.CODE_WITH_LANG);
	}

	/**
	 * This gets the content of the message in its current form.
	 *
	 * @return The current content of the message.
     */
	public String getContent() {
		return content;
	}

	/**
	 * Galactic law requires I have a build() method in
	 * my builder classes.
	 * Sends and creates the message object.
	 *
	 * @return The message object representing the sent message.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public IMessage build() throws RateLimitException, DiscordException, MissingPermissionsException {
		if (null == content || null == channel) {
			throw new RuntimeException("You need content and a channel to send a message!");
		} else {
			return channel.sendMessage(content, tts, nonce);
		}
	}

	/**
	 * Sends the message, does the same thing as {@link #build()}.
	 *
	 * @return The message object representing the sent message.
	 *
	 * @throws RateLimitException
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 */
	public IMessage send() throws RateLimitException, DiscordException, MissingPermissionsException {
		return build();
	}

	/**
	 * Enum describing Markdown formatting that can be used in chat.
	 */
	public enum Styles {
		ITALICS("*"),
		BOLD("**"),
		BOLD_ITALICS("***"),
		STRIKEOUT("~~"),
		CODE("``` "),
		INLINE_CODE("`"),
		UNDERLINE("__"),
		UNDERLINE_ITALICS("__*"),
		UNDERLINE_BOLD("__**"),
		UNDERLINE_BOLD_ITALICS("__***"),
		CODE_WITH_LANG("```");

		final String markdown, reverseMarkdown;

		Styles(String markdown) {
			this.markdown = markdown;
			this.reverseMarkdown = new StringBuilder(markdown).reverse().toString();
		}

		/**
		 * Gets the markdown formatting for the style.
		 *
		 * @return The markdown formatting.
		 */
		public String getMarkdown() {
			return markdown;
		}

		/**
		 * Reverses the markdown formatting to be appended to the end of a formatted string.
		 *
		 * @return The reversed markdown formatting.
		 */
		public String getReverseMarkdown() {
			return reverseMarkdown;
		}

		@Override
		public String toString() {
			return markdown;
		}
	}
}
