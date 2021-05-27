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
package discord4j.core.object.component;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ComponentData;

import java.util.Optional;

public class Button implements MessageComponent {

    private final ComponentData data;

    Button(ComponentData data) {
        this.data = data;
    }

    public Style getStyle() {
        return data.style().toOptional()
                .map(Style::of)
                .orElseThrow(IllegalStateException::new); // style should always be present on buttons
    }

    public Optional<String> getLabel() {
        return data.label().toOptional();
    }

    public Optional<ReactionEmoji> getEmoji() {
        return data.emoji().toOptional()
                .map(ReactionEmoji::of);
    }

    public Optional<String> getCustomId() {
        return data.customId().toOptional();
    }

    public Optional<String> getUrl() {
        return data.url().toOptional();
    }

    public boolean isDisabled() {
        return data.disabled().toOptional().orElse(false);
    }

    @Override
    public Type getType() {
        return Type.BUTTON;
    }

    @Override
    public ComponentData getData() {
        return data;
    }

    enum Style {
        UNKNOWN(-1),
        PRIMARY(1),
        SECONDARY(2),
        SUCCESS(3),
        DANGER(4),
        LINK(5);

        private final int value;

        Style(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Style of(int value) {
            switch (value) {
                case 1: return PRIMARY;
                case 2: return SECONDARY;
                case 3: return SUCCESS;
                case 4: return DANGER;
                case 5: return LINK;
                default: return UNKNOWN;
            }
        }
    }
}
