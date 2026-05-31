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
package discord4j.core.object.component.impl.layout;

import discord4j.core.object.component.Component;
import discord4j.core.object.component.usage.ICanBeUsedInActionRowComponent;
import discord4j.core.object.component.usage.ICanBeUsedInContainerComponent;
import discord4j.core.object.component.impl.Label;
import discord4j.core.object.component.impl.TextInput;
import discord4j.core.object.component.kind.ActionComponent;
import discord4j.core.object.component.kind.BaseComponent;
import discord4j.core.object.component.kind.LayoutComponent;
import discord4j.core.object.component.kind.TopLevelComponent;
import discord4j.discordjson.json.component.ActionRowComponentData;
import discord4j.discordjson.possible.Possible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A row of {@link ICanBeUsedInActionRowComponent} components.
 *
 * @apiNote {@link Label} is recommended for use over an Action Row in modals. Action Row with {@link TextInput} in
 * modals are now deprecated.
 * @see <a href="https://discord.com/developers/docs/components/reference#action-row">ActionRow</a>
 */
public class ActionRow
        extends LayoutComponent<ActionRowComponentData>
        implements TopLevelComponent, ICanBeUsedInContainerComponent {

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     * @apiNote Discord doesn't recommend use {@link TextInput} in {@code ActionRow}, for that you can use
     * {@link Label}.
     */
    public static ActionRow of(ICanBeUsedInActionRowComponent... components) {
        return of(Arrays.asList(components));
    }

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     * @apiNote Discord doesn't recommend use {@link TextInput} in {@code ActionRow}, for that you can use
     * {@link Label}.
     */
    public static ActionRow of(List<ICanBeUsedInActionRowComponent> components) {
        return new ActionRow(ActionRowComponentData.builder()
                .components(components.stream().map(BaseComponent::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param id         the component id
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     * @apiNote Discord doesn't recommend use {@link TextInput} in {@code ActionRow}, for that you can use
     * {@link Label}.
     */
    public static ActionRow of(int id, ICanBeUsedInActionRowComponent... components) {
        return of(id, Arrays.asList(components));
    }

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param id         the component id
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     * @apiNote Discord doesn't recommend use {@link TextInput} in {@code ActionRow}, for that you can use
     * {@link Label}.
     */
    public static ActionRow of(int id, List<ICanBeUsedInActionRowComponent> components) {
        return new ActionRow(ActionRowComponentData.builder()
                .id(id)
                .components(components.stream().map(BaseComponent::getData).collect(Collectors.toList()))
                .build());
    }

    protected ActionRow(Integer id, List<ICanBeUsedInActionRowComponent> components) {
        this(ActionRowComponentData.builder()
                .id(Possible.ofNullable(id))
                .components(components.stream().map(BaseComponent::getData).collect(Collectors.toList()))
                .build());
    }

    public static ActionRow of(ActionRowComponentData data) {
        return new ActionRow(data);
    }

    private ActionRow(ActionRowComponentData data) {
        super(data);
    }

    /**
     * Create a new {@link ActionRow} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return an {@code ActionRow} containing the existing and added components
     */
    public ActionRow withAddedComponent(ICanBeUsedInActionRowComponent component) {
        List<BaseComponent> components = new ArrayList<>(this.getChildren());
        components.add(component);
        return new ActionRow(ActionRowComponentData.builder()
                .components(components.stream().map(BaseComponent::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link ActionRow} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param customId the customId of the component to remove
     * @return an {@code ActionRow} containing all components that did not match the given {@code customId}
     */
    public ActionRow withRemovedComponent(String customId) {
        List<Component<?>> components = this.getChildren();
        components.removeIf(messageComponent -> {
            if (messageComponent instanceof ActionComponent) {
                return customId.equalsIgnoreCase(((ActionComponent<?>) messageComponent).getCustomId().orElse(null));
            }
            return false; // Should never happen
        });
        return new ActionRow(ActionRowComponentData.builder()
                .components(components.stream().map(Component::getData).collect(Collectors.toList()))
                .build());
    }
}
