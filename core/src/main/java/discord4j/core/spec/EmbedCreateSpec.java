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

import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmbedCreateSpec implements Spec<EmbedRequest> {

    private final EmbedRequest.Builder requestBuilder = EmbedRequest.builder();
    private final List<EmbedFieldEntity> fields = new ArrayList<>();

    public EmbedCreateSpec setTitle(String title) {
        requestBuilder.title(title);
        return this;
    }

    public EmbedCreateSpec setDescription(String description) {
        requestBuilder.description(description);
        return this;
    }

    public EmbedCreateSpec setUrl(String url) {
        requestBuilder.url(url);
        return this;
    }

    public EmbedCreateSpec setTimestamp(Instant timestamp) {
        requestBuilder.timestamp(DateTimeFormatter.ISO_INSTANT.format(timestamp));
        return this;
    }

    public EmbedCreateSpec setColor(final int color) {
        requestBuilder.color(color & 0xFFFFFF);
        return this;
    }

    public EmbedCreateSpec setColor(final Color color) {
        return setColor(color.getRGB());
    }

    public EmbedCreateSpec setFooter(String text, @Nullable String iconUrl) {
        requestBuilder.footer(new EmbedFooterRequest(text, iconUrl));
        return this;
    }

    public EmbedCreateSpec setImage(String url) {
        requestBuilder.image(new EmbedImageRequest(url));
        return this;
    }

    public EmbedCreateSpec setThumbnail(String url) {
        requestBuilder.thumbnail(new EmbedThumbnailRequest(url));
        return this;
    }

    public EmbedCreateSpec setAuthor(String name, @Nullable String url, @Nullable String iconUrl) {
        requestBuilder.author(new EmbedAuthorRequest(name, url, iconUrl));
        return this;
    }

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
