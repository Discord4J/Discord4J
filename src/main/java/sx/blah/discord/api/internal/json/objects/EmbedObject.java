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

package sx.blah.discord.api.internal.json.objects;

import sx.blah.discord.handle.obj.IEmbed;

import java.awt.Color;
import java.util.stream.Collectors;

/**
 * Represents a json embed object. Use {@link sx.blah.discord.util.EmbedBuilder} to build these objects.
 *
 * @see sx.blah.discord.util.EmbedBuilder
 */
public class EmbedObject {
	/**
	 * The title of the embed.
	 */
	public String title;
	/**
	 * The type of the embed.
	 */
	public String type;
	/**
	 * The description of the embed.
	 */
	public String description;
	/**
	 * The URL of the embed.
	 */
	public String url;
	/**
	 * The timestamp of the embed.
	 */
	public String timestamp;
	/**
	 * The side color of the embed.
	 */
	public int color;
	/**
	 * The footer of the embed.
	 */
	public FooterObject footer;
	/**
	 * The image of the embed.
	 */
	public ImageObject image;
	/**
	 * The thumbnail of the embed.
	 */
	public ThumbnailObject thumbnail;
	/**
	 * The video of the embed.
	 */
	public VideoObject video;
	/**
	 * The provider of the embed.
	 */
	public ProviderObject provider;
	/**
	 * The author of the embed.
	 */
	public AuthorObject author;
	/**
	 * The fields of the embed.
	 */
	public EmbedFieldObject[] fields;

	public EmbedObject() {
	}

	public EmbedObject(String title, String type, String description, String url, String timestamp, int color, FooterObject footer, ImageObject image, ThumbnailObject thumbnail, VideoObject video, ProviderObject provider, AuthorObject author, EmbedFieldObject[] fields) {
		this.title = title;
		this.type = type;
		this.description = description;
		this.url = url;
		this.timestamp = timestamp;
		this.color = color;
		this.footer = footer;
		this.image = image;
		this.thumbnail = thumbnail;
		this.video = video;
		this.provider = provider;
		this.author = author;
		this.fields = fields;
	}

	public EmbedObject(IEmbed embed) {
		this.title = embed.getTitle();
		this.type = embed.getType();
		this.description = embed.getDescription();
		this.url = embed.getUrl();
		if (embed.getTimestamp() == null)
			this.timestamp = null;
		else
			this.timestamp = embed.getTimestamp().toString();
		if (embed.getColor() == null)
			this.color = new Color(0, 0, 0).getRGB() & 0x00ffffff;
		else
			this.color = embed.getColor().getRGB() & 0x00ffffff;
		if (embed.getFooter() == null)
			this.footer = null;
		else
			this.footer = new FooterObject(embed.getFooter().getText(), embed.getFooter().getIconUrl(), null);
		if (embed.getImage() == null)
			this.image = null;
		else
			this.image = new ImageObject(embed.getImage().getUrl(), null, embed.getImage().getHeight(), embed.getImage().getWidth());
		if (embed.getThumbnail() == null)
			this.thumbnail = null;
		else
			this.thumbnail = new ThumbnailObject(embed.getThumbnail().getUrl(), null, embed.getThumbnail().getHeight(), embed.getThumbnail().getWidth());
		if (embed.getVideo() == null)
			this.video = null;
		else
			this.video = new VideoObject(embed.getVideo().getUrl(), embed.getVideo().getHeight(), embed.getVideo().getWidth());
		if (embed.getEmbedProvider() == null)
			this.provider = null;
		else
			this.provider = new ProviderObject(embed.getEmbedProvider().getName(), embed.getEmbedProvider().getUrl());
		if (embed.getAuthor() == null)
			this.author = null;
		else
			this.author = new AuthorObject(embed.getAuthor().getName(), embed.getAuthor().getUrl(), embed.getAuthor().getIconUrl(), null);
		if (embed.getEmbedFields() == null)
			this.fields = null;
		else
			this.fields = embed.getEmbedFields()
					.stream()
					.map(field -> new EmbedFieldObject(field.getName(), field.getValue(), field.isInline()))
					.collect(Collectors.toList())
					.toArray(new EmbedFieldObject[0]);
	}

	/**
	 * Represents a json thumbnail object.
	 */
	public static class ThumbnailObject {
		/**
		 * The URL of the thumbnail.
		 */
		public String url;
		/**
		 * The proxied URL of the thumbnail.
		 */
		public String proxy_url;
		/**
		 * The height of the thumbnail.
		 */
		public int height;
		/**
		 * The width of the thumbnail.
		 */
		public int width;

		public ThumbnailObject() {
		}

		public ThumbnailObject(String url, String proxy_url, int height, int width) {
			this.url = url;
			this.proxy_url = proxy_url;
			this.height = height;
			this.width = width;
		}
	}

	/**
	 * Represents a json video object.
	 */
	public static class VideoObject {
		/**
		 * The URL of the video.
		 */
		public String url;
		/**
		 * The height of the video.
		 */
		public int height;
		/**
		 * The width of the video.
		 */
		public int width;

		public VideoObject() {
		}

		public VideoObject(String url, int height, int width) {
			this.url = url;

			this.height = height;
			this.width = width;
		}
	}

	/**
	 * Represents a json image object.
	 */
	public static class ImageObject {
		/**
		 * The URL of the image.
		 */
		public String url;
		/**
		 * The proxied URL of the image.
		 */
		public String proxy_url;
		/**
		 * The height of the image.
		 */
		public int height;
		/**
		 * The width of the image.
		 */
		public int width;

		public ImageObject() {
		}

		public ImageObject(String url, String proxy_url, int height, int width) {
			this.url = url;
			this.proxy_url = proxy_url;
			this.height = height;
			this.width = width;
		}
	}

	/**
	 * Represents a json provider object.
	 */
	public static class ProviderObject {
		/**
		 * The name of the provider.
		 */
		public String name;
		/**
		 * The URL of the provider.
		 */
		public String url;

		public ProviderObject() {
		}

		public ProviderObject(String name, String url) {
			this.name = name;
			this.url = url;
		}
	}

	/**
	 * Represents a json author object.
	 */
	public static class AuthorObject {
		/**
		 * The name of the author.
		 */
		public String name;
		/**
		 * The URL of the author.
		 */
		public String url;
		/**
		 * The URL of the author icon.
		 */
		public String icon_url;
		/**
		 * The proxied URL of the author icon.
		 */
		public String proxy_icon_url;

		public AuthorObject() {
		}

		public AuthorObject(String name, String url, String icon_url, String proxy_icon_url) {
			this.name = name;
			this.url = url;
			this.icon_url = icon_url;
			this.proxy_icon_url = proxy_icon_url;
		}
	}

	/**
	 * Represents a json footer object.
	 */
	public static class FooterObject {
		/**
		 * The text in the footer.
		 */
		public String text;
		/**
		 * The URL of the icon in the footer.
		 */
		public String icon_url;
		/**
		 * The proxied URL of the icon in the footer.
		 */
		public String proxy_icon_url;

		public FooterObject() {
		}

		public FooterObject(String text, String icon_url, String proxy_icon_url) {
			this.text = text;
			this.icon_url = icon_url;
			this.proxy_icon_url = proxy_icon_url;
		}
	}

	/**
	 * Represents a json field object.
	 */
	public static class EmbedFieldObject {
		/**
		 * The name of the field.
		 */
		public String name;
		/**
		 * The content in the field.
		 */
		public String value;
		/**
		 * Whether the field should be displayed inline.
		 */
		public boolean inline;

		public EmbedFieldObject() {
		}

		public EmbedFieldObject(String name, String value, boolean inline) {
			this.name = name;
			this.value = value;
			this.inline = inline;
		}
	}
}
