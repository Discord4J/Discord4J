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
import discord4j.discordjson.json.ImmutableComponentData;

public class SeparatorComponent extends LayoutComponent {

    private final static ImmutableComponentData.Builder BUILDER = ComponentData.builder().from(ComponentData.builder().type(Type.SEPARATOR.getValue()).build());

    public static SeparatorComponent of(boolean divider) {
        return new SeparatorComponent(MessageComponent.getBuilder(Type.SEPARATOR).divider(divider).build());
    }

    public static SeparatorComponent of(boolean divider, SpacingSize spacingSize) {
        return new SeparatorComponent(MessageComponent.getBuilder(Type.SEPARATOR).divider(divider).spacing(spacingSize.getValue()).build());
    }

    SeparatorComponent(ComponentData data) {
        super(data);
    }

    public boolean isDivider() {
        return this.getData().divider().get();
    }

    public SpacingSize getSpacingSize() {
        return SpacingSize.of(this.getData().spacing().toOptional().orElse(0));
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
