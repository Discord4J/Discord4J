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
import discord4j.discordjson.possible.Possible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A row of {@link ActionComponent action components}.
 *
 * @see <a href="https://discord.com/developers/docs/interactions/message-components#actionrow">ActionRow</a>
 */
public class ActionRow extends LayoutComponent implements TopLevelMessageComponent, ICanBeUsedInContainerComponent {

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
    public static ActionRow of(List<? extends ActionComponent> components) {
        return new ActionRow(MessageComponent.getBuilder(Type.ACTION_ROW)
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param id the component id
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     */
    public static ActionRow of(int id, ActionComponent... components) {
        return of(id, Arrays.asList(components));
    }

    /**
     * Creates an {@code ActionRow} with the given components.
     *
     * @param id the component id
     * @param components The child components of the row.
     * @return An {@code ActionRow} containing the given components.
     */
    public static ActionRow of(int id, List<? extends ActionComponent> components) {
        return new ActionRow(MessageComponent.getBuilder(Type.ACTION_ROW)
            .id(id)
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    protected ActionRow(Integer id, List<? extends ActionComponent> components) {
        this(MessageComponent.getBuilder(Type.ACTION_ROW)
            .id(Possible.ofNullable(id))
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    ActionRow(ComponentData data) {
        super(data);
    }

    /**
     * Create a new {@link ActionRow} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return an {@code ActionRow} containing the existing and added components
     */
    public ActionRow withAddedComponent(ActionComponent component) {
        List<MessageComponent> components = new ArrayList<>(getChildren());
        components.add(component);
        return new ActionRow(ComponentData.builder()
                .type(this.getType().getValue())
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link ActionRow} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param customId the customId of the component to remove
     * @return an {@code ActionRow} containing all components that did not match the given {@code customId}
     */
    public ActionRow withRemovedComponent(String customId) {
        List<MessageComponent> components = getChildren();
        components.removeIf(messageComponent -> {
            return messageComponent instanceof ActionComponent && customId.equals(((ActionComponent) messageComponent).getCustomId());
        });
        return new ActionRow(ComponentData.builder()
                .type(this.getType().getValue())
                .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }
}
