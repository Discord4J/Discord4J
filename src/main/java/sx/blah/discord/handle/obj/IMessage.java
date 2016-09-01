package sx.blah.discord.handle.obj;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.MissingPermissionsException;

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
	List<IEmbedded> getEmbedded();

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
	 * Edits the message. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @return The new message (this).
	 *
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 */
	IMessage edit(String content) throws MissingPermissionsException, RateLimitException, DiscordException;

	/**
	 * Returns whether this message mentions everyone.
	 *
	 * @return True if it mentions everyone, false if otherwise.
	 */
	boolean mentionsEveryone();

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
	interface IEmbedded {

		/**
		 * Gets the title of the embedded media.
		 *
		 * @return The title of the embedded media. Can be null.
		 */
		public String getTitle();

		/**
		 * Gets the type of embedded media.
		 *
		 * @return The type of embedded media as a string.
		 */
		public String getType();

		/**
		 * Gets a description of the embedded media.
		 *
		 * @return A description of the embedded media. Can be null.
		 */
		public String getDescription();

		/**
		 * Gets the direct link to the media.
		 *
		 * @return The download link for the attachment.
		 */
		public String getUrl();

		/**
		 * Gets the thumbnail of the embedded media.
		 *
		 * @return An object containing information about the embedded media's thumbnail. Can be null.
		 */
		public String getThumbnail();

		/**
		 * Gets the provider of the embedded media.
		 *
		 * @return An object containing information about the embedded media's provider. <b>Can Be Null!</b>
		 */
		public IEmbedded.IEmbedProvider getEmbedProvider();

		/**
		 * Represents a site that provides media which is embedded in chat. Eg. Youtube, Imgur.
		 */
		interface IEmbedProvider {
			/**
			 * Gets the Embedded Media Provider's Name
			 *
			 * @return The Embedded Media Provider's Name
			 */
			public String getName();

			/**
			 * Gets the Embedded Media Provider's Url
			 *
			 * @return A url link to the Embedded Media Provider as a String
			 */
			public String getUrl();
		}
	}
}
