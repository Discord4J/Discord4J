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
package discord4j.core.spec;

import discord4j.common.json.EmbedFieldEntity;
import discord4j.rest.json.request.*;
import reactor.util.annotation.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A spec used to configure and send an embed.
 * @see <a href="https://i.stack.imgur.com/HRWHk.png">Embed Layout</a>
 */
public class EmbedCreateSpec implements Spec<EmbedRequest> {

    private final EmbedRequest.Builder requestBuilder = EmbedRequest.builder();
    private final List<EmbedFieldEntity> fields = new ArrayList<>();

    /**
     * Sets the title of the embed.
     *
     * @param title The title of the embed.
     * @return This spec.
     */
    public EmbedCreateSpec setTitle(String title) {
        requestBuilder.title(title);
        return this;
    }

    /**
     * Sets the description of the embed.
     *
     * @param description The description of the embed.
     * @return This spec.
     */
    public EmbedCreateSpec setDescription(String description) {
        requestBuilder.description(description);
        return this;
    }

    /**
     * Sets the URL of the embed.
     *
     * @param url A URL which can be clicked on through the title of the embed.
     * @return This spec.
     */
    public EmbedCreateSpec setUrl(String url) {
        requestBuilder.url(url);
        return this;
    }

    /**
     * Sets the timestamp to display in the embed. The timestamp is displayed locally for each user's timezone.
     *
     * @param timestamp A {@link Instant} to display in the embed footer.
     * @return This spec.
     */
    public EmbedCreateSpec setTimestamp(Instant timestamp) {
        requestBuilder.timestamp(DateTimeFormatter.ISO_INSTANT.format(timestamp));
        return this;
    }

    /**
     * Sets the color of the embed.
     *
     * @param color A {@link Color} to display on the embed.
     * @return This spec.
     */
    public EmbedCreateSpec setColor(final Color color) {
        setColor(color.getRGB());
    }
    
    /**
     * Sets the color of the embed.
     *
     * @param color An RGB color to display on the embed.
     * @return This spec.
     */
    public EmbedCreateSpec setColor(int color) {
        requestBuilder.color(color & 0xFFFFFF);
        return this;
    }

    /**
     * Sets the footer of the embed.
     *
     * @param text The footer text.
     * @param iconUrl An icon URL to display in the footer.
     * @return This spec.
     */
    public EmbedCreateSpec setFooter(String text, @Nullable String iconUrl) {
        requestBuilder.footer(new EmbedFooterRequest(text, iconUrl));
        return this;
    }

    /**
     * Sets the image of the embed.
     *
     * @param url An image URL.
     * @return This spec.
     */
    public EmbedCreateSpec setImage(String url) {
        requestBuilder.image(new EmbedImageRequest(url));
        return this;
    }

    /**
     * Sets the thumbnail of the embed.
     *
     * @param url An image URL.
     * @return This spec.
     */
    public EmbedCreateSpec setThumbnail(String url) {
        requestBuilder.thumbnail(new EmbedThumbnailRequest(url));
        return this;
    }

    /**
     * Sets the author section of the embed.
     *
     * @param name The author name to display.
     * @param url A URL which can be clicked on through the author name.
     * @param iconUrl An icon URL displayed as the avatar next to the author name.
     * @return This spec.
     */
    public EmbedCreateSpec setAuthor(String name, @Nullable String url, @Nullable String iconUrl) {
        requestBuilder.author(new EmbedAuthorRequest(name, url, iconUrl));
        return this;
    }

    /**
     * Adds a field to the embed.
     *
     * @param name The name of the field.
     * @param value The text inside of the field.
     * @param inline Whether to inline the field or not.
     * @return This spec.
     */
    public EmbedCreateSpec addField(String name, String value, boolean inline) {
        this.fields.add(new EmbedFieldEntity(name, value, inline));
        return this;
    }

    @Override
    public EmbedRequest asRequest() {
        requestBuilder.fields(this.fields.toArray(new EmbedFieldEntity[this.fields.size()]));
        return requestBuilder.build();
    }
}
