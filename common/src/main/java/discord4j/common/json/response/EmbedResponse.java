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
package discord4j.common.json.response;

import discord4j.common.json.EmbedFieldEntity;

import javax.annotation.Nullable;
import java.util.Arrays;

public class EmbedResponse {

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
    private EmbedFooterResponse footer;
    @Nullable
    private EmbedImageResponse image;
    @Nullable
    private EmbedThumbnailResponse thumbnail;
    @Nullable
    private EmbedVideoResponse video;
    @Nullable
    private EmbedProviderResponse provider;
    @Nullable
    private EmbedAuthorResponse author;
    @Nullable
    private EmbedFieldEntity[] fields;

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
    public EmbedFooterResponse getFooter() {
        return footer;
    }

    @Nullable
    public EmbedImageResponse getImage() {
        return image;
    }

    @Nullable
    public EmbedThumbnailResponse getThumbnail() {
        return thumbnail;
    }

    @Nullable
    public EmbedVideoResponse getVideo() {
        return video;
    }

    @Nullable
    public EmbedProviderResponse getProvider() {
        return provider;
    }

    @Nullable
    public EmbedAuthorResponse getAuthor() {
        return author;
    }

    @Nullable
    public EmbedFieldEntity[] getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "EmbedResponse[" +
                "title=" + title +
                ", type=" + type +
                ", description=" + description +
                ", url=" + url +
                ", timestamp=" + timestamp +
                ", color=" + color +
                ", footer=" + footer +
                ", image=" + image +
                ", thumbnail=" + thumbnail +
                ", video=" + video +
                ", provider=" + provider +
                ", author=" + author +
                ", fields=" + Arrays.toString(fields) +
                ']';
    }
}
