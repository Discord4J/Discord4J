package sx.blah.discord.handle.obj;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

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
	 * Gets the Embedded attachments in this message.
	 *
	 * @return The attachments.
	 */
	List<IEmbed> getEmbedded();

	/**
	 * Adds an "@mention," to the author of the referenced Message
	 * object before your content
	 *
	 * @param content Message to send.
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void reply(String content) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Adds an "@mention," to the author of the referenced Message
	 * object before your content.
	 *
	 * @param content Message content to send.
	 * @param embed The embed object
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	void reply(String content, EmbedObject embed) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Edits the message. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 */
	IMessage edit(String content) throws MissingPermissionsException, RateLimitException, DiscordException;

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
	IMessage edit(String content, EmbedObject embed) throws MissingPermissionsException, RateLimitException, DiscordException;

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
	void delete() throws MissingPermissionsException, RateLimitException, DiscordException;

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
	 * @param name The emoji text
	 * @return The reaction, or null if there aren't any that match
	 * @see IMessage#getReactionByIEmoji(IEmoji)
	 */
	IReaction getReactionByName(String name);

	/**
	 * Delete all reactions. Requires the MANAGE_MESSAGES permission.
	 *
	 * @see Permissions#MANAGE_MESSAGES
	 */
	void removeAllReactions() throws RateLimitException, MissingPermissionsException, DiscordException;

	/**
	 * Adds your reaction to an existing one.
	 *
	 * @param reaction The reaction object
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(IReaction reaction) throws MissingPermissionsException, RateLimitException,
			DiscordException;

	/**
	 * Adds your reaction as a custom emoji
	 *
	 * @param emoji The custom emoji
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(IEmoji emoji) throws MissingPermissionsException, RateLimitException,
			DiscordException;

	/**
	 * Adds your reaction as a normal emoji. This can be either a Unicode emoji, or an IEmoji formatted one (&lt;:name:id&gt;)
	 *
	 * @param emoji The string emoji
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void addReaction(String emoji) throws MissingPermissionsException, RateLimitException,
			DiscordException;

	/**
	 * Removes a reaction for a user.
	 *
	 * @param reaction The reaction to remove from
	 * @param user The user
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removeReaction(IUser user, IReaction reaction) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Removes a reaction for yourself.
	 *
	 * @param reaction The reaction to remove from
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void removeReaction(IReaction reaction) throws MissingPermissionsException, RateLimitException, DiscordException;

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
	 */
	String getWebhookID();

	/**
	 * Represents an attachment included in the message.
	 */
	class Attachment {

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
		protected final String id;

		/**
		 * The download link for the attachment.
		 */
		protected final String url;

		public Attachment(String filename, int filesize, String id, String url) {
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
		 */
		public String getId() {
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
