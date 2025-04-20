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
import discord4j.discordjson.possible.Possible;

/**
 * A separator component for message.
 *
 * @apiNote This component require {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#???">Separator</a>
 */
public class Separator extends LayoutComponent implements TopLevelMessageComponent, ICanBeUsedInContainerComponent {

    /**
     * Creates a {@link Separator}.
     *
     * @return A {@link Separator}
     */
    public static Separator of() {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR).build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param visible If the separator is visible
     * @return A {@link Separator}
     */
    public static Separator of(boolean visible) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .divider(visible)
            .build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param spacingSize The spacing size for the separator
     * @return A {@link Separator}
     */
    public static Separator of(SpacingSize spacingSize) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .spacing(spacingSize.getValue())
            .build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param visible If the separator is visible
     * @param spacingSize The spacing size for the separator
     * @return A {@link Separator}
     */
    public static Separator of(boolean visible, SpacingSize spacingSize) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .divider(visible)
            .spacing(spacingSize.getValue())
            .build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param id the component id
     * @return A {@link Separator}
     */
    public static Separator of(int id) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .id(id)
            .build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param id the component id
     * @param visible If the separator is visible
     * @return A {@link Separator}
     */
    public static Separator of(int id, boolean visible) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .id(id)
            .divider(visible)
            .build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param id the component id
     * @param spacingSize The spacing size for the separator
     * @return A {@link Separator}
     */
    public static Separator of(int id, SpacingSize spacingSize) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .id(id)
            .spacing(spacingSize.getValue())
            .build());
    }

    /**
     * Creates a {@link Separator}.
     *
     * @param id the component id
     * @param visible If the separator is visible
     * @param spacingSize The spacing size for the separator
     * @return A {@link Separator}
     */
    public static Separator of(int id, boolean visible, SpacingSize spacingSize) {
        return new Separator(MessageComponent.getBuilder(Type.SEPARATOR)
            .id(id)
            .divider(visible)
            .spacing(spacingSize.getValue())
            .build());
    }

    protected Separator(Integer id, boolean visible, SpacingSize spacingSize) {
        this(MessageComponent.getBuilder(Type.SEPARATOR)
            .id(Possible.ofNullable(id))
            .divider(visible)
            .spacing(spacingSize.getValue())
            .build());
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
     * Gets if the separator is visible
     *
     * @return {@code true} if visible, false otherwise
     */
    public boolean isVisible() {
        return this.isDivider();
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
        SMALL(1),
        LARGE(2),
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
                case 1:
                    return SMALL;
                case 2:
                    return LARGE;
                default:
                    throw new UnsupportedOperationException("Unknown SpacingSize: " + value);
            }
        }
    }
}
