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

public class SectionComponent extends LayoutComponent {

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the section.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static SectionComponent of(ActionComponent... components) {
        return of(Arrays.asList(components));
    }

    /**
     * Creates an {@code SectionComponent} with the given components.
     *
     * @param components The child components of the section.
     * @return An {@code SectionComponent} containing the given components.
     */
    public static SectionComponent of(List<? extends ActionComponent> components) {
        return new SectionComponent(ComponentData.builder()
            .type(Type.SECTION.getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    SectionComponent(ComponentData data) {
        super(data);
    }

    public List<MessageComponent> getAccessories() {
        return this.getData().accesory().toOptional()
                .map(components -> components.stream()
                        .map(MessageComponent::fromData)
                        .collect(Collectors.toList()))
                .orElseThrow(IllegalStateException::new); // components should always be present on a layout component
    }

    /**
     * Create a new {@link SectionComponent} instance from {@code this}, adding a given component.
     *
     * @param component the child component to be added
     * @return an {@code SectionComponent} containing the existing and added components
     */
    public SectionComponent withAddedComponent(ActionComponent component) {
        List<MessageComponent> components = new ArrayList<>(getChildren());
        components.add(component);
        return new SectionComponent(ComponentData.builder()
            .type(this.getType().getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Create a new {@link SectionComponent} instance from {@code this}, removing any existing component by {@code customId}.
     *
     * @param customId the customId of the component to remove
     * @return an {@code SectionComponent} containing all components that did not match the given {@code customId}
     */
    public SectionComponent withRemovedComponent(String customId) {
        List<MessageComponent> components = getChildren(customId);
        return new SectionComponent(ComponentData.builder()
            .type(this.getType().getValue())
            .components(components.stream().map(MessageComponent::getData).collect(Collectors.toList()))
            .build());
    }

    /**
     * Create a new {@link SectionComponent} instance from {@code this}, adding a given component.
     *
     * @param component the child accessorie to be added
     * @return an {@code SectionComponent} accessorie the existing and added accessorie
     */
    public SectionComponent withAddedAccessory(ActionComponent component) {
        List<MessageComponent> accessories = new ArrayList<>(this.getChildren());
        accessories.add(component);
        return new SectionComponent(ComponentData.builder()
                .type(this.getType().getValue())
                .accesory(accessories.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }

    /**
     * Create a new {@link SectionComponent} instance from {@code this}, removing any existing accessorie by {@code customId}.
     *
     * @param customId the customId of the accessorie to remove
     * @return an {@code SectionComponent} containing all accessorie that did not match the given {@code customId}
     */
    public SectionComponent withRemovedAccessory(String customId) {
        List<MessageComponent> accessories = this.getAccessories()
                .stream()
                .filter(it -> !it.getData().customId()
                        .toOptional()
                        .filter(customId::equals)
                        .isPresent())
                .collect(Collectors.toList());;
        return new SectionComponent(ComponentData.builder()
                .type(this.getType().getValue())
                .accesory(accessories.stream().map(MessageComponent::getData).collect(Collectors.toList()))
                .build());
    }
}
