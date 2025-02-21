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
package discord4j.core.object.component;

import discord4j.discordjson.json.ComponentData;

/**
 * A separator component for message.
 *
 * @apiNote This component require {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Separator</a>
 */
public class Separator extends LayoutComponent {

    /**
     * Creates an {@code Separator}.
     *
     * @return An {@code Separator}
     */
    public static Separator of() {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR).build());
    }

    /**
     * Creates an {@code Separator}.
     *
     * @param divider If the separator is a divider
     * @return An {@code Separator}
     */
    public static Separator of(boolean divider) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR).divider(divider).build());
    }

    /**
     * Creates an {@code Separator}.
     *
     * @param divider If the separator is a divider
     * @param spacingSize The spacing size for the divider
     * @return An {@code Separator}
     */
    public static Separator of(boolean divider, SpacingSize spacingSize) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR).divider(divider).spacing(spacingSize.getValue()).build());
    }

    Separator(ComponentData data) {
        super(data);
    }

    /**
     * Gets if this separator is a divider.
     *
     * @return {@code true} if is a divider, false otherwise
     */
    public boolean isDivider() {
        return this.getData().divider().toOptional().orElse(true);
    }

    /**
     * Gets the spacing size for this separator.
     *
     * @return An {@code SpacingSize}
     */
    public SpacingSize getSpacingSize() {
        return SpacingSize.of(this.getData().spacing().toOptional().orElse(1));
    }

    public enum SpacingSize {
        SMALL(0),
        LARGE(1),
        ;

        private final int value;

        SpacingSize(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SpacingSize of(int value) {
            switch (value) {
                case 0: return SMALL;
                case 1: return LARGE;
                default: throw new UnsupportedOperationException("Unknown SpacingSize: " + value);
            }
        }
    }
}
