package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.internal.json.objects.MessageObject;
import sx.blah.discord.handle.obj.IMessage.IEmbedded;

public class Embedded implements IEmbedded {
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

	public Embedded(String title, String type, String description, String url, MessageObject.ThumbnailObject thumbnail, MessageObject.ProviderObject provider) {
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
	public IEmbedProvider getEmbedProvider() {
		return provider;
	}

	/**
	 * Represents a site that provides media which is embedded in chat. Eg. Youtube, Imgur.
	 */
	public class EmbedProvider implements IEmbedProvider {

		/**
		 * The name of the Embedded Media Provider
		 */
		protected String name;

		/**
		 * The url link to the Embedded Media Provider
		 */
		protected String url;

		public EmbedProvider( String name, String url) {
			this.name = name;
			this.url = url;
		}

		/**
		 * Gets the Embedded Media Provider's Name
		 *
		 * @return The Embedded Media Provider's Name
		 */
		public String getName() {
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
