package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.internal.json.objects.MessageObject;
import sx.blah.discord.handle.obj.IMessage.IEmbedded;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
	protected final IEmbedProvider provider;

	protected final LocalDateTime timestamp;

	protected final Color color;

	protected final IEmbedFooter footer;

	protected final String image;

	protected final String video;

	protected final IEmbedAuthor author;

	protected final List<IEmbedField> embedFields;

	public Embedded(String title, String type, String description, String url, MessageObject.ThumbnailObject thumbnail, MessageObject.ProviderObject provider, LocalDateTime timestamp, Color color, MessageObject.FooterObject footer, MessageObject.ImageObject image, MessageObject.VideoObject video, MessageObject.AuthorObject author, MessageObject.EmbedFieldObject[] embedFields) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.url = url;
		if (thumbnail == null) {
			this.thumbnail = null;
		} else {
			this.thumbnail = thumbnail.url;
		}
		if (provider == null) {
			this.provider = null;
		} else {
			this.provider = new EmbedProvider(provider.name, provider.url);
		}
		this.timestamp = timestamp;
		this.color = color;
		if (footer == null)
			this.footer = null;
		else
			this.footer = new EmbedFooter(footer.text, footer.icon_url);
		if (image == null)
			this.image = null;
		else
			this.image = image.url;
		if (video == null)
			this.video = null;
		else
			this.video = video.url;
		if (author == null)
			this.author = null;
		else
			this.author = new EmbedAuthor(author.name, author.url, author.icon_url);

		if (embedFields == null || embedFields.length == 0)
			this.embedFields = null;
		else {
			this.embedFields = new CopyOnWriteArrayList<>();

			for (MessageObject.EmbedFieldObject embedField : embedFields)
				this.embedFields.add(new EmbedField(embedField.name, embedField.value, embedField.inline));
		}
	}

	public Embedded(String title, String type, String description, String url, String thumbnailUrl, IEmbedProvider provider, LocalDateTime timestamp, Color color, IEmbedFooter footer, String imageUrl, String videoUrl, IEmbedAuthor author, IEmbedField[] embedFields) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.url = url;
		this.thumbnail = thumbnailUrl;
		this.provider = provider;
		this.timestamp = timestamp;
		this.color = color;
		this.footer = footer;
		this.image = imageUrl;
		this.video = videoUrl;
		this.author = author;

		if (embedFields == null || embedFields.length == 0)
			this.embedFields = null;
		else {
			this.embedFields = new CopyOnWriteArrayList<>();

			Collections.addAll(this.embedFields, embedFields);
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

	@Override
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public IEmbedFooter getFooter() {
		return footer;
	}

	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Gets the thumbnail of the embedded media.
	 *
	 * @return An object containing information about the embedded media's thumbnail. Can be null.
	 */
	public String getThumbnail() {
		return thumbnail;
	}

	@Override
	public String getVideo() {
		return video;
	}

	/**
	 * Gets the provider of the embedded media.
	 *
	 * @return An object containing information about the embedded media's provider. <b>Can Be Null!</b>
	 */
	public IEmbedProvider getEmbedProvider() {
		return provider;
	}

	@Override
	public IEmbedAuthor getAuthor() {
		return author;
	}

	@Override
	public List<IEmbedField> getEmbeddedFields() {
		return embedFields;
	}

	public static class EmbedFooter implements IEmbedFooter {

		/**
		 * The text for the footer
		 */
		protected String text;

		/**
		 * The url link for the footer
		 */
		protected String iconUrl;

		public EmbedFooter(String text, String iconUrl) {
			this.text = text;
			this.iconUrl = iconUrl;
		}

		/**
		 * Gets the footer's text
		 *
		 * @return The footer's text
		 */
		public String getText() {
			return text;
		}

		/**
		 * Gets the footer's icon URL
		 *
		 * @return A url link as a string
		 */
		public String getIconUrl() {
			return iconUrl;
		}
	}

	/**
	 * Represents the author for an embedded object
	 */
	public static class EmbedAuthor implements IEmbedAuthor {

		protected String name;
		protected String url;
		protected String icon_url;

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}

		public String getIconUrl() {
			return icon_url;
		}

		public EmbedAuthor(String name, String url, String icon_url) {
			this.name = name;
			this.url = url;
			this.icon_url = icon_url;
		}
	}

	public static class EmbedField implements IEmbedField {

		protected String name;
		protected String value;
		protected boolean inline;

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public boolean isInline() {
			return inline;
		}

		public EmbedField(String name, String value, boolean inline) {
			this.name = name;
			this.value = value;
			this.inline = inline;
		}
	}

	/**
	 * Represents a site that provides media which is embedded in chat. Eg. Youtube, Imgur.
	 */
	public static class EmbedProvider implements IEmbedProvider {

		/**
		 * The name of the Embedded Media Provider
		 */
		protected String name;

		/**
		 * The url link to the Embedded Media Provider
		 */
		protected String url;

		public EmbedProvider(String name, String url) {
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
		public String getUrl() {
			return url;
		}
	}
}
