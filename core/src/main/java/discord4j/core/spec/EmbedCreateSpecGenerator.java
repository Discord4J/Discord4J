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

import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.EmbedFooterData;
import discord4j.discordjson.json.EmbedImageData;
import discord4j.discordjson.json.EmbedThumbnailData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import org.immutables.value.Value;
import reactor.util.annotation.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static discord4j.core.spec.InternalSpecUtils.flatMapPossible;
import static discord4j.core.spec.InternalSpecUtils.mapPossible;
import static discord4j.core.spec.InternalSpecUtils.toPossible;

@Value.Immutable(singleton = true)
interface EmbedCreateSpecGenerator extends Spec<EmbedData> {

    Possible<String> title();

    Possible<String> description();

    Possible<String> url();

    Possible<Instant> timestamp();

    Possible<Color> color();

    @Nullable // deepImmutablesDetection doesn't work for Possible types
    EmbedCreateFields.Footer footer();

    Possible<String> image();

    Possible<String> thumbnail();

    @Nullable
    EmbedCreateFields.Author author();

    @Value.Default
    default List<EmbedCreateFields.Field> fields() {
        return Collections.emptyList();
    }

    @Override
    default EmbedData asRequest() {
        return EmbedData.builder()
                .title(title())
                .description(description())
                .url(url())
                .timestamp(mapPossible(timestamp(), DateTimeFormatter.ISO_INSTANT::format))
                .color(mapPossible(color(), Color::getRGB))
                .footer(mapPossible(toPossible(footer()), EmbedCreateFields.Footer::asRequest))
                .image(mapPossible(image(), url -> EmbedImageData.builder().url(url).build()))
                .thumbnail(mapPossible(thumbnail(), url -> EmbedThumbnailData.builder().url(url).build()))
                .author(mapPossible(toPossible(author()), EmbedCreateFields.Author::asRequest))
                .fields(fields().stream().map(EmbedCreateFields.Field::asRequest).collect(Collectors.toList()))
                .build();
    }

    abstract class Builder {
        public EmbedCreateSpec.Builder from(final EmbedData data) {
            final EmbedCreateSpec.Builder $this = EmbedCreateSpec.Builder.class.cast(this)
                    .title(data.title())
                    .description(data.description())
                    .url(data.url())
                    .timestamp(mapPossible(data.timestamp(), Instant::parse))
                    .color(mapPossible(data.color(), Color::of))
                    .image(flatMapPossible(data.image(), EmbedImageData::url))
                    .thumbnail(flatMapPossible(data.thumbnail(), EmbedThumbnailData::url));

            if (!data.footer().isAbsent()) {
                final EmbedFooterData footer = data.footer().get();
                $this.footer(footer.text(), footer.iconUrl().toOptional().orElse(null));
            } else {
                $this.footer(null);
            }

            if (!data.author().isAbsent()) {
                final EmbedAuthorData author = data.author().get();
                $this.author(EmbedCreateFields.Author.of(
                        author.name().toOptional().orElseThrow(IllegalStateException::new),
                        Possible.flatOpt(author.url()).orElse(null),
                        author.iconUrl().toOptional().orElse(null)
                ));
            } else {
                $this.author(null);
            }

            if (!data.fields().isAbsent()) {
                final List<EmbedFieldData> fields = data.fields().get();
                $this.fields(
                        fields.stream()
                                .map(field -> EmbedCreateFields.Field.of(field.name(), field.value(), field.inline().toOptional().orElse(false)))
                                .collect(Collectors.toList())
                );
            } else {
                $this.fields(Collections.emptyList());
            }

            return $this;
        }
    }
}
