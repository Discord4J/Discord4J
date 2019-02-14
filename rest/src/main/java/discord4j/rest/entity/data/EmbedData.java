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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.rest.entity.data;

import discord4j.common.json.*;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

public class EmbedData {

    @Nullable
    private final String title;
    private final String type;
    @Nullable
    private final String description;
    @Nullable
    private final String url;
    @Nullable
    private final String timestamp;
    @Nullable
    private final Integer color;
    @Nullable
    private final EmbedFooterData footer;
    @Nullable
    private final EmbedImageData image;
    @Nullable
    private final EmbedThumbnailData thumbnail;
    @Nullable
    private final EmbedVideoData video;
    @Nullable
    private final EmbedProviderData provider;
    @Nullable
    private final EmbedAuthorData author;
    @Nullable
    private final EmbedFieldData[] fields;

    public EmbedData(EmbedResponse response) {
        title = response.getTitle();
        type = response.getType();
        description = response.getDescription();
        url = response.getUrl();
        timestamp = response.getTimestamp();
        color = response.getColor();

        EmbedFooterResponse footerResponse = response.getFooter();
        footer = (footerResponse == null) ? null : new EmbedFooterData(footerResponse);
        EmbedImageResponse imageResponse = response.getImage();
        image = (imageResponse == null) ? null : new EmbedImageData(imageResponse);
        EmbedThumbnailResponse thumbnailResponse = response.getThumbnail();
        thumbnail = (thumbnailResponse == null) ? null : new EmbedThumbnailData(thumbnailResponse);
        EmbedVideoResponse videoResponse = response.getVideo();
        video = (videoResponse == null) ? null : new EmbedVideoData(videoResponse);
        EmbedProviderResponse providerResponse = response.getProvider();
        provider = (providerResponse == null) ? null : new EmbedProviderData(providerResponse);
        EmbedAuthorResponse authorResponse = response.getAuthor();
        author = (authorResponse == null) ? null : new EmbedAuthorData(authorResponse);

        EmbedFieldEntity[] fieldResponse = response.getFields();
        fields = (fieldResponse == null) ? null : Arrays.stream(fieldResponse)
                .map(EmbedFieldData::new)
                .toArray(EmbedFieldData[]::new);
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getTimestamp() {
        return timestamp;
    }

    @Nullable
    public Integer getColor() {
        return color;
    }

    @Nullable
    public EmbedFooterData getFooter() {
        return footer;
    }

    @Nullable
    public EmbedImageData getImage() {
        return image;
    }

    @Nullable
    public EmbedThumbnailData getThumbnail() {
        return thumbnail;
    }

    @Nullable
    public EmbedVideoData getVideo() {
        return video;
    }

    @Nullable
    public EmbedProviderData getProvider() {
        return provider;
    }

    @Nullable
    public EmbedAuthorData getAuthor() {
        return author;
    }

    @Nullable
    public EmbedFieldData[] getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "EmbedData{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", color=" + color +
                ", footer=" + footer +
                ", image=" + image +
                ", thumbnail=" + thumbnail +
                ", video=" + video +
                ", provider=" + provider +
                ", author=" + author +
                ", fields=" + Arrays.toString(fields) +
                '}';
    }
}
