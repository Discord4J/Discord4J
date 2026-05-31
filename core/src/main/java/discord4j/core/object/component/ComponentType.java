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

public enum ComponentType {

    UNKNOWN(-1),
    ACTION_ROW(1),
    BUTTON(2),
    SELECT_MENU_STRING(3),
    TEXT_INPUT(4),
    SELECT_MENU_USER(5),
    SELECT_MENU_ROLE(6),
    SELECT_MENU_MENTIONABLE(7),
    SELECT_MENU_CHANNEL(8),
    SECTION(9, true),
    TEXT_DISPLAY(10, true),
    THUMBNAIL(11, true),
    MEDIA_GALLERY(12, true),
    FILE(13, true),
    SEPARATOR(14, true),
    CONTAINER(17, true),
    LABEL(18),
    FILE_UPLOAD(19),
    RADIO_GROUP(21),
    CHECKBOX_GROUP(22),
    CHECKBOX(23),
    ;

    private final int value;
    private final boolean requireFlag;

    ComponentType(int value) {
        this(value, false);
    }

    ComponentType(int value, boolean requireFlag) {
        this.value = value;
        this.requireFlag = requireFlag;
    }

    /**
     * Gets if this Type require the use of {@link discord4j.core.object.entity.Message.Flag#IS_COMPONENTS_V2}.
     *
     * @return {@code true} if need the use of the flag, {@code false} otherwise
     */
    public boolean isRequiredFlag() {
        return this.requireFlag;
    }

    public int getValue() {
        return this.value;
    }

    public static ComponentType of(int value) {
        switch (value) {
            case 1:
                return ComponentType.ACTION_ROW;
            case 2:
                return ComponentType.BUTTON;
            case 3:
                return ComponentType.SELECT_MENU_STRING;
            case 4:
                return ComponentType.TEXT_INPUT;
            case 5:
                return ComponentType.SELECT_MENU_USER;
            case 6:
                return ComponentType.SELECT_MENU_ROLE;
            case 7:
                return ComponentType.SELECT_MENU_MENTIONABLE;
            case 8:
                return ComponentType.SELECT_MENU_CHANNEL;
            case 9:
                return ComponentType.SECTION;
            case 10:
                return ComponentType.TEXT_DISPLAY;
            case 11:
                return ComponentType.THUMBNAIL;
            case 12:
                return ComponentType.MEDIA_GALLERY;
            case 13:
                return ComponentType.FILE;
            case 14:
                return ComponentType.SEPARATOR;
            case 17:
                return ComponentType.CONTAINER;
            case 18:
                return ComponentType.LABEL;
            case 19:
                return ComponentType.FILE_UPLOAD;
            case 21:
                return ComponentType.RADIO_GROUP;
            case 22:
                return ComponentType.CHECKBOX_GROUP;
            case 23:
                return ComponentType.CHECKBOX;
            default:
                return ComponentType.UNKNOWN;
        }
    }
}
