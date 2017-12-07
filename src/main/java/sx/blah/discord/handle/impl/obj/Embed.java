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

package sx.blah.discord.handle.impl.obj;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IEmbed;

import java.awt.Color;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The default implementation of {@link IEmbed}.
 */
public class Embed implements IEmbed {
	/**
	 * The nullable title of the embed.
	 */
	protected final String title;

	/**
	 * The type of embed.
	 */
	protected final String type;

	/**
	 * The nullable description of the embed.
	 */
	protected final String description;

	/**
	 * The nullable URL of the embed.
	 */
	protected final String url;

	/**
	 * The nullable thumbnail of the embed.
	 */
	protected final IEmbedImage thumbnail;

	/**
	 * The nullable provider of the embed.
	 */
	protected final IEmbedProvider provider;

	/**
	 * The timestamp of the embed.
	 */
	protected final Instant timestamp;

	/**
	 * The color of the embed.
	 */
	protected final Color color;

	/**
	 * The nullable footer of the embed.
	 */
	protected final IEmbedFooter footer;

	/**
	 * The nullable image of the embed.
	 */
	protected final IEmbedImage image;

	/**
	 * The nullable video of the embed.
	 */
	protected final IEmbedVideo video;

	/**
	 * The nullable author of the embed.
	 */
	protected final IEmbedAuthor author;

	/**
	 * The list of fields in the embed.
	 */
	protected final List<IEmbedField> embedFields;

	public Embed(String title, String type, String description, String url, EmbedObject.ThumbnailObject thumbnail, EmbedObject.ProviderObject provider, Instant timestamp, Color color, EmbedObject.FooterObject footer, EmbedObject.ImageObject image, EmbedObject.VideoObject video, EmbedObject.AuthorObject author, EmbedObject.EmbedFieldObject[] embedFields) {
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

	public Embed(String title, String type, String description, String url, String thumbnailUrl, IEmbedProvider provider, Instant timestamp, Color color, IEmbedFooter footer, String imageUrl, String videoUrl, IEmbedAuthor author, IEmbedField[] embedFields) {
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
	public Instant getTimestamp() {
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

	/**
	 * The default implementation of {@link sx.blah.discord.handle.obj.IEmbed.IEmbedImage}.
	 */
	public static class EmbedImage implements IEmbedImage {

		/**
		 * The image's URL.
		 */
		protected String url;
		/**
		 * The image's height.
		 */
		protected int height;
		/**
		 * The image's width
		 */
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

	/**
	 * The default implementation of {@link sx.blah.discord.handle.obj.IEmbed.IEmbedVideo}.
	 */
	public static class EmbedVideo implements IEmbedVideo {

		/**
		 * The video's URL.
		 */
		protected String url;
		/**
		 * The video's height.
		 */
		protected int height;
		/**
		 * The video's width.
		 */
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

	/**
	 * The default implementation of {@link sx.blah.discord.handle.obj.IEmbed.IEmbedFooter}.
	 */
	public static class EmbedFooter implements IEmbedFooter {

		/**
		 * The footer's text.
		 */
		protected String text;

		/**
		 * The footer's icon URL.
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
	 * The default implementation of {@link sx.blah.discord.handle.obj.IEmbed.IEmbedAuthor}.
	 */
	public static class EmbedAuthor implements IEmbedAuthor {

		/**
		 * The author's name.
		 */
		protected String name;
		/**
		 * The author's URL.
		 */
		protected String url;
		/**
		 * The author's icon URL.
		 */
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

	/**
	 * The default implementation of {@link sx.blah.discord.handle.obj.IEmbed.IEmbedField}.
	 */
	public static class EmbedField implements IEmbedField {

		/**
		 * The field's name.
		 */
		protected String name;
		/**
		 * The field's value.
		 */
		protected String value;
		/**
		 * Whether the field is inline.
		 */
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
	 * The default implementation of {@link sx.blah.discord.handle.obj.IEmbed.IEmbedProvider}.
	 */
	public static class EmbedProvider implements IEmbedProvider {

		/**
		 * The provider's name.
		 */
		protected String name;

		/**
		 * The provider's URL.
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
