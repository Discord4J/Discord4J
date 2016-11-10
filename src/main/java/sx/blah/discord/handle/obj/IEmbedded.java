package sx.blah.discord.handle.obj;

import java.awt.Color;
import java.time.LocalDateTime;

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
     * @return A url pointing to the image. Can be null.
     */
    String getImage();

    /**
     * Gets the thumbnail of the embedded media.
     *
     * @return An object containing information about the embedded media's thumbnail. Can be null.
     */
    String getThumbnail();

    /**
     * Gets the video url for the embedded media.
     *
     * @return A url pointing to the video. Can be null.
     */
    String getVideo();

    /**
     * Gets the provider of the embedded media.
     *
     * @return An object containing information about the embedded media's provider. <b>Can Be Null!</b>
     */
    IEmbedded.IEmbedProvider getEmbedProvider();

    /**
     * Gets the author for this embedded media.
     *
     * @return An object containing information about the author for the embedded media. Cna be null.
     */
    IEmbedded.IEmbedAuthor getAuthor();

    java.util.List<IEmbedField> getEmbeddedFields();

    /**
     * Represents an embedded footer object
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
     * Represents the author for an embedded object
     */
    interface IEmbedAuthor {
        String getName();

        String getUrl();

        String getIconUrl();
    }

    interface IEmbedField {
        String getName();

        String getValue();

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