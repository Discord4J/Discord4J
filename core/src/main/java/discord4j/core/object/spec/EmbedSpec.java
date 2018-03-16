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
package discord4j.core.object.spec;

import discord4j.common.json.EmbedFieldEntity;
import discord4j.common.json.request.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmbedSpec implements Spec<EmbedRequest> {

    private final EmbedRequest.Builder requestBuilder = EmbedRequest.builder();
    private final List<EmbedFieldEntity> fields = new ArrayList<>();

    public EmbedSpec setTitle(String title) {
        requestBuilder.title(title);
        return this;
    }

    public EmbedSpec setDescription(String description) {
        requestBuilder.description(description);
        return this;
    }

    public EmbedSpec setUrl(String url) {
        requestBuilder.url(url);
        return this;
    }

    public EmbedSpec setTimestamp(Instant timestamp) {
        requestBuilder.timestamp(DateTimeFormatter.ISO_INSTANT.format(timestamp));
        return this;
    }

    public EmbedSpec setFooter(String text, String iconUrl) {
        requestBuilder.footer(new EmbedFooterRequest(text, iconUrl));
        return this;
    }

    public EmbedSpec setImage(String url) {
        requestBuilder.image(new EmbedImageRequest(url));
        return this;
    }

    public EmbedSpec setThumbnail(String url) {
        requestBuilder.thumbnail(new EmbedThumbnailRequest(url));
        return this;
    }

    public EmbedSpec setAuthor(String name, String url, String iconUrl) {
        requestBuilder.author(new EmbedAuthorRequest(name, url, iconUrl));
        return this;
    }

    public EmbedSpec addField(String name, String value, boolean inline) {
        this.fields.add(new EmbedFieldEntity(name, value, inline));
        return this;
    }

    @Override
    public EmbedRequest asRequest() {
        requestBuilder.fields(this.fields.toArray(new EmbedFieldEntity[this.fields.size()]));
        return requestBuilder.build();
    }
}
