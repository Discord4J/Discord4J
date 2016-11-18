package sx.blah.discord.api.internal.json.objects;

import sx.blah.discord.handle.obj.IEmbed;

import java.awt.*;
import java.util.stream.Collectors;

public class EmbedObject {
    public String title;
    public String type;
    public String description;
    public String url;
    public String timestamp;
    public int color;
    public FooterObject footer;
    public ImageObject image;
    public ThumbnailObject thumbnail;
    public VideoObject video;
    public ProviderObject provider;
    public AuthorObject author;
    public EmbedFieldObject[] fields;

	/**
	 * Please use EmbedBuilder to build these objects.
	 * @see sx.blah.discord.util.EmbedBuilder
	 */
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

	/**
	 * Please use EmbedBuilder to build these objects.
	 * @see sx.blah.discord.util.EmbedBuilder
	 */
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

    public static class ThumbnailObject {
        public String url;
        public String proxy_url;
        public int height;
        public int width;

        public ThumbnailObject(String url, String proxy_url, int height, int width) {
            this.url = url;
            this.proxy_url = proxy_url;
            this.height = height;
            this.width = width;
        }
    }

    public static class VideoObject {
        public String url;
        public int height;
        public int width;

        public VideoObject(String url, int height, int width) {
            this.url = url;
            this.height = height;
            this.width = width;
        }
    }

    public static class ImageObject {
        public String url;
        public String proxy_url;
        public int height;
        public int width;

        public ImageObject(String url, String proxy_url, int height, int width) {
            this.url = url;
            this.proxy_url = proxy_url;
            this.height = height;
            this.width = width;
        }
    }

    public static class ProviderObject {
        public String name;
        public String url;

        public ProviderObject(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public static class AuthorObject {
        public String name;
        public String url;
        public String icon_url;
        public String proxy_icon_url;

        public AuthorObject(String name, String url, String icon_url, String proxy_icon_url) {
            this.name = name;
            this.url = url;
            this.icon_url = icon_url;
            this.proxy_icon_url = proxy_icon_url;
        }
    }

    public static class FooterObject {
        public String text;
        public String icon_url;
        public String proxy_icon_url;

        public FooterObject(String text, String icon_url, String proxy_icon_url) {
            this.text = text;
            this.icon_url = icon_url;
            this.proxy_icon_url = proxy_icon_url;
        }
    }

    public static class EmbedFieldObject {
        public String name;
        public String value;
        public boolean inline;

        public EmbedFieldObject(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }
}
