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
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.discordjson.json.EmbedFooterData;
import discord4j.discordjson.possible.Possible;
import org.immutables.value.Value;
import reactor.util.annotation.Nullable;

import java.util.Optional;

import static discord4j.core.spec.InternalSpecUtils.toPossible;

@InlineFieldStyle
@Value.Enclosing
public final class EmbedCreateFields {

    private EmbedCreateFields() {
        throw new AssertionError();
    }

    @Value.Immutable
    public interface Footer extends Spec<EmbedFooterData> {

        static Footer of(String text, @Nullable String iconUrl) {
            return ImmutableEmbedCreateFields.Footer.of(text, iconUrl);
        }

        String text();

        @Nullable
        String iconUrl();

        @Override
        default EmbedFooterData asRequest() {
            return EmbedFooterData.builder()
                    .text(text())
                    .iconUrl(toPossible(iconUrl()))
                    .build();
        }
    }

    @Value.Immutable
    public interface Author extends Spec<EmbedAuthorData> {

        static Author of(String name, @Nullable String url, @Nullable String iconUrl) {
            return ImmutableEmbedCreateFields.Author.of(name, url, iconUrl);
        }

        String name();

        @Nullable
        String url();

        @Nullable
        String iconUrl();

        @Override
        default EmbedAuthorData asRequest() {
            String url = url();
            return EmbedAuthorData.builder()
                    .name(name())
                    .url(url == null ? Possible.absent() : Possible.of(Optional.of(url)))
                    .iconUrl(toPossible(iconUrl()))
                    .build();
        }
    }

    @Value.Immutable
    public interface Field extends Spec<EmbedFieldData> {

        static Field of(String name, String value, boolean inline) {
            return ImmutableEmbedCreateFields.Field.of(name, value, inline);
        }

        String name();

        String value();

        boolean inline();

        @Override
        default EmbedFieldData asRequest() {
            return EmbedFieldData.builder()
                    .name(name())
                    .value(value())
                    .inline(inline())
                    .build();
        }
    }
}
