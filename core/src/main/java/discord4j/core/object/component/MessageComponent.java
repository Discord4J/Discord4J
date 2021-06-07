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

public class MessageComponent {

    private final ComponentData data;

    public MessageComponent(ComponentData data) {
        this.data = data;
    }

    public ComponentData getData() {
        return data;
    }

    public Type getType() {
        return Type.of(data.type());
    }

    public enum Type {
        UNKNOWN(-1),
        ACTION_ROW(1),
        BUTTON(2);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Type of(int value) {
            switch (value) {
                case 1: return ACTION_ROW;
                case 2: return BUTTON;
                default: return UNKNOWN;
            }
        }
    }

}
