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
package discord4j.core.object.data.stored.embed;

import discord4j.common.json.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;

public final class EmbedBean implements Serializable {

    private static final long serialVersionUID = -5897245445280188177L;

    @Nullable
    private String title;
    private String type;
    @Nullable
    private String description;
    @Nullable
    private String url;
    @Nullable
    private String timestamp;
    @Nullable
    private Integer color;
    @Nullable
    private EmbedFooterBean footer;
    @Nullable
    private EmbedImageBean image;
    @Nullable
    private EmbedThumbnailBean thumbnail;
    @Nullable
    private EmbedVideoBean video;
    @Nullable
    private EmbedProviderBean provider;
    @Nullable
    private EmbedAuthorBean author;
    @Nullable
    private EmbedFieldBean[] fields;

    public EmbedBean(final EmbedResponse response) {
        title = response.getTitle();
        type = response.getType();
        description = response.getDescription();
        url = response.getUrl();
        timestamp = response.getTimestamp();
        color = response.getColor();

        final EmbedFooterResponse footerResponse = response.getFooter();
        footer = (footerResponse == null) ? null : new EmbedFooterBean(footerResponse);
        final EmbedImageResponse imageResponse = response.getImage();
        image = (imageResponse == null) ? null : new EmbedImageBean(imageResponse);
        final EmbedThumbnailResponse thumbnailResponse = response.getThumbnail();
        thumbnail = (thumbnailResponse == null) ? null : new EmbedThumbnailBean(thumbnailResponse);
        final EmbedVideoResponse videoResponse = response.getVideo();
        video = (videoResponse == null) ? null : new EmbedVideoBean(videoResponse);
        final EmbedProviderResponse providerResponse = response.getProvider();
        provider = (providerResponse == null) ? null : new EmbedProviderBean(providerResponse);
        final EmbedAuthorResponse authorResponse = response.getAuthor();
        author = (authorResponse == null) ? null : new EmbedAuthorBean(authorResponse);

        final EmbedFieldEntity[] fieldResponse = response.getFields();
        fields = (fieldResponse == null) ? null : Arrays.stream(fieldResponse)
                .map(EmbedFieldBean::new)
                .toArray(EmbedFieldBean[]::new);
    }

    public EmbedBean() {}

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable final String description) {
        this.description = description;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable final String url) {
        this.url = url;
    }

    @Nullable
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@Nullable final String timestamp) {
        this.timestamp = timestamp;
    }

    @Nullable
    public Integer getColor() {
        return color;
    }

    public void setColor(@Nullable final Integer color) {
        this.color = color;
    }

    @Nullable
    public EmbedFooterBean getFooter() {
        return footer;
    }

    public void setFooter(@Nullable final EmbedFooterBean footer) {
        this.footer = footer;
    }

    @Nullable
    public EmbedImageBean getImage() {
        return image;
    }

    public void setImage(@Nullable final EmbedImageBean image) {
        this.image = image;
    }

    @Nullable
    public EmbedThumbnailBean getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(@Nullable final EmbedThumbnailBean thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Nullable
    public EmbedVideoBean getVideo() {
        return video;
    }

    public void setVideo(@Nullable final EmbedVideoBean video) {
        this.video = video;
    }

    @Nullable
    public EmbedProviderBean getProvider() {
        return provider;
    }

    public void setProvider(@Nullable final EmbedProviderBean provider) {
        this.provider = provider;
    }

    @Nullable
    public EmbedAuthorBean getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable final EmbedAuthorBean author) {
        this.author = author;
    }

    @Nullable
    public EmbedFieldBean[] getFields() {
        return fields;
    }

    public void setFields(@Nullable final EmbedFieldBean[] fields) {
        this.fields = fields;
    }
}
