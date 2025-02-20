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

import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;

/**
 * A Discord message component.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#message-components">Message Components</a>
 */
public class MessageComponent {

    static ImmutableComponentData.Builder getBuilder(Type type) {
        if (type == null) {
            throw new NullPointerException("Type cannot be null");
        }
        return ImmutableComponentData.builder().type(type.getValue());
    }

    /**
     * Constructs a {@code MessageComponent} from raw data.
     * <p>
     * The correct subtype will be chosen based on the component's {@link Type}.
     *
     * @param data The raw component data.
     * @return A component with the given data.
     */
    public static MessageComponent fromData(ComponentData data) {
        switch (Type.of(data.type())) {
            case CONTAINER: return new ContainerComponent(data);
            case SECTION: return new SectionComponent(data);
            case SEPARATOR: return new SeparatorComponent(data);
            case ACTION_ROW: return new ActionRow(data);
            case TEXT_DISPLAY: return new TextDisplayComponent(data);
            case THUMBNAIL: return new ThumbnailComponent(data);
            case MEDIA_GALLERY: return new MediaGalleryComponent(data);
            case FILE: return new FileComponent(data);
            case BUTTON: return new Button(data);
            case SELECT_MENU_ROLE:
            case SELECT_MENU_CHANNEL:
            case SELECT_MENU_MENTIONABLE:
            case SELECT_MENU_USER:
            case SELECT_MENU: return new SelectMenu(data);
            case TEXT_INPUT: return new TextInput(data);
            default: return new MessageComponent(data);
        }
    }

    private final ComponentData data;

    MessageComponent(ComponentData data) {
        this.data = data;
    }

    /**
     * Gets the data of the component.
     *
     * @return The data of the component.
     */
    public ComponentData getData() {
        return data;
    }

    /**
     * Gets if these components It's supported to be used in {@link ContainerComponent}.
     * @return true if can
     * @see Type#isSupportedInContainer()
     */
    public boolean isSupportedInContainer() {
        return this.getType().isSupportedForContainerComponent();
    }

    /**
     * Gets the type of the component.
     *
     * @return The type of the component.
     */
    public Type getType() {
        return Type.of(data.type());
    }

    public enum Type {
        UNKNOWN(-1),
        ACTION_ROW(1, true),
        BUTTON(2),
        SELECT_MENU(3),
        TEXT_INPUT(4),
        SELECT_MENU_USER(5),
        SELECT_MENU_ROLE(6),
        SELECT_MENU_MENTIONABLE(7),
        SELECT_MENU_CHANNEL(8),
        SECTION(9, true),
        TEXT_DISPLAY(10, true),
        THUMBNAIL(11),
        MEDIA_GALLERY(12, true),
        FILE(13, true),
        SEPARATOR(14, true),
        CONTAINER(17),
        ;

        private final int value;
        private final boolean supportedForContainerComponent;

        Type(int value) {
            this(value, false);
        }

        Type(int value, boolean supportedForContainerComponent) {
            this.value = value;
            this.supportedForContainerComponent = supportedForContainerComponent;
        }

        public int getValue() {
            return value;
        }

        public boolean isSupportedForContainerComponent() {
            return supportedForContainerComponent;
        }

        public static Type of(int value) {
            switch (value) {
                case 1: return ACTION_ROW;
                case 2: return BUTTON;
                case 3: return SELECT_MENU;
                case 4: return TEXT_INPUT;
                case 5: return SELECT_MENU_USER;
                case 6: return SELECT_MENU_ROLE;
                case 7: return SELECT_MENU_MENTIONABLE;
                case 8: return SELECT_MENU_CHANNEL;
                case 9: return SECTION;
                case 10: return TEXT_DISPLAY;
                case 11: return THUMBNAIL;
                case 12: return MEDIA_GALLERY;
                case 13: return FILE;
                case 14: return SEPARATOR;
                case 17: return CONTAINER;
                default: return UNKNOWN;
            }
        }
    }

}
