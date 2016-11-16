package sx.blah.discord.handle.obj;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a discord embed object.
 */
public interface IEmbed {

    /**
     * Gets the title of the embed media.
     *
     * @return The title of the embed media. Can be null.
     */
    String getTitle();

    /**
     * Gets the type of embed media.
     *
     * @return The type of embed media as a string.
     */
    String getType();

    /**
     * Gets a description of the embed media.
     *
     * @return A description of the embed media. Can be null.
     */
    String getDescription();

    /**
     * Gets the direct link to the media.
     *
     * @return The download link for the attachment.
     */
    String getUrl();

    /**
     * Gets the timestamp for the media.
     *
     * @return The timestamp.
     */
    LocalDateTime getTimestamp();

    /**
     * Gets the color of the embed.
     *
     * @return The color.
     */
    Color getColor();

    /**
     * Gets the footer of the embed media.
     * 
     * @return An object containing information about the embed media's footer. Can be null.
     */
    IEmbedFooter getFooter();

    /**
     * Gets the image in the embed media.
     *
     * @return An object containing information about the image. Can be null.
     */
    IEmbedImage getImage();

    /**
     * Gets the thumbnail of the embed media.
     *
     * @return An object containing information about the embed media's thumbnail. Can be null.
     */
    IEmbedImage getThumbnail();

    /**
     * Gets the video url for the embed media.
     *
     * @return A url pointing to the video. Can be null.
     */
    IEmbedVideo getVideo();

    /**
     * Gets the provider of the embed media.
     *
     * @return An object containing information about the embed media's provider. <b>Can Be Null!</b>
     */
    IEmbedProvider getEmbedProvider();

    /**
     * Gets the author for this embed media.
     *
     * @return An object containing information about the author for the embed media. Can be null.
     */
    IEmbedAuthor getAuthor();

    /**
     * Gets the list of embed fields for this embed media.
     *
     * @return A list containing objects with information about fields. Can be null.
     */
    List<IEmbedField> getEmbedFields();

    /**
     * Represents an embed image object.
     */
    interface IEmbedImage {
        /**
         * Gets the image's url
         *
         * @return The image's url
         */
        String getUrl();

        /**
         * Gets the image's height
         *
         * @return The image's height
         */
        int getHeight();

        /**
         * Gets the image's width
         *
         * @return The image's width
         */
        int getWidth();
    }

    /**
     * Represents an embed video object.
     */
    interface IEmbedVideo {
        /**
         * Gets the video's url
         *
         * @return The video's url
         */
        String getUrl();

        /**
         * Gets the video's height
         *
         * @return The video's height
         */
        int getHeight();

        /**
         * Gets the video's width
         *
         * @return The video's width
         */
        int getWidth();
    }

    /**
     * Represents an embed footer object.
     */
    interface IEmbedFooter {
        /**
         * Gets the footer's text
         *
         * @return The footer's text
         */
        String getText();

        /**
         * Gets the footer's icon URL
         *
         * @return A url link as a string
         */
        String getIconUrl();
    }

    /**
     * Represents the author for an embed object.
     */
    interface IEmbedAuthor {
        /**
         * Gets the author's name
         *
         * @return The author's name
         */
        String getName();

        /**
         * Gets the url for this author
         *
         * @return The author's url
         */
        String getUrl();

        /**
         * Gets the icon url for this author
         *
         * @return The icon url
         */
        String getIconUrl();
    }

    /**
     * Represents a field in the embed object.
     */
    interface IEmbedField {
        /**
         * Gets the field's name
         *
         * @return The field's name
         */
        String getName();

        /**
         * Gets the field's value
         *
         * @return The field's value
         */
        String getValue();

        /**
         * Gets if the field is inline or not
         *
         * @return If the field is inline or not
         */
        boolean isInline();
    }

    /**
     * Represents a site that provides media which is embed in chat. Eg. Youtube, Imgur.
     */
    interface IEmbedProvider {
        /**
         * Gets the Embedded Media Provider's Name
         *
         * @return The Embedded Media Provider's Name
         */
        String getName();

        /**
         * Gets the Embedded Media Provider's Url
         *
         * @return A url link to the Embedded Media Provider as a String
         */
        String getUrl();
    }
}