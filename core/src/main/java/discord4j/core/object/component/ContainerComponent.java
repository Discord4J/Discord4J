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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerComponent extends LayoutComponent {

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the container.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static ContainerComponent of(ActionComponent... components) {
        return of(Arrays.asList(components));
    }

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the container.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static ContainerComponent of(List<? extends ActionComponent> components) {
        return new ContainerComponent(ComponentData.builder()
            .type(Type.CONTAINER.getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    ContainerComponent(ComponentData data) {
        super(data);
    }

    /**
     * Create a new {@link ContainerComponent} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return an {@code SectionComponent} containing the existing and added components
     */
    public ContainerComponent withAddedComponent(ActionComponent component) {
        List<MessageComponent> components = new ArrayList<>(getChildren());
        components.add(component);
        return new ContainerComponent(ComponentData.builder()
            .type(Type.CONTAINER.getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Create a new {@link ContainerComponent} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param customId the customId of the component to remove
     * @return an {@code SectionComponent} containing all components that did not match the given {@code customId}
     */
    public ContainerComponent withRemovedComponent(String customId) {
        List<MessageComponent> components = getChildren()
            .stream()
            .filter(it -> !it.getData().customId()
                .toOptional()
                .filter(customId::equals)
                .isPresent())
            .collect(Collectors.toList());
        return new ContainerComponent(ComponentData.builder()
            .type(Type.CONTAINER.getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }
}
