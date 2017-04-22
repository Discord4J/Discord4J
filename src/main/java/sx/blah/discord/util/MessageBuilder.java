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

import org.apache.commons.lang3.ArrayUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Utility class designed to make message sending easier.
 */
public class MessageBuilder {

	private String content = "";
	private IChannel channel;
	private IDiscordClient client;
	private boolean tts = false;
	private EmbedObject embed;
	private InputStream stream;
	private String fileName;

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
	 * @param styles The styles to be applied to the content.
	 * @return The message builder instance.
	 */
	public MessageBuilder withContent(String content, Styles... styles) {
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
	 * @param styles The styles to be applied to the new content.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendContent(String content, Styles... styles) {
		for (Styles style : styles)
			this.content += style.getMarkdown();

		this.content += content;

		ArrayUtils.reverse(styles);
		for (Styles style : styles)
			this.content += style.getReverseMarkdown();

		return this;
	}

	/**
	 * Sets the channel that the message should go to.
	 *
	 * @param channelID The channel to send the message to.
	 * @return The message builder instance.
	 * @deprecated Use {@link #withChannel(long)} instead
	 */
	@Deprecated
	public MessageBuilder withChannel(String channelID) {
		return withChannel(Long.parseUnsignedLong(channelID));
	}

	/**
	 * Sets the channel that the message should go to.
	 *
	 * @param channelID The channel to send the message to.
	 * @return The message builder instance.
	 */
	public MessageBuilder withChannel(long channelID) {
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
	 * Sets the message to have tts enabled or disabled.
	 *
	 * @return The message builder instance.
	 */
	public MessageBuilder withTTS(boolean tts) {
		this.tts = tts;
		return this;
	}

	/**
	 * Sets the message to have tts enabled.
	 *
	 * @return The message builder instance.
	 */
	public MessageBuilder withTTS() {
		return withTTS(true);
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
	 * Sets the embed to be used (can be null).
	 *
	 * @param embed The embed object (build with EmbedBuilder)
	 * @return The message builder instance.
	 * @see EmbedBuilder
	 */
	public MessageBuilder withEmbed(EmbedObject embed) {
		this.embed = embed;
		return this;
	}

	/**
	 * Adds a file to be sent with the message.
	 *
	 * @param file The file to be sent with the message
	 * @return The message builder instance.
	 *
	 * @throws FileNotFoundException
	 */
	public MessageBuilder withFile(File file) throws FileNotFoundException {
		if (file == null)
			throw new NullPointerException("File argument is null");
		this.stream = new FileInputStream(file);
		this.fileName = file.getName();
		return this;
	}

	/**
	 * Adds a file to be sent with the message.
	 *
	 * @param stream the stream of data of the file
	 * @param fileName the name of the file to be sent to Discord
	 * @return The message builder instance.
	 */
	public MessageBuilder withFile(InputStream stream, String fileName) {
		this.stream = stream;
		this.fileName = fileName;
		return this;
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
	 * This gets the channel the message will be sent to.
	 *
	 * @return The channel.
	 */
	public IChannel getChannel() {
		return channel;
	}

	/**
	 * This gets the embed object for the message. May be null.
	 *
	 * @return The embed object.
	 */
	public EmbedObject getEmbedObject() {
		return embed;
	}

	/**
	 * This gets if the builder will have TTS enabled.
	 *
	 * @return If TTS will be used
	 */
	public boolean isUsingTTS() {
		return tts;
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
	public IMessage build() {
		if (null == content || null == channel)
			throw new RuntimeException("You need content and a channel to send a message!");
		if (stream == null) {
			return channel.sendMessage(content, embed, tts);
		} else {
			return channel.sendFile(content, tts, stream, fileName, embed);
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
	public IMessage send() {
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
		CODE("```\n"),
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
