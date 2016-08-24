package sx.blah.discord.handle.obj;

import sx.blah.discord.json.responses.MessageResponse;
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
	List<EmbeddedAttachment> getEmbedded();

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

	/**
	 * Represents an attachment embedded in the message.
	 */

	class EmbeddedAttachment {
		/**
		 * The title of the embedded media.
		 */
		protected final String title;

		/**
		 * The type of embedded media.
		 */
		protected final String type;

		/**
		 * The description of the embedded media.
		 */
		protected final String description;

		/**
		 * The download link for the embedded media.
		 */
		protected final String url;

		/**
		 * The url link to the embedded media's thumbnail thumbnail.
		 */
		protected final String thumbnail;

		/**
		 * The object containing information about the provider of the embedded media.
		 */
		protected final EmbedProvider provider;

		public EmbeddedAttachment(String title, String type, String description, String url, MessageResponse.EmbedResponse.ThumbnailResponse thumbnail, MessageResponse.EmbedResponse.ProviderResponse provider) {
			this.title = title;
			this.type = type;
			this.description = description;
			this.url = url;
			if(thumbnail == null){
				this.thumbnail = null;
			} else {
				this.thumbnail = thumbnail.url;
			}
			if(provider == null){
				this.provider = null;
			} else {
				this.provider = new EmbedProvider(provider.name, provider.url);
			}
		}

		/**
		 * Gets the title of the embedded media.
		 *
		 * @return The title of the embedded media. Can be null.
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Gets the type of embedded media.
		 *
		 * @return The type of embedded media as a string.
		 */
		public String getType() {
			return type;
		}

		/**
		 * Gets a description of the embedded media.
		 *
		 * @return A description of the embedded media. Can be null.
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Gets the direct link to the media.
		 *
		 * @return The download link for the attachment.
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * Gets the thumbnail of the embedded media.
		 *
		 * @return An object containing information about the embedded media's thumbnail. Can be null.
		 */
		public String getThumbnail() {
			return thumbnail;
		}

		/**
		 * Gets the provider of the embedded media.
		 *
		 * @return An object containing information about the embedded media's provider. <b>Can Be Null!</b>
		 */
		public EmbedProvider getEmbedProvider() {
			return provider;
		}

		/**
		 * Represents a site that provides media which is embedded in chat. Eg. Youtube, Imgur.
		 */
		public class EmbedProvider{

			/**
			 * The name of the Embedded Media Provider
			 */
			protected String name;

			/**
			 * The url link to the Embedded Media Provider
			 */
			protected String url;

			public EmbedProvider( String name, String url){
				this.name = name;
				this.url = url;
			}

			/**
			 * Gets the Embedded Media Provider's Name
			 *
			 * @return The Embedded Media Provider's Name
			 */
			public String getName(){
				return name;
			}

			/**
			 * Gets the Embedded Media Provider's Url
			 *
			 * @return A url link to the Embedded Media Provider as a String
			 */
			public String getUrl(){
				return url;
			}
		}


	}
}
