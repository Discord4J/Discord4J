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

package sx.blah.discord.handle.obj;

import com.vdurmont.emoji.Emoji;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents a discord message.
 */
public interface IMessage extends IDiscordObject<IMessage> {

	/**
	 * The maximum length of a discord message.
	 */
	int MAX_MESSAGE_LENGTH = 2000;

	/**
	 * Gets the string content of the message.
	 *
	 * @return The content of the message
	 */
	String getContent();

	/**
	 * Gets the channel that this message belongs to.
	 *
	 * @return The channel.
	 */
	IChannel getChannel();

	/**
	 * Gets the user who authored this message.
	 *
	 * @return The author.
	 */
	IUser getAuthor();

	/**
	 * Gets the timestamp for when this message was sent/edited.
	 *
	 * @return The timestamp.
	 */
	LocalDateTime getTimestamp();

	/**
	 * Gets the users mentioned in this message.
	 *
	 * @return The users mentioned.
	 */
	List<IUser> getMentions();

	/**
	 * Gets the roles mentioned in this message.
	 *
	 * @return The roles mentioned.
	 */
	List<IRole> getRoleMentions();

	/**
	 * Gets the channels mentioned in this message.
	 *
	 * @return The channels mentioned.
	 */
	List<IChannel> getChannelMentions();

	/**
	 * Gets the attachments in this message.
	 *
	 * @return The attachments.
	 */
	List<Attachment> getAttachments();

	/**
	 * Gets the embeds in this message.
	 *
	 * @return The embeds.
	 * @deprecated Use {@link #getEmbeds()} instead.
	 */
	@Deprecated
	List<IEmbed> getEmbedded();

	/**
	 * Gets the embeds in this message.
	 *
	 * @return The embeds.
	 */
	List<IEmbed> getEmbeds();

	/**
	 * Adds an "@mention," to the author of the referenced Message
	 * object before your content
	 *
	 * @param content Message to send.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IMessage reply(String content);

	/**
	 * Adds an "@mention," to the author of the referenced Message
	 * object before your content.
	 *
	 * @param content Message content to send.
	 * @param embed The embed object
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage reply(String content, EmbedObject embed);

	/**
	 * Edits the message. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 */
	IMessage edit(String content);

	/**
	 * Edits the message with an embed object. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @param embed The embed object
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage edit(String content, EmbedObject embed);

	/**
	 * Edits the message with only an embed object. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param embed The embed object
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage edit(EmbedObject embed);

	/**
	 * Returns whether this message mentions everyone through @everyone.
	 *
	 * @return True if it mentions everyone, false if otherwise.
	 */
	boolean mentionsEveryone();

	/**
	 * Returns whether this message mentions the online users through @here.
	 *
	 * @return True if it mentions all the online users, false if otherwise.
	 */
	boolean mentionsHere();

	/**
	 * Deletes the message.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete();

	/**
	 * Gets the time that this message was last edited.
	 *
	 * @return The edited timestamp.
	 */
	Optional<LocalDateTime> getEditedTimestamp();

	/**
	 * Returns whether this message has been pinned on its channel or not.
	 *
	 * @return True if pinned, false is otherwise.
	 */
	boolean isPinned();

	/**
	 * Gets the guild this message is from.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets formatted content. All user, channel, and role mentions are converted to a readable form.
	 *
	 * @return The formatted content.
	 */
	String getFormattedContent();

	/**
	 * Gets the reactions for this message.
	 *
	 * @return A list of reactions
	 */
	List<IReaction> getReactions();

	/**
	 * Gets a reaction by the IEmoji object.
	 *
	 * @param emoji The emoji
	 * @return The reaction, or null if there aren't any that match
	 */
	IReaction getReactionByIEmoji(IEmoji emoji);

	/**
	 * Gets a reaction by the emoji text. This will <b>not</b> work with custom emojis, use getReactionByIEmoji
	 * instead.
	 *
	 * @param name The emoji text (as Unicode)
	 * @return The reaction, or null if there aren't any that match
	 * @see IMessage#getReactionByIEmoji(IEmoji)
	 */
	IReaction getReactionByUnicode(String name);

	/**
	 * Gets a reaction by the Unicode emoji. This will <b>not</b> work with custom emojis, use getReactionByIEmoji
	 * instead. This simply calls {@link Emoji#getUnicode()} on the other overload of this method.
	 *
	 * @param emoji The emoji (as Unicode)
	 * @return The reaction, or null if there aren't any that match
	 * @see IMessage#getReactionByIEmoji(IEmoji)
	 */
	IReaction getReactionByUnicode(Emoji emoji);

	/**
	 * @deprecated Use {@link #getReactionByUnicode(String)} instead
	 */
	@Deprecated
	IReaction getReactionByName(String name);

	/**
	 * Delete all reactions. Requires the MANAGE_MESSAGES permission.
	 *
	 * @see Permissions#MANAGE_MESSAGES
	 */
	void removeAllReactions();

	/**
	 * Adds your reaction to an existing one.
	 *
	 * @param reaction The reaction object
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(IReaction reaction);

	/**
	 * Adds your reaction as a custom emoji.
	 *
	 * @param emoji The custom emoji
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(IEmoji emoji);

	/**
	 * Adds your reaction as a normal emoji. This can be either a Unicode emoji (☑), or an IEmoji formatted one (&lt;:name:id&gt;).
	 * Alternatively, you can provide the emoji alias like you would in normal Discord (ex: :ballot_box_with_check:) and we'll
	 * attempt to look it up in emoji-java (if it doesn't exist in emoji-java, you'll need to provide the Unicode version).
	 *
	 * @param emoji The string emoji
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(String emoji);

	/**
	 * Adds your reaction as a Unicode one. Use {@link com.vdurmont.emoji.EmojiManager#getForAlias(String)}
	 * to retrieve an Emoji object.
	 *
	 * @param emoji The string emoji
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(Emoji emoji);

	/**
	 * Removes a reaction for a user.
	 *
	 * @param reaction The reaction to remove from
	 * @param user The user
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removeReaction(IUser user, IReaction reaction);

	/**
	 * Removes a reaction for yourself.
	 *
	 * @param reaction The reaction to remove from
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removeReaction(IReaction reaction);

	/**
	 * This creates a new {@link MessageTokenizer} instance with this message instance.
	 *
	 * @return A new tokenizer.
	 */
	MessageTokenizer tokenize();

	/**
	 * Checks to see is this message deleted.
	 *
	 * @return True if this message is deleted
	 */
	boolean isDeleted();

	/**
	 * Gets the ID of the webhook that sent this message. May be null.
	 *
	 * @return The webhook ID.
	 * @deprecated Use {@link #getWebhookLongID()} instead
	 */
	@Deprecated
	default String getWebhookID() {
		return Long.toUnsignedString(getWebhookLongID());
	}

	/**
	 * Gets the ID of the webhook that sent this message. May be null.
	 *
	 * @return The webhook ID.
	 */
	long getWebhookLongID();

	/**
	 * Represents an attachment included in the message.
	 */
	class Attachment implements IIDLinkedObject {

		/**
		 * The file name of the attachment.
		 */
		protected final String filename;

		/**
		 * The size, in bytes of the attachment.
		 */
		protected final int filesize;

		/**
		 * The attachment id.
		 */
		protected final long id;

		/**
		 * The download link for the attachment.
		 */
		protected final String url;

		public Attachment(String filename, int filesize, long id, String url) {
			this.filename = filename;
			this.filesize = filesize;
			this.id = id;
			this.url = url;
		}

		/**
		 * Gets the file name for the attachment.
		 *
		 * @return The file name of the attachment.
		 */
		public String getFilename() {
			return filename;
		}

		/**
		 * Gets the size of the attachment.
		 *
		 * @return The size, in bytes of the attachment.
		 */
		public int getFilesize() {
			return filesize;
		}

		/**
		 * Gets the id of the attachment.
		 *
		 * @return The attachment id.
		 * @deprecated Use {@link #getLongID()} or {@link #getStringID()} instead
		 */
		@Deprecated
		public String getId() {
			return getStringID();
		}

		/**
		 * Gets the id of the attachment.
		 *
		 * @return The attachment id.
		 */
		@Override
		public long getLongID() {
			return id;
		}

		/**
		 * Gets the direct link to the attachment.
		 *
		 * @return The download link for the attachment.
		 */
		public String getUrl() {
			return url;
		}
	}

}
