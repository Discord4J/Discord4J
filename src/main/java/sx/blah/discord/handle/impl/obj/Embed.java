package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IEmbed;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Embed implements IEmbed {
	/**
	 * The title of the embed.
	 */
	protected final String title;

	/**
	 * The type of embed.
	 */
	protected final String type;

	/**
	 * The description of the embed.
	 */
	protected final String description;

	/**
	 * The download link for the embed.
	 */
	protected final String url;

	/**
	 * The object containing the image for the thumbnail for the embed.
	 */
	protected final IEmbedImage thumbnail;

	/**
	 * The object containing information about the provider of the embed.
	 */
	protected final IEmbedProvider provider;

	/**
	 * The timestamp for the embed.
	 */
	protected final LocalDateTime timestamp;

	/**
	 * The colored strip for the embed.
	 */
	protected final Color color;

	/**
	 * The object containing information about the footer of the embed.
	 */
	protected final IEmbedFooter footer;

	/**
	 * The object containing information about the image of the embed.
	 */
	protected final IEmbedImage image;

	/**
	 * The object containing information about the video of the embed.
	 */
	protected final IEmbedVideo video;

	/**
	 * The object containing information about the author for the embed.
	 */
	protected final IEmbedAuthor author;

	/**
	 * A list of objects containing information about fields in the embed
	 */
	protected final List<IEmbedField> embedFields;

	public Embed(String title, String type, String description, String url, EmbedObject.ThumbnailObject thumbnail, EmbedObject.ProviderObject provider, LocalDateTime timestamp, Color color, EmbedObject.FooterObject footer, EmbedObject.ImageObject image, EmbedObject.VideoObject video, EmbedObject.AuthorObject author, EmbedObject.EmbedFieldObject[] embedFields) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.url = url;
		if (thumbnail == null) {
			this.thumbnail = null;
		} else {
			this.thumbnail = new EmbedImage(thumbnail.url, thumbnail.height, thumbnail.width);
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
			this.image = new EmbedImage(image.url, image.height, image.width);
		if (video == null)
			this.video = null;
		else
			this.video = new EmbedVideo(video.url, video.height, video.width);
		if (author == null)
			this.author = null;
		else
			this.author = new EmbedAuthor(author.name, author.url, author.icon_url);

		if (embedFields == null || embedFields.length == 0) {
			this.embedFields = null;
		} else {
			this.embedFields = new CopyOnWriteArrayList<>();

			for (EmbedObject.EmbedFieldObject embedField : embedFields)
				this.embedFields.add(new EmbedField(embedField.name, embedField.value, embedField.inline));
		}
	}

	public Embed(String title, String type, String description, String url, String thumbnailUrl, IEmbedProvider provider, LocalDateTime timestamp, Color color, IEmbedFooter footer, String imageUrl, String videoUrl, IEmbedAuthor author, IEmbedField[] embedFields) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.url = url;
		this.thumbnail = new EmbedImage(thumbnailUrl, 0, 0);
		this.provider = provider;
		this.timestamp = timestamp;
		this.color = color;
		this.footer = footer;
		this.image = new EmbedImage(imageUrl, 0, 0);
		this.video = null;
		this.author = author;

		if (embedFields == null || embedFields.length == 0) {
			this.embedFields = null;
		} else {
			this.embedFields = new CopyOnWriteArrayList<>();

			Collections.addAll(this.embedFields, embedFields);
		}
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
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
	public IEmbedImage getImage() {
		return image;
	}

	@Override
	public IEmbedImage getThumbnail() {
		return thumbnail;
	}

	@Override
	public IEmbedVideo getVideo() {
		return video;
	}

	@Override
	public IEmbedProvider getEmbedProvider() {
		return provider;
	}

	@Override
	public IEmbedAuthor getAuthor() {
		return author;
	}

	@Override
	public List<IEmbedField> getEmbedFields() {
		return embedFields;
	}

	public static class EmbedImage implements IEmbedImage {

		protected String url;
		protected int height;
		protected int width;

		public EmbedImage(String url, int height, int width) {
			this.url = url;
			this.height = height;
			this.width = width;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getWidth() {
			return width;
		}
	}

	public static class EmbedVideo implements IEmbedVideo {

		protected String url;
		protected int height;
		protected int width;

		public EmbedVideo(String url, int height, int width) {
			this.url = url;
			this.height = height;
			this.width = width;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int getWidth() {
			return width;
		}
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

		@Override
		public String getText() {
			return text;
		}

		@Override
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

		public EmbedAuthor(String name, String url, String icon_url) {
			this.name = name;
			this.url = url;
			this.icon_url = icon_url;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public String getIconUrl() {
			return icon_url;
		}
	}

	public static class EmbedField implements IEmbedField {

		protected String name;
		protected String value;
		protected boolean inline;

		public EmbedField(String name, String value, boolean inline) {
			this.name = name;
			this.value = value;
			this.inline = inline;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public boolean isInline() {
			return inline;
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

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getUrl() {
			return url;
		}
	}
}