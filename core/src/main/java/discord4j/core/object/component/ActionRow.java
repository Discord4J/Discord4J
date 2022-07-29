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

/**
 * A row of {@link ActionComponent action components}.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#actionrow">ActionRow</a>
 */
public class ActionRow extends LayoutComponent {

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     */
    public static ActionRow of(ActionComponent... components) {
        return of(Arrays.asList(components));
    }

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     */
    public static ActionRow of(/*~~>*/List<? extends ActionComponent> components) {
        return new ActionRow(ComponentData.builder()
                .type(Type.ACTION_ROW.getValue())
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }

    ActionRow(ComponentData data) {
        super(data);
    }
}
