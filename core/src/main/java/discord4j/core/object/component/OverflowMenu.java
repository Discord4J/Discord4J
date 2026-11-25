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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An overflow menu component for message.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#overflow-menu">Overflow Menu</a>
 */
public class OverflowMenu extends ActionComponent implements IAccessoryComponent {

    /**
     * Creates an overflow menu.
     *
     * @param id the component id
     * @param groups The groups of items shown when the menu is opened.
     * @return An overflow menu with the given data.
     */
    public static OverflowMenu of(final int id, final OverflowMenuGroup... groups) {
        return of(id, Arrays.asList(groups));
    }

    /**
     * Creates an overflow menu.
     *
     * @param id the component id
     * @param groups The groups of items shown when the menu is opened.
     * @return An overflow menu with the given data.
     */
    public static OverflowMenu of(final int id, final List<OverflowMenuGroup> groups) {
        return new OverflowMenu(MessageComponent.getBuilder(Type.OVERFLOW_MENU)
                .id(id)
                .groups(groups.stream().map(OverflowMenuGroup::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Creates an overflow menu.
     *
     * @param groups The groups of items shown when the menu is opened.
     * @return An overflow menu with the given data.
     */
    public static OverflowMenu of(final OverflowMenuGroup... groups) {
        return of(Arrays.asList(groups));
    }

    /**
     * Creates an overflow menu.
     *
     * @param groups The groups of items shown when the menu is opened.
     * @return An overflow menu with the given data.
     */
    public static OverflowMenu of(final List<OverflowMenuGroup> groups) {
        return new OverflowMenu(MessageComponent.getBuilder(Type.OVERFLOW_MENU)
                .groups(groups.stream().map(OverflowMenuGroup::getData).collect(Collectors.toList()))
                .build());
    }

    OverflowMenu(final ComponentData data) {
        super(data);
    }

    /**
     * Get the groups of this {@link OverflowMenu}
     *
     * @return The groups of this component
     */
    public List<OverflowMenuGroup> getGroups() {
        return this.getData()
                .groups()
                .toOptional()
                .map(groups -> groups.stream()
                        .map(OverflowMenuGroup::fromData)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

}
