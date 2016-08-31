package sx.blah.discord.handle.obj;

import sx.blah.discord.handle.impl.obj.Embedded;

/**
 * Represents media embedded in the message.
 */

public interface IEmbedded {

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
