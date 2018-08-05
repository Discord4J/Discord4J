/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.data.stored.embed.*;
import discord4j.core.util.EntityUtil;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Discord embed.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#embed-object">Embed Object</a>
 */
public final class Embed implements DiscordObject {

    /** The ServiceMediator associated to this object. */
    private final ServiceMediator serviceMediator;

    /** The raw data as represented by Discord. */
    private final EmbedBean data;

    /**
     * Constructs an {@code Embed} with an associated ServiceMediator and Discord data.
     *
     * @param serviceMediator The ServiceMediator associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Embed(final ServiceMediator serviceMediator, final EmbedBean data) {
        this.serviceMediator = Objects.requireNonNull(serviceMediator);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DiscordClient getClient() {
        return serviceMediator.getClient();
    }

    /**
     * Gets the title of the embed, if present.
     *
     * @return The title of the embed, if present.
     */
    public Optional<String> getTitle() {
        return Optional.ofNullable(data.getTitle());
    }

    /**
     * Gets the type of embed, if present.
     *
     * @return The type of embed, if present.
     */
    public Type getType() {
        return Type.of(data.getType());
    }

    /**
     * Gets the description of the embed, if present.
     *
     * @return The description of the embed, if present.
     */
    public Optional<String> getDescription() {
        return Optional.ofNullable(data.getDescription());
    }

    /**
     * Gets the URL of the embed, if present.
     *
     * @return The URL of the embed, if present.
     */
    public Optional<String> getUrl() {
        return Optional.ofNullable(data.getUrl());
    }

    /**
     * Gets the timestamp of the embed content, if present.
     *
     * @return The timestamp of the embed content, if present.
     */
    public Optional<Instant> getTimestamp() {
        return Optional.ofNullable(data.getTimestamp())
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets the color of the embed, if present.
     *
     * @return The color of the embed, if present.
     */
    public Optional<Color> getColor() {
        return Optional.ofNullable(data.getColor()).map(color -> new Color(color, true));
    }

    /**
     * Gets the footer information, if present.
     *
     * @return The footer information, if present.
     */
    public Optional<Footer> getFooter() {
        return Optional.ofNullable(data.getFooter()).map(Footer::new);
    }

    /**
     * Gets the image information, if present.
     *
     * @return The image information, if present.
     */
    public Optional<Image> getImage() {
        return Optional.ofNullable(data.getImage()).map(Image::new);
    }

    /**
     * Gets the thumbnail information, if present.
     *
     * @return The thumbnail information, if present.
     */
    public Optional<Thumbnail> getThumbnail() {
        return Optional.ofNullable(data.getThumbnail()).map(Thumbnail::new);
    }

    /**
     * Gets the video information, if present.
     *
     * @return The video information, if present.
     */
    public Optional<Video> getVideo() {
        return Optional.ofNullable(data.getVideo()).map(Video::new);
    }

    /**
     * Gets the provider information, if present.
     *
     * @return The provider information, if present.
     */
    public Optional<Provider> getProvider() {
        return Optional.ofNullable(data.getProvider()).map(Provider::new);
    }

    /**
     * Gets the author information, if present.
     *
     * @return The author information, if present.
     */
    public Optional<Author> getAuthor() {
        return Optional.ofNullable(data.getAuthor()).map(Author::new);
    }

    /**
     * Gets the field information.
     *
     * @return The field information.
     */
    public List<Field> getFields() {
        final EmbedFieldBean[] fields = data.getFields();
        return (fields == null) ? Collections.emptyList() : Arrays.stream(fields)
                .map(Field::new)
                .collect(Collectors.toList());
    }

    /** Represents the various types of embeds. */
    public enum Type {

        /** An embed generated by an image. */
        IMAGE("image"),

        /** An embed generated by a link. */
        LINK("link"),

        /** An embed generated by rich content. */
        RICH("rich"),

        /** An embed generated by a video. */
        VIDEO("video");

        /** The underlying value as represented by Discord. */
        private final String value;

        /**
         * Constructs an {@code Embed.Type}.
         *
         * @param value The underlying value as represented by Discord.
         */
        Type(final String value) {
            this.value = value;
        }

        /**
         * Gets the underlying value as represented by Discord.
         *
         * @return The underlying value as represented by Discord.
         */
        public String getValue() {
            return value;
        }

        /**
         * Gets the type of embed. It is guaranteed that invoking {@link #getValue()} from the returned enum will equal
         * ({@link #equals(Object)}) the supplied {@code value}.
         *
         * @param value The underlying value as represented by Discord.
         * @return The type of embed.
         */
        public static Embed.Type of(final String value) {
            switch (value) {
                case "image": return IMAGE;
                case "link": return LINK;
                case "rich": return RICH;
                case "video": return VIDEO;
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }

    /** A footer for a Discord {@link Embed embed}. */
    public final class Footer {

        /** The raw data as represented by Discord. */
        private final EmbedFooterBean data;

        /**
         * Constructs a {@code Footer} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Footer(final EmbedFooterBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the footer text.
         *
         * @return The footer text.
         */
        public String getText() {
            return data.getText();
        }

        /**
         * The URL of the footer icon (only supports http(s) and attachments).
         *
         * @return The URL of the footer icon (only supports http(s) and attachments).
         */
        public String getIconUrl() {
            return data.getIconUrl();
        }

        /**
         * Gets a proxied URL of the footer icon.
         *
         * @return A proxied URL of the footer icon.
         */
        public String getProxyIconUrl() {
            return data.getProxyIconUrl();
        }
    }

    /** An image for a Discord {@link Embed embed}. */
    public final class Image {

        /** The raw data as represented by Discord. */
        private final EmbedImageBean data;

        /**
         * Constructs an {@code Image} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Image(final EmbedImageBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the source URL of the image (only supports http(s) and attachments).
         *
         * @return The source URL of the image (only supports http(s) and attachments).
         */
        public String getUrl() {
            return data.getUrl();
        }

        /**
         * Gets a proxied URL of the image.
         *
         * @return A proxied URL of the image.
         */
        public String getProxyUrl() {
            return data.getProxyUrl();
        }

        /**
         * Gets the height of the image.
         *
         * @return The height of the image.
         */
        public int getHeight() {
            return data.getHeight();
        }

        /**
         * Gets the width of the image.
         *
         * @return The width of the image.
         */
        public int getWidth() {
            return data.getWidth();
        }
    }

    /** A thumbnail for a Discord {@link Embed embed}. */
    public final class Thumbnail {

        /** The raw data as represented by Discord. */
        private final EmbedThumbnailBean data;

        /**
         * Constructs a {@code Thumbnail} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Thumbnail(final EmbedThumbnailBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the source URL of the thumbnail (only supports http(s) and attachments).
         *
         * @return The source URL of the thumbnail (only supports http(s) and attachments).
         */
        public String getUrl() {
            return data.getUrl();
        }

        /**
         * Gets a proxied URL of the thumbnail.
         *
         * @return A proxied URL of the thumbnail.
         */
        public String getProxyUrl() {
            return data.getProxyUrl();
        }

        /**
         * Gets the height of the thumbnail.
         *
         * @return The height of the thumbnail.
         */
        public int getHeight() {
            return data.getHeight();
        }

        /**
         * Gets the width of the thumbnail.
         *
         * @return The width of the thumbnail.
         */
        public int getWidth() {
            return data.getWidth();
        }
    }

    /** A video for a Discord {@link Embed embed}. */
    public final class Video {

        /** The raw data as represented by Discord. */
        private final EmbedVideoBean data;

        /**
         * Constructs a {@code Video} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Video(final EmbedVideoBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the source URL of the video.
         *
         * @return The source URL of the video.
         */
        public String getUrl() {
            return data.getUrl();
        }

        /**
         * Gets a proxied source URL of the video.
         *
         * @return A proxied source URL of the video.
         */
        public String getProxyUrl() {
            return data.getProxyUrl();
        }

        /**
         * Gets the height of the video.
         *
         * @return The height of the video.
         */
        public int getHeight() {
            return data.getHeight();
        }

        /**
         * Gets the width of the video.
         *
         * @return The width of the video.
         */
        public int getWidth() {
            return data.getWidth();
        }
    }

    /** A provider for a Discord {@link Embed embed}. */
    public final class Provider {

        /** The raw data as represented by Discord. */
        private final EmbedProviderBean data;

        /**
         * Constructs a {@code Provider} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Provider(final EmbedProviderBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the name of the provider.
         *
         * @return The name of the provider.
         */
        public String getName() {
            return data.getName();
        }

        /**
         * Gets the URL of the provider.
         *
         * @return The URL of the provider.
         */
        public String getUrl() {
            return data.getUrl();
        }
    }

    /** An image for a Discord {@link Embed embed}. */
    public final class Author {

        /** The raw data as represented by Discord. */
        private final EmbedAuthorBean data;

        /**
         * Constructs an {@code Author} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Author(final EmbedAuthorBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the name of the author.
         *
         * @return The name of the author.
         */
        public String getName() {
            return data.getName();
        }

        /**
         * Gets the URL of the author.
         *
         * @return The URL of the author.
         */
        public String getUrl() {
            return data.getUrl();
        }

        /**
         * Gets the URL of the author icon (only supports http(s) and attachments).
         *
         * @return The URL of the author icon (only supports http(s) and attachments).
         */
        public String getIconUrl() {
            return data.getIconUrl();
        }

        /**
         * Gets a proxied URL of the author icon.
         *
         * @return A proxied URL of the author icon.
         */
        public String getProxyIconUrl() {
            return data.getProxyIconUrl();
        }
    }

    /** A field for a Discord {@link Embed embed}. */
    public final class Field {

        /** The raw data as represented by Discord. */
        private final EmbedFieldBean data;

        /**
         * Constructs a {@code Field} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Field(final EmbedFieldBean data) {
            this.data = Objects.requireNonNull(data);
        }

        /**
         * Gets the {@code Embed} associated to this object.
         *
         * @return The {@code Embed} associated to this object.
         */
        public Embed getEmbed() {
            return Embed.this;
        }

        /**
         * Gets the name of the field.
         *
         * @return The name of the field.
         */
        public String getName() {
            return data.getName();
        }

        /**
         * Gets the value of the field.
         *
         * @return The value of the field.
         */
        public String getValue() {
            return data.getValue();
        }

        /**
         * Gets whether or not this field should display inline.
         *
         * @return {@code true} if this field should display inline, {@code false} otherwise.
         */
        public boolean isInline() {
            return data.isInline();
        }
    }
}
