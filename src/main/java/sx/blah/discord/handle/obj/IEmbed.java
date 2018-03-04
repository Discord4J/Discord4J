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

package sx.blah.discord.handle.obj;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * An embed in a message.
 */
public interface IEmbed {

    /**
     * Gets the title of the embed.
     *
     * @return The nullable title of the embed.
     */
    String getTitle();

    /**
     * Gets the type of embed.
     *
     * @return The type of embed.
     */
    String getType();

    /**
     * Gets the description of the embed.
     *
     * @return The nullable description of the embed.
     */
    String getDescription();

    /**
     * Gets the URL of the embed.
     *
     * @return The nullable URL of the embed.
     */
    String getUrl();

    /**
     * Gets the timestamp of the embed.
     *
     * @return The timestamp of the embed.
     */
    Instant getTimestamp();

    /**
     * Gets the color of the embed.
     *
     * @return The color of the embed.
     */
    Color getColor();

    /**
     * Gets the footer of the embed.
     *
     * @return The nullable footer of the embed.
     */
    IEmbedFooter getFooter();

    /**
     * Gets the image of the embed.
     *
     * @return The nullable image of the embed.
     */
    IEmbedImage getImage();

    /**
     * Gets the thumbnail of the embed.
     *
     * @return The nullable thumbnail of the embed.
     */
    IEmbedImage getThumbnail();

    /**
     * Gets the video of the embed media.
     *
     * @return The nullable video of the embed.
     */
    IEmbedVideo getVideo();

    /**
     * Gets the provider of the embed.
     *
     * @return The nullable provider of the embed.
     */
    IEmbedProvider getEmbedProvider();

    /**
     * Gets the author of the embed.
     *
     * @return The nullable author of the embed.
     */
    IEmbedAuthor getAuthor();

    /**
     * Gets the list of fields in the embed.
     *
     * @return The list of fields in the embed.
     */
    List<IEmbedField> getEmbedFields();

    /**
     * An embed image object.
     */
    interface IEmbedImage {
        /**
         * Gets the image's URL.
         *
         * @return The image's URL.
         */
        String getUrl();

        /**
         * Gets the image's height.
         *
         * @return The image's height.
         */
        int getHeight();

        /**
         * Gets the image's width.
         *
         * @return The image's width.
         */
        int getWidth();
    }

    /**
     * An embed video object.
     */
    interface IEmbedVideo {
        /**
         * Gets the video's URL.
         *
         * @return The video's URL.
         */
        String getUrl();

        /**
         * Gets the video's height.
         *
         * @return The video's height.
         */
        int getHeight();

        /**
         * Gets the video's width.
         *
         * @return The video's width.
         */
        int getWidth();
    }

    /**
     * An embed footer object.
     */
    interface IEmbedFooter {
        /**
         * Gets the footer's text.
         *
         * @return The footer's text.
         */
        String getText();

        /**
         * Gets the footer's icon URL.
         *
         * @return The footer's icon URL.
         */
        String getIconUrl();
    }

    /**
     * An embed author object.
     */
    interface IEmbedAuthor {
        /**
         * Gets the author's name.
         *
         * @return The author's name.
         */
        String getName();

        /**
         * Gets author's URL.
         *
         * @return The author's URL.
         */
        String getUrl();

        /**
         * Gets author's icon URL.
         *
         * @return The author's icon URL.
         */
        String getIconUrl();
    }

    /**
     * An embed field object.
     */
    interface IEmbedField {
        /**
         * Gets the field's name.
         *
         * @return The field's name.
         */
        String getName();

        /**
         * Gets the field's value.
         *
         * @return The field's value.
         */
        String getValue();

        /**
         * Gets whether the field is inline.
         *
         * @return Whether the field is inline.
         */
        boolean isInline();
    }

    /**
     * A site that provides media which is embedded in chat. Eg. Youtube, Imgur, etc.
     */
    interface IEmbedProvider {
        /**
         * Gets the provider's name.
         *
         * @return The provider's name.
         */
        String getName();

        /**
         * Gets the provider's URL.
         *
         * @return The provider's URL.
         */
        String getUrl();
    }
}
