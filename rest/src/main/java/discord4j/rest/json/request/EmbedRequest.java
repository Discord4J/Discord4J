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
package discord4j.rest.json.request;

import discord4j.common.jackson.Possible;
import discord4j.common.json.EmbedFieldEntity;

public class EmbedRequest {

    private final Possible<String> title;
    private final Possible<String> description;
    private final Possible<String> url;
    private final Possible<String> timestamp;
    private final Possible<Integer> color;
    private final Possible<EmbedFooterRequest> footer;
    private final Possible<EmbedImageRequest> image;
    private final Possible<EmbedThumbnailRequest> thumbnail;
    private final Possible<EmbedAuthorRequest> author;
    private final Possible<EmbedFieldEntity[]> fields;

    public EmbedRequest(Possible<String> title, Possible<String> description, Possible<String> url,
                        Possible<String> timestamp, Possible<Integer> color, Possible<EmbedFooterRequest> footer,
                        Possible<EmbedImageRequest> image, Possible<EmbedThumbnailRequest> thumbnail,
                        Possible<EmbedAuthorRequest> author, Possible<EmbedFieldEntity[]> fields) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.timestamp = timestamp;
        this.color = color;
        this.footer = footer;
        this.image = image;
        this.thumbnail = thumbnail;
        this.author = author;
        this.fields = fields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Possible<String> title;
        private Possible<String> description;
        private Possible<String> url;
        private Possible<String> timestamp;
        private Possible<Integer> color;
        private Possible<EmbedFooterRequest> footer;
        private Possible<EmbedImageRequest> image;
        private Possible<EmbedThumbnailRequest> thumbnail;
        private Possible<EmbedAuthorRequest> author;
        private Possible<EmbedFieldEntity[]> fields;

        public Builder title(String title) {
            this.title = Possible.of(title);
            return this;
        }

        public Builder description(String description) {
            this.description = Possible.of(description);
            return this;
        }

        public Builder url(String url) {
            this.url = Possible.of(url);
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = Possible.of(timestamp);
            return this;
        }

        public Builder color(Integer color) {
            this.color = Possible.of(color);
            return this;
        }

        public Builder footer(EmbedFooterRequest footer) {
            this.footer = Possible.of(footer);
            return this;
        }

        public Builder image(EmbedImageRequest image) {
            this.image = Possible.of(image);
            return this;
        }

        public Builder thumbnail(EmbedThumbnailRequest thumbnail) {
            this.thumbnail = Possible.of(thumbnail);
            return this;
        }

        public Builder author(EmbedAuthorRequest author) {
            this.author = Possible.of(author);
            return this;
        }

        public Builder fields(EmbedFieldEntity[] fields) {
            this.fields = Possible.of(fields);
            return this;
        }

        public EmbedRequest build() {
            return new EmbedRequest(title, description, url, timestamp, color, footer, image, thumbnail, author,
                    fields);
        }
    }

    @Override
    public String toString() {
        return "EmbedRequest{" +
                "title=" + title +
                ", description=" + description +
                ", url=" + url +
                ", timestamp=" + timestamp +
                ", color=" + color +
                ", footer=" + footer +
                ", image=" + image +
                ", thumbnail=" + thumbnail +
                ", author=" + author +
                ", fields=" + fields +
                '}';
    }
}
