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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionRow implements MessageComponent {

    private final List<MessageComponent> components;

    public static ActionRow of(MessageComponent... components) {
        return of(Arrays.asList(components));
    }

    public static ActionRow of(List<MessageComponent> components) {
        return new ActionRow(components);
    }

    private ActionRow(List<MessageComponent> components) {
        this.components = components;
    }

    public List<MessageComponent> getComponents() {
        return components;
    }

    @Override
    public Type getType() {
        return Type.ACTION_ROW;
    }

    @Override
    public ComponentData getData() {
        return ComponentData.builder()
                .type(Type.ACTION_ROW.getValue())
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build();
    }
}
