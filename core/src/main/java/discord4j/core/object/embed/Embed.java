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
package discord4j.core.object.embed;

import discord4j.core.DiscordClient;
import discord4j.core.ServiceMediator;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.data.stored.embed.EmbedBean;
import discord4j.core.object.data.stored.embed.EmbedFieldBean;
import discord4j.core.util.EntityUtil;

import java.time.Instant;
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
        return Optional.ofNullable(data.getTimestamp()).map(Instant::parse);
    }

    /**
     * Gets the color code of the embed, if present.
     *
     * @return The color code of the embed, if present.
     */
    public OptionalInt getColor() {
        final Integer color = data.getColor();
        return (color == null) ? OptionalInt.empty() : OptionalInt.of(color);
    }

    /**
     * Gets the footer information, if present.
     *
     * @return The footer information, if present.
     */
    public Optional<EmbedFooter> getFooter() {
        return Optional.ofNullable(data.getFooter()).map(bean -> new EmbedFooter(serviceMediator, bean));
    }

    /**
     * Gets the image information, if present.
     *
     * @return The image information, if present.
     */
    public Optional<EmbedImage> getImage() {
        return Optional.ofNullable(data.getImage()).map(bean -> new EmbedImage(serviceMediator, bean));
    }

    /**
     * Gets the thumbnail information, if present.
     *
     * @return The thumbnail information, if present.
     */
    public Optional<EmbedThumbnail> getThumbnail() {
        return Optional.ofNullable(data.getThumbnail()).map(bean -> new EmbedThumbnail(serviceMediator, bean));
    }

    /**
     * Gets the video information, if present.
     *
     * @return The video information, if present.
     */
    public Optional<EmbedVideo> getVideo() {
        return Optional.ofNullable(data.getVideo()).map(bean -> new EmbedVideo(serviceMediator, bean));
    }

    /**
     * Gets the provider information, if present.
     *
     * @return The provider information, if present.
     */
    public Optional<EmbedProvider> getProvider() {
        return Optional.ofNullable(data.getProvider()).map(bean -> new EmbedProvider(serviceMediator, bean));
    }

    /**
     * Gets the author information, if present.
     *
     * @return The author information, if present.
     */
    public Optional<EmbedAuthor> getAuthor() {
        return Optional.ofNullable(data.getAuthor()).map(bean -> new EmbedAuthor(serviceMediator, bean));
    }

    /**
     * Gets the field information.
     *
     * @return The field information.
     */
    public List<EmbedField> getFields() {
        final EmbedFieldBean[] fields = data.getFields();
        return (fields == null) ? Collections.emptyList() : Arrays.stream(fields)
                .map(bean -> new EmbedField(serviceMediator, bean))
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
         * Constructs a {@code Embed.Type}.
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
        public static Type of(final String value) {
            switch (value) {
                case "image": return IMAGE;
                case "link": return LINK;
                case "rich": return RICH;
                case "video": return VIDEO;
                default: return EntityUtil.throwUnsupportedDiscordValue(value);
            }
        }
    }
}
