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

import discord4j.discordjson.json.ImmutableOverflowMenuGroupData;
import discord4j.discordjson.json.OverflowMenuGroupData;
import discord4j.discordjson.possible.Possible;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A group of {@link MenuActionItem items} of an {@link OverflowMenu}. It is not a component by itself, so it has
 * no type nor id.
 *
 * @see <a href="https://discord.com/developers/docs/components/reference#overflow-menu">Overflow Menu</a>
 */
public class OverflowMenuGroup {

    /**
     * Creates a group without label.
     *
     * @param items The items of the group
     * @return A group with the given data
     */
    public static OverflowMenuGroup of(final MenuActionItem... items) {
        return of(null, items);
    }

    /**
     * Creates a group without label.
     *
     * @param items The items of the group.
     * @return A group with the given data.
     */
    public static OverflowMenuGroup of(final List<MenuActionItem> items) {
        return of(null, items);
    }

    /**
     * Creates a group.
     *
     * @param label The label shown above the items, if any
     * @param items The items of the group
     * @return A group with the given data
     */
    public static OverflowMenuGroup of(final @Nullable String label, final MenuActionItem... items) {
        return of(label, Arrays.asList(items));
    }

    /**
     * Creates a group.
     *
     * @param label The label shown above the items, if any
     * @param items The items of the group
     * @return A group with the given data
     */
    public static OverflowMenuGroup of(final @Nullable String label, final List<MenuActionItem> items) {
        final ImmutableOverflowMenuGroupData.Builder builder = OverflowMenuGroupData.builder()
                .components(items.stream().map(MenuActionItem::getData).collect(Collectors.toList()));

        if (label != null) {
            builder.label(Possible.of(Optional.of(label)));
        }

        return new OverflowMenuGroup(builder.build());
    }

    /**
     * Constructs a group from raw data.
     *
     * @param data The raw group data.
     * @return A group with the given data
     */
    public static OverflowMenuGroup fromData(final OverflowMenuGroupData data) {
        return new OverflowMenuGroup(data);
    }

    private final OverflowMenuGroupData data;

    private OverflowMenuGroup(final OverflowMenuGroupData data) {
        this.data = data;
    }

    /**
     * Gets the raw data of this group.
     *
     * @return The raw data
     */
    public OverflowMenuGroupData getData() {
        return this.data;
    }

    /**
     * Gets the label of this group.
     *
     * @return The label, if present
     */
    public Optional<String> getLabel() {
        return Possible.flatOpt(this.getData().label());
    }

    /**
     * Gets the items of this group.
     *
     * @return The items of this group
     */
    public List<MenuActionItem> getComponents() {
        return this.getData().components().stream()
                .map(MessageComponent::fromData)
                .filter(component -> component instanceof MenuActionItem)
                .map(component -> (MenuActionItem) component)
                .collect(Collectors.toList());
    }

}
