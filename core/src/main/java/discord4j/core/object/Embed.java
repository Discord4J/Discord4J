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

import discord4j.discordjson.json.*;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.possible.Possible;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Discord embed.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#embed-object">Embed Object</a>
 */
public final class Embed implements DiscordObject {

    /** The maximum amount of characters that can be in an embed title. */
    public static final int MAX_TITLE_LENGTH = 256;

    /** The maximum amount of characters that can be in an embed description. */
    public static final int MAX_DESCRIPTION_LENGTH = 2048;

    /** The maximum amount of fields that can be appended to an embed. */
    public static final int MAX_FIELDS = 25;

    /** The maximum amount of total characters that can be present in an embed. */
    public static final int MAX_CHARACTER_LENGTH = 6000;

    /** The gateway associated to this object. */
    private final GatewayDiscordClient gateway;

    /** The raw data as represented by Discord. */
    private final EmbedData data;

    /**
     * Constructs an {@code Embed} with an associated ServiceMediator and Discord data.
     *
     * @param gateway The {@link GatewayDiscordClient} associated to this object, must be non-null.
     * @param data The raw data as represented by Discord, must be non-null.
     */
    public Embed(final GatewayDiscordClient gateway, final EmbedData data) {
        this.gateway = Objects.requireNonNull(gateway);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return gateway;
    }

    /**
     * Gets the title of the embed, if present.
     *
     * @return The title of the embed, if present.
     */
    public Optional<String> getTitle() {
        return data.title().toOptional();
    }

    /**
     * Gets the type of embed, if present.
     *
     * @return The type of embed, if present.
     */
    public Type getType() {
        // TODO FIXME is this actually Possible?
        return Type.of(data.type().get());
    }

    /**
     * Gets the description of the embed, if present.
     *
     * @return The description of the embed, if present.
     */
    public Optional<String> getDescription() {
        return data.description().toOptional();
    }

    /**
     * Gets the URL of the embed, if present.
     *
     * @return The URL of the embed, if present.
     */
    public Optional<String> getUrl() {
        return data.url().toOptional();
    }

    /**
     * Gets the timestamp of the embed content, if present.
     *
     * @return The timestamp of the embed content, if present.
     */
    public Optional<Instant> getTimestamp() {
        return data.timestamp().toOptional()
                .map(timestamp -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp, Instant::from));
    }

    /**
     * Gets the color of the embed, if present.
     *
     * @return The color of the embed, if present.
     */
    public Optional<Color> getColor() {
        return data.color().toOptional().map(color -> new Color(color, true));
    }

    /**
     * Gets the footer information, if present.
     *
     * @return The footer information, if present.
     */
    public Optional<Footer> getFooter() {
        return data.footer().toOptional().map(Footer::new);
    }

    /**
     * Gets the image information, if present.
     *
     * @return The image information, if present.
     */
    public Optional<Image> getImage() {
        return data.image().toOptional().map(Image::new);
    }

    /**
     * Gets the thumbnail information, if present.
     *
     * @return The thumbnail information, if present.
     */
    public Optional<Thumbnail> getThumbnail() {
        return data.thumbnail().toOptional().map(Thumbnail::new);
    }

    /**
     * Gets the video information, if present.
     *
     * @return The video information, if present.
     */
    public Optional<Video> getVideo() {
        return data.video().toOptional().map(Video::new);
    }

    /**
     * Gets the provider information, if present.
     *
     * @return The provider information, if present.
     */
    public Optional<Provider> getProvider() {
        return data.provider().toOptional().map(Provider::new);
    }

    /**
     * Gets the author information, if present.
     *
     * @return The author information, if present.
     */
    public Optional<Author> getAuthor() {
        return data.author().toOptional().map(Author::new);
    }

    /**
     * Gets the field information.
     *
     * @return The field information.
     */
    public List<Field> getFields() {
        return data.fields().toOptional()
            .map(fields -> fields.stream().map(Field::new).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    /** Represents the various types of embeds. */
    public enum Type {

        /** Unknown type. */
        UNKNOWN("UNKNOWN"),

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
                default: return UNKNOWN;
            }
        }
    }

    /** A footer for a Discord {@link Embed embed}. */
    public final class Footer {

        /** The maximum amount of characters that can be in a footer text. */
        public static final int MAX_TEXT_LENGTH = 2048;

        /** The raw data as represented by Discord. */
        private final EmbedFooterData data;

        /**
         * Constructs a {@code Footer} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Footer(final EmbedFooterData data) {
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
            return data.text();
        }

        /**
         * The URL of the footer icon (only supports http(s) and attachments).
         *
         * @return The URL of the footer icon (only supports http(s) and attachments).
         */
        public String getIconUrl() {
            // TODO FIXME: is this actually Possible?
            return data.iconUrl().get();
        }

        /**
         * Gets a proxied URL of the footer icon.
         *
         * @return A proxied URL of the footer icon.
         */
        public String getProxyIconUrl() {
            // TODO FIXME: is this actually Possible?
            return data.proxyIconUrl().get();
        }
    }

    /** An image for a Discord {@link Embed embed}. */
    public final class Image {

        /** The raw data as represented by Discord. */
        private final EmbedImageData data;

        /**
         * Constructs an {@code Image} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Image(final EmbedImageData data) {
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
            // TODO FIXME: is this actually Possible?
            return data.url().get();
        }

        /**
         * Gets a proxied URL of the image.
         *
         * @return A proxied URL of the image.
         */
        public String getProxyUrl() {
            // TODO FIXME: is this actually Possible?
            return data.proxyUrl().get();
        }

        /**
         * Gets the height of the image.
         *
         * @return The height of the image.
         */
        public int getHeight() {
            // TODO FIXME: is this actually Possible?
            return data.height().get();
        }

        /**
         * Gets the width of the image.
         *
         * @return The width of the image.
         */
        public int getWidth() {
            // TODO FIXME: is this actually Possible?
            return data.width().get();
        }
    }

    /** A thumbnail for a Discord {@link Embed embed}. */
    public final class Thumbnail {

        /** The raw data as represented by Discord. */
        private final EmbedThumbnailData data;

        /**
         * Constructs a {@code Thumbnail} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Thumbnail(final EmbedThumbnailData data) {
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
            // TODO FIXME: is this actually Possible?
            return data.url().get();
        }

        /**
         * Gets a proxied URL of the thumbnail.
         *
         * @return A proxied URL of the thumbnail.
         */
        public String getProxyUrl() {
            // TODO FIXME: is this actually Possible?
            return data.proxyUrl().get();
        }

        /**
         * Gets the height of the thumbnail.
         *
         * @return The height of the thumbnail.
         */
        public int getHeight() {
            // TODO FIXME: is this actually Possible?
            return data.height().get();
        }

        /**
         * Gets the width of the thumbnail.
         *
         * @return The width of the thumbnail.
         */
        public int getWidth() {
            // TODO FIXME: is this actually Possible?
            return data.width().get();
        }
    }

    /** A video for a Discord {@link Embed embed}. */
    public final class Video {

        /** The raw data as represented by Discord. */
        private final EmbedVideoData data;

        /**
         * Constructs a {@code Video} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Video(final EmbedVideoData data) {
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
            // TODO FIXME: is this actually Possible?
            return data.url().get();
        }

        /**
         * Gets the height of the video.
         *
         * @return The height of the video.
         */
        public int getHeight() {
            // TODO FIXME: is this actually Possible?
            return data.height().get();
        }

        /**
         * Gets the width of the video.
         *
         * @return The width of the video.
         */
        public int getWidth() {
            // TODO FIXME: is this actually Possible?
            return data.width().get();
        }
    }

    /** A provider for a Discord {@link Embed embed}. */
    public final class Provider {

        /** The raw data as represented by Discord. */
        private final EmbedProviderData data;

        /**
         * Constructs a {@code Provider} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Provider(final EmbedProviderData data) {
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
            // TODO FIXME: is this actually Possible?
            return data.name().get();
        }

        /**
         * Gets the URL of the provider.
         *
         * @return The URL of the provider.
         */
        public Optional<String> getUrl() {
            // TODO FIXME: is this actually Possible?
            return Possible.flatOpt(data.url());
        }
    }

    /** An image for a Discord {@link Embed embed}. */
    public final class Author {

        /** The maximum amount of characters that can be in an author's name. */
        public static final int MAX_NAME_LENGTH = 256;

        /** The raw data as represented by Discord. */
        private final EmbedAuthorData data;

        /**
         * Constructs an {@code Author} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Author(final EmbedAuthorData data) {
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
            // TODO FIXME: is this actually Possible?
            return data.name().get();
        }

        /**
         * Gets the URL of the author.
         *
         * @return The URL of the author.
         */
        public String getUrl() {
            // TODO FIXME: is this actually Possible?
            return data.url().get();
        }

        /**
         * Gets the URL of the author icon (only supports http(s) and attachments).
         *
         * @return The URL of the author icon (only supports http(s) and attachments).
         */
        public String getIconUrl() {
            // TODO FIXME: is this actually Possible?
            return data.iconUrl().get();
        }

        /**
         * Gets a proxied URL of the author icon.
         *
         * @return A proxied URL of the author icon.
         */
        public String getProxyIconUrl() {
            // TODO FIXME: is this actually Possible?
            return data.proxyIconUrl().get();
        }
    }

    /** A field for a Discord {@link Embed embed}. */
    public final class Field {

        /** The maximum amount of characters that can be in a field name. */
        public static final int MAX_NAME_LENGTH = 256;

        /** The maximum amount of characters that can be in a field value. */
        public static final int MAX_VALUE_LENGTH = 1024;

        /** The raw data as represented by Discord. */
        private final EmbedFieldData data;

        /**
         * Constructs a {@code Field} with data as represented by Discord.
         *
         * @param data The raw data as represented by Discord, must be non-null.
         */
        private Field(final EmbedFieldData data) {
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
            return data.name();
        }

        /**
         * Gets the value of the field.
         *
         * @return The value of the field.
         */
        public String getValue() {
            return data.value();
        }

        /**
         * Gets whether or not this field should display inline.
         *
         * @return {@code true} if this field should display inline, {@code false} otherwise.
         */
        public boolean isInline() {
            // TODO FIXME: is this actually Possible?
            return data.inline().get();
        }
    }

    @Override
    public String toString() {
        return "Embed{" +
                "data=" + data +
                '}';
    }
}
