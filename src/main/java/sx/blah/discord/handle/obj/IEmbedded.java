package sx.blah.discord.handle.obj;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a discord embedded object.
 */
public interface IEmbedded {

    /**
     * Gets the title of the embedded media.
     *
     * @return The title of the embedded media. Can be null.
     */
    String getTitle();

    /**
     * Gets the type of embedded media.
     *
     * @return The type of embedded media as a string.
     */
    String getType();

    /**
     * Gets a description of the embedded media.
     *
     * @return A description of the embedded media. Can be null.
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

    IEmbedFooter getFooter();

    /**
     * Gets the image in the embedded media.
     *
     * @return An object containing information about the image. Can be null.
     */
    IEmbedImage getImage();

    /**
     * Gets the thumbnail of the embedded media.
     *
     * @return An object containing information about the embedded media's thumbnail. Can be null.
     */
    IEmbedImage getThumbnail();

    /**
     * Gets the video url for the embedded media.
     *
     * @return A url pointing to the video. Can be null.
     */
    IEmbedVideo getVideo();

    /**
     * Gets the provider of the embedded media.
     *
     * @return An object containing information about the embedded media's provider. <b>Can Be Null!</b>
     */
    IEmbedProvider getEmbedProvider();

    /**
     * Gets the author for this embedded media.
     *
     * @return An object containing information about the author for the embedded media. Can be null.
     */
    IEmbedAuthor getAuthor();

    /**
     * Gets the list of embedded fields for this embedded media.
     *
     * @return A list containing objects with information about fields. Can be null.
     */
    List<IEmbedField> getEmbeddedFields();


    /**
     * Represents an embedded image object.
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
     * Represents an embedded video object.
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
     * Represents an embedded footer object.
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
     * Represents the author for an embedded object.
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
     * Represents a field in the embedded object.
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
     * Represents a site that provides media which is embedded in chat. Eg. Youtube, Imgur.
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