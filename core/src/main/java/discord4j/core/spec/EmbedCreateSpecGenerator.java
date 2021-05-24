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

package discord4j.core.spec;

import discord4j.discordjson.json.*;
import discord4j.rest.util.Color;
import org.immutables.value.Value;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.*;

@SpecStyle
@Value.Immutable(singleton = true)
interface EmbedCreateSpecGenerator extends Spec<EmbedData> {

    @Nullable
    String title();

    @Nullable
    String description();

    @Nullable
    String url();

    @Nullable
    Instant timestamp();

    @Nullable
    Color color();

    @Nullable
    EmbedCreateFields.Footer footer();

    @Nullable
    String image();

    @Nullable
    String thumbnail();

    @Nullable
    EmbedCreateFields.Author author();

    @Value.Default
    default List<EmbedCreateFields.Field> fields() {
        return Collections.emptyList();
    }

    @Override
    default EmbedData asRequest() {
        return EmbedData.builder()
                .title(toPossible(title()))
                .description(toPossible(description()))
                .url(toPossible(url()))
                .timestamp(toPossible(mapNullable(timestamp(), DateTimeFormatter.ISO_INSTANT::format)))
                .color(toPossible(mapNullable(color(), Color::getRGB)))
                .footer(toPossible(mapNullable(footer(), EmbedCreateFields.Footer::asRequest)))
                .image(toPossible(mapNullable(image(), url -> EmbedImageData.builder().url(url).build())))
                .thumbnail(toPossible(mapNullable(thumbnail(), url -> EmbedThumbnailData.builder().url(url).build())))
                .author(toPossible(mapNullable(author(), EmbedCreateFields.Author::asRequest)))
                .fields(fields().stream().map(EmbedCreateFields.Field::asRequest).collect(Collectors.toList()))
                .build();
    }
}